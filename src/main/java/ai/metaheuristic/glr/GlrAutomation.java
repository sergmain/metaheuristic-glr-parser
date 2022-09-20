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

    public final GlrParser parser;
    private final GlrGrammar grammar;

    public GlrAutomation( String grammarText, String start) {
        this.grammar = GlrGrammarParser.parse(grammarText, start);
        this.parser = new GlrParser(grammar);
    }

    private static boolean validator(GlrGrammar grammar, GlrStack.SyntaxTree syntaxTree) {
        if (syntaxTree.ruleIndex() == null) {
            return true;
        }
        GlrGrammar.Rule rule = grammar.rules.get(syntaxTree.ruleIndex());
        List<GlrToken> tokens = syntaxTree.children().stream()
                .map(GlrStack.SyntaxTree::token)
                .filter(Objects::nonNull).toList();

        for (int i = 0; i < tokens.size(); i++) {
            GlrToken token = tokens.get(i);
            if (rule.params()==null) {
                continue;
            }
            Map<String, List<Object>> params = rule.params().get(i);
            for (Map.Entry<String, List<Object>> entry : params.entrySet()) {
                String labelKey = entry.getKey();
                List<Object> labelValues = entry.getValue();
                for (Object labelValue : labelValues) {
                    if (!(labelValue instanceof String labelValueStr)) {
                        throw new IllegalStateException("(!(labelValue instanceof String))");
                    }
                    GlrLabels.LabelCheck labelCheck = new GlrLabels.LabelCheck(labelValueStr, tokens, i);
                    boolean ok = GlrLabels.LABELS_CHECK.getOrDefault(labelKey, (v)->false).apply(labelCheck);
                    if (!ok) {
                        // #print 'Label failed: %s=%s for #%s in %s' % (labelKey, labelValue, i, tokens)
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

    private List<GlrStack.SyntaxTree> parse(List<GlrToken> tokens, boolean fullMath) {
        return parser.parse(tokens, fullMath, (syntaxTree) -> validator(grammar, syntaxTree));
    }
}


