/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 9:09 PM
 */
public class GlrGrammar {

    public record Rule(int index, String left_symbol, List<String> right_symbols,
                       boolean commit, List<String> params, int weight){}

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

    public final List<Rule> _rules = new ArrayList<>();
    public final LinkedHashMap<String, List<Integer>> _rules_for_symbol;
    public final LinkedHashSet<String> _symbols;
    public final LinkedHashSet<String> _nonterminals;
    public final LinkedHashSet<String> _terminals;

    public GlrGrammar(Rule ... rules) {
        Collections.addAll(this._rules, rules);
        this._rules_for_symbol = new LinkedHashMap<>();
        for (Rule rule : _rules) {
            _rules_for_symbol.computeIfAbsent(rule.left_symbol, o->new ArrayList<>()).add(_rules.indexOf(rule));
        }
        this._symbols = all_symbols();
        this._symbols.add("$");
        this._nonterminals = _rules.stream().map(o->o.left_symbol).collect(Collectors.toCollection(LinkedHashSet::new));
        this._terminals = _symbols.stream().filter(o->!_nonterminals.contains(o)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public LinkedHashSet<String> all_symbols() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Rule rule : _rules) {
            set.add(rule.left_symbol);
            set.addAll(rule.right_symbols);
        }
        return set;
    }
}
