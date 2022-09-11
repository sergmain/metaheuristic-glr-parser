/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import java.util.LinkedHashMap;
import java.util.List;

import static ai.metaheuristic.glr.glr.GlrGrammar.*;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 9:02 PM
 */
public class GlrGrammarParser {
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

    public static final GrlTokenizer.SimpleRegexTokenizer lr_grammar_tokenizer;

    static {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("sep", "=");
        map.put("alt", "[|]");
        map.put("word", "\\b[\\p{L}\\p{Digit}_]+\\b");
//        map.put("raw", "'[\\p{L}\\p{Digit}_]+'");
        map.put("raw", "(?:'.+?'|\\\".+?\\\")");
        map.put("whitespace", "[ \\t\\r\\n]+");
        map.put("minus", "[-]");
        map.put("label", "<.+?>");
        map.put("weight", "\\(\\d+(?:[.,]\\d+)?\\)");
        lr_grammar_tokenizer = new GrlTokenizer.SimpleRegexTokenizer(map, List.of("whitespace"));
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

    public GlrGrammar grammar = new GlrGrammar(
            new Rule(0, "@", List.of("S"), false, null, 1.0),
            new Rule(1, "S", List.of("S", "Rule"), false, null, 1.0),
            new Rule(2,"S", List.of("Rule"), false, null, 1.0),
            new Rule(3,"Rule", List.of("word", "sep", "Options"), false, null, 1.0),
            new Rule(4,"Options", List.of("Options", "alt", "Option"), false, null, 1.0),
            new Rule(5,"Options", List.of("Option"), false, null, 1.0),
            new Rule(6,"Option", List.of("Symbols", "weight"), false, null, 1.0),
            new Rule(7,"Option", List.of("Symbols"), false, null, 1.0),
            new Rule(8,"Symbols", List.of("Symbols", "Symbol"), false, null, 1.0),
            new Rule(9,"Symbols", List.of("Symbol"), false, null, 1.0),
            new Rule(10,"Symbol", List.of("word", "label"), false, null, 1.0),
            new Rule(11,"Symbol", List.of("word"), false, null, 1.0),
            new Rule(12,"Symbol", List.of("raw"), false, null, 1.0)
    );

    public GlrParser parser = new GlrParser(grammar);

}
