/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.GlrConsts;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Sergio Lissner
 * Date: 9/14/2022
 * Time: 10:21 AM
 */
public class GlrParserTest {

    @Test
    public void test_98() {
        List<LinkedHashMap<String, List<GlrLr.Action>>> actionTable = GlrLr.generate_action_goto_table(GlrGrammarParser.grammar);

        String[] keys = new String[] {
                "S,Rule,word", "$,Rule,word", "word,$", "sep", "word,$",
                "Options,Option,Symbols,Symbol,word,raw", "word,$,alt",
                "word,alt,$",
                "word,alt,$,weight,Symbol,raw", "weight,word,alt,raw,$", "weight,word,alt,raw,$,label",
                "weight,word,alt,raw,$", "Option,Symbols,Symbol,word,raw", "word,alt,$", "weight,word,alt,raw,$", "weight,word,alt,raw,$", "word,alt,$"
        };

        String expected = """
        00 = {defaultdict: 3} defaultdict(<class 'list'>, {'S': [Action(type='G', state=1, rule_index=None)], 'Rule': [Action(type='G', state=2, rule_index=None)], 'word': [Action(type='S', state=3, rule_index=None)]})
        01 = {defaultdict: 3} defaultdict(<class 'list'>, {'$': [Action(type='A', state=None, rule_index=None)], 'Rule': [Action(type='G', state=4, rule_index=None)], 'word': [Action(type='S', state=3, rule_index=None)]})
        02 = {defaultdict: 2} defaultdict(<class 'list'>, {'word': [Action(type='R', state=None, rule_index=2)], '$': [Action(type='R', state=None, rule_index=2)]})
        03 = {defaultdict: 1} defaultdict(<class 'list'>, {'sep': [Action(type='S', state=5, rule_index=None)]})
        04 = {defaultdict: 2} defaultdict(<class 'list'>, {'word': [Action(type='R', state=None, rule_index=1)], '$': [Action(type='R', state=None, rule_index=1)]})
        05 = {defaultdict: 6} defaultdict(<class 'list'>, {'Options': [Action(type='G', state=6, rule_index=None)], 'Option': [Action(type='G', state=7, rule_index=None)], 'Symbols': [Action(type='G', state=8, rule_index=None)], 'Symbol': [Action(type='G', state=9, rule_index=None)], 'word': [Action(type='S', state=10, rule_index=None)], 'raw': [Action(type='S', state=11, rule_index=None)]})
        06 = {defaultdict: 3} defaultdict(<class 'list'>, {'word': [Action(type='R', state=None, rule_index=3)], '$': [Action(type='R', state=None, rule_index=3)], 'alt': [Action(type='S', state=12, rule_index=None)]})
        07 = {defaultdict: 3} defaultdict(<class 'list'>, {'alt': [Action(type='R', state=None, rule_index=5)], 'word': [Action(type='R', state=None, rule_index=5)], '$': [Action(type='R', state=None, rule_index=5)]})
        08 = {defaultdict: 6} defaultdict(<class 'list'>, {'alt': [Action(type='R', state=None, rule_index=7)], 'word': [Action(type='R', state=None, rule_index=7), Action(type='S', state=10, rule_index=None)], '$': [Action(type='R', state=None, rule_index=7)], 'weight': [Action(type='S', state=13, rule_index=None)], 'Symbol': [Action(type='G', state=14, rule_index=None)], 'raw': [Action(type='S', state=11, rule_index=None)]})
        09 = {defaultdict: 5} defaultdict(<class 'list'>, {'weight': [Action(type='R', state=None, rule_index=9)], 'word': [Action(type='R', state=None, rule_index=9)], 'raw': [Action(type='R', state=None, rule_index=9)], 'alt': [Action(type='R', state=None, rule_index=9)], '$': [Action(type='R', state=None, rule_index=9)]})
        10 = {defaultdict: 6} defaultdict(<class 'list'>, {'weight': [Action(type='R', state=None, rule_index=11)], 'word': [Action(type='R', state=None, rule_index=11)], 'raw': [Action(type='R', state=None, rule_index=11)], 'alt': [Action(type='R', state=None, rule_index=11)], '$': [Action(type='R', state=None, rule_index=11)], 'label': [Action(type='S', state=15, rule_index=None)]})
        11 = {defaultdict: 5} defaultdict(<class 'list'>, {'weight': [Action(type='R', state=None, rule_index=12)], 'word': [Action(type='R', state=None, rule_index=12)], 'raw': [Action(type='R', state=None, rule_index=12)], 'alt': [Action(type='R', state=None, rule_index=12)], '$': [Action(type='R', state=None, rule_index=12)]})
        12 = {defaultdict: 5} defaultdict(<class 'list'>, {'Option': [Action(type='G', state=16, rule_index=None)], 'Symbols': [Action(type='G', state=8, rule_index=None)], 'Symbol': [Action(type='G', state=9, rule_index=None)], 'word': [Action(type='S', state=10, rule_index=None)], 'raw': [Action(type='S', state=11, rule_index=None)]})
        13 = {defaultdict: 3} defaultdict(<class 'list'>, {'alt': [Action(type='R', state=None, rule_index=6)], 'word': [Action(type='R', state=None, rule_index=6)], '$': [Action(type='R', state=None, rule_index=6)]})
        14 = {defaultdict: 5} defaultdict(<class 'list'>, {'weight': [Action(type='R', state=None, rule_index=8)], 'word': [Action(type='R', state=None, rule_index=8)], 'raw': [Action(type='R', state=None, rule_index=8)], 'alt': [Action(type='R', state=None, rule_index=8)], '$': [Action(type='R', state=None, rule_index=8)]})
        15 = {defaultdict: 5} defaultdict(<class 'list'>, {'weight': [Action(type='R', state=None, rule_index=10)], 'word': [Action(type='R', state=None, rule_index=10)], 'raw': [Action(type='R', state=None, rule_index=10)], 'alt': [Action(type='R', state=None, rule_index=10)], '$': [Action(type='R', state=None, rule_index=10)]})
        16 = {defaultdict: 3} defaultdict(<class 'list'>, {'alt': [Action(type='R', state=None, rule_index=4)], 'word': [Action(type='R', state=None, rule_index=4)], '$': [Action(type='R', state=None, rule_index=4)]})       
        """;

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
        List<GrlTokenizer.Token> tokens = GlrGrammarParser.lr_grammar_tokenizer.scan(GlrConsts.SIMPLE_GRAMMAR);
        assertEquals(11, tokens.size());
        String actual = "";
        for (int i = 0; i < tokens.size(); i++) {
            GrlTokenizer.Token t = tokens.get(i);
            actual += String.format(
                    "%02d = {Token: 6} Token(symbol='%s', value='%s', start=%d, end=%d, input_term='%s', params=None)\n",
                    i, t.symbol, t.value, t.start, t.end, t.input_term);
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
}
