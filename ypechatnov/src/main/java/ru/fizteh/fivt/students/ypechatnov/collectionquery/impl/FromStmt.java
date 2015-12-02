package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;


import org.apache.commons.lang.ObjectUtils;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.CollectionUnionTypeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Created by kormushin on 06.10.15.
 */
public class FromStmt<T> {
    Stream<T> source;
    Stream<?> previousSource;
    Class<?> previousClass, resultClass;
    boolean isNeedDistinct;

    public Stream<?> getPreviousSource() {
        return previousSource;
    }

    public Stream<T> getSource() {
        return source;
    }

    public void setSource(Stream<T> source) {
        this.source = source;
    }

    public boolean needDistinct() {
        return isNeedDistinct;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    public static <T> FromStmt<T> from(Iterable<T> iterable) {
        return from(StreamSupport.stream(iterable.spliterator(), false));
    }

    public static <T> FromStmt<T> from(Stream<T> stream) {
        return new FromStmt<T>(null, StreamSupport.stream(new ArrayList<T>().spliterator(), false), stream);
    }

    @SafeVarargs
    public final <R> SelectStmt<T, R> select(Class<R> clazz, Function<T, ?>... argumentFunctions) {
        resultClass = clazz;
        return new SelectStmt<T, R>(this, clazz, argumentFunctions) ;
    }

    @SafeVarargs
    public final <R> SelectStmt<T, R> selectDistinct(Class<R> clazz, Function<T, ?>... argumentFunctions)
            throws NullPointerException, CollectionUnionTypeException {
        if (clazz == null) {
            throw new NullPointerException();
        }
        if (previousClass != null && !clazz.equals(previousClass)) {
            throw new CollectionUnionTypeException();
        }
        isNeedDistinct = true;
        return select(clazz, argumentFunctions);
    }

    public FromStmt(Class<?> previousClassArg, Stream<?> previousStreamArg, Stream<T> streamArg) {
        isNeedDistinct = false;
        source = streamArg;
        previousClass = previousClassArg;
        previousSource = previousStreamArg;
    }
}
