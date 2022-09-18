/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static ai.metaheuristic.glr.GlrConsts.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Map<String, List<Object>> m = GlrLabels.parseLabel("agr-gnc=1");

        assertTrue(m.containsKey("agr-gnc"));
        assertEquals(1, m.get("agr-gnc").size());
        assertEquals("1", m.get("agr-gnc").get(0));
    }

    @Test
    public void test_56() {
        Map<String, List<Object>> m = GlrLabels.parseLabel("regex=(\\d{1,2})");

        assertTrue(m.containsKey(("regex")));
        assertEquals(1, m.size());
        assertEquals(1, m.get(("regex")).size());
        assertEquals("(\\d{1,2})", m.get(("regex")).get(0));
    }

    @Test
    public void test_57() {
        Map<String, List<Object>> m = GlrLabels.parseLabel("reg-l-all, gram=nomn");

        assertEquals(2, m.size());
        assertTrue(m.containsKey("reg-l-all"));
        assertTrue(m.containsKey("gram"));

        assertEquals(0, m.get("reg-l-all").size());
        assertEquals(1, m.get("gram").size());
        assertEquals("nomn", m.get("gram").get(0));
    }

    @Test
    public void test_58() {
        Map<String, List<Object>> m = GlrLabels.parseLabel(
                "reg-l-all, gram=nomn, reg-h-first, regex=(\\d{1,2}), reg-h-all");

        assertEquals(5, m.size());
        assertTrue(m.containsKey("reg-l-all"));
        assertTrue(m.containsKey("gram"));
        assertTrue(m.containsKey("reg-h-first"));
        assertTrue(m.containsKey("regex"));
        assertTrue(m.containsKey("reg-h-all"));
    }
}
