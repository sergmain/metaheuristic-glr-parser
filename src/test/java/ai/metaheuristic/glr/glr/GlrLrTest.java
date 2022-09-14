/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/12/2022
 * Time: 2:08 PM
 */
public class GlrLrTest {

    @Test
    public void test_97() {
        List<GlrLr.State> states = GlrLr.generate_state_graph(GlrGrammarParser.grammar);

        assertEquals(17, states.size());
    }


    @Test
    public void test_98() {
        List<GlrLr.Item> items = GlrLr.closure(List.of(GlrLr.EMPTY_ITEM), GlrGrammarParser.grammar);

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

}
