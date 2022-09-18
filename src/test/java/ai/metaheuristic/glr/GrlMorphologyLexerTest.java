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
 * Date: 9/15/2022
 * Time: 2:22 PM
 */
public class GrlMorphologyLexerTest {

    @Test
    public void test_() {

        final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
                "CLOTHES",  List.of("куртка", "пальто", "шубы"))
        );

        GlrWordTokenizer tokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        String text = "на вешалке висят пять красивых курток и старая шуба, а также пальто серое";

        List<GlrToken> tokens = lexer.initMorphology(tokenizer.tokenize(text), GlrTagMapper::map);
        assertEquals(15, tokens.size());


    }
}
