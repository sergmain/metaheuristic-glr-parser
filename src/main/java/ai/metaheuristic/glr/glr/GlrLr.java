/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 10:49 PM
 */
public class GlrLr {
    String py0 = """
        class Item(namedtuple('Item', ['rule_index', 'dot_position'])):
            __slots__ = ()
            
            def __repr__(self):
                return '#%d.%d' % self
            
            
        State = namedtuple('State', ['index', 'itemset', 'follow_dict', 'parent_state_index', 'parent_lookahead'])
            
        Action = namedtuple('Action', ['type', 'state', 'rule_index'])
        """;

    public record Item(int rule_index, int dot_position) {}
    public record State(int index, List<Item> itemset, Map<Integer, List<Integer>> follow_dict, @Nullable Integer parent_state_index, @Nullable Integer parent_lookahead) {}
    public record Action(String state, int rule_index) {}


    String py1 = """
        def generate_action_goto_table(grammar):
            assert isinstance(grammar, Grammar)
        
            states = generate_state_graph(grammar)
            followers = generate_followers(grammar)
        
            result = []
            for state in states:
                actions = defaultdict(list)
        
                # Reduces
                for item in state.itemset:
                    rule = grammar[item.rule_index]
                    if item.dot_position == len(rule.right_symbols):
                        if rule.left_symbol == '@':
                            actions['$'].append(Action('A', None, None))
                        else:
                            for follower in followers[rule.left_symbol]:
                                actions[follower].append(Action('R', None, item.rule_index))
                            actions['$'].append(Action('R', None, item.rule_index))
        
                # Shifts & goto's
                for lookahead, state_indexes in state.follow_dict.items():
                    for state_index in state_indexes:
                        child_state = states[state_index]
                        if lookahead in followers:
                            actions[lookahead].append(Action('G', child_state.index, None))
                        else:
                            actions[lookahead].append(Action('S', child_state.index, None))
        
                result.append(actions)
            return result
        """;

    public List<LinkedHashMap<String, Action>> generate_action_goto_table(GlrGrammar grammar) {
        List<LinkedHashMap<String, Action>> result = new ArrayList<>();


        return result;
    }

    String py2 = """
        def generate_state_graph(grammar):
            assert isinstance(grammar, Grammar)
        
            states = []
            state_by_itemset = {}
        
            first_itemset = closure([Item(0, 0)], grammar)
            first_itemset = tuple(sorted(first_itemset))
            stack = [(None, None, first_itemset)]
            while stack:
                parent_state_index, parent_lookahead, itemset = stack.pop(0)
        
                if itemset in state_by_itemset:
                    # State already exist, just add follow link
                    state = state_by_itemset[itemset]
                    states[parent_state_index].follow_dict[parent_lookahead].add(state.index)
                    continue
        
                state = State(len(states), itemset, defaultdict(set), parent_state_index, parent_lookahead)
                states.append(state)
                state_by_itemset[state.itemset] = state
        
                if parent_state_index is not None:
                    states[parent_state_index].follow_dict[parent_lookahead].add(state.index)
        
                for lookahead, itemset in follow(state.itemset, grammar):
                    itemset = tuple(sorted(itemset))
                    stack.append((state.index, lookahead, itemset))
            return states
            """;

    public record StackRec(@Nullable Integer index, @Nullable Integer lookahead, List<Item> itemset) {}

    public static List<State> generate_state_graph(GlrGrammar grammar) {
        List<State> states = new ArrayList<>();
        LinkedHashMap<List<Item>, State> state_by_itemset = new LinkedHashMap<>();
        List<Item> first_itemset = closure(List.of(new Item(0, 0)), grammar);
        first_itemset = first_itemset.stream().sorted(Comparator.comparingInt(Item::rule_index)).collect(Collectors.toList());
        LinkedList<StackRec> stack = new LinkedList<>();
        stack.add(new StackRec(null, null, first_itemset));

        while (!stack.isEmpty()) {
            StackRec stackRec = stack.removeFirst();
            Integer parent_state_index = stackRec.index;
            Integer parent_lookahead = stackRec.lookahead;
            List<Item> itemset = stackRec.itemset;
            State state;
            if (state_by_itemset.containsKey(itemset)) {
                // # State already exist, just add follow link
                state = state_by_itemset.get(itemset);
                states.get(parent_state_index).follow_dict.get(parent_lookahead).add(state.index);
            }
            state = new State(states.size(), itemset, Map.of(), parent_state_index, parent_lookahead);
            states.add(state);
            state_by_itemset.putIfAbsent(state.itemset, state);

            if (parent_state_index!=null) {
                states.get(parent_state_index).follow_dict.get(parent_lookahead).add(state.index);
            }
/*
            for lookahead, itemset in follow(state.itemset, grammar):
                    itemset = tuple(sorted(itemset))
                    stack.append((state.index, lookahead, itemset))
*/
        }
        return states;
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

        List<Item> items_to_process = itemset;
        LinkedHashSet<String> visited_lookaheads = new LinkedHashSet<>();
        while (true) {
            result.addAll(items_to_process);

            List<Item> nested_to_process = new ArrayList<>();
            for (Lookaheads iterateLookahead : iterate_lookaheads(items_to_process, grammar)) {
                Item item = iterateLookahead.item;
                String lookahead = iterateLookahead.lookahead;
                if (grammar.nonterminals.contains(lookahead) && !visited_lookaheads.contains(lookahead)) {
                    visited_lookaheads.add(lookahead);
                    for (Integer rule_index : grammar.rules_for_symbol.get(lookahead)) {
                        nested_to_process.add(new Item(rule_index, 0));
                    }
                }
            }
            if (!nested_to_process.isEmpty()) {
                // # no changes;
                break;
            }
            items_to_process = nested_to_process;
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

    public static List<Lookaheads> iterate_lookaheads(List<Item> itemset, GlrGrammar grammar) {
        List<Lookaheads> list = new ArrayList<>();
        for (Item item : itemset) {
            GlrGrammar.Rule rule = grammar.rules.get(item.rule_index);

            if (item.dot_position==rule.right_symbols().size()) {
                // # dot is in the end, there is no look ahead symbol
                continue;
            }
            String lookahead = rule.right_symbols().get(item.dot_position);
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
    public record Follows(Item item, String lookahead) {}

    public static List<Follows> follow(List<Item> itemset, GlrGrammar grammar) {
        List<Follows> list = new ArrayList<>();
        // All transitions from an item set in a dictionary [token]->item set
        LinkedHashMap<String, List<Item>> result = new LinkedHashMap<>();
        for (Lookaheads iterateLookahead : iterate_lookaheads(itemset, grammar)) {
            Item item = iterateLookahead.item;
            String lookahead = iterateLookahead.lookahead;
            List<Item> tmp = closure(List.of(new Item(item.rule_index, item.dot_position + 1)), grammar);
            if (!result.containsKey(lookahead)) {
                result.put(lookahead, new ArrayList<>());
            }
            result.get(lookahead).addAll(tmp);
        }
        for (Map.Entry<String, List<Item>> entry : result.entrySet()) {
            //list.add(new Follows(entry.getValue(), entry.getKey()));
        }
        return list;
    }


}
