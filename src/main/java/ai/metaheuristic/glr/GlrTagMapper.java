/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 9/17/2022
 * Time: 10:29 AM
 */
public class GlrTagMapper {

    private static final Map<String, String> TAG_MAPPER_1 = Map.of(
            "NOUN", "noun",
            "ADJF", "adj",
            "ADJS", "adj",
            "COMP", "adj",
            "VERB", "verb",
            "INFN", "verb",
            "PRTF", "pr",
            "PRTS", "pr",
            "GRND", "dpr",
            "NUMR", "num"
    );

    private static final Map<String, String> TAG_MAPPER_2 = Map.of(
            "ADVB", "adv",
            "NPRO", "pnoun",
            "PRED", "adv",
            "PREP", "prep",
            "CONJ", "conj",
            "PRCL", "prcl",
            "INTJ", "noun",
            "LATN", "lat",
            "NUMB", "num"
    );

    private static final Map<String, String> TAG_MAPPER = new HashMap<>();

    static {
        TAG_MAPPER.putAll(TAG_MAPPER_1);
        TAG_MAPPER.putAll(TAG_MAPPER_2);
    }

    public static String map(String tag) {
        return TAG_MAPPER.get(tag);
    }

}
