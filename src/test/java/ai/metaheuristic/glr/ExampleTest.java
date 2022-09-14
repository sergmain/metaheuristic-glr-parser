/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.glrengine.Example;
import ai.metaheuristic.glr.glrengine.Glr;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 10:24 PM
 */
public class ExampleTest {

    String expected = """
        S = adj<agr-gnc=1> CLOTHES
    
        Word = word
        Word = noun
        Word = adj
        Word = verb
        Word = pr
        Word = dpr
        Word = num
        Word = adv
        Word = pnoun
        Word = prep
        Word = conj
        Word = prcl
        Word = lat
    
    CLOTHES = 'куртка' | 'пальто' | 'шуба'""";

    @Test
    public void test_99() {
        final String grammarRules = Glr.combineGrammarRules(Example.grammar, Example.dictionaries, Glr.DEFAULT_GRAMMAR);


        assertEquals(expected, grammarRules);
    }
}
