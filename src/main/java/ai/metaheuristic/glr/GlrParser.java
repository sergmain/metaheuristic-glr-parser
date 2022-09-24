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
import java.util.function.Supplier;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 10:47 PM
 */
public class GlrParser {

    public void log(int level, String pattern) {
        log(level, pattern, null);
    }

    public void logList(int level, String pattern, @Nullable Supplier<List<Object>> objectsFunc) {
        if (objectsFunc==null) {
            log(level, pattern);
        }
        else {
            log(level, pattern, () -> objectsFunc.get().toArray(new Object[0]));
        }
    }
    public void log(int level, String pattern, @Nullable Supplier<Object[]> objectsFunc) {
        if (level <= logLevel) {
            if (objectsFunc!=null) {
                System.out.printf(pattern, objectsFunc.get());
            }
            else {
                System.out.printf(pattern);
            }
            System.out.println();
        }
    }

    public record NodeAndAction(GlrStack.StackItem node, GlrLr.Action action) {}
    public List<NodeAndAction> getByActionType(List<GlrStack.StackItem> nodes, GlrToken token, String actionType) {
        List<NodeAndAction> result = new ArrayList<>();
        for (GlrStack.StackItem node : nodes) {
            if (node.state==null) {
                throw new IllegalStateException("(node.state==null)");
            }
            List<GlrLr.Action> nodeActions = actionGotoTable.get(node.state).get(token.symbol);
            if (nodeActions==null) {
                continue;
            }
            for (GlrLr.Action action : nodeActions) {
                if (action.type().equals(actionType)) {
                    result.add(new NodeAndAction(node, action));
                }
            }
        }
        return result;
    }

    public final GlrGrammar grammar;
    private final int logLevel;
    public final List<LinkedHashMap<String, List<GlrLr.Action>>> actionGotoTable;

    public GlrParser(GlrGrammar grammar) {
        this(grammar, 0);
    }

    public GlrParser(GlrGrammar grammar, int logLevel) {
        this.grammar = grammar;
        this.actionGotoTable = GlrLr.generateActionGotoTable(grammar);
        this.logLevel = logLevel;
    }

    public List<GlrStack.SyntaxTree> parse(List<GlrToken> reduceByTokens, boolean fullMath) {
        return parse(reduceByTokens, fullMath, null);
    }

    public List<GlrStack.SyntaxTree> parse(
            List<? extends GlrToken> reduceByTokensParams, boolean fullMath, @Nullable Function<GlrStack.SyntaxTree, Boolean> reduceValidator) {

        List<GlrStack.StackItem> acceptedNodes = new ArrayList<>();

        List<GlrStack.StackItem> current = fullMath
                ? new ArrayList<>(List.of(GlrStack.StackItem.startNew()))
                : new ArrayList<>();
        for (int tokenIndex = 0; tokenIndex < reduceByTokensParams.size(); tokenIndex++) {
            GlrToken token = reduceByTokensParams.get(tokenIndex);

            logList(1, "\nTOKEN: %s", ()->List.of(token));

            List<GlrToken> reduceByTokens = new ArrayList<>(List.of(token));

            if (!fullMath) {
                if ( !grammar.terminals.contains(token.symbol)) {
                    log(1, "- Not in grammar, interpret as end of stream");
                    reduceByTokens = new ArrayList<>();
                }

                // # If not full match on each token we assume rule may start or end
                current.add(GlrStack.StackItem.startNew());
                if (!GlrConsts.END_OF_TOKEN_LIST.equals(token.symbol)) {
                    reduceByTokens.add(new GlrToken(GlrConsts.END_OF_TOKEN_LIST));
                }
            }

            for (GlrToken reduceByToken : reduceByTokens) {
                List<GlrStack.StackItem> processReduceNodes = new ArrayList<>(current);
                while (!processReduceNodes.isEmpty()) {
                    List<GlrStack.StackItem> newReduceNodes = new ArrayList<>();
                    for (NodeAndAction r : getByActionType(processReduceNodes, reduceByToken, "R")) {
                        GlrStack.StackItem node = r.node;
                        GlrLr.Action action = r.action;

                        if (action.ruleIndex() == null) {
                            throw new IllegalStateException("(action.ruleIndex()==null)");
                        }
                        GlrGrammar.Rule rule = grammar.rules.get(action.ruleIndex());
                        // - REDUCE: (word.10) by (#11: Symbol =
                        logList(1, "- REDUCE: (%s) by (%s)", ()->List.of(node, GlrUtils.formatRule(rule)));
                        List<GlrStack.StackItem> reducedNodes = node.reduce(actionGotoTable, rule, reduceValidator);
                        newReduceNodes.addAll(reducedNodes);
                        for (GlrStack.StackItem n : reducedNodes) {
                            logList(1, "     %s", ()->List.of(GlrUtils.formatStackItem(n, "     ")));
                        }
                    }
                    processReduceNodes = newReduceNodes;
                    current.addAll(newReduceNodes);
                }
                for (NodeAndAction nodeAndAction : getByActionType(current, reduceByToken, "A")) {
                    GlrStack.StackItem node = nodeAndAction.node;
                    logList(1, "- ACCEPT: (%s)", ()->List.of(node));
                    acceptedNodes.add(node);
                }
            }
            List<GlrStack.StackItem> shiftedNodes = new ArrayList<>();
            final List<NodeAndAction> list = getByActionType(current, token, "S");
            for (NodeAndAction nodeAndAction : list) {
                GlrStack.StackItem node = nodeAndAction.node;
                GlrLr.Action action = nodeAndAction.action;
                if (action.state()==null) {
                    throw new IllegalStateException("(action.state()==null)");
                }
                var shiftedNode = node.shift(token, action.state());
                logList(1, "- SHIFT: (%s) to (%s)  =>  %s", ()->List.of(node, action.state(), shiftedNode));
                shiftedNodes.add(shiftedNode);
            }
            current = shiftedNodes;
            current = new LinkedList<>(GlrStack.StackItem.merge(current));
            log(1, "\n- STACK:");
            for (GlrStack.StackItem node : current) {
                final String path = GlrUtils.formatStackItem(node, "    ");
                logList(1, "    %s", ()->List.of(path));
            }
            int i=0;
        }
        log(1, "\n--------------------\nACCEPTED:");
        for (GlrStack.StackItem node : acceptedNodes) {
            if (node.syntaxTree == null) {
                throw new IllegalStateException("(node.syntax_tree==null)");
            }
            logList(1, "%s", ()->List.of(GlrUtils.formatSyntaxTree(node.syntaxTree)));
        }
        return acceptedNodes.stream().map(o->o.syntaxTree).filter(Objects::nonNull).toList();
    }
}
