package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;

/**
 * Created by ura on 13.12.15.
 */
public class Tuple<X, Y> {
    private final X x;
    private final Y y;
    public X getFirst() {
        return x;
    }
    public Y getSecond() {
        return y;
    }
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
