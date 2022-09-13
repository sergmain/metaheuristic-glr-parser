/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 10:47 PM
 */
public class GlrParser {
    String py1 = """
    def __init__(self, grammar, log_level=0):
        assert isinstance(grammar, Grammar)
        self.grammar = grammar
        self.action_goto_table = generate_action_goto_table(self.grammar)
        self.log_level = log_level

    """;

    String py11 = """
    def log(self, level, pattern, *args):
        if level <= self.log_level:
            print(pattern % args)
    """;
    public void log(int level, String pattern, Object ... objects) {
        if (level<=log_level) {
            System.out.printf(pattern + "\n", objects);
        }
    }

    String py12 = """
    def get_by_action_type(self, nodes, token, action_type):
        for node in nodes:
            node_actions = self.action_goto_table[node.state][token.symbol]
            for action in node_actions:
                if action.type == action_type:
                    yield node, action
    """;
    public record NodeAndAction(GlrStack.StackItem node, GlrLr.Action action) {}
    public List<NodeAndAction> get_by_action_type(List<GlrStack.StackItem> nodes, GrlTokenizer.Token token, String action_type) {
        List<NodeAndAction> result = new ArrayList<>();
        for (GlrStack.StackItem node : nodes) {
            List<GlrLr.Action> node_actions = action_goto_table.get(node.state).get(token.symbol);
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
        this.log_level = log_level;
        this.action_goto_table = GlrLr.generate_action_goto_table(grammar);
    }


    String py2 = """
    def parse(self, reduce_by_tokens, full_math=False, reduce_validator=None):
        accepted_nodes = []

        current = [StackItem.start_new()] if full_math else []

        for token_index, token in enumerate(reduce_by_tokens):
            self.log(1, '\n\nTOKEN: %s', token)

            reduce_by_tokens = [token]

            if not full_math:
                if token.symbol not in self.grammar.terminals:
                    self.log(1, '- Not in grammar, interpret as end of stream')
                    reduce_by_tokens = []

                # If not full match on each token we assume rule may start or end
                current.append(StackItem.start_new())
                if token.symbol != '$':
                    reduce_by_tokens.append(Token('$'))

            for reduce_by_token in reduce_by_tokens:
                process_reduce_nodes = current[:]
                while process_reduce_nodes:
                    new_reduce_nodes = []
                    for node, action in self.get_by_action_type(process_reduce_nodes, reduce_by_token, 'R'):
                        rule = self.grammar[action.rule_index]
                        self.log(1, '- REDUCE: (%s) by (%s)', node, format_rule(rule))
                        reduced_nodes = node.reduce(self.action_goto_table, rule, reduce_validator)
                        new_reduce_nodes.extend(reduced_nodes)
                        for n in reduced_nodes:
                            self.log(1, '    %s', format_stack_item(n, '     '))
                    process_reduce_nodes = new_reduce_nodes
                    current.extend(new_reduce_nodes)

                for node, action in self.get_by_action_type(current, reduce_by_token, 'A'):
                    self.log(1, '- ACCEPT: (%s)', node)
                    accepted_nodes.append(node)

            shifted_nodes = []
            for node, action in self.get_by_action_type(current, token, 'S'):
                shifted_node = node.shift(token, action.state)
                self.log(1, '- SHIFT: (%s) to (%s)  =>  %s', node, action.state, shifted_node)
                shifted_nodes.append(shifted_node)

            current = shifted_nodes

            current = list(StackItem.merge(current))

            self.log(1, '\n- STACK:')
            for node in current:
                self.log(1, '    %s', format_stack_item(node, '     '))

        self.log(1, '\n--------------------\nACCEPTED:')
        for node in accepted_nodes:
            self.log(1, '%s', format_syntax_tree(node.syntax_tree))

        return [node.syntax_tree for node in accepted_nodes]
    """;


    public List<GlrStack.SyntaxTree> parse(List<GrlTokenizer.Token> reduce_by_tokens, boolean full_math) {
        return parse(reduce_by_tokens, full_math, null);
    }

    public List<GlrStack.SyntaxTree> parse(
            List<GrlTokenizer.Token> reduce_by_tokens_params, boolean full_math, @Nullable Function<GlrStack.SyntaxTree, Boolean> reduce_validator) {

        List<GlrStack.StackItem> accepted_nodes = new ArrayList<>();

        List<GlrStack.StackItem> current = full_math
                ? new ArrayList<>(List.of(GlrStack.StackItem.start_new()))
                : new ArrayList<>();
        for (int token_index = 0; token_index < reduce_by_tokens_params.size(); token_index++) {
            GrlTokenizer.Token token = reduce_by_tokens_params.get(token_index);

            log(1, "\nTOKEN: %s", token);

            List<GrlTokenizer.Token> reduce_by_tokens = new ArrayList<>(List.of(token));

            if (!full_math) {
                if ( !grammar.terminals.contains(token.symbol)) {
                    log(1, "- Not in grammar, interpret as end of stream");
                    reduce_by_tokens = new ArrayList<>();
                }

                // # If not full match on each token we assume rule may start or end
                current.add(GlrStack.StackItem.start_new());
                if (!"$".equals(token.symbol)) {
                    reduce_by_tokens.add(new GrlTokenizer.Token("$"));
                }
            }

            for (GrlTokenizer.Token reduce_by_token : reduce_by_tokens) {
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
            for (NodeAndAction nodeAndAction : get_by_action_type(current, token, "S")) {
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
            current = new LinkedList<>(List.of(GlrStack.StackItem.merge(current)));
            log(1, "\n- STACK:");
            for (GlrStack.StackItem node : current) {
                log(1, "    %s", GlrUtils.format_stack_item(node, "     "));
            }
        }
        log(1, "\n--------------------\nACCEPTED:");
        for (GlrStack.StackItem node : accepted_nodes) {
            log(1, "%s", format_syntax_tree(node.syntax_tree));
        }
        return accepted_nodes.stream().map(o->o.syntax_tree).filter(Objects::nonNull).toList();
    }

}
