/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrSimpleRegexTokenizer;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static ai.metaheuristic.glr.GlrConsts.*;
import static ai.metaheuristic.glr.GlrGrammar.*;
import static ai.metaheuristic.glr.GlrStack.*;

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

/*
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
*/

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
