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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 9/14/2022
 * Time: 10:21 AM
 */
public class GlrParserTest {

    public static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "CLOTHES",  List.of("куртка", "пальто", "шубы"),
            "NUMBER", List.of("пять")
    ));

    private static final String SIMPLE_GRAMMAR = """
        
        S = NUMBER adj<agr-gnc=1> CLOTHES
        S = NUMBER CLOTHES adj<agr-gnc=-1>
        """;

    // TODO P5 2022-09-19 this will be disabled until Parser&Co be fixed
    @Disabled
    @Test
    public void test_55() {
        String text = "на вешалке висят пять красивых курток и старая шуба, а также одно пальто серое";

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(dictionaries);
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = assertDoesNotThrow(()->new GlrAutomation(SIMPLE_GRAMMAR, "S"));
    }


}
