package org.example.playingcards;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;


/**
 * This is the game logic for the 24 card game.
 * Uses exact functions so cases like 8(3-8/3) = 24 are handled correctly.
 */

public final class Expr24 {
    private Expr24() {}

    public static final Frac TARGET = new Frac(24, 1);

    /** Throws an invalid expression exception if the expression is not valid */
    public static final class ExprException extends Exception {
        public ExprException(String msg) { super(msg); }
    }

    /**Exact rational number, always stored in lowest terms with positive denominator*/

    public record Frac (long n, long d) {
        public Frac{
            if (d == 0) throw new ArithmeticException("Division by zero");
            if (d < 0) { n = -n; d = -d; }
            long g = gcd(Math.abs(n), d);
            if (g > 1){ n/= g; d/= g; }
        }
        
    }

}
