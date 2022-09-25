/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrWordToken;
import ai.metaheuristic.glr.token.IndexPosition;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sergio Lissner
 * Date: 9/24/2022
 * Time: 1:55 PM
 */
public class GlrParserWrongResultTreeTest {

    @ToString
    @RequiredArgsConstructor
    public static class StaticInner {
        public final String text;
    }

    public record StringHolder(String s) implements GlrWordToken {
        @Override
        public String getWord() {
            return s;
        }
    }

    @Test
    public void test_46() {
        List<GlrToken> rawTokens = List.of(
                new GlrToken("word", new StaticInner("static"), new IndexPosition(1), "", null),
                new GlrToken("word", new StringHolder("12"), new IndexPosition(2), "", null),
                new GlrToken("word", new StringHolder(","), new IndexPosition(3), "", null),
                new GlrToken("word", new StaticInner("static"), new IndexPosition(4), "", null),
                new GlrToken("word", new StringHolder("21"), new IndexPosition(5), "", null),
                new GlrToken("$", "", new IndexPosition(6), "", null)
        );

        final String GRAMMAR = """
            S = RULE
            
            RULE = ADJ_DOC_PART
            RULE = DOC_PART_ADJ
            RULE = DOC_PART_DIGIT
            RULE = DOC_PART_DIGIT_COMMA
            
                ADJ_DOC_PART = adj<gram=Anum> word<class=StaticInner>
                DOC_PART_ADJ = word<class=StaticInner> adj<gram=Anum>
                DOC_PART_DIGIT = word<class=StaticInner> Word<regex=^[0-9]+(.[0-9]+)?$>
                DOC_PART_DIGIT_COMMA = word<class=StaticInner> Word<regex=^[0-9]+(.[0-9]+)?$> word<regex=^,$> Word<regex=^[0-9]+(.[0-9]+)?$>

                Word = word
                Word = noun
            """;


        GlrMorphologyLexer lexer = new GlrMorphologyLexer();
        List<GlrToken> tokens = lexer.initMorphology(rawTokens, GlrTagMapper::map);

        GlrAutomation automation = new GlrAutomation(GRAMMAR, "S", 1);
        List<GlrStack.SyntaxTree> parsed = automation.parse(tokens);
        for (GlrStack.SyntaxTree syntaxTree : parsed) {
            System.out.println(GlrUtils.formatSyntaxTree(syntaxTree));
        }
        System.out.println(GlrUtils.format_tokens(tokens));

        assertTrue(parsed.size()>=2);
    }
}
