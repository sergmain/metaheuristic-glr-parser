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
 * Time: 9:57 PM
 */
public class GlrCharTypeTokenizer extends GlrSimpleRegexTokenizer {
/*
    String py3 = """
            class CharTypeTokenizer(SimpleRegexTokenizer):
                def __init__(self):
                    symbol_regex_dict = {
                        'alpha': r'[^\\W\\d_]+',
                        'space': r'\\s+',
                        'digit': r'\\d+',
                        'punct': r'[^\\w\\s]|_',
                    }
                    super(CharTypeTokenizer, self).__init__(symbol_regex_dict, ['space'])
            """;

*/
    public static final LinkedHashMap<String, String> symbolRegexCharDict = new LinkedHashMap<>();

    static {
        symbolRegexCharDict.put("alpha", "[^\\p{L}\\p{Digit}_]+");
        symbolRegexCharDict.put("space", "\\s+");
        symbolRegexCharDict.put("digit", "\\d+");
        symbolRegexCharDict.put("punct", "[^[\\p{L}\\p{Digit}_-]\\s]|_");
    }

    public GlrCharTypeTokenizer() {
        super(symbolRegexCharDict, List.of("space"));
    }
}
