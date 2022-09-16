/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.GlrConsts;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static ai.metaheuristic.glr.GlrConsts.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 5:21 PM
 */
public class GlrGrammarParserTest {

    @Test
    public void test_54() {
        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_OPTION_SYMBOLS_WEIGHT_IDX)
                .right_symbols().contains(GlrConsts.SYMBOL_WEIGHT_RIGHT_SYMBOLS));
        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_WORD_WITH_LABEL_IDX)
                .right_symbols().contains(SYMBOL_WORD_RIGHT_SYMBOLS));
        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_WORD_WITH_LABEL_IDX)
                .right_symbols().contains(SYMBOL_LABEL_RIGHT_SYMBOLS));

        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_WORD_IDX)
                .right_symbols().contains(SYMBOL_WORD_RIGHT_SYMBOLS));

        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_RAW_IDX)
                .right_symbols().contains(SYMBOL_RAW_RIGHT_SYMBOLS));
    }

    @Test
    public void test_55() {
        Map<String, List<Object>> m = GlrGrammarParser._parse_labels("agr-gnc=1");

        assertTrue(m.containsKey("agr-gnc"));
        assertEquals(1, m.get("agr-gnc").size());
        assertEquals("1", m.get("agr-gnc").get(0));
    }
}
