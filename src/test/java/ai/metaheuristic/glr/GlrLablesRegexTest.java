/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.exceptions.GlrLabelRegexException;
import ai.metaheuristic.glr.token.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sergio Lissner
 * Date: 9/17/2022
 * Time: 3:09 PM
 */
public class GlrLablesRegexTest {

    private static final UtilsForTesing.StringHolder STR_17 = new UtilsForTesing.StringHolder("17");
    private static final UtilsForTesing.StringHolder STR_SEPTEMBER = new UtilsForTesing.StringHolder("сентября");


    private static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "MONTH",  List.of("январь", "сентябрь"))
    );

    private static final String SIMPLE_GRAMMAR = """
        S = word<regex=(\\d{1,2})> MONTH
        """;

    @Test
    public void test_54() {
        final String SIMPLE_GRAMMAR = """
        S = Word<regex=^[0-9]*(.[0-9]*)?$>
        Word = noun
        Word = word
        """;
        String text = "тест";

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer();
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        assertThrows(GlrLabelRegexException.class, ()-> automation.parse(tokens));
    }

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
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("17", st0.children().get(0).token().value);

        assertEquals("MONTH", st0.children().get(1).symbol());
        assertEquals("сентябрь", st0.children().get(1).token().value);

    }

    @Test
    public void test_57() {
        String text = "сегодня 17 сентября и это суббота";

        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", "17", new IndexPosition(2), "", null),
//                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("word", "сентября", new IndexPosition(3), "", null),
                new GlrToken("word", "и", new IndexPosition(4), "", null),
                new GlrToken("word", "это", new IndexPosition(5), "", null),
                new GlrToken("word", "суббота", new IndexPosition(6), "", null),
                new GlrToken("$", "", new GlrTextTokenPosition(text.length(), -1), "", null)
        );

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("17", st0.children().get(0).token().value);

        assertEquals("MONTH", st0.children().get(1).symbol());
        assertEquals("сентябрь", st0.children().get(1).token().value);
    }

    @Test
    public void test_58() {
        String text = "сегодня 17 сентября и это суббота";

        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", STR_17, new IndexPosition(2), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("word", "и", new IndexPosition(4), "", null),
                new GlrToken("word", "это", new IndexPosition(5), "", null),
                new GlrToken("word", "суббота", new IndexPosition(6), "", null),
                new GlrToken("$", "", new GlrTextTokenPosition(text.length(), -1), "", null)
        );

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("17", st0.children().get(0).token().value);
        assertEquals(new IndexPosition(2), st0.children().get(0).token().position);

        assertEquals("MONTH", st0.children().get(1).symbol());
        assertEquals("сентябрь", st0.children().get(1).token().value);
        assertEquals(new IndexPosition(3), st0.children().get(1).token().position);
    }

    @Test
    public void test_59() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", "17", new IndexPosition(2), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("$", "", new IndexPosition(4), "", null)
        );
        String regexGrammar = """
        S = word<regex=(\\d{1,2})>
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(1, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("17", st0.children().get(0).token().value);

    }

    @Test
    public void test_60() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", STR_17, new IndexPosition(2), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("$", "", new IndexPosition(4), "", null)
        );
        String regexGrammar = """
        S = word<regex=(\\d{1,2})> 
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(1, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("17", st0.children().get(0).token().value);

    }

    @Test
    public void test_61() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", STR_17, new IndexPosition(2), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("word", new UtilsForTesing.StringHolder("2022"), new IndexPosition(3), "", null),
                new GlrToken("$", "", new IndexPosition(4), "", null)
        );
        String regexGrammar = """
        S = word<regex=^\\d{1,2}$> MONTH_YEAR
        MONTH_YEAR =  MONTH word<regex=^\\d{4}$>
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        System.out.println(GlrUtils.format_tokens(tokens));

        assertEquals("17 сентября 2022", UtilsForTesing.asResultString(parsed.get(0)));
    }

    @Test
    public void test_62() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", new UtilsForTesing.StringHolder("12345"), new IndexPosition(1), "", null),
                new GlrToken("word", new UtilsForTesing.StringHolder("12"), new IndexPosition(2), "", null),
                new GlrToken("word", new UtilsForTesing.StringHolder("abc"), new IndexPosition(3), "", null),
                new GlrToken("word", new UtilsForTesing.StringHolder("2022"), new IndexPosition(4), "", null),
                new GlrToken("word", new UtilsForTesing.StringHolder("987654321"), new IndexPosition(5), "", null),
                new GlrToken("$", "", new IndexPosition(4), "", null)
        );
        String regexGrammar = """
        S = DAY_MONTH
        DAY_MONTH = word<regex=\\d{1,2}> word<regex=[a-z]+>
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(1, st0.children().size());
        final GlrStack.SyntaxTree child0 = st0.children().get(0);
        assertEquals("DAY_MONTH", child0.symbol());
        assertEquals(2, child0.children().size());

        assertEquals("word", child0.children().get(0).symbol());
        assertEquals("12", child0.children().get(0).token().value);
        assertEquals("word", child0.children().get(1).symbol());
        assertEquals("abc", child0.children().get(1).token().value);
    }

    @Test
    public void test_64() {
        String text = "12345 12 abc 2022 987654321";

        String regexGrammar = """
            S = DAY_MONTH
            DAY_MONTH = word<regex=^([0-9][0-9])$> word<regex=^([a-z]+)$>
        """;

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer();
        List<GlrToken> tokens = lexer.initMorphology(glrTokenizer.tokenize(text), GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        System.out.println( GlrUtils.format_tokens(tokens));
        System.out.println( GlrUtils.format_action_goto_table(automation.parser.actionGotoTable));

        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(1, st0.children().size());
        final GlrStack.SyntaxTree child0 = st0.children().get(0);
        assertEquals("DAY_MONTH", child0.symbol());
        assertEquals(2, child0.children().size());

        assertEquals("word", child0.children().get(0).symbol());
        assertEquals("12", child0.children().get(0).token().value);
        assertEquals("word", child0.children().get(1).symbol());
        assertEquals("abc", child0.children().get(1).token().value);
    }

    // TODO p0 2022-09-22 rn this order of terminals isn't working:
//    String regexGrammar = """
//        S = DAY_MONTH word<regex=^\\d{4}$>
//        DAY_MONTH = word<regex=^\\d{1,2}$> MONTH
//        """;
    // TODO must be:
//    String regexGrammar = """
//        S = word<regex=^\\d{1,2}$> MONTH_YEAR
//        MONTH_YEAR = MONTH word<regex=^\\d{4}$>
//        """;

    @Disabled
    @Test
    public void test_65() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сегодня", new IndexPosition(1), "", null),
                new GlrToken("word", STR_17, new IndexPosition(2), "", null),
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken("word", new UtilsForTesing.StringHolder("2022"), new IndexPosition(3), "", null),
                new GlrToken("$", "", new IndexPosition(4), "", null)
        );
        String regexGrammar = """
        S = DAY_MONTH word<regex=^\\d{4}$>
        DAY_MONTH = word<regex=^\\d{1,2}$> MONTH
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        System.out.println(GlrUtils.format_tokens(tokens));

        assertEquals("17 сентября 2022", UtilsForTesing.asResultString(parsed.get(0)));
    }


}
