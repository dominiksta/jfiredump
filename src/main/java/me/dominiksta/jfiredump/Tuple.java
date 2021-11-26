package me.dominiksta.jfiredump;

/**
 * A simple typed tuple with `a` and `b` values.
 */
public class Tuple<A, B> { 
    public final A a; 
    public final B b; 
    public Tuple(A a, B b) { 
        this.a = a; 
        this.b = b; 
    } 
} 