package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;

import java.util.List;
import java.util.function.Function;

/**
 * Created by ura on 27.11.15.
 */
public abstract class Aggregator<T, R> implements Function<T, R> {
    @Override
    public final R apply(T item) {
        return null;
    }
    public abstract Object applyList(List<T> list);
}
