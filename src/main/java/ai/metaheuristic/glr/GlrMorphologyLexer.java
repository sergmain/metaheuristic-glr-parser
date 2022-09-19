/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.token.GlrToken;
import company.evo.jmorphy2.Grammeme;
import company.evo.jmorphy2.MorphAnalyzer;
import company.evo.jmorphy2.ParsedWord;
import company.evo.jmorphy2.ResourceFileLoader;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 7:52 PM
 */
public class GlrMorphologyLexer {

    public static final String DICT_PATH = "/company/evo/jmorphy2/ru/pymorphy2_dicts";

    private final MorphAnalyzer morph;
    private final LinkedHashMap<String, String> dictionary = new LinkedHashMap<>();

    @SneakyThrows
    public GlrMorphologyLexer(@Nullable LinkedHashMap<String, List<String>> dictionaries) {
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
    public List<GlrToken> initMorphology(
            List<GlrToken> tokensOrigin,
            Function<String, String> mappingFunc
            ) {
        if (tokensOrigin.isEmpty()) {
            return List.of();
        }

        List<GlrToken> tokens;
        if (!tokensOrigin.get(tokensOrigin.size()-1).symbol.equals(GlrConsts.END_OF_TOKEN_LIST)) {
            tokens = new ArrayList<>(tokensOrigin.size() + 10);
            tokens.addAll(tokensOrigin);
            tokens.add(new GlrToken(GlrConsts.END_OF_TOKEN_LIST));
        }
        else {
            tokens = new ArrayList<>(tokensOrigin);
        }

        List<GlrToken> result = new ArrayList<>();

        for (GlrToken token : tokens) {
            if (token.symbol.equals("word")) {
                if (!(token.value instanceof String strValue)) {
                    result.add( new GlrToken(token.symbol, token.value, token.position, token.value.toString(), null) );
                }
                else {
                    List<ParsedWord> morphed = morph.parse(strValue);
                    if (!morphed.isEmpty()) {
                        final ParsedWord parsedWord = morphed.get(0);
                        String value = parsedWord.normalForm;
                        final String symbol;
                        if (this.dictionary.containsKey(value)) {
                            symbol = dictionary.get(value);
                        }
                        else {
                            final Grammeme pos = morphed.get(0).tag.POS;
                            final String tag = pos != null ? mappingFunc.apply(pos.value) : null;
                            symbol = tag != null ? tag : token.symbol;
                        }
                        result.add(new GlrToken(symbol, value, token.position, token.value.toString(), parsedWord.tag));
                    }
                    else {
                        throw new IllegalStateException("morph didn't find word " + token.value);
                    }
                }
            }
            else if ((token.symbol.equals("class"))) {
                String symbol = "word";
                String value = "<class="+token.value.getClass().getSimpleName()+'>';
                result.add( new GlrToken(symbol, token.value, token.position, value, null) );
            }
            else {
                if (!(token.value instanceof String strValue)) {
                    throw new IllegalStateException("(!(token.getValue() instanceof String strValue))");
                }
                result.add( new GlrToken(token.symbol, strValue, token.position, token.value.toString(), null));
            }
        }

        return result;
    }
}
