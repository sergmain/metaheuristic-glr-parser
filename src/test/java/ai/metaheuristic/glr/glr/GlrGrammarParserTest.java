/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 5:21 PM
 */
public class GlrGrammarParserTest {

    @Test
    public void test_55() {
        Map<String, List<Object>> m = GlrGrammarParser._parse_labels("agr-gnc=1");

        assertTrue(m.containsKey("agr-gnc"));
        assertEquals(1, m.get("agr-gnc").size());
        assertEquals("1", m.get("agr-gnc").get(0));
    }
}
