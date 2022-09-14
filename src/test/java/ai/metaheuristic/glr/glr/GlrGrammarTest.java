/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/14/2022
 * Time: 7:36 AM
 */
public class GlrGrammarTest {

    @Test
    public void test_99() {

        String expected = "['@', 'S', 'S', 'S', 'Rule', 'S', 'Rule', 'Rule', 'word', 'sep', 'Options', 'Options', 'Options', 'alt', 'Option', 'Options', 'Option', 'Option', 'Symbols', 'weight', 'Option', 'Symbols', 'Symbols', 'Symbols', 'Symbol', 'Symbols', 'Symbol', 'Symbol', 'word', 'label', 'Symbol', 'word', 'Symbol', 'raw']";

        List<String> allSymbols = GlrGrammarParser.grammar.all_symbols();

        assertEquals(expected, "["+allSymbols.stream().map(s->"'"+s+"'").collect(Collectors.joining(", "))+"]");
    }
}
