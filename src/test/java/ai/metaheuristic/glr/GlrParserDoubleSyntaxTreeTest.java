/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrWordTokenizer;
import ai.metaheuristic.glr.token.IndexPosition;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/20/2022
 * Time: 2:24 PM
 */
public class GlrParserDoubleSyntaxTreeTest {

    private static final GlrLablesRegexTest.StringHolder STR_17 = new GlrLablesRegexTest.StringHolder("17");
    private static final GlrLablesRegexTest.StringHolder STR_SEPTEMBER = new GlrLablesRegexTest.StringHolder("сентября");

    public static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "MONTH",  List.of("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"))
    );

    @Test
    public void test_45() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", STR_17, new IndexPosition(2), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("word", new GlrLablesRegexTest.StringHolder("2022"), new IndexPosition(3), "", null),
                new GlrToken("$", "", new IndexPosition(4), "", null)
        );
        String regexGrammar = """
        S = DAY_MONTH word<regex=(\\d{4})>
        DAY_MONTH = word<regex=(\\d{1,2})> MONTH
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(2, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("DAY_MONTH", st0.children().get(0).symbol());
        assertEquals(null, st0.children().get(0).token());

        assertEquals(2, st0.children().size());
        assertEquals("word", st0.children().get(1).symbol());
        assertEquals("2022", st0.children().get(1).token().value);


        final GlrStack.SyntaxTree st01 = st0.children().get(0);
        assertEquals(2, st01.children().size());
        assertEquals("word", st01.children().get(0).symbol());
        assertEquals("17", st01.children().get(0).token().value);

        assertEquals("MONTH", st01.children().get(1).symbol());
        assertEquals("сентябрь", st01.children().get(1).token().value);
    }

    @Test
    public void test_46() {
        String text = "20 декабря 2018 года";

        String regexGrammar = """
        S = FULL_YEAR_DATE
            Word = word
            Word = noun
                FULL_YEAR_DATE = DAY_MONTH word<regex=^(\\d{4})$>
                FULL_YEAR_DATE = DAY_MONTH YEAR_AND_NAME
                DAY_MONTH = word<regex=^([0-9][0-9])$> MONTH
                YEAR_AND_NAME = word<regex=^(\\d{4})$> Word<regex=^(гг.|год[а]?|г.)$>
        """;

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        assertEquals(2, parsed.size());
    }
}
