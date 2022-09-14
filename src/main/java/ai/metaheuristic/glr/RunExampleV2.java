/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.glr.GlrAutomation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:15 PM
 */
public class RunExampleV2 {

    public static final LinkedHashMap<String, List<String>> dictionaries = new LinkedHashMap<>(Map.of(
            "CLOTHES",  List.of("куртка", "пальто", "шубы"))
    );

    public static void main(String[] args) {
        String s = "на вешалке висят пять красивых курток и вонючая шуба, а также пальто серое";

        GlrAutomation automation = new GlrAutomation(GlrConsts.SIMPLE_GRAMMAR, dictionaries, "S");
    }

}
