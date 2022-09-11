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
 * Date: 9/10/2022
 * Time: 2:15 PM
 */
public class RunExample {

    public static final Map<String, List<String>> dictionaries = Map.of(
            "CLOTHES",  List.of("куртка", "пальто", "шубы")
    );

    public static final String grammar = """
        S = adj<agr-gnc=1> CLOTHES
        S = CLOTHES adj<agr-gnc=-1>
        """;

    public static void main(String[] args) {
        String s = "на вешалке висят пять красивых курток и вонючая шуба, а также пальто серое";

        Glr glr = new Glr(grammar, dictionaries, null, true);
        glr.parse(s).forEach(o->System.out.println("FOUND:" + o));
    }

}
