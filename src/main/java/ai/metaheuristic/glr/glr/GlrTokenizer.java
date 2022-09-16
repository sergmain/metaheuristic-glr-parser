/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.glr.token.GlrTextToken;

import java.util.List;
import java.util.function.Function;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:23 PM
 */
public interface GlrTokenizer {
    List<GlrTextToken> tokenize(String text);
}
