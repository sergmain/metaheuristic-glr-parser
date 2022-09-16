/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.token;

import ai.metaheuristic.glr.GlrToken;
import ai.metaheuristic.glr.GlrTokenPosition;
import company.evo.jmorphy2.Tag;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 8:09 PM
 */
@ToString(onlyExplicitlyIncluded = true)
public final class GlrTextToken implements GlrToken {

    @SuppressWarnings("unused")
    private final String py1 = """
    class Token(namedtuple('Token', ['symbol', 'value', 'start', 'end', 'input_term', 'params'])):
        '''
        Used as token and as node in AST (abstract syntax tree)
        '''
        __slots__ = ()

        def __new__(cls, symbol, value='', start=-1, end=-1, input_term='', params=None):
            return super(cls, Token).__new__(cls, symbol, value, start, end, input_term, params)

        # def __repr__(self):
        #     return u'%s(%s)' % (self.symbol, self.value)


    class TokenizerException(Exception):
        pass
    """;

    @ToString.Include
    private final String symbol;
    @ToString.Include
    private final String value;
    @ToString.Include
    @Nullable
    public GlrTextTokenPosition position;
    @ToString.Include
    private final String input_term;
    @ToString.Include
    @Nullable
    private final Tag params;

    public GlrTextToken(String symbol) {
        this(symbol, "", null, "", null);
    }

    public GlrTextToken(String symbol, String value, @Nullable GlrTokenPosition position, String input_term, @Nullable Tag params) {
        if (position!=null && !(position instanceof GlrTextTokenPosition)) {
            throw new IllegalStateException("(!(position instanceof GlrTextTokenPosition glrTextTokenPosition))");
        }
        this.symbol = symbol;
        this.value = value;
        this.position = (GlrTextTokenPosition)position;
        this.input_term = input_term;
        this.params = params;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getInput_term() {
        return input_term;
    }

    @Override
    @Nullable
    public Tag getParams() {
        return params;
    }

    @Override
    public GlrTokenPosition getPosition() {
        return position;
    }
}
