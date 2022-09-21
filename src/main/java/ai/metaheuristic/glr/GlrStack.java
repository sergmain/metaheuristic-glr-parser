/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
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

    public record SyntaxTree(String symbol, @Nullable GlrToken token, @Nullable Integer ruleIndex, List<SyntaxTree> children) {
        boolean isLeaf() {
            return children==null || children.isEmpty();
        }
    }

    public static class StackItem implements Comparable<StackItem> {
        @Nullable
        public SyntaxTree syntaxTree;
        @Nullable
        public Integer state;
        public final List<StackItem> prev = new ArrayList<>();

        public StackItem(@Nullable SyntaxTree syntaxTree, @Nullable Integer state, @Nullable List<StackItem> prev) {
            this.syntaxTree = syntaxTree;
            this.state = state;
            if (prev!=null) {
                this.prev.addAll(prev);
            }
        }

        public String toString() {
            if (!prev.isEmpty())
                return String.format("%s.%s", syntaxTree != null ? syntaxTree.symbol : "null", state);
            else {
                return "0";
            }
        }

        @Override
        public int compareTo(StackItem o) {
            final int compare = Integer.compare(
                    this.syntaxTree == null || this.syntaxTree.ruleIndex==null ? 0 : this.syntaxTree.ruleIndex,
                    o.syntaxTree == null || o.syntaxTree.ruleIndex==null ? 0 : o.syntaxTree.ruleIndex);
            return compare;
        }
/*
        @Override
        public int compareTo(StackItem o) {
            final int compare = Integer.compare(
                    this.syntaxTree == null ? 0 : this.syntaxTree.hashCode(),
                    o.syntaxTree == null ? 0 : o.syntaxTree.hashCode());
            return compare!=0 ? compare : Integer.compare(this.state == null ? 0 : this.state, o.state == null ? 0 : o.state);
        }
*/

        public static StackItem startNew() {
            return new StackItem(null, 0, null);
        }

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

        public List<StackItem> reduce(
                List<LinkedHashMap<String, List<GlrLr.Action>>> actionGotoTable,
                GlrGrammar.Rule rule,
                @Nullable Function<SyntaxTree, Boolean> reduceValidator) {

            List<StackItem> result = new ArrayList<>();
            int depth = rule.rightSymbols().size();
            for (List<StackItem> path : pop(depth)) {
                final Integer stateIdx = path.get(0).state;
                if (stateIdx==null) {
                    throw new IllegalStateException("(stateIdx==null)");
                }
                List<GlrLr.Action> gotoActions = actionGotoTable.get(stateIdx).get(rule.leftSymbol());

                // # TODO: probably assert that only 1 goto action and it is 'G'
                for (GlrLr.Action gotoAction : gotoActions ) {
                    if ("G".equals(gotoAction.type())) {
                        List<SyntaxTree> tree = path.size()<2 ? List.of() : path.subList(1, path.size()).stream().map(o->o.syntaxTree).filter(Objects::nonNull).toList();
                        SyntaxTree syntaxTree = new SyntaxTree(rule.leftSymbol(), null, rule.index(), tree);
                        if (reduceValidator ==null || reduceValidator.apply(syntaxTree)){
                            var newHead = new StackItem(syntaxTree, gotoAction.state(), List.of(path.get(0)));
                            result.add(newHead);
                        }
                    }
                }
            }
            return result;
        }

        public StackItem shift(GlrToken token, int state) {
            SyntaxTree syntaxTree = new SyntaxTree(token.symbol, token, null, List.of());
            return new StackItem(syntaxTree, state, List.of(this));
        }

        @EqualsAndHashCode
        @RequiredArgsConstructor
        public static class TreeStateAsKey {
            public final SyntaxTree syntaxTree;
            public final Integer state;
        }

        public static List<StackItem> merge(List<StackItem> stackItems) {
            List<StackItem> result = new ArrayList<>();
            List<StackItem> sorted = stackItems.stream().sorted().toList();
            LinkedHashMap<TreeStateAsKey, List<StackItem>> map = new LinkedHashMap<>();
            for (StackItem stackItem : sorted) {
                map.computeIfAbsent(new TreeStateAsKey(stackItem.syntaxTree, stackItem.state), (o)->new ArrayList<>()).add(stackItem);
            }
            for (Map.Entry<TreeStateAsKey, List<StackItem>> entry : map.entrySet()) {
                final List<StackItem> group = entry.getValue();
                if (group.size() > 1) {
                    List<StackItem> allPrevs = group.stream().flatMap(o->o.prev.stream()).toList();
                    result.add( new StackItem(group.get(0).syntaxTree, group.get(0).state, allPrevs));
                }
                else {
                    result.add(group.get(0));
                }
            }

            return result;
        }
    }

}
