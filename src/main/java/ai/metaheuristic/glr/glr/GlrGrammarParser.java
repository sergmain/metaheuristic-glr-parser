/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.Glr;
import ai.metaheuristic.glr.glrengine.GlrScanner;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

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

}
