/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrTextTokenPosition;
import company.evo.jmorphy2.MorphAnalyzer;
import company.evo.jmorphy2.ParsedWord;
import company.evo.jmorphy2.ResourceFileLoader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 7:18 PM
 */
public class GlrLabelsTest {

    @SneakyThrows
    @Test
    public void test_55() {

//0 = Token(symbol=adj, value=красивый, start=22, end=30, input_term=красивых, params=ADJF,Qual plur,gent)"
//1 = Token(symbol=CLOTHES, value=куртка, start=31, end=37, input_term=курток, params=NOUN,inan,femn plur,gent)"
        var morph = new MorphAnalyzer.Builder().cacheSize(0).fileLoader(new ResourceFileLoader(GlrMorphologyLexer.DICT_PATH)).build();
        List<ParsedWord> morphed1 = morph.parse("красивых");
        assertFalse(morphed1.isEmpty());
        final ParsedWord pw1 = morphed1.get(0);
        assertEquals("ADJF,Qual plur,gent", pw1.tag.toString());

        List<ParsedWord> morphed2 = morph.parse("курток");
        assertFalse(morphed2.isEmpty());
        final ParsedWord pw2 = morphed2.get(0);
        assertEquals("NOUN,inan,femn plur,gent", pw2.tag.toString());


        List<GlrToken> tokens = List.of(
                new GlrToken("adj", "красивый", new GlrTextTokenPosition(22,  30), "красивых", pw1.tag),
                new GlrToken("CLOTHES", "куртка", new GlrTextTokenPosition(31,  37), "курток", pw2.tag)
        );

        GlrLabels.LabelCheck labelCheck = new GlrLabels.LabelCheck("1", tokens, 0);
        boolean ok = GlrLabels.agr_gnc_label(labelCheck);
        assertTrue(ok);
    }
}
