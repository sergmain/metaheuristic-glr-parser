/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.token;

import ai.metaheuristic.glr.GlrConsts;
import ai.metaheuristic.glr.GlrTokenizer;
import ai.metaheuristic.glr.exceptions.GlrTokenizerException;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.regex.Pattern.*;

/**
 * @author Sergio Lissner
 * Date: 9/15/2022
 * Time: 9:58 PM
 */
public class GlrSimpleRegexTokenizer implements GlrTokenizer {
    private final Pattern re;
    private final LinkedHashSet<String> discardSymbols;
    private final LinkedHashSet<String> symbols;
    private final LinkedHashSet<String> allSymbols = new LinkedHashSet<>();

    public GlrSimpleRegexTokenizer(
            LinkedHashMap<String, String> symbolRegexDict, @Nullable List<String> discardSymbols) {
        this(symbolRegexDict, discardSymbols, CASE_INSENSITIVE | MULTILINE | UNICODE_CASE);
    }

    @SuppressWarnings("WeakerAccess")
    public GlrSimpleRegexTokenizer(
            LinkedHashMap<String, String> symbolRegexDict, @Nullable List<String> discardSymbols,
            int regex_flags) {
        List<String> patterns = new ArrayList<>();
        for (Map.Entry<String, String> entry : symbolRegexDict.entrySet()) {
            String symbol = entry.getKey();
            String regex = entry.getValue();
            if (regex.contains("(?<")) {
                throw new GlrTokenizerException(String.format("Invalid regex %s for symbol %s", regex, symbol));
            }
            patterns.add(String.format("(?<%s>%s)", symbol, regex));
        }
        this.re = Pattern.compile(String.join("|", patterns), regex_flags);

        this.discardSymbols = discardSymbols == null ? new LinkedHashSet<>() : new LinkedHashSet<>(discardSymbols);
        this.symbols = symbolRegexDict.keySet().stream().filter(o -> !this.discardSymbols.contains(o)).collect(Collectors.toCollection(LinkedHashSet::new));
        this.allSymbols.addAll(this.discardSymbols);
        this.allSymbols.addAll(this.symbols);
    }

    private Stream<String> getParserRulesKeys() {
        return allSymbols.stream();
    }

    public List<GlrToken> tokenize(String text) {
        List<GlrToken> items = new ArrayList<>();
        int pos = 0;

        while (true) {
            final String subText = text.substring(pos);
            Matcher m = re.matcher(subText);
            if (!m.find()) {
                break;
            }
            int currPos = pos;
            pos += m.end();

            String tokname = getParserRulesKeys().filter(name -> m.group(name) != null).findFirst().orElse(null);

            if (this.discardSymbols.contains(tokname)) {
                continue;
            }
            if (tokname == null) {
                throw new RuntimeException("Can't find any group, name: ");
            }
            String tokvalue;
            try {
                tokvalue = m.group(tokname);
            }
            catch (Exception e) {
                throw new RuntimeException("No such group: " + tokname, e);
            }
            items.add(new GlrToken(tokname, tokvalue, new GlrTextTokenPosition(currPos + m.start(), currPos + m.end()), tokvalue, null));
        }
        items.add(new GlrToken(GlrConsts.END_OF_TOKEN_LIST, "", new GlrTextTokenPosition(text.length(), -1), "", null));

        if (pos != text.length()) {
            String msg = String.format("tokenizer stopped at pos %d of %d in \"%s\" at \"%s\"",
                    pos, text.length(), text, "[" + text.substring(pos) + "]");
            throw new RuntimeException(msg);
        }
        return items;
    }
}
