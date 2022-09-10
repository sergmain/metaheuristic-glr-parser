/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

/**
 * @author Serge
 * Date: 2/22/2022
 * Time: 5:14 PM
 */
public class TestGlrParser {
    String py = """
            from glr import GLRParser

            dictionaries = {
                u"CLOTHES": [u"куртка", u"пальто", u"шубы"]
            }

            grammar = u""\"
                S = adj<agr-gnc=1> CLOTHES
            ""\"

            glr = GLRParser(grammar, dictionaries=dictionaries, debug=False)

            text = u"на вешалке висят пять красивых курток и вонючая шуба"
            for parsed in glr.parse(text):
                print "FOUND:", parsed
    
            """;


}
