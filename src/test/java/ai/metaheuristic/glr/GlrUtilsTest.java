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

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Sergio Lissner
 * Date: 9/19/2022
 * Time: 1:45 PM
 */
public class GlrUtilsTest {

    private static final GlrLablesRegexTest.StringHolder STR_17 = new GlrLablesRegexTest.StringHolder("17");
    private static final GlrLablesRegexTest.StringHolder STR_SEPTEMBER = new GlrLablesRegexTest.StringHolder("сентября");


    private static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "MONTH",  List.of("январь", "сентябрь"))
    );

    private static final String SIMPLE_GRAMMAR = """
        S = word<regex=(\\d{1,2})> MONTH
        """;

    @Test
    public void test_57() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "17", new IndexPosition(1), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken(GlrConsts.END_OF_TOKEN_LIST)
        );

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);


        String ss = "";
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            String s = GlrUtils.formatSyntaxTree(syntaxTree);
            System.out.println(s);
            ss += s;
        }


        assertFalse(ss.contains("StringHolder"));
    }
}
