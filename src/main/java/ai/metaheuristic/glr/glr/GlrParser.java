/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 10:47 PM
 */
public class GlrParser {
    String py1 = """
    def __init__(self, grammar, log_level=0):
        assert isinstance(grammar, Grammar)
        self.grammar = grammar
        self.action_goto_table = generate_action_goto_table(self.grammar)
        self.log_level = log_level

    """;

    public final GlrGrammar grammar;
    private final int log_level;
    public final List<LinkedHashMap<String, List<GlrLr.Action>>> action_goto_table;

    public GlrParser(GlrGrammar grammar) {
        this(grammar, 0);
    }

    public GlrParser(GlrGrammar grammar, int log_level) {
        this.grammar = grammar;
        this.log_level = log_level;
        this.action_goto_table = GlrLr.generate_action_goto_table(grammar);
    }
}
