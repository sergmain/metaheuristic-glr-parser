/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ai.metaheuristic.glr.glrengine.GlrNormalizer.*;
import static java.util.regex.Pattern.*;

/**
 * @author Serge
 * Date: 2/22/2022
 * Time: 7:44 PM
 */
@SuppressWarnings("PyUnresolvedReferences")
public class GlrScanner {

    public static final int DEFAULT_FLAGS = CASE_INSENSITIVE | MULTILINE | UNICODE_CASE;

    public record LineColumn(int line, int column ) {}

    public static Pattern check_groups = Pattern.compile("[(][?]P=(\\w+)[)]");

    public Pattern re = Pattern.compile("", DEFAULT_FLAGS);

    public Map<String, String> tokens = new HashMap<>();
    public Map<String, String> state_enter = new HashMap<>();
    public Map<String, String> state_leave = new HashMap<>();
    public Map<String, Map<String, Set<String>>> state_discard = new HashMap<>();
    public final Glr.ParserRules parserRules;

    public Set<String> discard_names = new HashSet<>();
    public Set<String> discard_values = new HashSet<>();

    public Stream<String> getParserRulesKeys() {
        return parserRules.parser_rules.keySet().stream().sorted(Comparator.reverseOrder());
    }

    String py0 = """
        def __call__(self, text):
            ""\"
                Iteratively scans through text and yield each token
            ""\"
            pos = 0
            states = [None]
            while True:
                m = self.re.match(text, pos)
                if not m:
                    break

                pos = m.end()
                tokname = m.lastgroup

                try:
                    tokvalue = m.group(tokname)
                except:
                    print("No such group", tokname)

                tokpos = m.start()
                if tokname in self.state_leave and states[-1] == self.state_leave[tokname]:
                    states.pop()

                if tokname in self.state_enter:
                    states.append(self.state_enter[tokname])

                if self.must_publish_token(states[-1], tokname, tokvalue):
                    yield tokname, tokvalue, tokpos

            if pos != len(text):
                msg = 'tokenizer stopped at pos %r of %r in "%s" at "%s"' % (pos, len(text), text, text[pos:pos + 3])
                print(msg)
                raise ScannerException(msg)
                        
        """;

    public List<NormalizedToken> process(String text) {
        List<NormalizedToken> items = new ArrayList<>();
        int pos = 0;
        LinkedList<String> states = new LinkedList<>();
        states.add(null);

        while(true) {
            Matcher m = re.matcher(text.substring(pos));
            if (!m.find()) {
                break;
            }
            pos += m.end();


            String tokname = getParserRulesKeys().filter(name -> m.group(name)!=null).findFirst().orElse(null);;
            if (tokname==null) {
                throw new RuntimeException("Can't find any group");
            }
            String tokvalue;
            try {
                tokvalue = m.group(tokname);
            }
            catch (Exception e) {
                throw new RuntimeException("No such group: " + tokname, e);
            }
            int tokpos = m.start();
            if (state_leave.containsKey(tokname) && states.get(states.size()-1).equals(state_leave.get(tokname))) {
                states.pop();
            }
            if (state_enter.containsKey(tokname)) {
                states.add(state_enter.get(tokname));
            }
            if (must_publish_token(states.get(states.size()-1), tokname, tokvalue)) {
                items.add( new NormalizedToken(tokname,tokvalue, tokpos));
            }

        }
        if (pos != text.length()) {
            String msg = String.format("tokenizer stopped at pos %d of %d in \"%s\" at \"%s\"",
                    pos, text.length(), text, text.substring(pos));
            throw new RuntimeException(msg);
        }
        return items;
    }

    String py11 = """
        def must_publish_token(self, state, tokname, tokvalue):
            return (tokname not in self.state_discard[state]['discard_names']
                    and
                    tokvalue not in self.state_discard[state]['discard_values'])
        """;

    public boolean must_publish_token(String state, String tokname, String tokvalue) {
        final Map<String, Set<String>> stringSetMap = state_discard.get(state);

        return !mapContains(stringSetMap, "discard_names", tokname) &&
               !mapContains(stringSetMap, "discard_values", tokname);
    }

    public static boolean mapContains(Map<String, Set<String>> map, String key, String lookingFor) {
        if (map.isEmpty()) {
            return false;
        }
        final Set<String> set = map.get(key);
        if (set==null) {
            return false;
        }
        return set.contains(lookingFor);
    }

    String py1 ="""
        self.re = re.compile('', re.M | re.U | re.I)
        self.tokens = {}
        self.state_enter = {}
        self.state_leave = {}
        self.state_discard = {None: {'discard_names': set(),
                                     'discard_values': set()}}
        #self.discard_names = set()
        #self.discard_values = set()
        self.add(**tokens)
        """;
    public GlrScanner(Glr.ParserRules parserRules) {
        this.parserRules = parserRules;
        final HashMap<String, Set<String>> map = new HashMap<>();
        state_discard.put(null, map);
        map.put("discard_names", new HashSet<>());
        map.put("discard_values", new HashSet<>());

        add(parserRules);
    }

    public String py2 = """
            def add(self, **tokens):
                ""\"
                    Each named keyword is a token type and its value is the
                    corresponding regular expression. Returns a function that iterates
                    tokens in the form (type, value) over a string.

                    Special keywords are discard_names and discard_values, which specify
                    lists (actually any iterable is accepted) containing tokens names or
                    values that must be discarded from the scanner output.
                ""\"
                for d in ('discard_values', 'discard_names'):
                    if d in tokens:
                        for s in self.state_discard:
                            self.state_discard[s][d].update(tokens[d])
                        del tokens[d]

                # Check there is no undefined group in an assertion
                for k, v in tokens.items():
                    bad_groups = list(filter(lambda g: g not in tokens,
                                             check_groups.findall(v)))
                    if bad_groups:
                        print( "Unknown groups", bad_groups)
                pattern_gen = ('(?P<%s>%s)' % (k, v) for k, v in tokens.items())
                if self.re.pattern:
                    pattern_gen = chain((self.re.pattern,), pattern_gen)
                self.re = re.compile('|'.join(pattern_gen), re.M | re.U | re.I)
                self.tokens.update(tokens)
                return self           
            """;

    private void add(Glr.ParserRules tokens) {
        for (String d : List.of("discard_values", "discard_names")) {
            if (tokens.discards.containsKey(d)) {
                for (String s : state_discard.keySet()) {
                    state_discard.get(s).put(d, tokens.discards.get(d));
                }
            }
        }
        // TODO not ported # Check there is no undefined group in an assertion
//                for k, v in tokens.items():
//                    bad_groups = list(filter(lambda g: g not in tokens,
//                                             check_groups.findall(v)))
//                    if bad_groups:
//                        print( "Unknown groups", bad_groups)

        List<String> parts = new ArrayList<>();
        if (!re.pattern().isBlank()) {
            parts.add(re.pattern());
        }
        for (Map.Entry<String, String> entry : tokens.parser_rules.entrySet()) {
            parts.add( String.format("(?<%s>%s)", entry.getKey(), entry.getValue()));
        }
        re = Pattern.compile(String.join("|", parts), DEFAULT_FLAGS);
        this.tokens.putAll(tokens.parser_rules);
    }

    String py3= """
            def token_line_col(text, tok):
                ""\"
                    Converts the token offset into (line, column) position.
                    First character is at position (1, 1).
                ""\"
                line = text.count('\\n', 0, tok[2]) + 1
                offset = text.rfind('\\n', 0, tok[2])
                if offset == -1:
                    column = tok[2] + 1
                else:
                    column = tok[2] - offset
                return line, column
                        
            check_groups = re.compile('[(][?]P=(\\w+)[)]')
            """;
    public static LineColumn token_line_col(String text, NormalizedToken token) {
        int line = StringUtils.countOccurrencesOf(text, "\n") + 1;

        int offset = text.substring(0, token.tokpos).lastIndexOf('\n');

        int column;
        if (offset == -1) {
            column = token.tokpos + 1;
        }
        else {
            column = token.tokpos - offset;
        }
        return new LineColumn(line, column);
    }

//    def def make_scanner(**tokens):
//    return GLRScanner(**tokens)(**tokens):
//            return GLRScanner(**tokens)

}
