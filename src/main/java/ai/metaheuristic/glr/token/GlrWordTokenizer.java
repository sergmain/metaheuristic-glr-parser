/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.token;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 9:56 PM
 */
public class GlrWordTokenizer extends GlrSimpleRegexTokenizer {
/*
    String py4 = """
    class WordTokenizer(SimpleRegexTokenizer):
        def __init__(self):
            symbol_regex_dict = {
                "word": r"[\\w\\d_-]+",
                "number": r"[\\d]+",
                "space": r"[\\s]+",
                "newline": r"[\\n]+",
                "dot": r"[\\.]+",
                "comma": r"[,]+",
                "colon": r"[:]+",
                "percent": r"[%]+",
                "quote": r"[\\"\\'«»`]+",
                "brace": r"[\\(\\)\\{\\}\\[\\]]+",
            }
            super(WordTokenizer, self).__init__(symbol_regex_dict, ['space'])
    """;
*/


    private static final LinkedHashMap<String, String> symbol_regex_word_dict = new LinkedHashMap<>();

    static {
        symbol_regex_word_dict.put("word", "[\\p{L}\\p{Digit}_-]+");
        symbol_regex_word_dict.put("number", "[\\d]+");
        symbol_regex_word_dict.put("space", "[\\s]+");
        symbol_regex_word_dict.put("newline", "[\\n]+");
        symbol_regex_word_dict.put("dot", "[\\.]+");
        symbol_regex_word_dict.put("comma", "[,]+");
        symbol_regex_word_dict.put("colon", "[:]+");
        symbol_regex_word_dict.put("percent", "[%]+");
        symbol_regex_word_dict.put("quote", "[\"'«»`]+");
        symbol_regex_word_dict.put("brace", "[\\(\\)\\{\\}\\[\\]]+");
    }

    public GlrWordTokenizer() {
        super(symbol_regex_word_dict, List.of("space"));
    }
}
