/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Sergio Lissner
 * Date: 9/12/2022
 * Time: 2:08 PM
 */
public class GlrLrTest {

    @Test
    public void test_97() {
        List<GlrLr.State> states = GlrLr.generateStateGraph(GlrGrammarParser.GLR_BASE_GRAMMAR);

        assertEquals(17, states.size());
    }


    @Test
    public void test_98() {
        List<GlrLr.Item> items = GlrLr.closure(List.of(GlrLr.EMPTY_ITEM), GlrGrammarParser.GLR_BASE_GRAMMAR);

        assertEquals(4, items.size());
        // (#0.0, #1.0, #2.0, #3.0)
        Assertions.assertEquals(new GlrLr.Item(0, 0), items.get(0));
        Assertions.assertEquals(new GlrLr.Item(1, 0), items.get(1));
        Assertions.assertEquals(new GlrLr.Item(2, 0), items.get(2));
        Assertions.assertEquals(new GlrLr.Item(3, 0), items.get(3));
    }

    @Test
    public void test_99() {
        // unique([Item(3, 4), Item(1, 2), Item(0, 3)])
        List<GlrLr.Item> items = List.of(
                new GlrLr.Item(3, 4),
                new GlrLr.Item(1, 2),
                new GlrLr.Item(0, 3));


        // [#0.3, #1.2, #3.4]
        LinkedList<GlrLr.Item> itemset = GlrLr.uniqueAndSorted(items);

        Assertions.assertEquals(new GlrLr.Item(0, 3), itemset.get(0));
        Assertions.assertEquals(new GlrLr.Item(1, 2), itemset.get(1));
        Assertions.assertEquals(new GlrLr.Item(3, 4), itemset.get(2));
    }

    @Test
    public void test_56() {
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
    public void test_57() {
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

        String actual = UtilsForTesing.actionTableAsString(actionTable);
        assertEquals(expected, actual);
    }


}
