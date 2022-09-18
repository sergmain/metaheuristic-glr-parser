/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import javax.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 10:49 PM
 */
public class GlrLr {
    public static final Item EMPTY_ITEM = new Item(0, 0);

    public record Item(int ruleIndex, int dotPosition) implements Comparable<Item> {
        @Override
            public int compareTo(Item o) {
                int compare = Integer.compare(ruleIndex, o.ruleIndex);
                if (compare != 0) {
                    return compare;
                }
                return Integer.compare(dotPosition, o.dotPosition);
            }
        }
    public record State(int index, List<Item> itemset, Map<String, Set<Integer>> followDict, @Nullable Integer parentStateIndex, @Nullable String parentLookahead) {}
    public record Action(String type, @Nullable Integer state, @Nullable Integer ruleIndex) {}

    public static List<LinkedHashMap<String, List<Action>>> generateActionGotoTable(GlrGrammar grammar) {
        List<LinkedHashMap<String, List<Action>>> result = new ArrayList<>();
        List<State> states = generateStateGraph(grammar);
        GenerateFollowers generateFollowers = new GenerateFollowers(grammar);
        LinkedHashMap<String, LinkedHashSet<String>> followers = generateFollowers.followers;

        for (State state : states) {
            LinkedHashMap<String, List<Action>> actions = new LinkedHashMap<>();

            // # Reduces
            for (Item item : state.itemset) {
                GlrGrammar.Rule rule = grammar.rules.get(item.ruleIndex);
                if (item.dotPosition == rule.rightSymbols().size()) {
                    if ("@".equals(rule.leftSymbol())) {
                        actions.computeIfAbsent("$", o->new ArrayList<>()).add(new Action("A", null, null));
                    }
                    else {
                        for (String follower : followers.get(rule.leftSymbol())) {
                            actions.computeIfAbsent(follower, o->new ArrayList<>()).add(new Action("R", null, item.ruleIndex));
                        }
                        actions.computeIfAbsent("$", o->new ArrayList<>()).add(new Action("R", null, item.ruleIndex));
                    }
                }
            }

            // # Shifts & goto's
            for (Map.Entry<String, Set<Integer>> followEntry : state.followDict.entrySet()) {
                String lookahead = followEntry.getKey();
                Set<Integer> stateIndexes = followEntry.getValue();
                for (Integer stateIndex : stateIndexes) {
                    State childState = states.get(stateIndex);
                    if (followers.containsKey(lookahead)) {
                        actions.computeIfAbsent(lookahead, o->new ArrayList<>()).add(new Action("G", childState.index, null));
                    }
                    else{
                        actions.computeIfAbsent(lookahead, o->new ArrayList<>()).add(new Action("S", childState.index, null));
                    }
                }
            }
            result.add(actions);
        }

        return result;
    }

    private record StackRec(@Nullable Integer index, @Nullable String lookahead, List<Item> itemset) {}

    public static List<State> generateStateGraph(GlrGrammar grammar) {
        List<State> states = new ArrayList<>();
        LinkedHashMap<List<Item>, State> stateByItemset = new LinkedHashMap<>();
        List<Item> firstItemset = closure(List.of(EMPTY_ITEM), grammar);
        firstItemset = firstItemset.stream().sorted(Comparator.comparingInt(Item::ruleIndex)).collect(Collectors.toList());
        LinkedList<StackRec> stack = new LinkedList<>();
        stack.add(new StackRec(null, null, firstItemset));

        while (!stack.isEmpty()) {
            StackRec stackRec = stack.removeFirst();
            Integer parentStateIndex = stackRec.index;
            String parentLookahead = stackRec.lookahead;
            List<Item> itemset = stackRec.itemset;
            State state;
            if (stateByItemset.containsKey(itemset)) {
                if (parentStateIndex==null) {
                    throw new IllegalStateException("(parentStateIndex==null)");
                }
                // # State already exist, just add follow link
                state = stateByItemset.get(itemset);
                states.get(parentStateIndex).followDict.computeIfAbsent(parentLookahead, (o)->new HashSet<>()).add(state.index);
                continue;
            }
            state = new State(states.size(), itemset, new LinkedHashMap<>(), parentStateIndex, parentLookahead);
            states.add(state);
            stateByItemset.putIfAbsent(state.itemset, state);

            if (parentStateIndex!=null) {
                states.get(parentStateIndex).followDict.computeIfAbsent(parentLookahead, (o)->new HashSet<>()).add(state.index);
            }
            int i=0;
            for (Follows follows : follow(state.itemset, grammar)){
                String lookahead = follows.lookahead;
                LinkedList<GlrLr.Item> itemset1 = GlrLr.uniqueAndSorted(follows.item);
                stack.add(new StackRec(state.index, lookahead, itemset1));
            }
        }
        return states;
    }

    public static class GenerateFollowers {
        public final GlrGrammar grammar;
        public final LinkedHashMap<String, LinkedHashSet<String>> starters = new LinkedHashMap<>();
        public final LinkedHashMap<String, LinkedHashSet<String>> followers = new LinkedHashMap<>();

        public GenerateFollowers(GlrGrammar grammar) {
            this.grammar = grammar;
            for (String s : grammar.nonterminals) {
                this.starters.put(s, getStarters(s, grammar));
            }
            for (String s : grammar.nonterminals) {
                this.followers.put(s, getFollowers(s, null, grammar));
            }
        }

        public static LinkedHashSet<String> getStarters(String symbol, GlrGrammar grammar) {
            LinkedHashSet<String> result = new LinkedHashSet<>();
            for (Integer ruleIndex : grammar.rulesForSymbol.get(symbol)) {
                GlrGrammar.Rule rule = grammar.rules.get(ruleIndex);
                final String rightSymbol = rule.rightSymbols().get(0);
                if (grammar.nonterminals.contains(rightSymbol)) {
                    if (!rightSymbol.equals(symbol)) {
                        result.addAll(getStarters(rightSymbol, grammar));
                    }
                }
                else {
                    result.add(rightSymbol);
                }
            }
            return result;
        }

        public LinkedHashSet<String> getFollowers(String symbol, @Nullable LinkedHashSet<String> seenSymbolsTemp, GlrGrammar grammar) {
            LinkedHashSet<String> seenSymbols = seenSymbolsTemp!=null ? seenSymbolsTemp : new LinkedHashSet<>();
            LinkedHashSet<String> result = new LinkedHashSet<>();
            for (GlrGrammar.Rule rule : grammar.rules) {
//                if isinstance(rule, set):  # TODO: remove workaround
//                    continue
                if (!rule.rightSymbols().contains(symbol)) {
                    continue;
                }
                int index = rule.rightSymbols().indexOf(symbol);
                if (index + 1 == rule.rightSymbols().size()) {
                    if (!rule.leftSymbol().equals(symbol) && !seenSymbols.contains(rule.leftSymbol())) {
                        result.addAll(getFollowers(rule.leftSymbol(), seenSymbols, grammar));
                    }
                }
                else {
                    String next = rule.rightSymbols().get(index + 1);
                    if (grammar.nonterminals.contains(next)) {
                        result.addAll(starters.get(next));
                    }
                    else {
                        result.add(next);
                    }
                }
            }


            return result;
        }
    }

    String py3 = """
        def closure(itemset, grammar):
            ""\"
                The epsilon-closure of this item set
            ""\"
            items_to_process = itemset
            visited_lookaheads = set()
            while True:
                for item in items_to_process:
                    yield item
            
                nested_to_process = []
                for item, lookahead in iterate_lookaheads(items_to_process, grammar):
                    if lookahead in grammar.nonterminals and lookahead not in visited_lookaheads:
                        visited_lookaheads.add(lookahead)
                        for rule_index in grammar.rules_for_symbol(lookahead):
                            nested_to_process.append(Item(rule_index, 0))
            
                if not nested_to_process:
                    # no changes
                    return
            
                items_to_process = nested_to_process
        """;

    public static List<Item> closure(List<Item> itemset, GlrGrammar grammar) {
        List<Item> result = new ArrayList<>();

        List<Item> itemsToProcess = itemset;
        LinkedHashSet<String> visitedLookaheads = new LinkedHashSet<>();
        while (true) {
            result.addAll(itemsToProcess);

            List<Item> nestedToProcess = new ArrayList<>();
            for (Lookaheads iterateLookahead : iterateLookaheads(itemsToProcess, grammar)) {
                Item item = iterateLookahead.item;
                String lookahead = iterateLookahead.lookahead;
                if (grammar.nonterminals.contains(lookahead) && !visitedLookaheads.contains(lookahead)) {
                    visitedLookaheads.add(lookahead);
                    for (Integer ruleIndex : grammar.rulesForSymbol.get(lookahead)) {
                        nestedToProcess.add(new Item(ruleIndex, 0));
                    }
                }
            }
            if (nestedToProcess.isEmpty()) {
                // # no changes;
                break;
            }
            itemsToProcess = nestedToProcess;
        }

        return result;
    }

    String py4 = """
        def iterate_lookaheads(itemset, grammar):
            for item in itemset:
                rule = grammar[item.rule_index]
        
                if item.dot_position == len(rule.right_symbols):
                    # dot is in the end, there is no look ahead symbol
                    continue
        
                lookahead = rule.right_symbols[item.dot_position]
        
                yield item, lookahead
        """;
    public record Lookaheads(Item item, String lookahead) {}

    public static List<Lookaheads> iterateLookaheads(List<Item> itemset, GlrGrammar grammar) {
        List<Lookaheads> list = new ArrayList<>();
        for (Item item : itemset) {
            GlrGrammar.Rule rule = grammar.rules.get(item.ruleIndex);

            if (item.dotPosition == rule.rightSymbols().size()) {
                // # dot is in the end, there is no look ahead symbol
                continue;
            }
            String lookahead = rule.rightSymbols().get(item.dotPosition);
            list.add(new Lookaheads(item, lookahead));
        }

        return list;
    }

    String py5 = """
        def follow(itemset, grammar):
            ""\"
                All transitions from an item set in a dictionary [token]->item set
            ""\"
            result = OrderedDict()
            for item, lookahead in iterate_lookaheads(itemset, grammar):
                tmp = closure([Item(item.rule_index, item.dot_position + 1)], grammar)

                if lookahead not in result:
                    result[lookahead] = []
                result[lookahead].extend(tmp)

            for lookahead, itemset in result.items():
                yield lookahead, unique(itemset)

        """;
    public record Follows(List<Item> item, String lookahead) {}

    public static List<Follows> follow(List<Item> itemset, GlrGrammar grammar) {
        List<Follows> list = new ArrayList<>();
        // All transitions from an item set in a dictionary [token]->item set
        LinkedHashMap<String, List<Item>> result = new LinkedHashMap<>();
        for (Lookaheads iterateLookahead : iterateLookaheads(itemset, grammar)) {
            Item item = iterateLookahead.item;
            String lookahead = iterateLookahead.lookahead;
            List<Item> tmp = closure(List.of(new Item(item.ruleIndex, item.dotPosition + 1)), grammar);
            if (!result.containsKey(lookahead)) {
                result.put(lookahead, new ArrayList<>());
            }
            result.get(lookahead).addAll(tmp);
        }
        for (Map.Entry<String, List<Item>> entry : result.entrySet()) {
            list.add(new Follows(new ArrayList<>(entry.getValue()), entry.getKey()));
        }
        return list;
    }


    public static LinkedList<Item> uniqueAndSorted(List<Item> items) {
        LinkedHashSet<Item> itemset1 = new LinkedHashSet<>(items);
        LinkedList<Item> itemset = new LinkedList<>();
        itemset1.stream().sorted().collect(Collectors.toCollection(()->itemset));
        return itemset;
    }
}
