/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import ai.metaheuristic.glr.glr.token.GlrTextToken;
import company.evo.jmorphy2.MorphAnalyzer;
import company.evo.jmorphy2.ParsedWord;
import company.evo.jmorphy2.ResourceFileLoader;
import lombok.SneakyThrows;
import javax.annotation.Nullable;

import java.util.*;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 7:52 PM
 */
public class GlrMorphologyLexer {

    private static final Map<String, String> TAG_MAPPER_1 = Map.of(
            "NOUN", "noun",
            "ADJF", "adj",
            "ADJS", "adj",
            "COMP", "adj",
            "VERB", "verb",
            "INFN", "verb",
            "PRTF", "pr",
            "PRTS", "pr",
            "GRND", "dpr",
            "NUMR", "num"
    );

    private static final Map<String, String> TAG_MAPPER_2 = Map.of(
            "ADVB", "adv",
            "NPRO", "pnoun",
            "PRED", "adv",
            "PREP", "prep",
            "CONJ", "conj",
            "PRCL", "prcl",
            "INTJ", "noun",
            "LATN", "lat",
            "NUMB", "num"
    );

    private static final Map<String, String> TAG_MAPPER = new HashMap<>();

    static {
        TAG_MAPPER.putAll(TAG_MAPPER_1);
        TAG_MAPPER.putAll(TAG_MAPPER_2);
    }

    public static final String DICT_PATH = "/company/evo/jmorphy2/ru/pymorphy2_dicts";

    private final GlrTokenizer tokenizer;
    private final MorphAnalyzer morph;
    private final LinkedHashMap<String, String> dictionary = new LinkedHashMap<>();

    @SneakyThrows
    public GlrMorphologyLexer(GlrTokenizer tokenizer, @Nullable LinkedHashMap<String, List<String>> dictionaries) {
        this.tokenizer = tokenizer;
        this.morph = new MorphAnalyzer.Builder().cacheSize(0).fileLoader(new ResourceFileLoader(DICT_PATH)).build();

        if (dictionaries!=null) {
            for (Map.Entry<String, List<String>> entry : dictionaries.entrySet()) {
                for (String val : entry.getValue()) {
                    String value = normal(val);
                    if (dictionary.containsKey(value)) {
                        throw new RuntimeException(String.format("Duplicate value in dictionaries %s and %s", entry.getKey(), dictionary.get(value)));
                    }
                    dictionary.put(value, entry.getKey());
                }
            }
        }
    }

    @SneakyThrows
    private String normal(String word) {
        List<ParsedWord> morphed = morph.parse(word);
        if (!morphed.isEmpty()) {
            return morphed.get(0).normalForm;
        }
        return word;
    }

    @SneakyThrows
    public List<GlrToken> scan(String text) {
        List<GlrToken> tokens = new ArrayList<>();

        for (GlrToken token : tokenizer.tokenize(text)) {
            if (token.getSymbol().equals("word")) {
                List<ParsedWord> morphed = morph.parse(token.getValue());
                if (!morphed.isEmpty()) {
                    final ParsedWord parsedWord = morphed.get(0);
                    String value = parsedWord.normalForm;
                    final String symbol;
                    if (this.dictionary.containsKey(value)) {
                        symbol = dictionary.get(value);
                    }
                    else {
                        final String tag = TAG_MAPPER.get(morphed.get(0).tag.POS.value);
                        symbol = tag != null ? tag : token.getSymbol();
                    }
                    tokens.add( new GlrTextToken(symbol, value, token.getPosition(), token.getInput_term(), parsedWord.tag));
                }
            }
            else {
                tokens.add(token);
            }
        }

        return tokens;
    }
}
