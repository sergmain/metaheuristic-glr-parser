/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * @author Serge
 * Date: 2/22/2022
 * Time: 5:15 PM
 */
@SuppressWarnings("PyUnresolvedReferences")
@Slf4j
public class Glr {

    String py = """
            from glrengine import GLRScanner, GLRAutomaton, GLRSplitter, morph_parser


            class GLRParser(object):
                DEFAULT_PARSER = {
                    "word", "[\\w\\d_-]+",
                    "number", "[\\d]+",
                    "space", "[\\s]+",
                    "newline", "[\\n]+",
                    "dot", "[\\.]+",
                    "comma", [,]+",
                    "colon", "[:]+",
                    "percent", "[%]+",
                    "quote", "[\\"\\'«»`]+",
                    "brace", "[\\(\\)\\{\\}\\[\\]]+",
                }

                DEFAULT_PARSER_DISCARD_NAMES = ["space"]

                DEFAULT_GRAMMAR = ""\"
                    Word = word
                    Word = noun
                    Word = adj
                    Word = verb
                    Word = pr
                    Word = dpr
                    Word = num
                    Word = adv
                    Word = pnoun
                    Word = prep
                    Word = conj
                    Word = prcl
                    Word = lat
                ""\"

                def __init__(self, grammar, root="S", dictionaries=None, parser=None, debug=False):
                    grammar_rules = u"%s\\n%s" % (grammar, self.DEFAULT_GRAMMAR)
                    if dictionaries:
                        # превращает {k: [a, b, c]} -> "k = 'a' | 'b' | 'c'"
                        for dict_name, dict_words in dictionaries.items():
                            morphed = []
                            for word in dict_words:
                                morphed.append(morph_parser.normal(word))
                            grammar_rules += u"\\n%s = '%s'" % (dict_name, "' | '".join(morphed))

                    if debug:
                        print grammar_rules

                    parser_rules = self.DEFAULT_PARSER
                    parser_rules.update({"discard_names": self.DEFAULT_PARSER_DISCARD_NAMES})
                    if parser:
                        parser_rules.update(parser)

                    self.splitter = GLRSplitter()
                    self.scanner = GLRScanner(**parser_rules)
                    self.glr = GLRAutomaton(
                        start_sym=root,
                        grammar=grammar_rules,
                        scanner=self.scanner,
                        debug=debug
                    )

                def parse(self, text):
                    result = []
                    for sentence in self.splitter(text):
                        result += self.glr(sentence)
                    return result
                             
            """;

    public static LinkedHashMap<String, String > DEFAULT_PARSER = new LinkedHashMap<>();
    static {
        DEFAULT_PARSER.put("word", "[\\p{L}\\p{Digit}_-]+");
        DEFAULT_PARSER.put("number", "[\\d]+");
        DEFAULT_PARSER.put("space", "[\\s]+");
        DEFAULT_PARSER.put("newline", "[\\n]+");
        DEFAULT_PARSER.put("dot", "[\\.]+");
        DEFAULT_PARSER.put("comma", "[,]+");
        DEFAULT_PARSER.put("colon", "[:]+");
        DEFAULT_PARSER.put("percent", "[%]+");
        DEFAULT_PARSER.put("quote", "[\"\\'«»`]+");
        DEFAULT_PARSER.put("brace", "[\\(\\)\\{\\}\\[\\]]+");
    }

    public static LinkedHashSet<String> DEFAULT_PARSER_DISCARD_NAMES = new LinkedHashSet<>();
    static {
        DEFAULT_PARSER_DISCARD_NAMES.add("space");
    }

    public static String DEFAULT_GRAMMAR = """
        Word = word
        Word = noun
        Word = adj
        Word = verb
        Word = pr
        Word = dpr
        Word = num
        Word = adv
        Word = pnoun
        Word = prep
        Word = conj
        Word = prcl
        Word = lat
    """;

    public static class ParserRules {
        public final LinkedHashMap<String, String> parser_rules = new LinkedHashMap<>();
        public final LinkedHashMap<String, LinkedHashSet<String>> discards = new LinkedHashMap<>();

        public ParserRules(Map<String, String> defaultParser, LinkedHashSet<String> discard_names, @Nullable Map<String, String> parser) {
            this.parser_rules.putAll(defaultParser);
            this.discards.put("discard_names", discard_names);
            this.discards.put("discard_values", new LinkedHashSet<>());
            if (parser!=null) {
                this.parser_rules.putAll(parser);
            }
        }
    }

    public final ParserRules parserRules;

    public final GlrAutomaton automaton;

    public Glr(String grammar, @Nullable Map<String, List<String>> dictionaries, @Nullable Map<String, String> parser, boolean debug) {
        this(grammar, "S", dictionaries, parser, debug);
    }

    public Glr(String grammar, String root, @Nullable Map<String, List<String>> dictionaries, @Nullable Map<String, String> parser, boolean debug) {

        String grammar_rules = combineGrammarRules(grammar, dictionaries, DEFAULT_GRAMMAR);

        log.debug("{}", grammar_rules);

        parserRules = new ParserRules(DEFAULT_PARSER, DEFAULT_PARSER_DISCARD_NAMES, parser);

/*
        self.scanner = GLRScanner(**parser_rules)
        self.glr = GLRAutomaton(
                start_sym=root,
                grammar=grammar_rules,
                scanner=self.scanner,
                debug=debug
        )
*/
        GlrScanner scanner = new GlrScanner(parserRules);
        this.automaton = new GlrAutomaton(root, grammar_rules, scanner, debug);

    }

    @SneakyThrows
    public static String combineGrammarRules(String grammar, @Nullable Map<String, List<String>> dictionaries, String defaultGrammar) {
        String grammar_rules = String.format("%s\n%s", grammar, defaultGrammar);

        String py5 = """
            if dictionaries:
                # превращает {k: [a, b, c]} -> "k = 'a' | 'b' | 'c'"
                for dict_name, dict_words in dictionaries.items():
                    morphed = []
                    for word in dict_words:
                        morphed.append(morph_parser.normal(word))
                    grammar_rules += u"\\n%s = '%s'" % (dict_name, "' | '".join(morphed))
    
            """;
        if (dictionaries!=null) {
            for (Map.Entry<String, List<String>> entry : dictionaries.entrySet()) {
                List<String> morphed = new ArrayList<>();
                for (String word : entry.getValue()) {
                    morphed.add(GlrNormalizerFactory.getInstance().normal(word));
                }
                grammar_rules += (String.format("\n%s = '%s'", entry.getKey(), String.join("' | '", morphed)));
            }
        }

        log.debug("{}", grammar_rules);

        return grammar_rules;
    }

    public List<String> parse(String text) {
        List<String> result = new ArrayList<>();
        for (String sentence : GlrSplitter.process(text)) {
            result.addAll(this.automaton.process(sentence));
        }
        return result;
    }
}
