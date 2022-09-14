/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

/**
 * @author Sergio Lissner
 * Date: 9/7/2022
 * Time: 11:41 PM
 */
public class GlrConsts {

    public static final String DEFUALT_START = "S";
    public static final String SIMPLE_GRAMMAR = """
        
        S = adj<agr-gnc=1> CLOTHES
        S = CLOTHES adj<agr-gnc=-1>
        """;
}
