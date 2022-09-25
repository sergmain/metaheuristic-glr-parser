/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:19 PM
 */
public class GlrAutomation {

    public final GlrParser parser;
    private final GlrGrammar grammar;

    public GlrAutomation( String grammarText, String start) {
        this(grammarText, start, 0);
    }

    public GlrAutomation( String grammarText, String start, int log_level) {
        this.grammar = GlrGrammarParser.parse(grammarText, start);
        this.parser = new GlrParser(grammar, log_level);
    }

    private static void collectChildren(LinkedHashSet<GlrToken> list, GlrStack.SyntaxTree syntaxTree) {
        if (syntaxTree.token()!=null) {
            list.add(syntaxTree.token());
        }
        if (syntaxTree.children()!=null && !syntaxTree.children().isEmpty()) {
            for (GlrStack.SyntaxTree child : syntaxTree.children()) {
                collectChildren(list, child);
            }
        }
    }

    private static boolean validator(GlrGrammar grammar, GlrStack.SyntaxTree syntaxTree) {
        if (syntaxTree.ruleIndex() == null) {
            return true;
        }
        GlrGrammar.Rule rule = grammar.rules.get(syntaxTree.ruleIndex());
        LinkedHashSet<GlrToken> set = new LinkedHashSet<>();
        for (GlrStack.SyntaxTree child : syntaxTree.children()) {
            collectChildren(set, child);
        }
        syntaxTree.children().stream()
                .map(GlrStack.SyntaxTree::token)
                .collect(Collectors.toCollection(()->set));

        List<GlrToken> tokens = new ArrayList<>(set);

        for (int i = 0; i < tokens.size(); i++) {
            GlrToken token = tokens.get(i);
            if (rule.params()==null || rule.params().size()<=i) {
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
                    if (token==null) {
                        return false;
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


