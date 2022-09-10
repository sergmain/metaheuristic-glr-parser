/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import java.util.List;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 2:30 PM
 */
public class Example {


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
            print("FOUND:", parsed)
        """;

    public static final Map<String, List<String>> dictionaries = Map.of(
            "CLOTHES",  List.of("куртка", "пальто", "шубы")
    );

    public static final String grammar = """
        
            S = adj<agr-gnc=1> CLOTHES
        """;

    public static void main(String[] args) {
        String s = "на вешалке висят пять красивых курток и вонючая шуба";

        Glr glr = new Glr(grammar, dictionaries, null, true);
        glr.parse(s).forEach(o->System.out.println("FOUND:" + o));
    }
}
