/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.glr.token.GlrSimpleRegexTokenizer;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static ai.metaheuristic.glr.GlrConsts.*;
import static ai.metaheuristic.glr.glr.GlrGrammar.*;
import static ai.metaheuristic.glr.glr.GlrStack.*;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 9:02 PM
 */
public class GlrGrammarParser {
    public static final Integer RULE_OPTION_SYMBOLS_WEIGHT_IDX = 6;
    public static final Integer RULE_SYMBOL_WORD_WITH_LABEL_IDX = 10;
    public static final Integer RULE_SYMBOL_WORD_IDX = 11;
    public static final Integer RULE_SYMBOL_RAW_IDX = 12;

    String py1 = """
        lr_grammar_tokenizer = SimpleRegexTokenizer(dict(
            sep='=',
            alt='\\|',
            word=r"\\b\\w+\\b",
            raw=r"(?:'.+?'|\\".+?\\")",
            whitespace=r'[ \\t\\r\\n]+',
            minus=r'-',
            label=r'<.+?>',
            weight=r'\\(\\d+(?:[.,]\\d+)?\\)',
        ), ['whitespace'])
        """;

    public static final GlrSimpleRegexTokenizer lr_grammar_tokenizer;

    static {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("sep", "=");
        map.put("alt", "[|]");
        map.put(SYMBOL_WORD_RIGHT_SYMBOLS, "\\b[\\p{L}\\p{Digit}_]+\\b");
//        map.put("raw", "'[\\p{L}\\p{Digit}_]+'");
        map.put(SYMBOL_RAW_RIGHT_SYMBOLS, "(?:'.+?'|\\\".+?\\\")");
        map.put("whitespace", "[ \\t\\r\\n]+");
        map.put("minus", "[-]");
        map.put(SYMBOL_LABEL_RIGHT_SYMBOLS, "<.+?>");
        map.put(SYMBOL_WEIGHT_RIGHT_SYMBOLS, "\\(\\d+(?:[.,]\\d+)?\\)");
        lr_grammar_tokenizer = new GlrSimpleRegexTokenizer(map, List.of("whitespace"));
    }

    String py2 = """
    grammar = Grammar([
        Rule(0, '@', ('S',), False, None, 1.0),
        Rule(1, 'S', ('S', 'Rule'), False, None, 1.0),
        Rule(2, 'S', ('Rule',), False, None, 1.0),
        Rule(3, 'Rule', ('word', 'sep', 'Options'), False, None, 1.0),
        Rule(4, 'Options', ('Options', 'alt', 'Option'), False, None, 1.0),
        Rule(5, 'Options', ('Option',), False, None, 1.0),
        Rule(6, 'Option', ('Symbols', 'weight'), False, None, 1.0),
        Rule(7, 'Option', ('Symbols',), False, None, 1.0),
        Rule(8, 'Symbols', ('Symbols', 'Symbol'), False, None, 1.0),
        Rule(9, 'Symbols', ('Symbol',), False, None, 1.0),
        Rule(10, 'Symbol', ('word', 'label'), False, None, 1.0),
        Rule(11, 'Symbol', ('word',), False, None, 1.0),
        Rule(12, 'Symbol', ('raw',), False, None, 1.0),
    ])
    parser = Parser(grammar)
    """;

    public static final GlrGrammar GLR_BASE_GRAMMAR = new GlrGrammar(
            new Rule(0, "@", List.of("S"), false, null, 1.0),
            new Rule(1, "S", List.of("S", "Rule"), false, null, 1.0),
            new Rule(2,"S", List.of("Rule"), false, null, 1.0),
            new Rule(3,"Rule", List.of(SYMBOL_WORD_RIGHT_SYMBOLS, "sep", "Options"), false, null, 1.0),
            new Rule(4,"Options", List.of("Options", "alt", "Option"), false, null, 1.0),
            new Rule(5,"Options", List.of("Option"), false, null, 1.0),
            new Rule(6,"Option", List.of("Symbols", SYMBOL_WEIGHT_RIGHT_SYMBOLS), false, null, 1.0),
            new Rule(7,"Option", List.of("Symbols"), false, null, 1.0),
            new Rule(8,"Symbols", List.of("Symbols", "Symbol"), false, null, 1.0),
            new Rule(9,"Symbols", List.of("Symbol"), false, null, 1.0),
            new Rule(10,"Symbol", List.of(SYMBOL_WORD_RIGHT_SYMBOLS, SYMBOL_LABEL_RIGHT_SYMBOLS), false, null, 1.0),
            new Rule(11,"Symbol", List.of(SYMBOL_WORD_RIGHT_SYMBOLS), false, null, 1.0),
            new Rule(12,"Symbol", List.of(SYMBOL_RAW_RIGHT_SYMBOLS), false, null, 1.0)
    );

    private static final Map<Integer, GlrParser> parser = new HashMap<>();
    private static GlrParser getGlrParser() {
        return parser.computeIfAbsent(1, (o)->new GlrParser(GLR_BASE_GRAMMAR, 1));
    }


    String py3 = """
    def parse(self, grammar, start='S'):
        rules = [Rule(0, '@', (start,), False, ('',), 1.0)]
        for left_symbol, weight, right_symbols in self._scan_rules(grammar):
            if len(right_symbols) > 0:
                rules.append(
                    Rule(len(rules), left_symbol, tuple(s for s, l in right_symbols), False, tuple(l for s, l in right_symbols), weight))
            else:
                raise Exception('GLR parser does not support epsilon free rules')

        return Grammar(rules)
    """;

    public static GlrGrammar parse(String grammar) {
        return parse(grammar, "S");
    }

    public static GlrGrammar parse(String grammar, String start) {
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule(0, "@", new ArrayList<>(List.of(start)), false, new ArrayList<>(List.of(Map.of("", List.of()))), 1.0));
        for (ScanRule scan_rule : _scan_rules(grammar)) {
            var left_symbol = scan_rule.left_symbol;
            var weight = scan_rule.weight;
            List<SymbolWithMap> right_symbols = scan_rule.right_symbols;
            if (right_symbols.size()>0) {
                final List<String> symbols = right_symbols.stream().map(o -> o.symbol).distinct().toList();
                List<Map<String, List<Object>>> map = right_symbols.stream().map(o -> o.map).distinct().toList();
                rules.add(new Rule(rules.size(), left_symbol, symbols, false, map, weight));
            }
            else {
                throw new IllegalStateException("GLR parser does not support epsilon free rules");
            }
        }
        return new GlrGrammar(rules);

    }

    String py4 = """
    def _scan_rules(self, grammar_str):
        syntax_trees = self.parser.parse(self.lr_grammar_tokenizer.scan(grammar_str), full_math=True)
        if len(syntax_trees) > 1:
            raise Exception('Ambiguous grammar')

        for rule_node in flatten_syntax_tree(syntax_trees[0], 'Rule'):
            left_symbol = rule_node.children[0].token.input_term

            for option_node in flatten_syntax_tree(rule_node.children[2], 'Option'):
                if option_node.rule_index == 6:
                    weight = float(option_node.children[1].token.input_term[1:-1].replace(',', '.'))
                else:
                    weight = 1.0

                right_symbols = []
                for symbol_node in flatten_syntax_tree(option_node, 'Symbol'):
                    if symbol_node.rule_index == 11:
                        right_symbols.append((symbol_node.children[0].token.input_term, dict()))
                    elif symbol_node.rule_index == 12:
                        right_symbols.append((symbol_node.children[0].token.input_term[1:-1].strip(), {'raw': [True]}))
                    elif symbol_node.rule_index == 10:
                        right_symbols.append((symbol_node.children[0].token.input_term,
                                              self._parse_labels(symbol_node.children[1].token.input_term[1:-1])))

                yield left_symbol, weight, right_symbols
    """;

    public record SymbolWithMap(String symbol, Map<String, List<Object>> map) {}
    public record ScanRule(String left_symbol, double weight, List<SymbolWithMap> right_symbols){}


    public static List<ScanRule> _scan_rules(String grammar_str) {
        List<ScanRule> result = new ArrayList<>();
        List<SyntaxTree> syntax_trees = getGlrParser().parse(lr_grammar_tokenizer.tokenize(grammar_str), true);
        if (syntax_trees.size() > 1) {
            throw new RuntimeException("Ambiguous grammar. count: " + syntax_trees.size());
        }
        for (SyntaxTree rule_node : GlrUtils.flatten_syntax_tree(syntax_trees.get(0), "Rule")) {
            String left_symbol = rule_node.children().get(0).token().getInput_term();
            for (SyntaxTree option_node : GlrUtils.flatten_syntax_tree(rule_node.children().get(2), "Option")) {
                double weight;
                if (RULE_OPTION_SYMBOLS_WEIGHT_IDX.equals(option_node.rule_index())) {
                    final String s = option_node.children().get(1).token().getInput_term();
                    weight = Double.parseDouble(StringUtils.substring(s, 1, -1).replace(',', '.'));
                }
                else {
                    weight = 1.0;
                }

                List<SymbolWithMap> right_symbols = new ArrayList<>();
                for (SyntaxTree symbol_node : GlrUtils.flatten_syntax_tree(option_node, "Symbol")) {
                    SymbolWithMap symbolWithMap = null;
                    if (RULE_SYMBOL_WORD_IDX.equals(symbol_node.rule_index())) {
                        symbolWithMap = new SymbolWithMap(symbol_node.children().get(0).token().getInput_term(), Map.of());
                    }
                    else if (RULE_SYMBOL_RAW_IDX.equals(symbol_node.rule_index())) {
                        final String s = symbol_node.children().get(0).token().getInput_term();
                        symbolWithMap = new SymbolWithMap(StringUtils.substring(s, 1, -1).strip(), Map.of(SYMBOL_RAW_RIGHT_SYMBOLS, List.of(true)));
                    }
                    else if (RULE_SYMBOL_WORD_WITH_LABEL_IDX.equals(symbol_node.rule_index())) {
                        final String s0 = symbol_node.children().get(0).token().getInput_term();
                        final String s1 = StringUtils.substring(symbol_node.children().get(1).token().getInput_term(), 1, -1);
                        symbolWithMap = new SymbolWithMap(s0, _parse_labels(s1));
                    }

                    if (symbolWithMap!=null) {
                        right_symbols.add(symbolWithMap);
                    }
                }
                result.add(new ScanRule(left_symbol, weight, right_symbols));
            }
        }

        return result;
    }

    String py5 = """
    @staticmethod
    def _parse_labels(labels_str):
        labels_str = labels_str.strip().replace(" ", "")
        labels = {}
        for key_value in labels_str.split(","):
            if '=' in key_value:
                key, value = tuple(key_value.split("=", 1))
                labels.setdefault(key, []).append(value)
            else:
                labels.setdefault(key_value, []).append(True)
        return labels
    """;

    public static Map<String, List<Object>> _parse_labels(String labels_str_param) {
        String labels_str = labels_str_param.strip().replace(" ", "");
        Map<String, List<Object>> labels = new LinkedHashMap<>();
        for (String key_value : labels_str.split(",")) {
            if (key_value.indexOf('=')!=-1) {
                String[] sp = key_value.split("=", 2);
                String key = sp[0];
                String value = sp[1];
                labels.computeIfAbsent(key, (o)->new ArrayList<>()).add(value);
            }
            else {
                labels.computeIfAbsent(key_value, (o)->new ArrayList<>()).add(true);
            }
        }
        return labels;

    }
}
