/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrWordTokenizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sergio Lissner
 * Date: 9/18/2022
 * Time: 10:18 AM
 */
public class GlrParserRawTest {

    private static final String SIMPLE_GRAMMAR = """
        S = 'N'
        """;

    // TODO 'raw' synbols isn't working rn. use dictionary.
    //  see ai.metaheuristic.glr.GlrLabelsClassTest.test_58
    @Disabled
    @Test
    public void test_55() {
        String text = "N";

        GlrTokenizer glrTokenizer = new GlrWordTokenizer();
        GlrMorphologyLexer lexer = new GlrMorphologyLexer(null);
        final List<GlrToken> rawTokens = glrTokenizer.tokenize(text);
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(SIMPLE_GRAMMAR, "S");
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.format_syntax_tree(syntaxTree));
        }
        assertEquals(1, parsed.size());
        GlrStack.SyntaxTree st0 = parsed.get(0);
        assertEquals(1, st0.children().size());
        assertEquals("word", st0.children().get(0).symbol());
        assertEquals("N", st0.children().get(0).token().value);
    }

}
