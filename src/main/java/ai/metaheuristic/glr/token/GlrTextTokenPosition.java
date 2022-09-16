/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.token;

import ai.metaheuristic.glr.GlrTokenPosition;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 7:54 PM
 */
@RequiredArgsConstructor
@ToString
public class GlrTextTokenPosition implements GlrTokenPosition {

    @ToString.Include
    public final int start;
    @ToString.Include
    public final int end;

}
