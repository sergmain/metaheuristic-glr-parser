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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/17/2022
 * Time: 2:33 PM
 */
public class GlrGeneralTest {
    public static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "CLOTHES",  List.of("куртка", "пальто", "шубы"))
    );

    private static final String SIMPLE_GRAMMAR = """
        
        S = adj<agr-gnc=1> CLOTHES
        S = CLOTHES adj<agr-gnc=-1>
        """;

    @Test
    public void test_55() {
        String text = "на вешалке висят пять красивых курток и старая шуба, а также пальто серое";

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        assertEquals(3, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(2, st0.children().size());
        assertEquals("adj", st0.children().get(0).symbol());
        assertEquals("красивый", st0.children().get(0).token().value);

        assertEquals("CLOTHES", st0.children().get(1).symbol());
        assertEquals("куртка", st0.children().get(1).token().value);

        GlrStack.SyntaxTree st1 = parsed.get(1);
        assertEquals(2, st1.children().size());
        assertEquals("adj", st1.children().get(0).symbol());
        assertEquals("старый", st1.children().get(0).token().value);

        assertEquals("CLOTHES", st1.children().get(1).symbol());
        assertEquals("шуба", st1.children().get(1).token().value);

        GlrStack.SyntaxTree st2 = parsed.get(2);
        assertEquals(2, st2.children().size());
        assertEquals("CLOTHES", st2.children().get(0).symbol());
        assertEquals("пальто", st2.children().get(0).token().value);

        assertEquals("adj", st2.children().get(1).symbol());
        assertEquals("серый", st2.children().get(1).token().value);

    }


    @Test
    public void test_56_with_end_of_list() {
        String text = "договор от 25 января 2022 N 123";

        final LocalDate localDate = LocalDate.of(2022, 1, 25);
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "договор", new IndexPosition(1), "", null),
                new GlrToken("word", "от", new IndexPosition(2), "", null),
                new GlrToken("word", localDate, new IndexPosition(3), "", null),
                new GlrToken("word", "N", new IndexPosition(4), "", null),
                new GlrToken("word", "123", new IndexPosition(5), "", null),
                new GlrToken(GlrConsts.END_OF_TOKEN_LIST, "", new IndexPosition(6), "", null)
        );

        testWithObject(localDate, rawTokens);
    }

    @Test
    public void test_57_without_end_of_list() {
        String text = "договор от 25 января 2022 N 123";

        final LocalDate localDate = LocalDate.of(2022, 1, 25);
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "договор", new IndexPosition(1), "", null),
                new GlrToken("word", "от", new IndexPosition(2), "", null),
                new GlrToken("word", localDate, new IndexPosition(3), "", null),
                new GlrToken("word", "N", new IndexPosition(4), "", null),
                new GlrToken("word", "123", new IndexPosition(5), "", null)
        );

        testWithObject(localDate, rawTokens);
    }

    @Test
    public void test_58_with_simple_end_of_list() {
        String text = "договор от 25 января 2022 N 123";

        final LocalDate localDate = LocalDate.of(2022, 1, 25);
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "договор", new IndexPosition(1), "", null),
                new GlrToken("word", "от", new IndexPosition(2), "", null),
                new GlrToken("word", localDate, new IndexPosition(3), "", null),
                new GlrToken("word", "N", new IndexPosition(4), "", null),
                new GlrToken("word", "123", new IndexPosition(5), "", null),
                new GlrToken(GlrConsts.END_OF_TOKEN_LIST)
        );

        testWithObject(localDate, rawTokens);
    }

    @Test
    public void test_59() {
        String text = "договор от 25 января 2022 N 123";

        final LocalDate localDate = LocalDate.of(2022, 1, 25);
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "договор", new IndexPosition(1), "", null),
                new GlrToken("word", "от", new IndexPosition(2), "", null),
                new GlrToken("word", localDate, new IndexPosition(3), "", null),
                new GlrToken("word", "N", new IndexPosition(4), "", null),
                new GlrToken("word", "123", new IndexPosition(5), "", null),
                new GlrToken(GlrConsts.END_OF_TOKEN_LIST)
        );

        testWithObject(localDate, rawTokens);
    }

    private static void testWithObject(LocalDate localDate, List<GlrToken> rawTokens) {
        LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
                "DOC_NUMBER",  List.of("N", "№"))
        );

        String grammar = """
            S = word<class=LocalDate> DOC_NUMBER
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
        final GlrToken token0 = st0.children().get(0).token();
        assertEquals(localDate, token0.value);
        assertEquals(new IndexPosition(3), token0.position);

        assertEquals("DOC_NUMBER", st0.children().get(1).symbol());
        final GlrToken token1 = st0.children().get(1).token();
        assertEquals("N", token1.value);
        assertEquals(new IndexPosition(4), token1.position);
    }

}
