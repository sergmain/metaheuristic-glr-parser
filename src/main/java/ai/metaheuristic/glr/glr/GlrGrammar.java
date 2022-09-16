/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import javax.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 9:09 PM
 */
public class GlrGrammar {

    public record Rule(int index, String left_symbol, List<String> right_symbols,
                       boolean commit, @Nullable List<Map<String, List<Object>>> params, double weight){}

    String py1 = """
    def __init__(self, rules):
        self._rules = rules
        self._rules_for_symbol = dict()
        for rule in self._rules:
            self._rules_for_symbol.setdefault(rule.left_symbol, []).append(self._rules.index(rule))

        def all_symbols():
            for rule in self._rules:
                yield rule.left_symbol
                for symbol in rule.right_symbols:
                    yield symbol

        self._symbols = set(all_symbols())
        self._symbols.add('$')
        self._nonterminals = set(rule.left_symbol for rule in self._rules)
        self._terminals = self._symbols - self._nonterminals
    """;

    public final List<Rule> rules = new ArrayList<>();
    public final LinkedHashMap<String, List<Integer>> rules_for_symbol;
    public final LinkedHashSet<String> symbols;
    public final LinkedHashSet<String> nonterminals;
    public final LinkedHashSet<String> terminals;

    public GlrGrammar(Rule ... rules) {
        this(Arrays.stream(rules).toList());
    }
    public GlrGrammar(List<Rule> rules) {
        this.rules.addAll(rules);

        this.rules_for_symbol = new LinkedHashMap<>();
        for (Rule rule : this.rules) {
            rules_for_symbol.computeIfAbsent(rule.left_symbol, o->new ArrayList<>()).add(this.rules.indexOf(rule));
        }
        this.symbols = new LinkedHashSet<>(all_symbols());
        this.symbols.add("$");
        this.nonterminals = this.rules.stream().map(o->o.left_symbol).collect(Collectors.toCollection(LinkedHashSet::new));
        this.terminals = symbols.stream().filter(o->!nonterminals.contains(o)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<String> all_symbols() {
        List<String> set = new ArrayList<>();
        for (Rule rule : rules) {
            set.add(rule.left_symbol);
            set.addAll(rule.right_symbols);
        }
        return set;
    }
}
