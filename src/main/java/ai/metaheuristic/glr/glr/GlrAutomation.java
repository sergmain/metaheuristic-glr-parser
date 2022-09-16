/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.glr.token.GlrTextToken;
import ai.metaheuristic.glr.glr.token.GlrWordTokenizer;
import javax.annotation.Nullable;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final GlrMorphologyLexer lexer;
    private final GlrParser parser;
    private final GlrGrammar grammar;

    public GlrAutomation(GlrTokenizer glrTokenizer, String grammar_text, @Nullable LinkedHashMap<String, List<String>> dictionaries, String start) {
        this.lexer = new GlrMorphologyLexer(glrTokenizer, dictionaries);
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

    public static boolean validator(GlrGrammar grammar, GlrStack.SyntaxTree syntax_tree) {
        if (syntax_tree.rule_index()==null) {
            return true;
        }
        GlrGrammar.Rule rule = grammar.rules.get(syntax_tree.rule_index());
        List<GlrTextToken> tokens = syntax_tree.children().stream()
                .map(GlrStack.SyntaxTree::token)
                .filter(Objects::nonNull).toList();
        for (int i = 0; i < tokens.size(); i++) {
            GlrTextToken token = tokens.get(i);
            if (rule.params()==null) {
                continue;
            }
            Map<String, List<Object>> params = rule.params().get(i);
            for (Map.Entry<String, List<Object>> entry : params.entrySet()) {
                String label_key = entry.getKey();
                List<Object> label_values = entry.getValue();
                for (Object label_value : label_values) {
                    if (!(label_value instanceof String label_value_str)) {
                        throw new IllegalStateException("(!(label_value instanceof String))");
                    }
                    GlrLabels.LabelCheck labelCheck = new GlrLabels.LabelCheck(label_value_str, tokens, i);
                    boolean ok = GlrLabels.LABELS_CHECK.getOrDefault(label_key, (v)->false).apply(labelCheck);
                    if (!ok) {
                        // #print 'Label failed: %s=%s for #%s in %s' % (label_key, label_value, i, tokens)
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<GlrStack.SyntaxTree> parse(String text) {
        return parse(text, false);
    }

    public List<GlrStack.SyntaxTree> parse(String text, boolean full_math) {
        List<GlrTextToken> tokens = lexer.scan(text);

        return parser.parse(tokens, full_math, (syntaxTree) -> validator(grammar, syntaxTree));
    }
}


