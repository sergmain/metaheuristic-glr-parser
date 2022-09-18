/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/13/2022
 * Time: 11:17 AM
 */
public class GlrUtils {

    public static String formatRule(GlrGrammar.Rule rule) {
        return formatRule(rule, 0);
    }

    public static String formatRule(GlrGrammar.Rule rule , int ljustSymbol) {
        List<String> rightSymbols = new ArrayList<>();
        for (int i = 0; i < rule.rightSymbols().size(); i++) {
            String symbol = rule.rightSymbols().get(i);
            rightSymbols.add(formatSymbol(rule, i, symbol));
        }
        final String s = String.format("#%d: %s = %s%s",
                rule.index(),
                StringUtils.leftPad(rule.leftSymbol(), ljustSymbol),
                String.join(" ", rightSymbols),
                rule.weight() != 1.0 ? String.format("   (%g)", rule.weight()) : "");
        return s;
    }

    public static String formatSymbol(GlrGrammar.Rule rule, int i, String symbol) {
        if (rule.params()!=null && rule.params().get(i)!=null) {
            return symbol;
        }
        if (rule.params()==null) {
            return "rule.params()==null";
        }

        LinkedHashMap<String, Object> allPairsTemp = new LinkedHashMap<>();
        for (Map.Entry<String, List<Object>> entry : rule.params().get(i).entrySet()) {
            for (Object o : entry.getValue()) {
                allPairsTemp.put(entry.getKey(), o);
            }
        }

        LinkedHashMap<String, Object> allPairs = new LinkedHashMap<>();
        allPairsTemp.entrySet().stream().sorted().forEach(en->allPairs.put(en.getKey(), en.getValue()));

        if (allPairs.size()==1 && Boolean.TRUE.equals(allPairs.get("raw"))) {
            return String.format("'%s'", symbol);
        }


        LinkedList<String> temp = new LinkedList<>();
        for (Map.Entry<String, List<Object>> entry : rule.params().get(i).entrySet()) {
            for (Object o : entry.getValue()) {
                temp.add(String.format("%s=%s", entry.getKey(), o instanceof Boolean ? entry.getKey() : o.toString()));
            }
        }

        return String.format("%s<%s>", symbol, String.join(", ", temp));
    }

    public static String formatStackItem(GlrStack.StackItem stackItem, String secondLinePrefix) {
        if (!stackItem.prev.isEmpty()) {
            List<String> pathes = new ArrayList<>();
            for (List<GlrStack.StackItem> path : getPathes(stackItem)) {
                pathes.add(path.stream().map(Object::toString).collect(Collectors.joining(" > ")));
            }
            int length = pathes.stream().mapToInt(String::length).max().orElse(0);
            List<String> results = new ArrayList<>();
            for (int i = 0; i < pathes.size(); i++) {
                String path = pathes.get(i);
                String result = (i == 0 ? "" : secondLinePrefix);
                result += StringUtils.rightPad(path, length);
                if (pathes.size()>1) {
                    if (i == 0) {
                        result += " ╮";
                    }
                    else if (i != pathes.size() - 1) {
                        result += " │";
                    }
                    else {
                        result += " ╯";
                    }
                }
                results.add(result);
            }
            final String join = String.join("\n", results);
            return join;
        }
        else {
            return "0";
        }
    }
    public static String formatStackItem(GlrStack.StackItem stackItem) {
        return formatStackItem(stackItem, "");
    }


    public static List<List<GlrStack.StackItem>> getPathes(GlrStack.StackItem stackItem) {
        List<List<GlrStack.StackItem>> result = new ArrayList<>();
        if (!stackItem.prev.isEmpty()) {
            for (GlrStack.StackItem prev : stackItem.prev) {
                for (List<GlrStack.StackItem> p : getPathes(prev)) {
                    List<GlrStack.StackItem> pp = new ArrayList<>(p);
                    pp.add(stackItem);
                    result.add(pp);
                }
            }
        }
        else {
            result.add(new ArrayList<>(List.of(stackItem)));
        }

        return result;
    }

    public static String formatSyntaxTree(GlrStack.SyntaxTree syntaxTree) {
        ArrayList<LineAndValue> ast = new ArrayList<>(generateSyntaxTreeLines(syntaxTree));
        int depth = ast.stream().mapToInt(o->o.line.length()).max().orElse(0);
        List<String> lines = new ArrayList<>();
        for (LineAndValue lineAndValue : ast) {
            String s = StringUtils.rightPad(lineAndValue.line, depth, lineAndValue.value.isBlank() ? " " : ".");
            lines.add( String.format("%s %s", s, lineAndValue.value));
        }

        return String.join("\n", lines);
    }

    public record LineAndValue(String line, String value) {}
    public static List<LineAndValue> generateSyntaxTreeLines(GlrStack.SyntaxTree syntaxTree) {
        return generateSyntaxTreeLines(syntaxTree, false, "");
    }

    public static List<LineAndValue> generateSyntaxTreeLines(GlrStack.SyntaxTree syntaxTree, boolean last, String prefix) {
        List<LineAndValue> result = new ArrayList<>();
        String line = StringUtils.substring(prefix, 0, -1);
        if (prefix.length()>0) {
            if (!last) {
                line += "├──";
            }
            else {
                line += "╰──";
            }
        }
        else {
            line = "  ";
        }
        if (syntaxTree.isLeaf()) {
            result.add(new LineAndValue(line + syntaxTree.symbol(), syntaxTree.token().inputTerm));
        }
        else {
            result.add(new LineAndValue(line + syntaxTree.symbol(), ""));
            for (int i = 0; i <syntaxTree.children().size(); i++) {
                GlrStack.SyntaxTree r = syntaxTree.children().get(i);
                last = i == syntaxTree.children().size() - 1;
                final List<LineAndValue> c = generateSyntaxTreeLines(r, last, prefix + (last ? "   " : "  │"));
                boolean exist = c.stream().anyMatch(o->o.line.contains("  Options"));
                result.addAll(c);
            }
        }

        return result;
    }

    public static List<GlrStack.SyntaxTree> flattenSyntaxTree(GlrStack.SyntaxTree syntaxTree, String symbol) {
        List<GlrStack.SyntaxTree> result = new ArrayList<>();
//        Recursively traverse syntax tree until finds searched symbol.
//        If found does not go deeper.
        if (syntaxTree.symbol().equals(symbol)) {
            result.add(syntaxTree);
        }
        else if (!syntaxTree.children().isEmpty()) {
            for (GlrStack.SyntaxTree child : syntaxTree.children()) {
                result.addAll(flattenSyntaxTree(child, symbol));
            }
        }

        return result;
    }
}
