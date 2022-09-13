/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glr;

import company.evo.jmorphy2.Tag;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.regex.Pattern.*;

/**
 * @author Sergio Lissner
 * Date: 9/10/2022
 * Time: 2:23 PM
 */
public class GrlTokenizer {
    String py1 = """
    class Token(namedtuple('Token', ['symbol', 'value', 'start', 'end', 'input_term', 'params'])):
        '''
        Used as token and as node in AST (abstract syntax tree)
        '''
        __slots__ = ()

        def __new__(cls, symbol, value='', start=-1, end=-1, input_term='', params=None):
            return super(cls, Token).__new__(cls, symbol, value, start, end, input_term, params)

        # def __repr__(self):
        #     return u'%s(%s)' % (self.symbol, self.value)


    class TokenizerException(Exception):
        pass
    """;

    public static final class Token {
        public final String symbol;
        public final String value;
        public final int start;
        public final int end;
        public final String input_term;
        @Nullable
        public final Tag params;

        public Token(String symbol) {
            this(symbol, "", -1, -1, "", null);
        }

        public Token(String symbol, String value, int start, int end, String input_term, @Nullable Tag params) {
            this.symbol = symbol;
            this.value = value;
            this.start = start;
            this.end = end;
            this.input_term = input_term;
            this.params = params;
        }
    }

    public static class TokenizerException extends RuntimeException {
        public TokenizerException(String message) {
            super(message);
        }
    }

    String py2 = """
    class SimpleRegexTokenizer(object):
        def __init__(self, symbol_regex_dict, discard_symbols=None, regex_flags=re.M | re.U | re.I):
            patterns = []
            for symbol, regex in symbol_regex_dict.items():
                if '(?P<' in regex:
                    raise TokenizerException('Invalid regex "%s" for symbol "%s"' % (regex, symbol))
                patterns.append('(?P<%s>%s)' % (symbol, regex))
            self.re = re.compile('|'.join(patterns), regex_flags)
            self.discard_symbols = set(discard_symbols) or set()
            self.symbols = set(symbol for symbol in symbol_regex_dict.keys() if symbol not in self.discard_symbols)

        def scan(self, text):
            for m in self.re.finditer(text):
                if m.lastgroup not in self.discard_symbols:
                    yield Token(m.lastgroup, m.group(m.lastgroup), m.start(), m.end(), m.group(m.lastgroup), None)
            yield Token('$', '', m.end(), -1, '', None)
    """;
    public static class SimpleRegexTokenizer {

        public List<String> patterns = new ArrayList<>();
        public Pattern re;
        public final LinkedHashSet<String> discard_symbols;
        public final LinkedHashSet<String> symbols;

        public SimpleRegexTokenizer(LinkedHashMap<String, String> symbol_regex_dict) {
            this(symbol_regex_dict, null, CASE_INSENSITIVE | MULTILINE | UNICODE_CASE);
        }

        public SimpleRegexTokenizer(
                LinkedHashMap<String, String> symbol_regex_dict, @Nullable List<String> discard_symbols ) {
            this(symbol_regex_dict, discard_symbols, CASE_INSENSITIVE | MULTILINE | UNICODE_CASE);
        }

        public SimpleRegexTokenizer(
                LinkedHashMap<String, String> symbol_regex_dict, @Nullable List<String> discard_symbols,
                int regex_flags) {
            for (Map.Entry<String, String> entry : symbol_regex_dict.entrySet()) {
                String symbol = entry.getKey();
                String regex = entry.getValue();
                if (regex.contains("(?<")) {
                    throw new TokenizerException(String.format("Invalid regex %s for symbol %s", regex, symbol));
                }
                patterns.add(String.format("(?<%s>%s)", symbol, regex));
            }
            this.re = Pattern.compile(String.join("|", patterns), regex_flags);

            this.discard_symbols = discard_symbols==null ? new LinkedHashSet<>() : new LinkedHashSet<>(discard_symbols);
            this.symbols = symbol_regex_dict.keySet().stream().filter(o -> !this.discard_symbols.contains(o)).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        public Stream<String> getParserRulesKeys() {
            return symbols.stream().sorted(Comparator.reverseOrder());
        }

        public List<Token> scan(String text) {
            List<Token> items = new ArrayList<>();
            int pos = 0;

            Matcher m = re.matcher(text);
            while(m.find()) {
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

                int startPos = m.start();
                pos = m.end();
                items.add( new Token(tokname, tokvalue, startPos, pos, tokvalue, null));
            }
            items.add( new Token("$", "", text.length(), -1, "", null));

            if (pos != text.length()) {
                String msg = String.format("tokenizer stopped at pos %d of %d in \"%s\" at \"%s\"",
                        pos, text.length(), text, "["+text.substring(pos)+"]");
                throw new RuntimeException(msg);
            }
            return items;
        }
    }

    String py3 = """
    class CharTypeTokenizer(SimpleRegexTokenizer):
        def __init__(self):
            symbol_regex_dict = {
                'alpha': r'[^\\W\\d_]+',
                'space': r'\\s+',
                'digit': r'\\d+',
                'punct': r'[^\\w\\s]|_',
            }
            super(CharTypeTokenizer, self).__init__(symbol_regex_dict, ['space'])
    """;
    public static final LinkedHashMap<String, String> symbol_regex_char_dict = new LinkedHashMap<>();
    static {
        symbol_regex_char_dict.put("alpha", "[^\\p{L}\\p{Digit}_]+");
        symbol_regex_char_dict.put("space", "\\s+");
        symbol_regex_char_dict.put("digit", "\\d+");
        symbol_regex_char_dict.put("punct", "[^[\\p{L}\\p{Digit}_-]\\s]|_");
    }

    public static class CharTypeTokenizer extends SimpleRegexTokenizer {
        public CharTypeTokenizer() {
            super(symbol_regex_char_dict, List.of("space"));
        }
    }

    String py4 = """
    class WordTokenizer(SimpleRegexTokenizer):
        def __init__(self):
            symbol_regex_dict = {
                "word": r"[\\w\\d_-]+",
                "number": r"[\\d]+",
                "space": r"[\\s]+",
                "newline": r"[\\n]+",
                "dot": r"[\\.]+",
                "comma": r"[,]+",
                "colon": r"[:]+",
                "percent": r"[%]+",
                "quote": r"[\\"\\'«»`]+",
                "brace": r"[\\(\\)\\{\\}\\[\\]]+",
            }
            super(WordTokenizer, self).__init__(symbol_regex_dict, ['space'])
    """;
    public static final LinkedHashMap<String, String > symbol_regex_word_dict = new LinkedHashMap<>();
    static {
        symbol_regex_word_dict.put("word", "[\\p{L}\\p{Digit}_-]+");
        symbol_regex_word_dict.put("number", "[\\d]+");
        symbol_regex_word_dict.put("space", "[\\s]+");
        symbol_regex_word_dict.put("newline", "[\\n]+");
        symbol_regex_word_dict.put("dot", "[\\.]+");
        symbol_regex_word_dict.put("comma", "[,]+");
        symbol_regex_word_dict.put("colon", "[:]+");
        symbol_regex_word_dict.put("percent", "[%]+");
        symbol_regex_word_dict.put("quote", "[\"'«»`]+");
        symbol_regex_word_dict.put("brace", "[\\(\\)\\{\\}\\[\\]]+");
    }


    public static class WordTokenizer extends SimpleRegexTokenizer {
        public WordTokenizer() {
            super(symbol_regex_word_dict, List.of("space"));
        }
    }


}
