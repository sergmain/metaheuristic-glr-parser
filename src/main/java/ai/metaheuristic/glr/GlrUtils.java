/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import ai.metaheuristic.glr.token.GlrWordToken;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
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
        List<String> rightSymbols = collectRightSymbols(rule);
        final String s = String.format("#%d: %s = %s%s",
                rule.index(),
                StringUtils.leftPad(rule.leftSymbol(), ljustSymbol),
                String.join(" ", rightSymbols),
                rule.weight() != 1.0 ? String.format("   (%g)", rule.weight()) : "");
        return s;
    }

    private static List<String> collectRightSymbols(GlrGrammar.Rule rule) {
        List<String> rightSymbols = new ArrayList<>();
        for (int i = 0; i < rule.rightSymbols().size(); i++) {
            String symbol = rule.rightSymbols().get(i);
            rightSymbols.add(formatSymbol(rule, i, symbol));
        }
        return rightSymbols;
    }

    public static String formatSymbol(GlrGrammar.Rule rule, int i, String symbol) {
        if (rule.params()==null || rule.params().get(i)==null) {
            return symbol;
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
            if (syntaxTree.token()==null) {
                throw new IllegalStateException("(syntaxTree.token()==null)");
            }
            String value = asStringValue(syntaxTree.token());
            result.add(new LineAndValue(line + syntaxTree.symbol(), value==null ? syntaxTree.token().inputTerm : value));
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

    @Nullable
    public static String asStringValue(@Nullable GlrToken token) {
        if (token.value instanceof String strValue) {
            return strValue;
        }
        if (token.value instanceof GlrWordToken wordToken) {
            return wordToken.getWord();
        }
        return null;
    }

    String py0 = """
def format_tokens(tokens):
    table = [('Sym', 'Value', 'Input', 'Params')]
    for token in tokens:
        table.append((token.symbol, token.value, token.input_term, token.params))
    return format_table(table)
            """;

    public static String format_tokens(List<GlrToken> tokens) {
        List<String[]> table = new ArrayList<>();
        table.add(new String[]{"Sym", "Value", "Input", "Params"});
        for (GlrToken token : tokens) {
            final String[] symbol = new String[]{token.symbol, token.value.toString(), token.inputTerm, "" + token.params};
            table.add(symbol);
        }
        return format_table(table);
    }

    public static String format_table(List<String[]> table) {
        return format_table(table, true);
    }

    public static void print_row(StringBuilder sb, boolean stripe, int[] col_widths, int i, String chars) {
        print_row(sb, stripe, col_widths, i, chars, null);
    }

    public static void print_row(StringBuilder sb, boolean stripe, int[] col_widths, int i, String chars, @Nullable String[] row) {
        if (stripe && i > 0 && i % 2 == 0) {
            // https://stackoverflow.com/a/21786287/2672202
            sb.append("\033[0;30;47m");
        }
        for (int j = 0; j < col_widths.length; j++) {
            if (j==0) {
                sb.append(chars, 0, 2);
            }
            if (row!=null) {
                sb.append(StringUtils.rightPad(row[j], col_widths[j]));
            }
            else {
                sb.append(chars.substring(1,2).repeat(col_widths[j]));
            }
            if (j<col_widths.length-1) {
                sb.append(chars, 1, 4);
            }
            else {
                sb.append(chars, 3, 5);
            }
        }
        if (stripe) {
            sb.append("\033[m");
        }
        if (i >= 0) {
            sb.append('\n');
        }
    }


    public static String format_table(List<String[]> table, boolean stripe) {
        StringBuilder sb = new StringBuilder();

        int[] col_widths = new int[table.get(0).length];
        for (String[] row : table) {
            for (int j = 0; j < row.length; j++) {
                String cell = row[j];
                col_widths[j] = Math.max(col_widths[j], cell==null ? 0 : cell.length());
            }
        }
        for (int i = 0; i < table.size(); i++) {
            String[] row = table.get(i);
            if (i == 0) {
                print_row(sb, stripe, col_widths, i, "┌─┬─┐");
            }
            else if (i == 1) {
                print_row(sb, stripe, col_widths, i, "├─┼─┤");
            }
            print_row(sb, stripe, col_widths, i, "│ │ │", row);
            if (i == table.size() - 1) {
                print_row(sb, stripe, col_widths, -1, "└─┴─┘");
            }
        }

        return sb.toString();
    }

    String py1 = """
    def format_action_goto_table(action_goto_table):
        table = []
        symbols = unique(k for row in action_goto_table for k in row.keys())
    
        def sort_key(symbol):
            actions = [a for row in action_goto_table if symbol in row for a in row[symbol]]
            has_goto = any(a.type == 'G' for a in actions)
            min_state = min([a.state for a in actions if a.state] or [1000])
            return has_goto, min_state
    
        symbols = sorted(symbols, key=sort_key)
        table.append([''] + symbols)
        for i, row in enumerate(action_goto_table):
            res = [i]
            for k in symbols:
                res.append(', '.join('%s%s%s' % (a if a != 'G' else '', s or '', r or '') for a, s, r in row[k]) if k in row else '')
            table.append(res)
        return format_table(table)
                """;

    private record SortKey(boolean hasGoto, int minState) implements Comparable<SortKey> {
        @Override
        public int compareTo(SortKey o2) {
            int compare = Boolean.compare(this.hasGoto, o2.hasGoto);
            return compare==0 ? Integer.compare(this.minState, o2.minState) : compare;
        }
    }

    public static SortKey sort_key(List<LinkedHashMap<String, List<GlrLr.Action>>> actionGotoTable, String symbol) {
        List<GlrLr.Action> actions = actionGotoTable.stream()
                .flatMap(o->o.entrySet().stream())
                .filter(en->en.getKey().equals(symbol))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream).toList();

        boolean has_goto = actions.stream().anyMatch(o->o.type().equals("G"));
        int min_state = actions.stream().map(GlrLr.Action::state).filter(Objects::nonNull).min(Integer::compareTo).orElse(1000);

        return new SortKey(has_goto, min_state);
    }

    public static String format_action_goto_table(List<LinkedHashMap<String, List<GlrLr.Action>>> actionGotoTable) {
        List<String[]> table = new ArrayList<>();
        //     symbols = unique(k for row in action_goto_table for k in row.keys())
        final List<String> symbols = actionGotoTable.stream().flatMap(o -> o.keySet().stream()).distinct()
                .sorted(Comparator.comparing(o -> sort_key(actionGotoTable, o))).toList();

        final List<String> header = new ArrayList<>();
        header.add("");
        header.addAll(symbols);
        table.add(header.toArray(new String[]{}));
        for (int i = 0; i < actionGotoTable.size(); i++) {
            LinkedHashMap<String, List<GlrLr.Action>> row = actionGotoTable.get(i);
            List<String> res = new ArrayList<>();
            res.add(Integer.toString(i));
            for (String k : symbols) {
                if (row.containsKey(k)) {
                    List<GlrLr.Action> actions = row.get(k);

                    for (GlrLr.Action action : actions) {
                        String a = action.type().equals("G") ? action.type() : "";
                        String s = action.state() !=null ? action.state().toString() : "";
                        String r = action.ruleIndex() !=null ? action.ruleIndex().toString() : "";
                        res.addAll(List.of(a,s,r));
                    }
                }
            }
            table.add(res.toArray(new String[]{}));
        }
        return format_table(table);
    }
}
