/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 10:47 PM
 */
public class GlrParser {

    public void log(int level, String pattern, Object ... objects) {
        if (level<=log_level) {
            System.out.printf(pattern + "\n", objects);
        }
    }

    public record NodeAndAction(GlrStack.StackItem node, GlrLr.Action action) {}
    public List<NodeAndAction> get_by_action_type(List<GlrStack.StackItem> nodes, GlrToken token, String action_type) {
        List<NodeAndAction> result = new ArrayList<>();
        for (GlrStack.StackItem node : nodes) {
            if (node.state==null) {
                throw new IllegalStateException("(node.state==null)");
            }
            List<GlrLr.Action> node_actions = action_goto_table.get(node.state).get(token.symbol);
            if (node_actions==null) {
                continue;
            }
            for (GlrLr.Action action : node_actions) {
                if (action.type().equals(action_type)) {
                    result.add(new NodeAndAction(node, action));
                }
            }
        }
        return result;
    }

    public final GlrGrammar grammar;
    private final int log_level;
    public final List<LinkedHashMap<String, List<GlrLr.Action>>> action_goto_table;

    public GlrParser(GlrGrammar grammar) {
        this(grammar, 0);
    }

    public GlrParser(GlrGrammar grammar, int log_level) {
        this.grammar = grammar;
        this.action_goto_table = GlrLr.generate_action_goto_table(grammar);
        this.log_level = log_level;
    }

    public List<GlrStack.SyntaxTree> parse(List<GlrToken> reduce_by_tokens, boolean full_math) {
        return parse(reduce_by_tokens, full_math, null);
    }

    public List<GlrStack.SyntaxTree> parse(
            List<? extends GlrToken> reduce_by_tokens_params, boolean full_math, @Nullable Function<GlrStack.SyntaxTree, Boolean> reduce_validator) {

        List<GlrStack.StackItem> accepted_nodes = new ArrayList<>();

        List<GlrStack.StackItem> current = full_math
                ? new ArrayList<>(List.of(GlrStack.StackItem.start_new()))
                : new ArrayList<>();
        for (int token_index = 0; token_index < reduce_by_tokens_params.size(); token_index++) {
            GlrToken token = reduce_by_tokens_params.get(token_index);

            log(1, "\nTOKEN: %s", token);

            List<GlrToken> reduce_by_tokens = new ArrayList<>(List.of(token));

            if (!full_math) {
                if ( !grammar.terminals.contains(token.symbol)) {
                    log(1, "- Not in grammar, interpret as end of stream");
                    reduce_by_tokens = new ArrayList<>();
                }

                // # If not full match on each token we assume rule may start or end
                current.add(GlrStack.StackItem.start_new());
                if (!"$".equals(token.symbol)) {
                    reduce_by_tokens.add(new GlrToken("$"));
                }
            }

            for (GlrToken reduce_by_token : reduce_by_tokens) {
                List<GlrStack.StackItem> process_reduce_nodes = new ArrayList<>(current);
                while (!process_reduce_nodes.isEmpty()) {
                    List<GlrStack.StackItem> new_reduce_nodes = new ArrayList<>();
                    for (NodeAndAction r : get_by_action_type(process_reduce_nodes, reduce_by_token, "R")) {
                        GlrStack.StackItem node = r.node;
                        GlrLr.Action action = r.action;

                        if (action.rule_index()==null) {
                            throw new IllegalStateException("(action.rule_index()==null)");
                        }
                        GlrGrammar.Rule rule = grammar.rules.get(action.rule_index());
                        log(1, "- REDUCE: (%s) by (%s)", node, GlrUtils.format_rule(rule));
                        List<GlrStack.StackItem> reduced_nodes = node.reduce(action_goto_table, rule, reduce_validator);
                        new_reduce_nodes.addAll(reduced_nodes);
                        for (GlrStack.StackItem n : reduced_nodes) {
                            log(1, "    %s", GlrUtils.format_stack_item(n, "     "));
                        }
                    }
                    process_reduce_nodes = new_reduce_nodes;
                    current.addAll(new_reduce_nodes);
                }
                for (NodeAndAction nodeAndAction : get_by_action_type(current, reduce_by_token, "A")) {
                    GlrStack.StackItem node = nodeAndAction.node;
                    log(1, "- ACCEPT: (%s)", node);
                    accepted_nodes.add(node);
                }
            }
            List<GlrStack.StackItem> shifted_nodes = new ArrayList<>();
            final List<NodeAndAction> list = get_by_action_type(current, token, "S");
            for (NodeAndAction nodeAndAction : list) {
                GlrStack.StackItem node = nodeAndAction.node;
                GlrLr.Action action = nodeAndAction.action;
                if (action.state()==null) {
                    throw new IllegalStateException("(action.state()==null)");
                }
                var shifted_node = node.shift(token, action.state());
                log(1, "- SHIFT: (%s) to (%s)  =>  %s", node, action.state(), shifted_node);
                shifted_nodes.add(shifted_node);
            }
            current = shifted_nodes;
            current = new LinkedList<>(GlrStack.StackItem.merge(current));
            log(1, "\n- STACK:");
            for (GlrStack.StackItem node : current) {
                final String path = GlrUtils.format_stack_item(node, "    ");
                log(1, "    %s", path);
            }
            int i=0;
        }
        log(1, "\n--------------------\nACCEPTED:");
        for (GlrStack.StackItem node : accepted_nodes) {
            if (node.syntax_tree==null) {
                throw new IllegalStateException("(node.syntax_tree==null)");
            }
            log(1, "%s", GlrUtils.format_syntax_tree(node.syntax_tree));
        }
        return accepted_nodes.stream().map(o->o.syntax_tree).filter(Objects::nonNull).toList();
    }
}
