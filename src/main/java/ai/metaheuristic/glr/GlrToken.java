/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import company.evo.jmorphy2.Tag;
import javax.annotation.Nullable;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 9:23 PM
 */
public interface GlrToken {
    String getSymbol();

    String getValue();

    String getInput_term();

    @Nullable
    Tag getParams();

    GlrTokenPosition getPosition();
}
