/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:23 PM
 */
public interface GlrTokenizer {
    List<? extends GlrToken> tokenize(String text);
}
