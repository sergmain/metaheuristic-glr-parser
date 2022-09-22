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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Sergio Lissner
 * Date: 9/20/2022
 * Time: 4:59 PM
 */
public class GlrParserWithTerminalsTest {

    private static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "MONTH",  List.of("январь", "сентябрь"))
    );

    @Test
    public void test_65() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", new GlrLablesRegexTest.StringHolder("12345"), new IndexPosition(1), "", null),
                new GlrToken("word", new GlrLablesRegexTest.StringHolder("12"), new IndexPosition(2), "", null),
                new GlrToken("word", new GlrLablesRegexTest.StringHolder("тест"), new IndexPosition(3), "", null),
                new GlrToken("word", new GlrLablesRegexTest.StringHolder("2022"), new IndexPosition(4), "", null),
                new GlrToken("word", new GlrLablesRegexTest.StringHolder("987654321"), new IndexPosition(5), "", null),
                new GlrToken("$", "", new IndexPosition(6), "", null)
        );
        String regexGrammar = """
        S = word<regex=^\\d{1,2}$> Word<regex=^[а-яА-Я]+$>
        Word = word
        Word = noun
        """;

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }

        System.out.println( GlrUtils.format_tokens(tokens));
        System.out.println( GlrUtils.format_action_goto_table(automation.parser.actionGotoTable));

        assertEquals(1, parsed.size());
        assertEquals("12 тест", UtilsForTesing.asResultString(parsed.get(0)));
    }

    @Test
    public void test_66() {
        String text = "12345 12 тест 2022 987654321";

        String regexGrammar = """
        S = word<regex=^\\d{1,2}$> Word<regex=^[а-яА-Я]+$>
        Word = word
        Word = noun
        """;

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer();
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        assertEquals(1, parsed.size());

        assertEquals("12 тест", UtilsForTesing.asResultString(parsed.get(0)));
    }

    @Test
    public void test_67() {
        String text = "12";

        String regexGrammar = """
        S = Word<regex=^\\d{1,2}$>
        Word = word
        """;

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer();
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(regexGrammar, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        System.out.println(GlrUtils.format_tokens(tokens));


        assertEquals(1, parsed.size());
        assertEquals("12", UtilsForTesing.asResultString(parsed.get(0)));
    }

    @Test
    public void test_68() {
        final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
                "TEST",  List.of("test"))
        );

        String text = "aaa 12 test 1234 bbb";

        String regexGrammar = """
                S = DIGIT_TEST_DIGIT
                        DIGIT_TEST_DIGIT = word<regex=^\\d{1,2}$> DIGIT_TEST  
                        DIGIT_TEST =  TEST word<regex=^\\d{4}$>
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
        System.out.println(GlrUtils.format_tokens(tokens));


        assertFalse(parsed.isEmpty());
        assertEquals("12 test 1234", UtilsForTesing.asResultString(parsed.get(0)));
    }
}
