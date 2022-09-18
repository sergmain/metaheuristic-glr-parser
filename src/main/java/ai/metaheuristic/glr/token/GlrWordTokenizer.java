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


    private static final LinkedHashMap<String, String> symbolRegexWordDict = new LinkedHashMap<>();

    static {
        symbolRegexWordDict.put("word", "[\\p{L}\\p{Digit}_-]+");
        symbolRegexWordDict.put("number", "[\\d]+");
        symbolRegexWordDict.put("space", "[\\s]+");
        symbolRegexWordDict.put("newline", "[\\n]+");
        symbolRegexWordDict.put("dot", "[\\.]+");
        symbolRegexWordDict.put("comma", "[,]+");
        symbolRegexWordDict.put("colon", "[:]+");
        symbolRegexWordDict.put("percent", "[%]+");
        symbolRegexWordDict.put("quote", "[\"'«»`]+");
        symbolRegexWordDict.put("brace", "[\\(\\)\\{\\}\\[\\]]+");
    }

    public GlrWordTokenizer() {
        super(symbolRegexWordDict, List.of("space"));
    }
}
