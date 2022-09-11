/*
 * License: Apache 2.0
 * Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
 * Copyright (c) 2022. Sergio Lissner
 *
 */

package ai.metaheuristic.glr.glrengine;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sergio Lissner
 * Date: 9/8/2022
 * Time: 4:49 PM
 */
public class GlrStack {

    public interface StackItemData {}

    public record NormalizedTokenStackItemData(GlrNormalizer.NormalizedToken token) implements StackItemData {}
    public record IntegerStackItemData(int token) implements StackItemData {}

    public static final class StackItem {
        String py1= """
        class StackItem(object):
            def __init__(self, prev, x):
                self.prev = prev
                self.data = x
            
            def __str__(self):
                return "StackItem<%s>" % str(self.data)
            
            __repr__ = __str__
        """;


        @Nullable
        public final Set<StackItem> prev;
        @Nullable
        public final StackItemData data;

        public StackItem(@Nullable Set<StackItem> prev, StackItemData data) {
            this.prev = prev;
            this.data = data;
        }

        public StackItem(@Nullable Set<StackItem> prev, int data) {
            this.prev = prev;
            this.data = new IntegerStackItemData(data);
        }

        public StackItem(@Nullable Set<StackItem> prev, @Nullable GlrNormalizer.NormalizedToken data) {
            this.prev = prev;
            this.data = data==null ? null : new NormalizedTokenStackItemData(data);
        }

        public StackItem(@Nullable StackItem prev, int data) {
            this.prev = prev!=null ?new LinkedHashSet<>(Set.of(prev)) : null;
            this.data = new IntegerStackItemData(data);
        }

        public StackItem(@Nullable StackItem prev, @Nullable GlrNormalizer.NormalizedToken data) {
            this.prev = prev!=null ?new LinkedHashSet<>(Set.of(prev)) : null ;
            this.data = data==null ? null : new NormalizedTokenStackItemData(data);
        }
    }

    public List<StackItem> active = new ArrayList<>();
    public GlrAutomaton A;
    public int count_active = 0;
    public List<StackItem> previously_active = new ArrayList<>();

    public GlrStack(GlrAutomaton A) {
        String py2 = """
            self.active = []
            self.A = A
            self.count_active = 0
            self.previously_active = []
        """;
        this.A = A;
    }

    public void shift(@Nullable StackItem source, @Nullable GlrNormalizer.NormalizedToken token, int state) {
        String py2 = """
            self.active = []
            self.A = A
            self.count_active = 0
            self.previously_active = []
        """;
        StackItem sit = new StackItem(source, token);
        StackItem sis = new StackItem(sit, state);
        active.add(sis);
    }

    public static void dump() {
        String py3 = """
        def dump(self):
            # print "GSS HAS", self.count_active, "ACTIVE STATES"
            for a in self.active:
                print (self.A.itemsetstr(self.A.kernel(self.A.LR0[a.data]), a.data))

        """;
    }

    public record EnumerateActive(int i, StackItem node) {}

    public List<EnumerateActive> enumerate_active() {
        String py4 = """
        def enumerate_active(self):
            i = 0
            while i < len(self.active):
                yield i, list(self.active)[i]
                i += 1
        """;
        List<EnumerateActive> l = new ArrayList<>();
        for (int i = 0; i < active.size(); i++) {
            l.add(new EnumerateActive(i, active.get(i)));
        }
        return l;
    }

    String py5 = """
        def reduce(self, node, ruleidx):
            name, elems, commit = self.A.R[ruleidx]
            pathes = self.rec_path(node, len(elems) * 2)
            for path in pathes:
                tokens = tuple(e for el in path[1::2] for e in el)
                if commit:
                    ast = tuple(chain([name], tokens))
                    ok = self.A.validate_ast(ast)
                    if ok is not None:
                        ok = (ok != tuple()) and (ok,) or tuple()
                else:
                    ast = tokens
                    ok = ast
                if ok is not None:
                    goto = self.A.ACTION[path[0].data][name]
                    self.shift(path[0], ok, goto[0][1])
    
        """;
    public void reduce(StackItem node, int ruleidx) {
        final GlrParser.ParsedRule parsedRule = A.R.byIndex.get(ruleidx);

        String name = parsedRule.rulename();
        List<String> elems = parsedRule.words();
        boolean commit = parsedRule.commit();

        List<StackItemNormalizedToken> pathes = rec_path(node, elems.size() * 2);
/*
        for (StackItemNormalizedToken path : pathes) {
            tokens = tuple(e for el in path[1::2] for e in el)
            Boolean ok = null;
            if (commit) {
                ast = tuple(chain([name], tokens))
                ok = self.A.validate_ast(ast)
                if (ok!=null) {
                    ok = (ok != tuple()) and(ok, ) or tuple ()
                }
            } else {
                ast = tokens
                ok = ast
            }
            if (ok!=null) {
                List<GlrAutomaton.ShortToken> shortToken = A.ACTION.get(((IntegerStackItemData)path.stackItem.data).token()).get(name);
                shift(path[0], ok, shortToken[0][1])
            }
        }
*/
    }

    public record StackItemNormalizedToken(StackItem stackItem, @Nullable GlrNormalizer.NormalizedToken token) {}

    String py6 = """
        def rec_path(self, node, n):
            # print "rec_path(%s, %s)" % (str(node), str(n))
            if n == 0:
                return set(((node,),))
    
            if not node.prev:
                return []
    
            result = []
            for prev in node.prev:
                for path in self.rec_path(prev, n - 1):
                    result.append(path + (node.data,))
            return result
    
        """;
    public static List<StackItemNormalizedToken> rec_path(StackItem node, int n) {
        if (!(node.data instanceof NormalizedTokenStackItemData normToken)) {
            return List.of();
        }

        // # print "rec_path(%s, %s)" % (str(node), str(n))
        if (n == 0) {
            return List.of(new StackItemNormalizedToken(node, null));
        }

        if (node.prev == null) {
            return List.of();
        }

        List<StackItemNormalizedToken> result = new ArrayList<>();
        for (StackItem prev : node.prev) {
            for (StackItemNormalizedToken item : rec_path(prev, n - 1)) {
                StackItem path = item.stackItem;
                result.add(new StackItemNormalizedToken(path, normToken.token()));
            }
        }
        return result;

    }
}
