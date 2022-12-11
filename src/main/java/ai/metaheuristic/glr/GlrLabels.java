/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import ai.metaheuristic.glr.exceptions.GlrLabelRegexException;
import ai.metaheuristic.glr.token.GlrToken;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ai.metaheuristic.glr.GlrEnums.Labels;
import static ai.metaheuristic.glr.GlrEnums.Labels.*;

/**
 * @author Sergio Lissner
 * Date: 9/14/2022
 * Time: 8:47 PM
 */
public class GlrLabels {

    public record LabelCheck(@Nullable String value, List<GlrToken> tokens, int i) {}

    @SuppressWarnings("ObjectEquality")
    public static boolean agr_gnc_label(LabelCheck labelCheck) {
        if (labelCheck.value==null) {
            throw new IllegalStateException("(labelCheck.value==null)");
        }
        var one = labelCheck.tokens.get(labelCheck.i).params;
        var another = labelCheck.tokens.get(labelCheck.i + Integer.parseInt(labelCheck.value)).params;
        if (one==null || another==null) {
            return false;
        }
        return (one.Case==another.Case && one.Case!=null)
               && (one.gender==another.gender || one.gender==null || another.gender==null)
               && (one.number==another.number  || one.number==null || another.number==null);
    }

    private final static ConcurrentHashMap<String, Pattern> patterns = new ConcurrentHashMap<>();

    public static boolean regex_label(LabelCheck labelCheck) {
        if (labelCheck.value==null) {
            return false;
        }
        final String inputTerm = labelCheck.tokens.get(labelCheck.i).inputTerm;
        Matcher m = patterns.computeIfAbsent(labelCheck.value,
                GlrLabels::compilePattern).matcher(inputTerm);
        final boolean b = m.find();
        return b;
    }

    private static Pattern compilePattern(String regex) {
        final Pattern p = Pattern.compile(regex, Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
        if (p.matcher("").find() || p.matcher("  ").find()) {
            throw new GlrLabelRegexException("regex: " + regex + " find a positive match even empty string was provided");
        }
        return p;
    }

    public static boolean class_label(LabelCheck labelCheck) {
        if (labelCheck.value==null) {
            throw new IllegalStateException("(labelCheck.value==null)");
        }
        final GlrToken glrToken = labelCheck.tokens.get(labelCheck.i);
        final boolean b = glrToken.value.getClass().getSimpleName().equals(labelCheck.value);
        return b;
    }

    String py1 = """
        def gram_label(value, tokens, i):
            return value in tokens[i].params
        """;

    public static boolean gram_label(LabelCheck labelCheck) {
        final GlrToken glrToken = labelCheck.tokens.get(labelCheck.i);
        if (glrToken.params == null) {
            return false;
        }
        return glrToken.params.contains(labelCheck.value);
    }

/*
    String py99 = """
    LABELS_CHECK = {
        "gram": gram_label,
        "reg-l-all": reg_l_all_label,
        "reg-h-first": reg_h_first_label,
        "reg-h-all": reg_h_all_label,
        "agr-gnc": agr_gnc_label,
        "agr-nc": agr_nc_label,
        "agr-c": agr_c_label,
        "agr-gn": agr_gn_label,
        "agr-gc": agr_gc_label,
        "regex": regex_label
    }
    """;
*/



    public static final Map<String, Function<LabelCheck, Boolean>> LABELS_CHECK  = new HashMap<>(
            Map.of(
                    agr_gnc.label,  GlrLabels::agr_gnc_label,
                    regex.label,  GlrLabels::regex_label,
                    clazz.label,  GlrLabels::class_label,
                    gram.label,  GlrLabels::gram_label
            )
    );

    public static Map<String, List<Object>> parseLabel(String labelsStr1) {
        String str = labelsStr1.strip().replace(" ", "");
        Map<String, List<Object>> labels = new LinkedHashMap<>();
        List<Integer> idxs = collectIdxs(str);
        List<String> partss = splitToLabels(str, idxs);
        for (String s : partss) {
            int idx = s.indexOf('=');
            if (idx==-1) {
                extractedParamLess(labels, s);
            }
            else {
                String key = s.substring(0, idx);
                String value = s.substring(idx+1);
                final Labels label = Labels.fromName(key);
                if (!label.hasParam) {
                    throw new IllegalStateException("(!label.hasParam)");
                }
                labels.computeIfAbsent(label.label, (o)->new ArrayList<>()).add(value);
            }
        }
        return labels;
    }

    private static List<String> splitToLabels(String str, List<Integer> idxs) {
        List<String> result = new ArrayList<>();
        int lastIdx = 0;
        for (Integer idx : idxs) {
            result.add(str.substring(lastIdx, idx));
            lastIdx = idx +1;
        }
        result.add(str.substring(lastIdx));
        return result;
    }

    private static List<Integer> collectIdxs(String str) {
        Set<Integer> idxs = new HashSet<>();
        for (Labels value : values()) {
            collectIdxsForLabel(idxs, str, value);
        }
        final ArrayList<Integer> list = new ArrayList<>(idxs);
        list.sort(Comparator.naturalOrder());
        return list;
    }

    private static void collectIdxsForLabel(Set<Integer> idxs, String s, Labels label) {
        int idx = 0;
        String l = ","+label.label;
        while ((idx=s.indexOf(l, idx))!= -1) {
            idxs.add(idx);
            idx += l.length();
        }
    }

    private static void extractedParamLess(Map<String, List<Object>> labels, String s) {
        final Labels label = Labels.fromName(s);
        if (label.hasParam) {
            throw new IllegalStateException("(label.hasParam)");
        }
        labels.computeIfAbsent(label.label, (o)->new ArrayList<>());
    }
}

