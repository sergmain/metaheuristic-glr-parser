/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 9/12/2022
 * Time: 5:58 PM
 */
public class GlrStack {

    public record SyntaxTree(String symbol, String token, int rule_index, List<SyntaxTree> children) {
        boolean is_leaf() {
            return children==null || children.isEmpty();
        }
    }


    String py2 = """
        class StackItem(namedtuple('StackItem', ['syntax_tree', 'state', 'prev'])):
        """;

    public static class StackItem {
        @Nullable
        public SyntaxTree syntax_tree;
        public int state;
        @Nullable
        public StackItem prev;

        public StackItem(@Nullable SyntaxTree syntax_tree, int state, @Nullable StackItem prev) {
            this.syntax_tree = syntax_tree;
            this.state = state;
            this.prev = prev;
        }

        public static StackItem start_new() {
            return new StackItem(null, 0, null);
        }
    }
}
