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

    public static String format_rule(GlrGrammar.Rule rule) {
        return format_rule(rule, 0);
    }

    public static String format_rule(GlrGrammar.Rule rule , int ljust_symbol) {
        List<String> right_symbols = new ArrayList<>();
        for (int i = 0; i < rule.right_symbols().size(); i++) {
            String symbol = rule.right_symbols().get(i);
            right_symbols.add(format_symbol(rule, i, symbol));
        }
        final String s = String.format("#%d: %s = %s%s",
                rule.index(),
                StringUtils.leftPad(rule.left_symbol(), ljust_symbol),
                String.join(" ", right_symbols),
                rule.weight() != 1.0 ? String.format("   (%g)", rule.weight()) : "");
        return s;
    }

    public static String format_symbol(GlrGrammar.Rule rule, int i, String symbol) {
        if (rule.params()!=null && rule.params().get(i)!=null) {
            return symbol;
        }
        if (rule.params()==null) {
            return "rule.params()==null";
        }

        LinkedHashMap<String, Object> all_pairs_temp = new LinkedHashMap<>();
        for (Map.Entry<String, List<Object>> entry : rule.params().get(i).entrySet()) {
            for (Object o : entry.getValue()) {
                all_pairs_temp.put(entry.getKey(), o);
            }
        }

        LinkedHashMap<String, Object> all_pairs = new LinkedHashMap<>();
        all_pairs_temp.entrySet().stream().sorted().forEach(en->all_pairs.put(en.getKey(), en.getValue()));

        if (all_pairs.size()==1 && Boolean.TRUE.equals(all_pairs.get("raw"))) {
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

    public static String format_stack_item(GlrStack.StackItem stack_item, String second_line_prefix) {
        if (!stack_item.prev.isEmpty()) {
            List<String> pathes = new ArrayList<>();
            for (List<GlrStack.StackItem> path : get_pathes(stack_item)) {
                pathes.add(path.stream().map(Object::toString).collect(Collectors.joining(" > ")));
            }
            int length = pathes.stream().mapToInt(String::length).max().orElse(0);
            List<String> results = new ArrayList<>();
            for (int i = 0; i < pathes.size(); i++) {
                String path = pathes.get(i);
                String result = (i == 0 ? "" : second_line_prefix);
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
    public static String format_stack_item(GlrStack.StackItem stack_item) {
        return format_stack_item(stack_item, "");
    }


    public static List<List<GlrStack.StackItem>> get_pathes(GlrStack.StackItem stack_item) {
        List<List<GlrStack.StackItem>> result = new ArrayList<>();
        if (!stack_item.prev.isEmpty()) {
            for (GlrStack.StackItem prev : stack_item.prev) {
                for (List<GlrStack.StackItem> p : get_pathes(prev)) {
                    List<GlrStack.StackItem> pp = new ArrayList<>(p);
                    pp.add(stack_item);
                    result.add(pp);
                }
            }
        }
        else {
            result.add(new ArrayList<>(List.of(stack_item)));
        }

        return result;
    }

    public static String format_syntax_tree(GlrStack.SyntaxTree syntax_tree) {
        ArrayList<LineAndValue> ast = new ArrayList<>(generate_syntax_tree_lines(syntax_tree));
        int depth = ast.stream().mapToInt(o->o.line.length()).max().orElse(0);
        List<String> lines = new ArrayList<>();
        for (LineAndValue lineAndValue : ast) {
            String s = StringUtils.rightPad(lineAndValue.line, depth, lineAndValue.value.isBlank() ? " " : ".");
            lines.add( String.format("%s %s", s, lineAndValue.value));
        }

        return String.join("\n", lines);
    }

    public record LineAndValue(String line, String value) {}
    public static List<LineAndValue> generate_syntax_tree_lines(GlrStack.SyntaxTree syntax_tree) {
        return generate_syntax_tree_lines(syntax_tree, false, "");
    }

    public static List<LineAndValue> generate_syntax_tree_lines(GlrStack.SyntaxTree syntax_tree, boolean last, String prefix) {
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
        if (syntax_tree.is_leaf()) {
            result.add(new LineAndValue(line + syntax_tree.symbol(), syntax_tree.token().input_term));
        }
        else {
            result.add(new LineAndValue(line + syntax_tree.symbol(), ""));
            for (int i = 0; i <syntax_tree.children().size(); i++) {
                GlrStack.SyntaxTree r = syntax_tree.children().get(i);
                last = i == syntax_tree.children().size() - 1;
                final List<LineAndValue> c = generate_syntax_tree_lines(r, last, prefix + (last ? "   " : "  │"));
                boolean exist = c.stream().anyMatch(o->o.line.contains("  Options"));
                result.addAll(c);
            }
        }

        return result;
    }

    public static List<GlrStack.SyntaxTree> flatten_syntax_tree(GlrStack.SyntaxTree syntax_tree, String symbol) {
        List<GlrStack.SyntaxTree> result = new ArrayList<>();
//        Recursively traverse syntax tree until finds searched symbol.
//        If found does not go deeper.
        if (syntax_tree.symbol().equals(symbol)) {
            result.add(syntax_tree);
        }
        else if (!syntax_tree.children().isEmpty()) {
            for (GlrStack.SyntaxTree child : syntax_tree.children()) {
                result.addAll(flatten_syntax_tree(child, symbol));
            }
        }

        return result;
    }
}
