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

    public static final GlrSimpleRegexTokenizer LR_GRAMMAR_TOKENIZER;

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
        LR_GRAMMAR_TOKENIZER = new GlrSimpleRegexTokenizer(map, List.of("whitespace"));
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
        for (ScanRule scanRule : scanRules(grammar)) {
            var leftSymbol = scanRule.leftSymbol;
            var weight = scanRule.weight;
            List<SymbolWithMap> rightSymbols = scanRule.rightSymbols;
            if (rightSymbols.isEmpty()) {
                throw new IllegalStateException("GLR parser does not support epsilon free rules");
            }
            if (rightSymbols.size()>2) {
                // TODO p5 2022-09-19 see ai.metaheuristic.glr.GlrParserTest.test_55
                throw new IllegalStateException("Right now parser doesn't support grammar with more than 2 right symbols: " + rightSymbols);
            }
            final List<String> symbols = rightSymbols.stream().map(o -> o.symbol).toList();
            List<Map<String, List<Object>>> map = rightSymbols.stream().map(o -> o.map).toList();
            rules.add(new Rule(rules.size(), leftSymbol, symbols, false, map, weight));
        }
        return new GlrGrammar(rules);
    }

    public record SymbolWithMap(String symbol, Map<String, List<Object>> map) {}
    public record ScanRule(String leftSymbol, double weight, List<SymbolWithMap> rightSymbols){}

    public static List<ScanRule> scanRules(String grammarStr) {
        List<ScanRule> result = new ArrayList<>();
        List<SyntaxTree> syntaxTrees = getGlrParser().parse(LR_GRAMMAR_TOKENIZER.tokenize(grammarStr), true);
        if (syntaxTrees.size() > 1) {
            throw new RuntimeException("Ambiguous grammar. count: " + syntaxTrees.size());
        }
        for (SyntaxTree ruleNode : GlrUtils.flattenSyntaxTree(syntaxTrees.get(0), "Rule")) {
            String leftSymbol = ruleNode.children().get(0).token().inputTerm;
            for (SyntaxTree optionNode : GlrUtils.flattenSyntaxTree(ruleNode.children().get(2), "Option")) {
                double weight;
                if (RULE_OPTION_SYMBOLS_WEIGHT_IDX.equals(optionNode.ruleIndex())) {
                    final String s = optionNode.children().get(1).token().inputTerm;
                    weight = Double.parseDouble(StringUtils.substring(s, 1, -1).replace(',', '.'));
                }
                else {
                    weight = 1.0;
                }

                List<SymbolWithMap> rightSymbols = new ArrayList<>();
                for (SyntaxTree symbolNode : GlrUtils.flattenSyntaxTree(optionNode, "Symbol")) {
                    SymbolWithMap symbolWithMap = null;
                    if (RULE_SYMBOL_WORD_IDX.equals(symbolNode.ruleIndex())) {
                        symbolWithMap = new SymbolWithMap(symbolNode.children().get(0).token().inputTerm, Map.of());
                    }
                    else if (RULE_SYMBOL_RAW_IDX.equals(symbolNode.ruleIndex())) {
                        final String s = symbolNode.children().get(0).token().inputTerm;
                        symbolWithMap = new SymbolWithMap(StringUtils.substring(s, 1, -1).strip(), Map.of(SYMBOL_RAW_RIGHT_SYMBOLS, List.of(true)));
                    }
                    else if (RULE_SYMBOL_WORD_WITH_LABEL_IDX.equals(symbolNode.ruleIndex())) {
                        final String s0 = symbolNode.children().get(0).token().inputTerm;
                        final String s1 = StringUtils.substring(symbolNode.children().get(1).token().inputTerm, 1, -1);
                        symbolWithMap = new SymbolWithMap(s0, GlrLabels.parseLabel(s1));
                    }

                    if (symbolWithMap!=null) {
                        rightSymbols.add(symbolWithMap);
                    }
                }
                result.add(new ScanRule(leftSymbol, weight, rightSymbols));
            }
        }

        return result;
    }
}
