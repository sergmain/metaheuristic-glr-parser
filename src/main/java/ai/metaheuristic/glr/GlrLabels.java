/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        var one = labelCheck.tokens.get(labelCheck.i).getParams();
        var another = labelCheck.tokens.get(labelCheck.i + Integer.parseInt(labelCheck.value)).getParams();
        if (one==null || another==null) {
            return false;
        }
        return (one.Case==another.Case && one.Case!=null)
               && (one.gender==another.gender || one.gender==null || another.gender==null)
               && (one.number==another.number  || one.number==null || another.number==null);
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
                    "agr-gnc",  GlrLabels::agr_gnc_label
            )
    );

}
