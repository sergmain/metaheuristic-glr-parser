/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
            List<GrlTokenizer.Token> reduce_by_tokens, boolean full_math, @Nullable Function<GlrStack.SyntaxTree, Boolean> reduce_validator) {

        List<GlrStack.StackItem> accepted_nodes = new ArrayList<>();

        List<GlrStack.StackItem> current = full_math
                ? new ArrayList<>(List.of(GlrStack.StackItem.start_new()))
                : new ArrayList<>();
        for (int token_index = 0; token_index <reduce_by_tokens.size(); token_index++) {
            GrlTokenizer.Token token = reduce_by_tokens.get(token_index);

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


        }

    }

}
