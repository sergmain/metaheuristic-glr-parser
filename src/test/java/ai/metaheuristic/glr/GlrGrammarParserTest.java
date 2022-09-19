/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrTextTokenPosition;
import ai.metaheuristic.glr.token.GlrToken;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ai.metaheuristic.glr.GlrConsts.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 5:21 PM
 */
public class GlrGrammarParserTest {

    @Test
    public void test_54() {
        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_OPTION_SYMBOLS_WEIGHT_IDX)
                .rightSymbols().contains(GlrConsts.SYMBOL_WEIGHT_RIGHT_SYMBOLS));
        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_WORD_WITH_LABEL_IDX)
                .rightSymbols().contains(SYMBOL_WORD_RIGHT_SYMBOLS));
        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_WORD_WITH_LABEL_IDX)
                .rightSymbols().contains(SYMBOL_LABEL_RIGHT_SYMBOLS));

        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_WORD_IDX)
                .rightSymbols().contains(SYMBOL_WORD_RIGHT_SYMBOLS));

        assertTrue(GlrGrammarParser.GLR_BASE_GRAMMAR.rules.get(GlrGrammarParser.RULE_SYMBOL_RAW_IDX)
                .rightSymbols().contains(SYMBOL_RAW_RIGHT_SYMBOLS));
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

    @Test
    public void test_197() {
        final String SIMPLE_GRAMMAR = """
        
        S = adj<agr-gnc=1> CLOTHES
        S = CLOTHES adj<agr-gnc=-1>
        """;

        GlrGrammar grammar = GlrGrammarParser.parse(SIMPLE_GRAMMAR, "S");


        assertEquals(3, grammar.rules.size());
        final GlrGrammar.Rule rule0 = grammar.rules.get(0);
        assertEquals("@", rule0.leftSymbol());
        assertEquals(0, rule0.index());
        assertNotNull(rule0.params());
        assertEquals(1, rule0.params().size());
        assertTrue(rule0.params().get(0).containsKey(""));
        assertEquals(0, rule0.params().get(0).get("").size());


        final GlrGrammar.Rule rule1 = grammar.rules.get(1);
        assertEquals("S", rule1.leftSymbol());
        assertEquals(1, rule1.index());
        assertNotNull(rule1.params());
        assertEquals(2, rule1.params().size());
        assertTrue(rule1.params().get(0).containsKey("agr-gnc"));
        assertEquals(1, rule1.params().get(0).get("agr-gnc").size());
        assertEquals("1", rule1.params().get(0).get("agr-gnc").get(0));
        assertEquals(0, rule1.params().get(1).size());


        final GlrGrammar.Rule rule2 = grammar.rules.get(2);
        assertEquals("S", rule2.leftSymbol());
        assertEquals(2, rule2.index());
        assertNotNull(rule2.params());
        assertEquals(2, rule2.params().size());
        assertEquals(0, rule2.params().get(0).size());
        assertTrue(rule2.params().get(1).containsKey("agr-gnc"));
        assertEquals("-1", rule2.params().get(1).get("agr-gnc").get(0));


    }

    @Test
    public void test_99() {
        final String SIMPLE_GRAMMAR = """
        
        S = adj<agr-gnc=1> CLOTHES
        S = CLOTHES adj<agr-gnc=-1>
        """;

        List<GlrToken> tokens = GlrGrammarParser.LR_GRAMMAR_TOKENIZER.tokenize(SIMPLE_GRAMMAR);
        assertEquals(11, tokens.size());
        String actual = "";
        for (int i = 0; i < tokens.size(); i++) {
            GlrToken t = tokens.get(i);
            assertNotNull(t.position);
            GlrTextTokenPosition pos = (GlrTextTokenPosition)t.position;
            actual += String.format(
                    "%02d = {Token: 6} Token(symbol='%s', value='%s', start=%d, end=%d, input_term='%s', params=None)\n",
                    i, t.symbol, t.value, pos.start, pos.end, t.inputTerm);
        }

        String expected = """
        00 = {Token: 6} Token(symbol='word', value='S', start=1, end=2, input_term='S', params=None)
        01 = {Token: 6} Token(symbol='sep', value='=', start=3, end=4, input_term='=', params=None)
        02 = {Token: 6} Token(symbol='word', value='adj', start=5, end=8, input_term='adj', params=None)
        03 = {Token: 6} Token(symbol='label', value='<agr-gnc=1>', start=8, end=19, input_term='<agr-gnc=1>', params=None)
        04 = {Token: 6} Token(symbol='word', value='CLOTHES', start=20, end=27, input_term='CLOTHES', params=None)
        05 = {Token: 6} Token(symbol='word', value='S', start=28, end=29, input_term='S', params=None)
        06 = {Token: 6} Token(symbol='sep', value='=', start=30, end=31, input_term='=', params=None)
        07 = {Token: 6} Token(symbol='word', value='CLOTHES', start=32, end=39, input_term='CLOTHES', params=None)
        08 = {Token: 6} Token(symbol='word', value='adj', start=40, end=43, input_term='adj', params=None)
        09 = {Token: 6} Token(symbol='label', value='<agr-gnc=-1>', start=43, end=55, input_term='<agr-gnc=-1>', params=None)
        10 = {Token: 6} Token(symbol='$', value='', start=56, end=-1, input_term='', params=None)
        """;

        assertEquals(expected, actual);
    }

    @Test
    public void test_198() {
        final String SIMPLE_GRAMMAR = """
        
        S = adj<agr-gnc=1> CLOTHES
        S = CLOTHES adj<agr-gnc=-1>
        """;

        GlrGrammar grammar = GlrGrammarParser.parse(SIMPLE_GRAMMAR, "S");
        List<LinkedHashMap<String, List<GlrLr.Action>>> actionTable = GlrLr.generateActionGotoTable(grammar);

        String expected = """
        00 = {'S': [Action(type='G', state=1, ruleIndex=None)], 'adj': [Action(type='S', state=2, ruleIndex=None)], 'CLOTHES': [Action(type='S', state=3, ruleIndex=None)]})
        01 = {'$': [Action(type='A', state=None, ruleIndex=None)]})
        02 = {'CLOTHES': [Action(type='S', state=4, ruleIndex=None)]})
        03 = {'adj': [Action(type='S', state=5, ruleIndex=None)]})
        04 = {'$': [Action(type='R', state=None, ruleIndex=1)]})
        05 = {'$': [Action(type='R', state=None, ruleIndex=2)]})
        """;

        assertEquals(6, actionTable.size());

        String actual = UtilsForTesing.actionTableAsString(actionTable);
        assertEquals(expected, actual);

    }


}
