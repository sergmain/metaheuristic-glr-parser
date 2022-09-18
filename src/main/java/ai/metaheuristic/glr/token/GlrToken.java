/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.token;

import ai.metaheuristic.glr.GlrTokenPosition;
import company.evo.jmorphy2.Tag;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 8:09 PM
 */
@ToString
public final class GlrToken {

    public final String symbol;
    public final Object value;
    @Nullable
    public GlrTokenPosition position;
    public final String inputTerm;
    @Nullable
    public final Tag params;

    public GlrToken(String symbol) {
        this(symbol, "", null, "", null);
    }

    public GlrToken(String symbol, Object value) {
        this(symbol, value, null, "", null);
    }

    public GlrToken(String symbol, Object value, @Nullable GlrTokenPosition position, String inputTerm, @Nullable Tag params) {
        this.symbol = symbol;
        this.value = value;
        this.position = position;
        this.inputTerm = inputTerm;
        this.params = params;
    }

}
