/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.*;
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

    private static final StringHolder STR_17 = new StringHolder("17");
    private static final StringHolder STR_SEPTEMBER = new StringHolder("сентября");


    private static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
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

    public record StringHolder(String s) implements GlrWordToken {
        @Override
        public String getWord() {
            return s;
        }
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
                new GlrToken("word", new StringHolder("2022"), new IndexPosition(3), "", null),
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
        System.out.println(GlrUtils.format_tokens(tokens));


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
    public void test_62() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", new StringHolder("12345"), new IndexPosition(1), "", null),
                new GlrToken("word", new StringHolder("12"), new IndexPosition(2), "", null),
                new GlrToken("word", new StringHolder("abc"), new IndexPosition(3), "", null),
                new GlrToken("word", new StringHolder("2022"), new IndexPosition(4), "", null),
                new GlrToken("word", new StringHolder("987654321"), new IndexPosition(5), "", null),
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
}
