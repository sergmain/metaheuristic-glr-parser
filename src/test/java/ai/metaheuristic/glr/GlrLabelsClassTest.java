/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.IndexPosition;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/18/2022
 * Time: 10:15 AM
 */
public class GlrLabelsClassTest {

    @Test
    public void test_58() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", Integer.valueOf(17), new IndexPosition(2), "", null),
                new GlrToken("word", "N", new IndexPosition(3), "", null),
                new GlrToken("word", "сентября", new IndexPosition(4), "", null),
                new GlrToken("$", "", new IndexPosition(5), "", null)
        );

        LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
                "DOC_NUMBER",  List.of("N", "№"))
        );

        String grammar = """
            S = word<class=Integer> DOC_NUMBER
            """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(grammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals(17, st0.children().get(0).token().value);

        assertEquals("DOC_NUMBER", st0.children().get(1).symbol());
        assertEquals("N", st0.children().get(1).token().value);
    }


}
