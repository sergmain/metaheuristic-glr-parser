/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.some;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 7:10 PM
 */
public class PatternTest {
    @Test
    public void test_99() {
        String s = "123 aaa 456 bbb 789";
        Pattern p = Pattern.compile("(?<aaa>aaa)|(?<bbb>bbb)");

        Matcher m = p.matcher(s);
        List<String> ss = new ArrayList<>();
        while (m.find()) {
            ss.add(m.group(0));
        }
        ss.forEach(System.out::println);
        assertTrue(ss.contains("aaa"));
        assertTrue(ss.contains("bbb"));
    }
}
