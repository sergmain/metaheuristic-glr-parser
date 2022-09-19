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
 * Time: 9:09 PM
 */
public class GlrGrammar {

    public record Rule(int index, String leftSymbol, List<String> rightSymbols,
                       boolean commit, @Nullable List<Map<String, List<Object>>> params, double weight){}

    public final List<Rule> rules = new ArrayList<>();
    public final LinkedHashMap<String, List<Integer>> rulesForSymbol;
    private final LinkedHashSet<String> symbols;
    public final LinkedHashSet<String> nonterminals;
    public final LinkedHashSet<String> terminals;

    public GlrGrammar(Rule ... rules) {
        this(Arrays.stream(rules).toList());
    }
    public GlrGrammar(List<Rule> rules) {
        this.rules.addAll(rules);

        this.rulesForSymbol = new LinkedHashMap<>();
        for (Rule rule : this.rules) {
            rulesForSymbol.computeIfAbsent(rule.leftSymbol, o->new ArrayList<>()).add(this.rules.indexOf(rule));
        }
        this.symbols = new LinkedHashSet<>(allSymbols());
        this.symbols.add(GlrConsts.END_OF_TOKEN_LIST);
        this.nonterminals = this.rules.stream().map(o->o.leftSymbol).collect(Collectors.toCollection(LinkedHashSet::new));
        this.terminals = symbols.stream().filter(o->!nonterminals.contains(o)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<String> allSymbols() {
        List<String> set = new ArrayList<>();
        for (Rule rule : rules) {
            set.add(rule.leftSymbol);
            set.addAll(rule.rightSymbols);
        }
        return set;
    }
}
