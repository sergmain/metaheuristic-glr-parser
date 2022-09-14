/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/13/2022
 * Time: 11:17 AM
 */
public class GlrUtils {

    String py1 = """
    def format_rule(rule, ljust_symbol=0):
        def format_symbol(i, symbol):
            if not rule.params or not rule.params[i]:
                return symbol
    
            all_pairs = sorted((k, v) for k, values in rule.params[i].items() for v in values)
            if all_pairs == [('raw', True)]:
                return '"%s"' % symbol
            return '%s<%s>' % (symbol, ','.join('%s=%s' % (k, v) if v is not True else k for k, v in all_pairs))
    
        right_symbols = [format_symbol(i, symbol) for i, symbol in enumerate(rule.right_symbols)]
        return '#%d: %s = %s%s' % (
            rule.index,
            rule.left_symbol.ljust(ljust_symbol),
            ' '.join(right_symbols),
            '   (%g)' % rule.weight if rule.weight != 1.0 else '')
    """;


    public static String format_rule(GlrGrammar.Rule rule) {
        return format_rule(rule, 0);
    }

    public static String format_rule(GlrGrammar.Rule rule , int ljust_symbol) {
        List<String> right_symbols = new ArrayList<>();
        for (int i = 0; i < rule.right_symbols().size(); i++) {
            String symbol = rule.right_symbols().get(i);
            right_symbols.add(format_symbol(rule, i, symbol));
        }
        return String.format("#%d: %s = %s%s",
                rule.index(),
                StringUtils.leftPad(rule.left_symbol(), ljust_symbol),
                String.join(" ", right_symbols),
                rule.weight() != 1.0 ? String.format("   (%g)", rule.weight()) : "");

    }

    String py2 = """
        def format_symbol(i, symbol):
            if not rule.params or not rule.params[i]:
                return symbol
    
            all_pairs = sorted((k, v) for k, values in rule.params[i].items() for v in values)
            if all_pairs == [('raw', True)]:
                return '"%s"' % symbol
            return '%s<%s>' % (symbol, ','.join('%s=%s' % (k, v) if v is not True else k for k, v in all_pairs))
    
    """;
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

    String py3 = """
    def format_stack_item(stack_item, second_line_prefix=''):
        def get_pathes(stack_item):
            if stack_item.prev:
                for prev in stack_item.prev:
                    for p in get_pathes(prev):
                        yield p + [stack_item]
            else:
                yield [stack_item]
    
        if stack_item.prev:
            pathes = []
            for path in get_pathes(stack_item):
                pathes.append(' > '.join(repr(i) for i in path))
            length = max(len(p) for p in pathes)
    
            results = []
            for i, path in enumerate(pathes):
                result = '' if i == 0 else second_line_prefix
                result += path.rjust(length)
                if len(pathes) > 1:
                    if i == 0:
                        result += ' ╮'
                    elif i != len(pathes) - 1:
                        result += ' │'
                    else:
                        result += ' ╯'
                results.append(result)
    
            return '\n'.join(results)
        else:
            return '0'
    
    """;

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


    String py4 = """
        def get_pathes(stack_item):
            if stack_item.prev:
                for prev in stack_item.prev:
                    for p in get_pathes(prev):
                        yield p + [stack_item]
            else:
                yield [stack_item]
        """;
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

    String py5 = """
    def format_syntax_tree(syntax_tree):
        assert isinstance(syntax_tree, SyntaxTree)
        ast = list(generate_syntax_tree_lines(syntax_tree))
        depth = max(len(l) for l, v in ast)
        lines = []
        for l, v in ast:
            lines.append('%s %s' % (l.ljust(depth, '.' if v else ' '), v))
            
        return '\n'.join(lines)
        """;

    public static String format_syntax_tree(GlrStack.SyntaxTree syntax_tree) {
        ArrayList<LineAndValue> ast = new ArrayList<>(generate_syntax_tree_lines(syntax_tree));
        int depth = ast.stream().mapToInt(o->o.line.length()).max().orElse(0);
        List<String> lines = new ArrayList<>();
        for (LineAndValue lineAndValue : ast) {
            String s = StringUtils.leftPad(lineAndValue.line, depth, lineAndValue.value.isBlank() ? " " : ".");
            lines.add( String.format("%s %s", s, lineAndValue.value));
        }

        return String.join("\n", lines);
    }

    String py6 = """
    def generate_syntax_tree_lines(syntax_tree, last=False, prefix=''):
        line = prefix[:-1]
        if prefix:
            if not last:
                line += u'├──'
            else:
                line += u'╰──'
        else:
            line = '  '
    
        if syntax_tree.is_leaf():
            yield line + syntax_tree.symbol, syntax_tree.token.input_term
        else:
            yield line + syntax_tree.symbol, ''
            for i, r in enumerate(syntax_tree.children):
                last = i == len(syntax_tree.children) - 1
                for line, value in generate_syntax_tree_lines(r, last, prefix + ('   ' if last else u'  │')):
                    yield line, value
    
    """;

    public record LineAndValue(String line, String value) {}
    public static List<LineAndValue> generate_syntax_tree_lines(GlrStack.SyntaxTree syntax_tree) {
        return generate_syntax_tree_lines(syntax_tree, false, "");
    }

    public static List<LineAndValue> generate_syntax_tree_lines(GlrStack.SyntaxTree syntax_tree, boolean last, String prefix) {
        List<LineAndValue> result = new ArrayList<>();
        String line = StringUtils.substring(prefix, 0, -1);
        if (prefix.isBlank()) {
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
                result.addAll(generate_syntax_tree_lines(r, last, prefix + (last ? "   " : "  │")));
            }
        }

        return result;
    }

    String py7 = """
    def flatten_syntax_tree(syntax_tree, symbol):
    ""/"
    Recursively traverse syntax tree until finds searched symbol.
    If found does not go deeper.
    ""/"
    if syntax_tree.symbol == symbol:
        yield syntax_tree
        return

    if syntax_tree.children:
        for child in syntax_tree.children:
            for res in flatten_syntax_tree(child, symbol):
                yield res

    """;

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
