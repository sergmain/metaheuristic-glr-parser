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
    public void test_() {
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
