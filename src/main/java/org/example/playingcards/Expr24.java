package org.example.playingcards;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the game logic for the 24 card game.
 * Uses exact functions so cases like 8(3-8/3) = 24 are handled correctly.
 */

public final class Expr24 {
    private Expr24() {
    }

    public static final Frac TARGET = new Frac(24, 1);

    /**
     * Throws an invalid expression exception if the expression is not valid
     */
    public static final class ExprException extends Exception {
        public ExprException(String msg) {
            super(msg);
        }
    }

    /**
     * Exact rational number, always stored in lowest terms with positive denominator
     */

    public record Frac(long n, long d) {
        public Frac {
            if (d == 0) throw new ArithmeticException("Division by zero");
            if (d < 0) {
                n = -n;
                d = -d;
            }
            long g = gcd(Math.abs(n), d);
            if (g > 1) {
                n /= g;
                d /= g;
            }
        }

        private static long gcd(long a, long b) {
            return b == 0 ? a : gcd(b, a % b);
        }

        public Frac add(Frac o) {
            return new Frac(n * o.d + o.n * d, d * o.d);
        }

        public Frac sub(Frac o) {
            return new Frac(n * o.d - o.n * d, d * o.d);
        }

        public Frac mul(Frac o) {
            return new Frac(n * o.n, d * o.d);
        }

        public Frac div(Frac o) {
            return new Frac(n * o.d, d * o.n);
        }

        public boolean isZero() {
            return n == 0;
        }

        @Override
        public String toString() {
            return d == 1 ? Long.toString(n) : n + "/" + d;
        }
    }

    /**
     * Value of a parsed expression plus the numbers it used, in the order typed.
     */
    public record Result(Frac value, List<Integer> numbers) {
    }


    //----------------------- parsing
    public static Result evaluate(String expr) throws ExprException {
        if (expr == null || expr.isBlank()) throw new ExprException("Please enter an expression.");
        Parser p = new Parser(expr);
        Frac value = p.expr();
        char c = p.peek();
        if (c != '\0') {
            if (c == ')') throw new ExprException("Unmatched ')' at position " + (p.pos + 1) + ".");
            throw new ExprException("Unexpected '" + c + "' at position " + (p.pos + 1)
                    + ". Is an operator missing, or is there a space inside a number?");
        }
        return new Result(value, p.nums);
    }

    /**
     * Recursive descent: expr = term {(+|-) term}; term = factor {(*|/) factor}; factor = number | '(' expr ')'.
     */
    private static final class Parser {
        final String s;
        int pos = 0;
        final List<Integer> nums = new ArrayList<>();

        Parser(String s) {
            this.s = s;
        }

        char peek() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
            return pos < s.length() ? s.charAt(pos) : '\0';
        }

        Frac expr() throws ExprException {
            Frac v = term();
            while (true) {
                char c = peek();
                if (c == '+') {
                    pos++;
                    v = v.add(term());
                } else if (c == '-') {
                    pos++;
                    v = v.sub(term());
                } else return v;
            }
        }

        Frac term() throws ExprException {
            Frac v = factor();
            while (true) {
                char c = peek();
                if (c == '*') {
                    pos++;
                    v = v.mul(factor());
                } else if (c == '/') {
                    pos++;
                    Frac r = factor();
                    if (r.isZero()) throw new ExprException("Division by zero.");
                    v = v.div(r);
                } else return v;
            }
        }

        Frac factor() throws ExprException {
            char c = peek();
            if (c == '(') {
                pos++;
                Frac v = expr();
                if (peek() != ')') throw new ExprException("Missing closing ')'.");
                pos++;
                return v;
            }
            if (Character.isDigit(c)) {
                int start = pos;
                while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
                String digits = s.substring(start, pos);
                if (digits.length() > 2) throw new ExprException("Number too large: " + digits);
                int n = Integer.parseInt(digits);
                nums.add(n);
                return new Frac(n, 1);
            }
            if (c == '\0') throw new ExprException("The expression ended unexpectedly.");
            throw new ExprException("Unexpected '" + c + "' at position " + (pos + 1)
                    + ". Only numbers, + - * / and parentheses are allowed.");
        }
    }
    //---------------- solver

    private record Node(Frac value, String expr){}

    /** Returns an expression using each value exactly once that equals 24, or null if none exists. */

    public static String solve(int[] values) {
        List<Node> nodes = new ArrayList<>();
        for (int v : values) nodes.add(new Node(new Frac(v, 1), Integer.toString(v)));
        String found = search(nodes);
        // Top-level result is always wrapped in one redundant pair of parentheses.
        return found == null ? null : found.substring(1, found.length() - 1);
    }

    private static String search(List<Node> nodes) {
        if (nodes.size() == 1) {
            Node only = nodes.get(0);
            return only.value.equals(TARGET) ? only.expr : null;
        }
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node a = nodes.get(i), b = nodes.get(j);
                List<Node> rest = new ArrayList<>();
                for (int k = 0; k < nodes.size(); k++) if (k != i && k != j) rest.add(nodes.get(k));

                List<Node> candidates = new ArrayList<>();
                candidates.add(combine(a, "+", b, a.value().add(b.value())));
                candidates.add(combine(a, "*", b, a.value().mul(b.value())));
                candidates.add(combine(a, "-", b, a.value().sub(b.value())));
                candidates.add(combine(b, "-", a, b.value().sub(a.value())));

                if (!b.value().isZero()) candidates.add(combine(a, "/", b, a.value().div(b.value())));
                if (!a.value().isZero()) candidates.add(combine(b, "/", a, b.value().div(a.value())));

                for (Node c : candidates) {
                    rest.add(c);
                    String r = search(rest);
                    if (r != null) return r;
                    rest.remove(rest.size() - 1);
                }
            }
        }
        return null;
    }

    private static Node combine(Node l, String op, Node r, Frac v) {
        return new Node(v, "(" + l.expr() + op + r.expr() + ")");
    }
}



