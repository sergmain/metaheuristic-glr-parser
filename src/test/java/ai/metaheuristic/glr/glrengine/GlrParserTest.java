/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import ai.metaheuristic.glr.Example;
import ai.metaheuristic.glr.Glr;
import ai.metaheuristic.glr.GlrConsts;
import org.junit.jupiter.api.Test;
import org.springframework.lang.Nullable;

import java.util.*;

import static ai.metaheuristic.glr.Glr.DEFAULT_PARSER;
import static ai.metaheuristic.glr.glrengine.GlrParser.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 11:41 PM
 */
public class GlrParserTest {


    @Test
    public void test_99() {

        final String grammarRules = Glr.combineGrammarRules(Example.grammar, Example.dictionaries, Glr.DEFAULT_GRAMMAR);
        Set<String> keys = new LinkedHashSet<>(DEFAULT_PARSER.keySet());
        keys.add("$");


        List<ParsedRule> parsedRules = make_rules(GlrConsts.DEFUALT_START, grammarRules, keys);


        assertEquals(18, parsedRules.size());
/*
        0('@', ('S',), True, [])
        1('S', ('adj', 'CLOTHES'), True, [defaultdict(<class 'list'>, {'agr-gnc': ['1']}), None])
        2('Word', ('word',), True, [None])
        3('Word', ('noun',), True, [None])
        4('Word', ('adj',), True, [None])
        5('Word', ('verb',), True, [None])
        6('Word', ('pr',), True, [None])
        7('Word', ('dpr',), True, [None])
        8('Word', ('num',), True, [None])
        9('Word', ('adv',), True, [None])
        0('Word', ('pnoun',), True, [None])
        1('Word', ('prep',), True, [None])
        2('Word', ('conj',), True, [None])
        3('Word', ('prcl',), True, [None])
        4('Word', ('lat',), True, [None])
        5('CLOTHES', ("'куртка'",), True, [])
        6('CLOTHES', ("'пальто'",), True, [])
        7('CLOTHES', ("'шуба'",), True, [])
*/
        assertEquals(new ParsedRule("@", ofNull("S"), true, List.of()), parsedRules.get(0));
        assertEquals(new ParsedRule("S", ofNull("adj", "CLOTHES"), true, List.of(Map.of("agr-gnc", List.of("1")))), parsedRules.get(1));
        assertEquals(new ParsedRule("Word", ofNull("word"), true, List.of()), parsedRules.get(2));
        assertEquals(new ParsedRule("Word", ofNull("noun"), true, List.of()), parsedRules.get(3));
        assertEquals(new ParsedRule("Word", ofNull("adj"), true, List.of()), parsedRules.get(4));
        assertEquals(new ParsedRule("Word", ofNull("verb"), true, List.of()), parsedRules.get(5));
        assertEquals(new ParsedRule("Word", ofNull("pr"), true, List.of()), parsedRules.get(6));
        assertEquals(new ParsedRule("Word", ofNull("dpr"), true, List.of()), parsedRules.get(7));
        assertEquals(new ParsedRule("Word", ofNull("num"), true, List.of()), parsedRules.get(8));
        assertEquals(new ParsedRule("Word", ofNull("adv"), true, List.of()), parsedRules.get(9));
        assertEquals(new ParsedRule("Word", ofNull("pnoun"), true, List.of()), parsedRules.get(10));
        assertEquals(new ParsedRule("Word", ofNull("prep"), true, List.of()), parsedRules.get(11));
        assertEquals(new ParsedRule("Word", ofNull("conj"), true, List.of()), parsedRules.get(12));
        assertEquals(new ParsedRule("Word", ofNull("prcl"), true, List.of()), parsedRules.get(13));
        assertEquals(new ParsedRule("Word", ofNull("lat"), true, List.of()), parsedRules.get(14));
        assertEquals(new ParsedRule("CLOTHES", ofNull("'куртка'"), true, List.of()), parsedRules.get(15));
        assertEquals(new ParsedRule("CLOTHES", ofNull("'пальто'"), true, List.of()), parsedRules.get(16));
        assertEquals(new ParsedRule("CLOTHES", ofNull("'шуба'"), true, List.of()), parsedRules.get(17));
    }

    public static <T> List<T> ofNull(@Nullable T ... t) {
        List<T> list = new ArrayList<>();
        if (t==null) {
            list.add(null);
        }
        else {
            Collections.addAll(list, t);
        }
        return list;
    }
}
