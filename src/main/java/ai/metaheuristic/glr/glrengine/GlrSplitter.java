/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import java.util.regex.Pattern;

/**
 * @author Serge
 * Date: 2/22/2022
 * Time: 7:06 PM
 */
public class GlrSplitter {

    public static Pattern SPLIT_PATTERN = Pattern.compile("[!?;.\\\\]+ ", Pattern.DOTALL | Pattern.UNICODE_CASE| Pattern.CASE_INSENSITIVE);
    public static Pattern CLEAN_PATTERN = Pattern.compile("[^[\\p{L}\\p{Digit}_]+\\d\\s\\-\\n\\.\\(\\)\\{\\}\\[\\]\\\"\\'«»`%,:_]", Pattern.DOTALL | Pattern.UNICODE_CASE| Pattern.CASE_INSENSITIVE);


    public static String clear(String text) {
        // удалить все символы из текста, кроме тех, которые точно разберет парсер
        text = CLEAN_PATTERN.matcher(text).replaceAll(" ");
        return text;
    }

    public static String[] process(String text) {
        String t = clear(text);
        return SPLIT_PATTERN.split(t);
    }
}
