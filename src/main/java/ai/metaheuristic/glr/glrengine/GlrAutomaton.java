/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.metaheuristic.glr.glrengine.GlrNormalizer.*;
import static ai.metaheuristic.glr.glrengine.GlrStack.*;

/**
 * @author Serge
 * Date: 2/22/2022
 * Time: 7:33 PM
 */
@SuppressWarnings("PyUnresolvedReferences")
public class GlrAutomaton extends GlrParser.Parser {

    public record ShortToken(String tokname, int idx){}

    public NormalizedToken INITIAL_TOKEN = new NormalizedToken("$", "$", 0);

    String s = """
        A GLR parser.
    
        def __init__(self, start_sym, grammar, scanner, dictionaries=None, debug=False):
            Parser.__init__(self, start_sym, grammar, scanner.tokens.keys())
            self.scanner = scanner
            self.dictionaries = dictionaries or {}
            self.results = []
            self.debug_mode = debug
            """;

    public final GlrScanner scanner;
    public boolean debug_mode = false;
    public List<LinkedHashMap<String, List<ShortToken>>> ACTION = new ArrayList<>();

    public GlrAutomaton(String root, String grammar_rules, GlrScanner scanner, boolean debug) {
        super(root, grammar_rules, new LinkedHashSet<>(scanner.tokens.keySet()));
        this.scanner = scanner;
        this.debug_mode = debug;
    }

    String py1 = """
        def __call__(self, text):
            return self.recognize(text, chain(self.scanner(text), [('$', '$', len(text))]))
        """;

    List<NormalizedToken> tokenItems;

    public List<String> process(String text) {
        tokenItems = new ArrayList<>(scanner.process(text));
        tokenItems.add(new NormalizedToken("$", "$", text.length()));
        return recognize(text, tokenItems);
    }

    String py3 = """
        def recognize(self, text, token_stream):
            self.results = []
            tokens = morph_parser(token_stream)
            while True:
                stack = Stack(self)
                stack.shift(None, None, 0)
                stack.count_active = 1
                prev_tok = INITIAL_TOKEN
                labels_ok = True
    
                for token_num, token in enumerate(tokens):
                    self.debug("\n\n\nNEW ITERATION. Token:", token[1])
                    self.debug(token)
                    if len(stack.active) == 0:
                        if not self.error_detected(text, tokens, prev_tok, stack.previously_active):
                            text, tokens = self.without_first_word(text, tokens)
                            break
                        else:
                            continue
                    prev_tok = token
    
                    # свертка
                    for i, node in stack.enumerate_active():  # S.active may grow
                        state = node.data
    
                        # raw-слова в кавычках
                        raw_token = "'%s'" % token[1]
                        if raw_token in self.ACTION[state]:
                            for r, rule in filter(lambda x: x[0] == 'R', self.ACTION[state][raw_token]):
                                self.debug("- Reduce")
                                self.debug("-- Actions", self.ACTION[state][raw_token])
                                self.debug("-- Raw token", node, rule)
                                labels_ok = self.check_labels(tokens, self.R.labels[rule])
                                if not labels_ok:
                                    break
                                stack.reduce(node, rule)
    
                        # обычные состояния
                        if labels_ok:
                            for r, rule in filter(lambda x: x[0] == 'R', self.ACTION[state][token[0]]):
                                self.debug("- Reduce")
                                self.debug("-- Actions", self.ACTION[state])
                                self.debug("-- Normal", node, rule)
                                labels_ok = self.check_labels(tokens, self.R.labels[rule])
                                if not labels_ok:
                                    break
                                stack.reduce(node, rule)
    
                        # имитация конца предложения
                        if labels_ok:
                            for r, rule in filter(lambda x: x[0] == 'R', self.ACTION[state]["$"]):
                                self.debug("- Reduce")
                                self.debug("-- Actions", self.ACTION[state])
                                self.debug("-- EOS", node, rule)
                                labels_ok = self.check_labels(tokens, self.R.labels[rule])
                                if not labels_ok:
                                    break
                                stack.reduce(node, rule)
    
                        self.debug("- STACK")
                        if self.debug_mode:
                            stack.dump()
    
                    # последняя свертка не удовлетворила лейблам
                    if not labels_ok:
                        self.debug("- Labels not OK")
                        text, tokens = self.without_first_word(text, tokens)
                        break
    
                    # конец?
                    if token[0] == '$':
                        acc = stack.accepts()
                        if acc:
                            self.results.append(text)
                            self.debug("- Found new result:", self.results)
                        else:
                            self.error_detected(text, tokens, token, stack.active)
                        return self.results
    
                    # перенос
                    stack.count_active = len(stack.active)
                    for node in (stack.active[i] for i in range(len(stack.active))):
                        # из стека могут удаляться состояния, так что верхний длинный for правда оказался нужен
                        state = node.data
    
                        # raw-слова в кавычках
                        raw_token = "'%s'" % token[1]
                        if raw_token in self.ACTION[state]:
                            for r, state in filter(lambda x: x[0] == 'S',  self.ACTION[state][raw_token]):
                                self.debug("- Shift")
                                self.debug("-- Raw", node, token)
                                stack.shift(node, (token,), state)
    
                        # обычные состояния
                        for r, state in filter(lambda x: x[0] == 'S',  self.ACTION[state][token[0]]):
                            self.debug("- Shift")
                            self.debug("-- Normal", node, token)
                            stack.shift(node, (token,), state)
    
                        self.debug("- Stack:")
                        if self.debug_mode:
                            stack.dump()
    
                    # слияние состояний
                    stack.merge()
    
            return self.results
        """;

    public record TextAndNormalizedTokens(String text, List<NormalizedToken> tokens) {}

    public List<String> recognize(String text, List<NormalizedToken> tokenItems) {
        List<String> results = new ArrayList<>();
        List<NormalizedToken> tokens = GlrNormalizerFactory.getInstance().process(tokenItems);
        while (true) {
            GlrStack stack = new GlrStack(this);
            stack.shift(null, null, 0);
            stack.count_active = 1;
            NormalizedToken prev_tok = INITIAL_TOKEN;
            boolean labels_ok = true;

            for (int token_num = 0; token_num < tokens.size(); token_num++) {
                NormalizedToken token = tokens.get(token_num);
                System.out.println("\n\n\nNEW ITERATION. Token: " + token.tokvalue);
                System.out.println(token);
                if (stack.active.isEmpty()) {
                    if (error_detected(text, tokens, prev_tok, stack.previously_active)) {
                        continue;
                    }
                    TextAndNormalizedTokens tmp = without_first_word(text, tokens);
                    text = tmp.text;
                    tokens = tmp.tokens;
                    break;
                }
                prev_tok = token;

                // # свертка
                for (EnumerateActive ea : stack.enumerate_active()) {  // # S.active may grow
                    int i = ea.i();
                    StackItem node = ea.node();
                    StackItemData state = node.data;

                    // # raw-слова в кавычках
                    String raw_token = "'"+token.tokvalue+"'";
                    if (raw_token in self.ACTION[state]) {
                        for (r, ruleIdx in filter(lambda x: x[0] == 'R', self.ACTION[state][raw_token])) {
                            debug("- Reduce");
                            debug("-- Actions", self.ACTION[state][raw_token]);
                            debug("-- Raw token", node, ruleIdx);
                            labels_ok = check_labels(tokens, self.R.labels[ruleIdx]);
                            if (!labels_ok) {
                                break;
                            }
                            stack.reduce(node, ruleIdx);
                        }
                    }

                    // # обычные состояния
                    if (labels_ok) {
                        for (r, rule in filter(lambda x: x[0] == 'R', self.ACTION[state][token[0]])) {
                            debug("- Reduce");
                            debug("-- Actions", self.ACTION[state]);
                            debug("-- Normal", node, rule);
                            labels_ok = check_labels(tokens, self.R.labels[rule])
                            if (!labels_ok){
                                break;
                            }
                            stack.reduce(node, rule);
                        }
                    }
                    // # имитация конца предложения
                    if (labels_ok) {
                        for (r, rule in filter(lambda x: x[0] == 'R', self.ACTION[state]["$"])) {
                            debug("- Reduce");
                            debug("-- Actions", self.ACTION[state]);
                            debug("-- EOS", node, rule);
                            labels_ok = check_labels(tokens, self.R.labels[rule]);
                            if (!labels_ok) {
                                break;
                            }
                            stack.reduce(node, rule);
                        }
                    }
                    debug("- STACK");
                    if (debug_mode) {
                        GlrStack.dump();
                    }
               }
            }
        }
    }

    String py31 = """
        def without_first_word(self, text, tokens):
            new_text = text[tokens[1][2]:]
            new_tokens = [(token[0], token[1], token[2] - tokens[1][2], token[3], token[4]) for token in tokens[1:]]
            return new_text, new_tokens
    
        def validate_ast(self, ast):
            return ast
        """;

    public static TextAndNormalizedTokens without_first_word(String text, List<NormalizedToken> tokens) {
        String new_text = text.substring(tokens.get(1).tokpos);

        List<NormalizedToken> new_tokens = tokens.stream()
                .skip(1)
                .map(t->new NormalizedToken(t.tokname, t.tokvalue, t.tokpos - tokens.get(1).tokpos, t.tokparams, t.orig_tokvalue))
                .toList();

        return new TextAndNormalizedTokens(new_text, new_tokens);
    }

    String py4 = """
        def error_detected(self, text, tokens, cur_tok, last_states):
            line, column = token_line_col(text, cur_tok)
    
            lines = text.splitlines()
            if lines:
                if len(lines) > (line - 1):
                    self.debug(lines[line - 1])
                    self.debug('%s^' % (''.join(c == '\t' and '\t' or ' ' for c in lines[line - 1][:column - 1])))
                else:
                    self.debug("at end of text")
    
//            toks = set(kw for st in last_states for kw in self.kw_set
//                           if len(self.ACTION[st.data][kw]) > 0
//                              and kw not in self.R and kw != '$')
            arr = []
            for st in last_states:
                for kw in self.kw_set:
                    if len(self.ACTION[st.data][kw]) > 0 and kw not in self.R and kw != '$':
                        arr.append(kw)
            toks1 = set(arr)
    
            if not toks:
                self.debug("Text", text)
                self.debug("-- Part", text[:cur_tok[2]])
                self.results.append(text[:cur_tok[2]])
                self.debug("- Found new result:", self.results)
    
            return False
        
        """;

    public boolean error_detected(String text, List<NormalizedToken> tokens,
                                  NormalizedToken cur_tok, List<StackItem> last_states) {
        GlrScanner.LineColumn lc = GlrScanner.token_line_col(text, cur_tok);
        int line = lc.line();
        int column = lc.column();

/*
        List<String> lines = text.lines().toList();
        if lines:
            if len(lines) > (line - 1):
                self.debug(lines[line - 1])
                self.debug('%s^' % (''.join(c == '\t' and '\t' or ' ' for c in lines[line - 1][:column - 1])))
            else:
                self.debug("at end of text")
*/
        String py4 = """
        arr = []
        for st in last_states:
            for kw in self.kw_set:
                if len(self.ACTION[st.data][kw]) > 0 and kw not in self.R and kw != '$':
                    arr.append(kw)
        toks1 = set(arr)
        """;

        LinkedHashSet<String> toks1 = new LinkedHashSet<>();
        for (StackItem st : last_states) {
            for (String kw : kw_set) {
                if (st.data instanceof IntegerStackItemData idx && ACTION.get(idx.token()).get(kw).size() > 0 &&
                    R.byName.get(kw)==null && !kw.equals("$")){
                    toks1.add(kw);
                }
            }
        }

        if (!toks1.isEmpty()) {
//            self.debug("Text", text)
//            self.debug("-- Part", text[:cur_tok[2]])
//            self.results.append(text[:cur_tok[2]])
//            self.debug("- Found new result:", self.results)
        }

        return false;
    }

    String py6 = """
        def debug(self, *args):
            if self.debug_mode:
                # print(" ".join(args))
                print("".join(str(tup) for tup in args))
        """;
    public void debug(Object ... args) {
        if (debug_mode) {
            // #print(" ".join(args))
            System.out.println(Stream.of(args).map(Object::toString).collect(Collectors.joining("")));
        }
    }
}
