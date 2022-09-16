/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.glr.token.GlrTextToken;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * @author Sergio Lissner
 * Date: 9/12/2022
 * Time: 5:58 PM
 */
public class GlrStack {

    public record SyntaxTree(String symbol, @Nullable GlrTextToken token, @Nullable Integer rule_index, List<SyntaxTree> children) {
        boolean is_leaf() {
            return children==null || children.isEmpty();
        }
    }

    String py2 = """
        class StackItem(namedtuple('StackItem', ['syntax_tree', 'state', 'prev'])):
        """;

    public static class StackItem implements Comparable<StackItem> {
        @Nullable
        public SyntaxTree syntax_tree;
        @Nullable
        public Integer state;
        public final List<StackItem> prev = new ArrayList<>();

        public StackItem(@Nullable SyntaxTree syntax_tree, @Nullable Integer state, @Nullable List<StackItem> prev) {
            this.syntax_tree = syntax_tree;
            this.state = state;
            if (prev!=null) {
                this.prev.addAll(prev);
            }
        }

        String py01 = """
        def __repr__(self):
            if self.prev:
                return '%s.%s' % (self.syntax_tree.symbol, self.state)
            else:
                return '0'
        """;
        public String toString() {
            if (!prev.isEmpty())
                return String.format("%s.%s", syntax_tree!=null ? syntax_tree.symbol : "null", state);
            else {
                return "0";
            }
        }

        @Override
        public int compareTo(StackItem o) {
            final int compare = Integer.compare(
                    this.syntax_tree == null ? 0 : this.syntax_tree.hashCode(),
                    o.syntax_tree == null ? 0 : o.syntax_tree.hashCode());
            return compare!=0 ? compare : Integer.compare(this.state == null ? 0 : this.state, o.state == null ? 0 : o.state);
        }

        public static StackItem start_new() {
            return new StackItem(null, 0, null);
        }

        String py0 = """
        def pop(self, depth):
            if depth == 0:
                return [[self]]
            if not self.prev:
                return []
    
            result = []
            for prev in self.prev:
                for path in prev.pop(depth - 1):
                    result.append(path + [self])
            return result
        """;

        public List<List<StackItem>> pop(int depth) {
            if (depth==0) {
                return List.of(List.of(this));
            }
            if (prev.isEmpty()) {
                return List.of();
            }
            List<List<StackItem>> result = new ArrayList<>();
            for (StackItem prev : this.prev) {
                for (List<StackItem> path : prev.pop(depth-1)) {
                    List<StackItem> temp = new ArrayList<>(path);
                    temp.add(this);
                    result.add(temp);
                }
            }
            return result;
        }

        String py1 = """
        def reduce(self, action_goto_table, rule, reduce_validator=None):
            result = []
            depth = len(rule.right_symbols)
            for path in self.pop(depth):
                goto_actions = action_goto_table[path[0].state][rule.left_symbol]
                # TODO: probably assert that only 1 goto action and it is 'G'
                for goto_action in goto_actions:
                    if goto_action.type == 'G':
                        syntax_tree = SyntaxTree(rule.left_symbol, None, rule.index, tuple(stack_item.syntax_tree for stack_item in path[1:]))
                        if reduce_validator is None or reduce_validator(syntax_tree):
                            new_head = StackItem(syntax_tree, goto_action.state, (path[0],))
                            result.append(new_head)
            return result
        """;

        public List<StackItem> reduce(
                List<LinkedHashMap<String, List<GlrLr.Action>>> action_goto_table,
                GlrGrammar.Rule rule,
                @Nullable Function<SyntaxTree, Boolean> reduce_validator) {

            List<StackItem> result = new ArrayList<>();
            int depth = rule.right_symbols().size();
            for (List<StackItem> path : pop(depth)) {
                final Integer stateIdx = path.get(0).state;
                if (stateIdx==null) {
                    throw new IllegalStateException("(stateIdx==null)");
                }
                List<GlrLr.Action> goto_actions = action_goto_table.get(stateIdx).get(rule.left_symbol());

                // # TODO: probably assert that only 1 goto action and it is 'G'
                for (GlrLr.Action goto_action : goto_actions ) {
                    if ("G".equals(goto_action.type())) {
                        List<SyntaxTree> tree = path.size()<2 ? List.of() : path.subList(1, path.size()).stream().map(o->o.syntax_tree).filter(Objects::nonNull).toList();
                        SyntaxTree syntax_tree = new SyntaxTree(rule.left_symbol(), null, rule.index(), tree);
                        if (reduce_validator ==null || reduce_validator.apply(syntax_tree)){
                            var new_head = new StackItem(syntax_tree, goto_action.state(), List.of(path.get(0)));
                            result.add(new_head);
                        }
                    }
                }
            }
            return result;
        }

        String py3 = """
        def shift(self, token, state):
            syntax_tree = SyntaxTree(token.symbol, token, None, ())
            return StackItem(syntax_tree, state, (self,))
        """;

        public StackItem shift(GlrTextToken token, int state) {
            SyntaxTree syntax_tree = new SyntaxTree(token.getSymbol(), token, null, List.of());
            return new StackItem(syntax_tree, state, List.of(this));
        }

        String py4 = """
        @classmethod
        def merge(cls, stack_items):
            for key, group in groupby(sorted(stack_items), lambda si: (si.syntax_tree, si.state)):
                group = [g for g in group]
                if len(group) > 1:
                    all_prevs = tuple(p for stack_item in group for p in stack_item.prev)
                    yield StackItem(group[0].syntax_tree, group[0].state, all_prevs)
                else:
                    yield group[0]
        """;

        @EqualsAndHashCode
        @RequiredArgsConstructor
        public static class TreeStateAsKey {
            public final SyntaxTree syntax_tree;
            public final Integer state;
        }

        public static List<StackItem> merge(List<StackItem> stack_items) {
            List<StackItem> result = new ArrayList<>();
            List<StackItem> sorted = stack_items.stream().sorted().toList();
            LinkedHashMap<TreeStateAsKey, List<StackItem>> map = new LinkedHashMap<>();
            for (StackItem stackItem : sorted) {
                map.computeIfAbsent(new TreeStateAsKey(stackItem.syntax_tree, stackItem.state), (o)->new ArrayList<>()).add(stackItem);
            }
            for (Map.Entry<TreeStateAsKey, List<StackItem>> entry : map.entrySet()) {
                final List<StackItem> group = entry.getValue();
                if (group.size() > 1) {
                    List<StackItem> all_prevs = group.stream().flatMap(o->o.prev.stream()).toList();
                    result.add( new StackItem(group.get(0).syntax_tree, group.get(0).state, all_prevs));
                }
                else {
                    result.add(group.get(0));
                }
            }

            return result;
        }
    }

}
