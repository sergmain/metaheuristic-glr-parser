/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrWordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/17/2022
 * Time: 3:09 PM
 */
public class GlrLablesRegexTest {

    public static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "MONTH",  List.of("январь", "сентябрь"))
    );

    private static final String SIMPLE_GRAMMAR = """
        S = word<regex=(\\d{1,2})> MONTH
        """;

    @Test
    public void test_55() {
        String text = "сегодня 17 сентября и это суббота";

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.format_syntax_tree(syntaxTree));
        }
        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("17", st0.children().get(0).token().value);

        assertEquals("MONTH", st0.children().get(1).symbol());
        assertEquals("сентябрь", st0.children().get(1).token().value);

    }
}