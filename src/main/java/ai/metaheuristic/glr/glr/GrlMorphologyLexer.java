/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import company.evo.jmorphy2.MorphAnalyzer;
import company.evo.jmorphy2.ParsedWord;
import company.evo.jmorphy2.ResourceFileLoader;
import lombok.SneakyThrows;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 7:52 PM
 */
public class GrlMorphologyLexer {

    public static final Map<String, String> TAG_MAPPER_1 = Map.of(
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

    public static final Map<String, String> TAG_MAPPER_2 = Map.of(
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

    public static final Map<String, String> TAG_MAPPER = new HashMap<>();

    static {
        TAG_MAPPER.putAll(TAG_MAPPER_1);
        TAG_MAPPER.putAll(TAG_MAPPER_2);
    }

    String py1 = """
    def __init__(self, tokenizer, dictionaries=None):
        self.tokenizer = tokenizer
        self.morph = pymorphy2.MorphAnalyzer()

        self.dictionary = {}
        if dictionaries:
            for category, values in dictionaries.items():
                for value in values:
                    value = self.normal(value)
                    if value in self.dictionary:
                        raise Exception('Duplicate value in dictionaries %s and %s' % (category, self.dictionary[value]))
                    self.dictionary[value] = category
    """;

    private static final String DICT_PATH = "/company/evo/jmorphy2/ru/pymorphy2_dicts";

    public final GrlTokenizer.WordTokenizer tokenizer;
    public final MorphAnalyzer morph;
    public LinkedHashMap<String, String> dictionary = new LinkedHashMap<>();

    @SneakyThrows
    public GrlMorphologyLexer(GrlTokenizer.WordTokenizer tokenizer, @Nullable LinkedHashMap<String, List<String>> dictionaries) {
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

    String py3 = """
    def normal(self, word):
        morphed = self.morph.parse(word)
        if morphed:
            return morphed[0].normal_form
        return word
    """;

    @SneakyThrows
    public String normal(String word) {
        List<ParsedWord> morphed = morph.parse(word);
        if (!morphed.isEmpty()) {
            return morphed.get(0).normalForm;
        }
        return word;
    }

    String py4 = """
    def scan(self, text):
        for token in self.tokenizer.scan(text):
            assert isinstance(token, Token)

            if token.symbol == 'word':
                morphed = self.morph.parse(token.value)
                if morphed:
                    value = morphed[0].normal_form
                    if value in self.dictionary:
                        token = Token(
                            symbol=self.dictionary[value],
                            value=value,
                            start=token.start,
                            end=token.end,
                            input_term=token.input_term,
                            params=morphed[0].tag
                        )
                    else:
                        token = Token(
                            symbol=self.TAG_MAPPER.get(morphed[0].tag.POS) or token.symbol,
                            value=value,
                            start=token.start,
                            end=token.end,
                            input_term=token.input_term,
                            params=morphed[0].tag
                        )
            yield token
    """;

    @SneakyThrows
    public List<GrlTokenizer.Token> scan(String text) {
        List<GrlTokenizer.Token> tokens = new ArrayList<>();

        for (GrlTokenizer.Token token : tokenizer.scan(text)) {
            if (token.symbol.equals("word")) {
                List<ParsedWord> morphed = morph.parse(token.value);
                if (!morphed.isEmpty()) {
                    final ParsedWord parsedWord = morphed.get(0);
                    String value = parsedWord.normalForm;
                    final String symbol;
                    if (this.dictionary.containsKey(value)) {
                        symbol = dictionary.get(value);
                    }
                    else {
                        final String tag = TAG_MAPPER.get(morphed.get(0).tag.POS.value);
                        symbol = tag != null ? tag : token.symbol;
                    }
                    tokens.add( new GrlTokenizer.Token(symbol, value, token.start, token.end, token.input_term, parsedWord.tag));
                }
            }
        }

        return tokens;
    }

    String py5 = """
    def parse_tags(self, word):
        parsed = self.morph.parse(word)
        if not parsed:
            return None
        return parsed[0].tag
    """;

}
