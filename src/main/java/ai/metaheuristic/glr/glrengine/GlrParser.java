/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import org.apache.commons.lang3.NotImplementedException;

import java.util.*;

import static ai.metaheuristic.glr.glrengine.GlrNormalizer.NormalizedToken;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 9:47 PM
 */
@SuppressWarnings({"PyUnresolvedReferences", "unused"})
public class GlrParser {
    String py1 = """
        lr_grammar_scanner = make_scanner(
            sep='=',
            alt='[|]',
            word=r"\\b\\w+\\b",
            raw=r"\\'\\w+?\\'",
            whitespace=r'[ \\t\\r\\n]+',
            minus=r'[-]',
            label=r'\\<.+?\\>',
            discard_names=('whitespace',)
        )
        """;

    public static final GlrScanner lr_grammar_scanner;

    static {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("sep", "=");
        map.put("alt", "[|]");
        map.put("word", "\\b[\\p{L}\\p{Digit}_]+\\b");
        map.put("raw", "'[\\p{L}\\p{Digit}_]+'");
        map.put("whitespace", "[ \\t\\r\\n]+");
        map.put("minus", "[-]");
        map.put("label", "\\<.+?\\>");
        final LinkedHashSet<String> discard_names = new LinkedHashSet<>();
        discard_names.add("whitespace");
        Glr.ParserRules rules = new Glr.ParserRules(map, discard_names, null);
        lr_grammar_scanner = new GlrScanner(rules);
    }

    String py2 = """
        def make_rules(start, grammar, kw):
            words = [start]
            labels = []
            edit_rule = '@'
            edit_rule_commit = True
            next_edit_rule_commit = True
            kw.add(edit_rule)
            for tokname, tokvalue, tokpos in lr_grammar_scanner(grammar):
                if tokname == 'minus':
                    next_edit_rule_commit = False
                if tokname == 'word' or tokname == 'raw':
                    words.append(tokvalue)
                    labels.append(None)
                    kw.add(tokvalue)
                elif tokname == 'alt':
                    yield (edit_rule, tuple(words), edit_rule_commit, labels[1:-1])
                    words = []
                    labels = []
                elif tokname == 'sep':
                    tmp = words.pop()
                    yield (edit_rule, tuple(words), edit_rule_commit, labels[1:-1])
                    edit_rule_commit = next_edit_rule_commit
                    next_edit_rule_commit = True
                    edit_rule = tmp
                    words = []
                    labels = [None]
                elif tokname == 'label':
                    # "a=b, b=c, d" -> {"a": "b", "b": "c", "d": None}
                    tokvalue = tokvalue.strip().replace(" ", "")
                    label = defaultdict(list)
                    for l in tokvalue[1:-1].split(","):
                        key, value = tuple(l.split("=", 1) + [None])[:2]
                        label[key].append(value)
                    # label = dict([tuple(l.split("=", 1) + [None])[:2] for l in tokvalue[1:-1].split(",")])
                    labels[-1] = label
            yield (edit_rule, tuple(words), edit_rule_commit, labels[1:-1])
        """;

    public record ParsedRule(
            String rulename, List<String> words, boolean commit, List<Map<String, List<String>>> labels) {}

    public static List<ParsedRule> make_rules(String start, String grammar, Set<String> kw) {
        LinkedList<String> words =new LinkedList<>(List.of(start));
        LinkedList<Map<String, List<String>>> labels = new LinkedList<>();
        String edit_rule = "@";
        boolean edit_rule_commit = true;
        boolean next_edit_rule_commit = true;
        kw.add(edit_rule);
        List<ParsedRule> result = new ArrayList<>();

        for (NormalizedToken item : lr_grammar_scanner.process(grammar)) {
            switch (item.tokname) {
                case "minus" -> next_edit_rule_commit = false;
                case "word", "raw" -> {
                    words.add(item.tokvalue);
                    kw.add(item.tokvalue);
                }
                case "alt" -> {
                    result.add(new ParsedRule(edit_rule,
                            new ArrayList<>(words),
                            edit_rule_commit,
                            new ArrayList<>(labels.subList(0, labels.size()))));
                    words.clear();
                    labels.clear();
                }
                case "sep" -> {
                    String tmp = words.removeLast();
                    result.add(new ParsedRule(edit_rule,
                            new ArrayList<>(words),
                            edit_rule_commit,
                            new ArrayList<>(labels.subList(0, labels.size()))));
                    edit_rule_commit = next_edit_rule_commit;
                    next_edit_rule_commit = true;
                    edit_rule = tmp;
                    words.clear();
                    labels.clear();
                }
                case "label" -> {
                    // "a=b, b=c, d" ->{"a":"b", "b":"c", "d":None }
                    String tokvalue = item.tokvalue.strip().replace(" ", "");
                    LinkedHashMap<String, List<String>> label = new LinkedHashMap<>();
                    for (String l : tokvalue.substring(1, tokvalue.length() - 1).split(",")) {
                        String[] split = l.split("=", 2);
                        String key, value;
                        if (split.length > 1) {
                            key = split[0];
                            value = split[1];
                        }
                        else {
                            key = "";
                            value = null;
                        }
                        label.computeIfAbsent(key, (o)->new ArrayList<>()).add(value);
                    }
                    // label = dict([tuple(l.split("=", 1) +[None])[:2]for l in tokvalue[1:-1].split(",")])
                    labels.add(label);
                }
            }
        }
        result.add( new ParsedRule(edit_rule,
                new ArrayList<>(words),
                edit_rule_commit,
                new ArrayList<>(labels.subList(0, labels.size()))));

        return result;
    }

    public static class RuleSetMap {
        public final LinkedHashMap<String, Set<Integer>> byName = new LinkedHashMap<>();
        public final LinkedHashMap<Integer, ParsedRule> byIndex = new LinkedHashMap<>();

        public void clear() {
            byName.clear();
            byIndex.clear();
        }
    }

    @SuppressWarnings({"ConstantConditions", "unused"})
    public static class RuleSet extends RuleSetMap {
        public int names_count=0;
        public int rules_count=0;
        public LinkedHashMap<Integer, List<Map<String, List<String>>>> labels = new LinkedHashMap<>();

        public RuleSet(List<ParsedRule> rules) {
            init(rules);
        }

        String py6 = """
        def init(self, rules):
            epsilons = self.fill(rules)
            must_cleanup = False
            while epsilons:
                eps = epsilons.pop()
                if eps in self:
                    # Rule produces something and has an epsilon alternative
                    self.add_epsilon_free(eps, epsilons)
                else:
                    must_cleanup |= self.remove_epsilon(eps, epsilons)
            if must_cleanup:
                rules = sorted(self[i] for i in range(self.rules_count) if self[i] is not None)
                epsilons = self.fill(rules)
                if epsilons:
                    #print "D'oh ! I left epsilon rules in there !", epsilons
                    raise Exception("There is a bug ! There is a bug ! " +
                                    "Failed to refactor this grammar into " +
                                    "an epsilon-free one !")
        """;

        public void init(List<ParsedRule> rules) {
            LinkedList<String> epsilons = fill(rules);
            boolean must_cleanup = false;
            while (!epsilons.isEmpty()) {
                String eps = epsilons.removeFirst();
                if (this.byName.containsKey(eps)) {
                    // # Rule produces something and has an epsilon alternative
                    add_epsilon_free(eps, epsilons);
                }
                else {
                    must_cleanup |= remove_epsilon(eps, epsilons);
                }
            }
            if (must_cleanup) {

            }
        }

        String py7 = """
        def fill(self, rules):
            self.names_count = 0
            self.rules_count = 0
            self.clear()
            epsilons = set()
            for rulename, elems, commit, labels in rules:
                if len(elems) > 0:
                    self.add(rulename, elems, commit, labels)
                else:
                    epsilons.add(rulename)
            #print 'found epsilon rules', epsilons
            return epsilons
        """;

        public LinkedList<String> fill(List<ParsedRule> rules) {
            names_count = 0;
            rules_count = 0;
            this.clear();
            LinkedHashSet<String> epsilons = new LinkedHashSet<>();
            for (ParsedRule rule : rules) {
                if (!rule.words.isEmpty()) {
                    this.add(rule.rulename, rule.words, rule.commit, rule.labels);
                }
                else {
                    epsilons.add(rule.rulename);
                }
            }
            // #print 'found epsilon rules', epsilons
            return new LinkedList<>(epsilons);
        }

        String py8 = """
        def add(self, rulename, elems, commit, labels):
            if rulename not in self:
                self.names_count += 1
                self[rulename] = set()
            rule = (rulename, elems, commit)
            if rule not in (self[i] for i in self[rulename]):
                self[rulename].add(self.rules_count)
                self[self.rules_count] = rule
                self.labels[self.rules_count] = labels
                self.rules_count += 1
        """;

        public void add(String rulename, List<String> elems, boolean commit, List<Map<String, List<String>>> labels) {
            if (!this.byName.containsKey(rulename)) {
                ++names_count;
                this.byName.computeIfAbsent(rulename, (o)->new LinkedHashSet<>());
            }
            ParsedRule rule = new ParsedRule(rulename, elems, commit, labels);
            if (!this.byIndex.containsValue(rule)) {
                this.byName.computeIfAbsent(rulename, (o)->new LinkedHashSet<>()).add(rules_count);
                this.byIndex.put(rules_count, rule);
                this.labels.put(rules_count, labels);
                ++rules_count;
            }
        }

        String py81 = """
            def add_epsilon_free(self, eps, epsilons):
                #print "Adding", eps, "-free variants"
                i = 0
                while i < self.rules_count:
                    if self[i] is None:
                        i += 1
                        continue
                    rulename, elems, commit = self[i]
                    if eps in elems:
                        #print "... to", rulename, elems
                        E = set([elems])
                        old = 0
                        while len(E) != old:
                            old = len(E)
                            E = self.union_elements(E, eps)
                        #print "Created variants", E
                        for elems in E:
                            if len(elems) == 0:
                                #print "got new epsilon rule", rulename
                                epsilons.add(rulename)
                            else:
                                self.add(rulename, elems, commit, [])
                                #
                                #
                    i += 1
                    #
            """;

        public void add_epsilon_free(String eps, LinkedList<String> epsilons) {
            if (true) {
                throw new NotImplementedException("not yet");
            }

            //#print "Adding", eps, "-free variants"
            int i = 0;
            while (i<rules_count) {
                final ParsedRule rule = this.byIndex.get(i);
                if (rule == null) {
                    ++i;
                    continue;
                }
                if (rule.words.contains(eps)) {
                    // #print "... to", rulename, elems
                    LinkedHashSet<List<String>> E = new LinkedHashSet<>();
                    E.add(rule.words);
                    int old = 0;
                    while (E.size()!=old) {
                        old = E.size();
                        E = union_elements(E, eps);
                    }
                    // #print "Created variants", E
                    for (List<String> s : E) {

                    }
                }
                ++i;
            }

        }

        String py811 = """
            @staticmethod
            def union_elements(E, eps):
                print("union before: ", E, eps)
                U = E.union(elems[:i] + elems[i + 1:]
                               for elems in E
                               for i in range(len(elems))
                               if elems[i] == eps)
                print("union after: ", U)
                return U
            """;
        public static LinkedHashSet<List<String>> union_elements(LinkedHashSet<List<String>> E, String eps) {
            throw new NotImplementedException("not yet");
        }

        String py82 = """
            def remove_epsilon(self, eps, epsilons):
                must_cleanup = False
                i = 0
                while i < self.rules_count:
                    if self[i] is None:
                        i += 1
                        continue
                    rulename, elems, commit = self[i]
                    if eps in elems:
                        elems = tuple(e for e in elems if e != eps)
                        if len(elems) == 0:
                            # yet another epsilon :/
                            self[i] = None
                            self[rulename].remove(i)
                            if not self[rulename]:
                                del self[rulename]
                            must_cleanup = True
                            epsilons.add(rulename)
                            #print "epsilon removal created new epsilon rule", rulename
                        else:
                            self[i] = (rulename, elems, commit)
                            #
                    i += 1
                return must_cleanup
            """;

        public static boolean remove_epsilon(String eps, LinkedList<String> epsilons) {
            if (true) {
                throw new NotImplementedException("not yet");
            }
            return false;
        }
    }

    @SuppressWarnings("unused")
    public static class Parser {
        String py5 = """
            def __init__(self, start_sym, grammar, scanner_kw=[]):
                self.kw_set = set(scanner_kw)
                self.kw_set.add('$')
                self.R = RuleSet(make_rules(start_sym, grammar, self.kw_set))
                self.I = set((r, i) for r in range(self.R.rules_count)
                             for i in range(len(self.R[r][1]) + 1))
                self.precompute_next_items()
                self.compute_lr0()
                self.LR0 = list(sorted(self.LR0))
                self.LR0_idx = {}
                for i, s in enumerate(self.LR0):
                    self.LR0_idx[s] = i
                self.initial_state = self.index(self.initial_items)
                self.compute_ACTION()
                        
            """;
        public LinkedHashSet<String> kw_set = new LinkedHashSet<>();
        public RuleSet R;

        public Parser(String start_sym, String grammar, LinkedHashSet<String> scanner_kw) {
            kw_set.addAll(scanner_kw);
            kw_set.add("$");
            R = new RuleSet(make_rules(start_sym, grammar, kw_set));
        }
    }
}
