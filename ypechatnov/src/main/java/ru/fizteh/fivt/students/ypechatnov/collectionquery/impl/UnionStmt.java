package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by kormushin on 09.10.15.
 */
public class UnionStmt<R> {
    private Stream<R> previousSource;
    private Class<?> previousClass;

    public Stream<R> getPreviousSource() {
        return previousSource;
    }
    public Class<?> getPreviousClass() {
        return previousClass;
    }

    public <T> FromStmt<T> from(Iterable<T> list) {
        return new FromStmt<T>(previousClass, previousSource,
                StreamSupport.stream(list.spliterator(), false));
    }

    UnionStmt(Class<?> previousClassArg, Stream<R> previousSourceArg) {
        previousClass = previousClassArg;
        previousSource = previousSourceArg;
    }
}
