/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import ai.metaheuristic.glr.Glr;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.metaheuristic.glr.Glr.DEFAULT_PARSER;
import static ai.metaheuristic.glr.Glr.DEFAULT_PARSER_DISCARD_NAMES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 9:09 PM
 */
public class GlrScannerTest {

    @Test
    public void test_96() {
        String ps = "(?<raw>'[\\p{L}\\p{Digit}_]+')";
        Pattern p = Pattern.compile(ps, GlrScanner.DEFAULT_FLAGS);
        String s = """
                
                CLOTHES = 'куртка' | 'пальто' | 'шуба'
                """;

        Matcher m = p.matcher(s);


        assertTrue(m.find());
        assertEquals("'куртка'", m.group("raw"));

    }

    @Test
    public void test_97() {
        String s = """
                                
                    S = adj<agr-gnc=1> CLOTHES
                                
                                
                        Word = word
                        Word = noun
                        Word = adj
                        Word = verb
                        Word = pr
                        Word = dpr
                        Word = num
                        Word = adv
                        Word = pnoun
                        Word = prep
                        Word = conj
                        Word = prcl
                        Word = lat
                   \s
                CLOTHES = 'куртка' | 'пальто' | 'шуба'
                """;

        String t = s.substring(0);


        Matcher m = GlrParser.lr_grammar_scanner.re.matcher(t);


        assertTrue(m.find());
        String tokname = GlrParser.lr_grammar_scanner.getParserRulesKeys().filter(name -> m.group(name)!=null).findFirst().orElse(null);
        assertEquals("whitespace", tokname);
    }

    @Test
    public void test_99() {
        String expected = "(?<sep>=)|(?<alt>[|])|(?<word>\\b\\w+\\b)|(?<raw>'[\\p{L}\\p{Digit}_]+')|(?<whitespace>[ \\t\\r\\n]+)|(?<minus>[-])|(?<label>\\<.+?\\>)";


        assertEquals(expected, GlrParser.lr_grammar_scanner.re.pattern());
    }

    @Test
    public void test_98() {
        String expected = "(?<word>[\\p{L}\\p{Digit}_-]+)|(?<number>[\\d]+)|(?<space>[\\s]+)|(?<newline>[\\n]+)|(?<dot>[\\.]+)|(?<comma>[,]+)|(?<colon>[:]+)|(?<percent>[%]+)|(?<quote>[\"\\'«»`]+)|(?<brace>[\\(\\)\\{\\}\\[\\]]+)";

        Glr.ParserRules parserRules = new Glr.ParserRules(DEFAULT_PARSER, DEFAULT_PARSER_DISCARD_NAMES, null);
        GlrScanner scanner = new GlrScanner(parserRules);

        assertEquals(expected, scanner.re.pattern());
    }
}
