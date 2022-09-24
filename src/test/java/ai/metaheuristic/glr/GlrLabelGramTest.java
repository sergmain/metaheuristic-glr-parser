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
 * Date: 9/24/2022
 * Time: 1:20 AM
 */
public class GlrLabelGramTest {
    @Test
    public void test_58() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", "первое", new IndexPosition(2), "", null),
                new GlrToken("word", "сентября", new IndexPosition(3), "", null),
                new GlrToken("$", "", new IndexPosition(5), "", null)
        );

        LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
                "MONTH",  List.of("сентябрь"))
        );

        String grammar = """
            S = adj<gram=Anum> MONTH
            """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(grammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        System.out.println(GlrUtils.format_tokens(tokens));

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("adj", st0.children().get(0).symbol());
        assertEquals("первый", st0.children().get(0).token().value);

        assertEquals("MONTH", st0.children().get(1).symbol());
        assertEquals("сентябрь", st0.children().get(1).token().value);
    }
}
