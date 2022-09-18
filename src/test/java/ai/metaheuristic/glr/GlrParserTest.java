/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrTextTokenPosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 9/14/2022
 * Time: 10:21 AM
 */
public class GlrParserTest {

    @Test
    public void test_97() {
        GlrGrammar grammar = new GlrGrammar(
                new GlrGrammar.Rule(0, "@", List.of("S"), false, null, 1.0),
                new GlrGrammar.Rule(1, "S", List.of("S", "Rule"), false, null, 1.0),
                new GlrGrammar.Rule(2,"S", List.of("Rule"), false, null, 1.0),
                new GlrGrammar.Rule(3,"Rule", List.of("word", "sep", "Options"), false, null, 1.0),
                new GlrGrammar.Rule(4,"Options", List.of("Options", "alt", "Option"), false, null, 1.0),
                new GlrGrammar.Rule(5,"Options", List.of("Option"), false, null, 1.0),
                new GlrGrammar.Rule(6,"Option", List.of("Symbols", "weight"), false, null, 1.0),
                new GlrGrammar.Rule(7,"Option", List.of("Symbols"), false, null, 1.0),
                new GlrGrammar.Rule(8,"Symbols", List.of("Symbols", "Symbol"), false, null, 1.0),
                new GlrGrammar.Rule(9,"Symbols", List.of("Symbol"), false, null, 1.0),
                new GlrGrammar.Rule(10,"Symbol", List.of("word", "label"), false, null, 1.0),
                new GlrGrammar.Rule(11,"Symbol", List.of("word"), false, null, 1.0),
                new GlrGrammar.Rule(12,"Symbol", List.of("raw"), false, null, 1.0)
        );

        List<LinkedHashMap<String, List<GlrLr.Action>>> actionTable = GlrLr.generateActionGotoTable(grammar);

        String expected = """
        00 = {'S': [Action(type='G', state=1, ruleIndex=None)], 'Rule': [Action(type='G', state=2, ruleIndex=None)], 'word': [Action(type='S', state=3, ruleIndex=None)]})
        01 = {'$': [Action(type='A', state=None, ruleIndex=None)], 'Rule': [Action(type='G', state=4, ruleIndex=None)], 'word': [Action(type='S', state=3, ruleIndex=None)]})
        02 = {'word': [Action(type='R', state=None, ruleIndex=2)], '$': [Action(type='R', state=None, ruleIndex=2)]})
        03 = {'sep': [Action(type='S', state=5, ruleIndex=None)]})
        04 = {'word': [Action(type='R', state=None, ruleIndex=1)], '$': [Action(type='R', state=None, ruleIndex=1)]})
        05 = {'Options': [Action(type='G', state=6, ruleIndex=None)], 'Option': [Action(type='G', state=7, ruleIndex=None)], 'Symbols': [Action(type='G', state=8, ruleIndex=None)], 'Symbol': [Action(type='G', state=9, ruleIndex=None)], 'word': [Action(type='S', state=10, ruleIndex=None)], 'raw': [Action(type='S', state=11, ruleIndex=None)]})
        06 = {'word': [Action(type='R', state=None, ruleIndex=3)], '$': [Action(type='R', state=None, ruleIndex=3)], 'alt': [Action(type='S', state=12, ruleIndex=None)]})
        07 = {'word': [Action(type='R', state=None, ruleIndex=5)], 'alt': [Action(type='R', state=None, ruleIndex=5)], '$': [Action(type='R', state=None, ruleIndex=5)]})
        08 = {'word': [Action(type='R', state=None, ruleIndex=7), Action(type='S', state=10, ruleIndex=None)], 'alt': [Action(type='R', state=None, ruleIndex=7)], '$': [Action(type='R', state=None, ruleIndex=7)], 'weight': [Action(type='S', state=13, ruleIndex=None)], 'Symbol': [Action(type='G', state=14, ruleIndex=None)], 'raw': [Action(type='S', state=11, ruleIndex=None)]})
        09 = {'weight': [Action(type='R', state=None, ruleIndex=9)], 'word': [Action(type='R', state=None, ruleIndex=9)], 'alt': [Action(type='R', state=None, ruleIndex=9)], 'raw': [Action(type='R', state=None, ruleIndex=9)], '$': [Action(type='R', state=None, ruleIndex=9)]})
        10 = {'weight': [Action(type='R', state=None, ruleIndex=11)], 'word': [Action(type='R', state=None, ruleIndex=11)], 'alt': [Action(type='R', state=None, ruleIndex=11)], 'raw': [Action(type='R', state=None, ruleIndex=11)], '$': [Action(type='R', state=None, ruleIndex=11)], 'label': [Action(type='S', state=15, ruleIndex=None)]})
        11 = {'weight': [Action(type='R', state=None, ruleIndex=12)], 'word': [Action(type='R', state=None, ruleIndex=12)], 'alt': [Action(type='R', state=None, ruleIndex=12)], 'raw': [Action(type='R', state=None, ruleIndex=12)], '$': [Action(type='R', state=None, ruleIndex=12)]})
        12 = {'Option': [Action(type='G', state=16, ruleIndex=None)], 'Symbols': [Action(type='G', state=8, ruleIndex=None)], 'Symbol': [Action(type='G', state=9, ruleIndex=None)], 'word': [Action(type='S', state=10, ruleIndex=None)], 'raw': [Action(type='S', state=11, ruleIndex=None)]})
        13 = {'word': [Action(type='R', state=None, ruleIndex=6)], 'alt': [Action(type='R', state=None, ruleIndex=6)], '$': [Action(type='R', state=None, ruleIndex=6)]})
        14 = {'weight': [Action(type='R', state=None, ruleIndex=8)], 'word': [Action(type='R', state=None, ruleIndex=8)], 'alt': [Action(type='R', state=None, ruleIndex=8)], 'raw': [Action(type='R', state=None, ruleIndex=8)], '$': [Action(type='R', state=None, ruleIndex=8)]})
        15 = {'weight': [Action(type='R', state=None, ruleIndex=10)], 'word': [Action(type='R', state=None, ruleIndex=10)], 'alt': [Action(type='R', state=None, ruleIndex=10)], 'raw': [Action(type='R', state=None, ruleIndex=10)], '$': [Action(type='R', state=None, ruleIndex=10)]})
        16 = {'word': [Action(type='R', state=None, ruleIndex=4)], 'alt': [Action(type='R', state=None, ruleIndex=4)], '$': [Action(type='R', state=None, ruleIndex=4)]})       
        """;

        assertEquals(17, actionTable.size());

        String actual = actionTableAsString(actionTable);
        assertEquals(expected, actual);
    }

    private static String actionTableAsString(List<LinkedHashMap<String, List<GlrLr.Action>>> actionTable) {
        String actual = "";
        for (int i = 0; i < actionTable.size(); i++) {
            LinkedHashMap<String, List<GlrLr.Action>> t = actionTable.get(i);
            String line = String.format("%02d = {", i);
            List<String> ss = new ArrayList<>();
            for (Map.Entry<String, List<GlrLr.Action>> en : t.entrySet()) {
                // [Action(type='R', state=None, ruleIndex=7), Action(type='S', state=10, ruleIndex=None)]
                String as = en.getValue().stream().map(o-> "Action(type='" + o.type() + "', state=" +
                                                           (o.state()==null ? "None" : o.state().toString()) + ", ruleIndex=" + (o.ruleIndex() == null?"None":o.ruleIndex().toString()) + ")").collect(Collectors.joining(", "));
                String s = "'"+en.getKey()+"': [" + as +"]";
                ss.add(s);
            }
            line += String.join(", ", ss);
            line += "})\n";
            actual += line;
        }
        return actual;
    }

    @Test
    public void test_98() {
        List<LinkedHashMap<String, List<GlrLr.Action>>> actionTable = GlrLr.generateActionGotoTable(GlrGrammarParser.GLR_BASE_GRAMMAR);

        String[] keys = new String[] {
                "S,Rule,word", "$,Rule,word", "word,$", "sep", "word,$",
                "Options,Option,Symbols,Symbol,word,raw", "word,$,alt",
                "word,alt,$",
                "word,alt,$,weight,Symbol,raw", "weight,word,alt,raw,$", "weight,word,alt,raw,$,label",
                "weight,word,alt,raw,$", "Option,Symbols,Symbol,word,raw", "word,alt,$", "weight,word,alt,raw,$", "weight,word,alt,raw,$", "word,alt,$"
        };

        assertEquals(17, actionTable.size());

        for (int i = 0; i < actionTable.size(); i++) {
            LinkedHashMap<String, List<GlrLr.Action>> map = actionTable.get(i);
            assertFalse(map.isEmpty());
            String resultKeys = String.join(",", map.keySet());
            assertEquals(keys[i], resultKeys, "i: " + i);
        }
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

        String actual = actionTableAsString(actionTable);
        assertEquals(expected, actual);

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


}
