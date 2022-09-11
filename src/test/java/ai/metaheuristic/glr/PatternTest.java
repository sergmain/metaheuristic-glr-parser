/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 7:10 PM
 */
public class PatternTest {
    @Test
    public void test_() {
        String s = "123 aaa 456 bbb 789";
        Pattern p = Pattern.compile("(?<aaa>aaa)|(?<bbb>bbb)");

        Matcher m = p.matcher(s);
        while (m.find()) {
            System.out.println(m.group(0));
        }
    }
}
