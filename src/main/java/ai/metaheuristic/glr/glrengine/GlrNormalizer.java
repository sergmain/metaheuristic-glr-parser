/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import company.evo.jmorphy2.MorphAnalyzer;
import company.evo.jmorphy2.ParsedWord;
import company.evo.jmorphy2.ResourceFileLoader;
import company.evo.jmorphy2.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 3:15 PM
 */
public class GlrNormalizer {
    String py = """
        TAG_MAPPER = {
        "NOUN": "noun",
        "ADJF": "adj",
        "ADJS": "adj",
        "COMP": "adj",
        "VERB": "verb",
        "INFN": "verb",
        "PRTF": "pr",
        "PRTS": "pr",
        "GRND": "dpr",
        "NUMR": "num",
        "ADVB": "adv",
        "NPRO": "pnoun",
        "PRED": "adv",
        "PREP": "prep",
        "CONJ": "conj",
        "PRCL": "prcl",
        "INTJ": "noun",
        "LATN": "lat",
        "NUMB": "num"
    }

    def __init__(self):
        self.morph = pymorphy2.MorphAnalyzer()

    def __call__(self, tokens):
        results = []
        for token in tokens:
            tokname, tokvalue, tokpos = token
            orig_tokvalue = tokvalue
            tokparams = []
            if tokname == "word":
                morphed = self.morph.parse(tokvalue)
                if morphed:
                    tokvalue = morphed[0].normal_form
                    tokname = self.TAG_MAPPER.get(morphed[0].tag.POS) or tokname
                    # tokparams = unicode(morphed[0].tag).lower().split(",")
                    tokparams = morphed[0].tag
                    # print tokname, tokvalue, tokpos, tokparams, orig_tokvalue
            results.append((tokname, tokvalue, tokpos, tokparams, orig_tokvalue))
        return results

    def normal(self, word):
        morphed = self.morph.parse(word)
        if morphed:
            return morphed[0].normal_form
        return word

    def parse_tags(self, word):
        parsed = self.morph.parse(word)
        if not parsed:
            return None
        return parsed[0].tag

    """;

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

    private static final String DICT_PATH = "/company/evo/jmorphy2/ru/pymorphy2_dicts";

    public final MorphAnalyzer morphAnalyzer;

    @SneakyThrows
    public GlrNormalizer() {
        this.morphAnalyzer = new MorphAnalyzer.Builder().cacheSize(0).fileLoader(new ResourceFileLoader(DICT_PATH)).build();
    }

    @RequiredArgsConstructor
    public static class NormalizedToken {
        public final String tokname;
        public final String tokvalue;
        public final int tokpos;
        @Nullable
        public final Tag tokparams;
        @Nullable
        public final String orig_tokvalue;

        public NormalizedToken(String tokname, String tokvalue, int tokpos) {
            this.tokname = tokname;
            this.tokvalue = tokvalue;
            this.tokpos = tokpos;
            this.tokparams = null;
            this.orig_tokvalue = null;
        }
    }

    @SneakyThrows
    public List<NormalizedToken> process(List<NormalizedToken> tokens) {
        String s = """
            def __call__(self, tokens):
                results = []
                for token in tokens:
                    tokname, tokvalue, tokpos = token
                    orig_tokvalue = tokvalue
                    tokparams = []
                    if tokname == "word":
                        morphed = self.morph.parse(tokvalue)
                        if morphed:
                            tokvalue = morphed[0].normal_form
                            tokname = self.TAG_MAPPER.get(morphed[0].tag.POS) or tokname
                            # tokparams = unicode(morphed[0].tag).lower().split(",")
                            tokparams = morphed[0].tag
                            # print tokname, tokvalue, tokpos, tokparams, orig_tokvalue
                    results.append((tokname, tokvalue, tokpos, tokparams, orig_tokvalue))
                return results
            """;

        List<NormalizedToken> results = new ArrayList<>();
        for (NormalizedToken token : tokens) {
            String tokname = token.tokname;
            String tokvalue = token.tokvalue;
            int tokpos = token.tokpos;

            String orig_tokvalue = tokvalue;
            Tag tokparams = null;
            if ("word".equals(tokname)) {
                List<ParsedWord> morphed = morphAnalyzer.parse(tokvalue);
                if (!morphed.isEmpty()) {
                    tokvalue = morphed.get(0).normalForm;
                    final String tag = TAG_MAPPER.get(morphed.get(0).tag.POS.value);
                    tokname = tag !=null ? tag : tokname;
                    tokparams = morphed.get(0).tag;
                }

            }
            results.add(new NormalizedToken(tokname, tokvalue, tokpos, tokparams, orig_tokvalue));
        }
        return results;
    }

    @SneakyThrows
    public String normal(String word) {
        List<ParsedWord> morphed = morphAnalyzer.parse(word);
        if (!morphed.isEmpty()) {
            return morphed.get(0).normalForm;
        }
        return word;
    }

    @SneakyThrows
    @Nullable
    public Tag parse_tags(String word) {
        List<ParsedWord> parsed = morphAnalyzer.parse(word);
        if (parsed.isEmpty()) {
            return null;
        }
        return parsed.get(0).tag;
    }
}
