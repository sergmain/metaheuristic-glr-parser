/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import javax.annotation.Nullable;

/**
 * @author Sergio Lissner
 * Date: 9/17/2022
 * Time: 4:58 PM
 */
public class GlrEnums {

    public enum Labels {
        gram("gram", true),
        reg_l_all("reg-l-all", false),
        reg_h_first("reg-h-first", false),
        reg_h_all("reg-h-all", false),
        agr_gnc("agr-gnc", true),
        agr_nc("agr-nc", true),
        agr_c("agr-c", true),
        agr_gn("agr-gn", true),
        agr_gc("agr-gc", true),
        regex("regex", true);

        public final String label;
        public boolean hasParam;

        Labels(String label, boolean hasParam) {
            this.label = label;
            this.hasParam = hasParam;
        }

        public static Labels fromName(String name) {
            Labels l = fromNameNullable(name);
            if (l==null) {
                throw new IllegalStateException("(l==null)");
            }
            return l;
        }

        @Nullable
        public static Labels fromNameNullable(String name) {
            for (Labels l : values()) {
                if (l.label.equals(name)) {
                    return l;
                }
            }
            return null;
        }
    }
}
