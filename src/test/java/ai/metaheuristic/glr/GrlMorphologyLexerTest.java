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
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 2:22 PM
 */
public class GrlMorphologyLexerTest {

    @Test
    public void test_55() {

        final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
                "CLOTHES",  List.of("куртка", "пальто", "шубы"))
        );

        GlrWordTokenizer tokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        String text = "на вешалке висят пять красивых курток и старая шуба, а также пальто серое";

        List<GlrToken> tokens = lexer.initMorphology(tokenizer.tokenize(text), GlrTagMapper::map);
        assertEquals(15, tokens.size());
    }

    private static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "MONTH",  List.of("январь", "сентябрь"))
    );

    @Test
    public void test_56() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", "сентября", new IndexPosition(3), "", null),
                new GlrToken(GlrConsts.END_OF_TOKEN_LIST)
        );

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        assertEquals(2, tokens.size());
        assertEquals("MONTH", tokens.get(0).symbol);
    }

    private static final UtilsForTesing.StringHolder STR_SEPTEMBER = new UtilsForTesing.StringHolder("сентября");

    @Test
    public void test_57() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", STR_SEPTEMBER, new IndexPosition(3), "", null),
                new GlrToken(GlrConsts.END_OF_TOKEN_LIST)
        );

        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        assertEquals(2, tokens.size());
        assertEquals("MONTH", tokens.get(0).symbol);
        assertFalse(tokens.get(0).inputTerm.contains("StringHolder"));
    }

}
