/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrWordToken;

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
    public record StringHolder(String s) implements GlrWordToken {
        @Override
        public String getWord() {
            return s;
        }
    }

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

    static String asResultString(GlrStack.SyntaxTree syntaxTree) {
        List<GlrToken> list = new ArrayList<>();
        GlrUtils.collectChildren(list, syntaxTree);
        String s = list.stream().map(o->o.inputTerm).collect(Collectors.joining(" "));
        return s;
    }
}
