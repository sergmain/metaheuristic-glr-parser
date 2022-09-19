/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/19/2022
 * Time: 3:24 PM
 */
public class UtilsForTesing {
    public static String actionTableAsString(List<LinkedHashMap<String, List<GlrLr.Action>>> actionTable) {
        String actual = "";
        for (int i = 0; i < actionTable.size(); i++) {
            LinkedHashMap<String, List<GlrLr.Action>> t = actionTable.get(i);
            String line = String.format("%02d = {", i);
            List<String> ss = new ArrayList<>();
            for (Map.Entry<String, List<GlrLr.Action>> en : t.entrySet()) {
                String as = en.getValue().stream().map(o-> "Action(type='" + o.type() + "', state=" +
                                                           (o.state()==null ? "None" : o.state().toString()) + ", ruleIndex=" + (o.ruleIndex() == null?"None":o.ruleIndex().toString()) + ")").collect(Collectors.joining(", "));
                String s = "'"+en.getKey()+"': [" + as +"]";
                ss.add(s);
            }
            line += String.join(", ", ss);
            line += "})\n";
            actual += line;
        }
        return actual;
    }
}
