/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.glr.token.GlrWordTokenizer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 9:47 PM
 */
public class GlrWordTokenizerFactory {
    public static final Map<Integer, GlrWordTokenizer> WORD_TOKENIZER_MAP = new HashMap<>();

    public static GlrWordTokenizer getInstance() {
        return WORD_TOKENIZER_MAP.computeIfAbsent(1, (o)->new GlrWordTokenizer());
    }

}
