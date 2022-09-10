/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 10:39 PM
 */
public class GlrNormalizerFactory {
    public static final GlrNormalizer INSTANCE = new GlrNormalizer();

    public static GlrNormalizer getInstance() {
        return INSTANCE;
    }
}
