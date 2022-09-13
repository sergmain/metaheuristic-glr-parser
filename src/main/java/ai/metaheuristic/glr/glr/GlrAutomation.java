/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:19 PM
 */
public class GlrAutomation {

    String py1 = """
    def __init__(self, grammar_text, dictionaries=None, start='S'):
        self.tokenizer = WordTokenizer()
        self.lexer = MorphologyLexer(self.tokenizer, dictionaries)
        self.grammar_parser = GrammarParser()

        self.grammar = self.grammar_parser.parse(grammar_text, start)
        self.parser = Parser(self.grammar)

    """;
    public final GrlTokenizer.WordTokenizer tokenizer;
    public final GrlMorphologyLexer lexer;
    public final GlrGrammarParser grammar_parser;
    public final GlrParser parser;
    public final GlrGrammar grammar;

    public GlrAutomation(String grammar_text, @Nullable LinkedHashMap<String, List<String>> dictionaries) {
        this(grammar_text, dictionaries, "S");
    }

    public GlrAutomation(String grammar_text, @Nullable LinkedHashMap<String, List<String>> dictionaries, String start) {
        this.tokenizer = new GrlTokenizer.WordTokenizer();
        this.lexer = new GrlMorphologyLexer(tokenizer, dictionaries);
        this.grammar_parser = new GlrGrammarParser();

        this.grammar = GlrGrammarParser.parse(grammar_text, start);
        this.parser = new GlrParser(grammar);
    }


    String py2 = """
    def parse(self, text, full_math=False):
        def validator(syntax_tree):
            rule = self.grammar[syntax_tree.rule_index]
            tokens = [child.token for child in syntax_tree.children]
            for i, token in enumerate(tokens):
                params = rule.params[i]
                for label_key, label_values in params.items():
                    for label_value in label_values:
                        ok = LABELS_CHECK[label_key](label_value, tokens, i)
                        if not ok:
                            #print 'Label failed: %s=%s for #%s in %s' % (label_key, label_value, i, tokens)
                            return False
            return True

        tokens = list(self.lexer.scan(text))

        return self.parser.parse(tokens, full_math, validator)
    """;
}


