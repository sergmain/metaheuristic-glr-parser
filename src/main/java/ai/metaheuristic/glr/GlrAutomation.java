/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:19 PM
 */
public class GlrAutomation {

    private final GlrParser parser;
    private final GlrGrammar grammar;

    public GlrAutomation( String grammar_text, String start) {
        this.grammar = GlrGrammarParser.parse(grammar_text, start);
        this.parser = new GlrParser(grammar);
    }

    private static boolean validator(GlrGrammar grammar, GlrStack.SyntaxTree syntax_tree) {
        if (syntax_tree.rule_index()==null) {
            return true;
        }
        GlrGrammar.Rule rule = grammar.rules.get(syntax_tree.rule_index());
        List<GlrToken> tokens = syntax_tree.children().stream()
                .map(GlrStack.SyntaxTree::token)
                .filter(Objects::nonNull).toList();

        for (int i = 0; i < tokens.size(); i++) {
            GlrToken token = tokens.get(i);
            if (rule.params()==null) {
                continue;
            }
            Map<String, List<Object>> params = rule.params().get(i);
            for (Map.Entry<String, List<Object>> entry : params.entrySet()) {
                String label_key = entry.getKey();
                List<Object> label_values = entry.getValue();
                for (Object label_value : label_values) {
                    if (!(label_value instanceof String label_value_str)) {
                        throw new IllegalStateException("(!(label_value instanceof String))");
                    }
                    GlrLabels.LabelCheck labelCheck = new GlrLabels.LabelCheck(label_value_str, tokens, i);
                    boolean ok = GlrLabels.LABELS_CHECK.getOrDefault(label_key, (v)->false).apply(labelCheck);
                    if (!ok) {
                        // #print 'Label failed: %s=%s for #%s in %s' % (label_key, label_value, i, tokens)
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<GlrStack.SyntaxTree> parse(List<GlrToken> tokens) {
        return parse(tokens, false);
    }

    private List<GlrStack.SyntaxTree> parse(List<GlrToken> tokens, boolean full_math) {
        return parser.parse(tokens, full_math, (syntaxTree) -> validator(grammar, syntaxTree));
    }
}


