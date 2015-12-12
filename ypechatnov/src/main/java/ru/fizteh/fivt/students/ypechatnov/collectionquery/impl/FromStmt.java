package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;


import org.mockito.cglib.core.Predicate;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.CollectionUnionTypeException;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.JoinOnNotPrimaryKeyException;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Created by kormushin on 06.10.15.
 */
public class FromStmt<T> {
    private Stream<T> source;
    private Stream<?> previousSource;
    private Class<?> previousClass;
    private boolean isNeedDistinct;


    public static <T> FromStmt<T> from(Iterable<T> iterable) {
        return from(StreamSupport.stream(iterable.spliterator(), false));
    }

    public static <T> FromStmt<T> from(Stream<T> stream) {
        return new FromStmt<T>(null, new ArrayList<T>().stream(), stream);
    }

    public FromStmt(Class<?> previousClassArg, Stream<?> previousStreamArg, Stream<T> streamArg) {
        isNeedDistinct = false;
        source = streamArg;
        previousClass = previousClassArg;
        previousSource = previousStreamArg;
    }

    @SafeVarargs
    public final <R> SelectStmt<T, R> select(Class<R> resultClass, Function<T, ?>... argumentFunctions)
            throws NullPointerException, CollectionUnionTypeException {
        if (resultClass == null) {
            throw new NullPointerException();
        }
        if (previousClass != null && !resultClass.equals(previousClass)) {
            throw new CollectionUnionTypeException();
        }
        return new SelectStmt<T, R>(source, previousSource, isNeedDistinct, resultClass, argumentFunctions);
    }

    @SafeVarargs
    public final <R> SelectStmt<T, R> selectDistinct(Class<R> resultClass, Function<T, ?>... argumentFunctions)
            throws NullPointerException, CollectionUnionTypeException {
        isNeedDistinct = true;
        return select(resultClass, argumentFunctions);
    }

    public final <T2> JoinStmt<T2> join(Iterable<T2> iterable) {
        return join(StreamSupport.stream(iterable.spliterator(), false));
    }

    public final <T2> JoinStmt<T2> join(Stream<T2> stream) {
        return new JoinStmt<T2>(stream);
    }

    public class JoinStmt<T2> {
        Stream<T2> source2;
        Stream <Tuple<T, T2>> joinedSource = null;

        protected <X> Stream<X> listSupplierToStream(Supplier<List<X>> supplier) {
            Iterator<X> joinedIterator = new Iterator<X>() {
                Iterator<X> result = null;
                @Override
                public boolean hasNext() {
                    if (result == null) {
                        result = supplier.get().iterator();
                    }
                    return result.hasNext();
                }

                @Override
                public X next() {
                    if (result == null) {
                        result = supplier.get().iterator();
                    }
                    return result.next();
                }
            };
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(joinedIterator,
                            Spliterator.ORDERED), false
            );
        }


        protected void predicateJoin(BiPredicate<T, T2> predicate) {
            Supplier<List<Tuple<T, T2>>> joinedSupplier = new Supplier<List<Tuple<T,T2>>>() {
                @Override
                public List<Tuple<T, T2>> get() {
                    List<T> sourceList = new ArrayList<T>();
                    Iterator<T> sourceIterator = source.iterator();
                    while (sourceIterator.hasNext()) {
                        sourceList.add(sourceIterator.next());
                    }
                    List<Tuple<T, T2>> joinedList = new ArrayList<Tuple<T, T2>>();
                    Iterator<T2> sourceIterator2 = source2.iterator();
                    while (sourceIterator2.hasNext()) {
                        T2 item2 = sourceIterator2.next();
                        for (T item: sourceList) {
                            if (predicate.test(item, item2)) {
                                joinedList.add(new Tuple<T, T2>(item, item2));
                            }
                        }
                    }
                    return joinedList;
                }
            };
            joinedSource = listSupplierToStream(joinedSupplier);
        }

        protected <R> void expressionJoin(Function<T, R> expression1, Function<T2, R> expression2) {
            Supplier<List<Tuple<T, T2>>> joinedSupplier = new Supplier<List<Tuple<T,T2>>>() {
                @Override
                public List<Tuple<T, T2>> get() {
                    Map<R, T> map1 = new HashMap<R, T>();
                    Iterator<T> sourceIterator = source.iterator();
                    while (sourceIterator.hasNext()) {
                        T item1 = sourceIterator.next();
                        R key1 = expression1.apply(item1);
                        if (map1.containsKey(key1)) {
                            throw new JoinOnNotPrimaryKeyException();
                        }
                        map1.put(key1, item1);
                    }
                    Map<R, T2> map2 = new HashMap<R, T2>();
                    Iterator<T2> sourceIterator2 = source2.iterator();
                    while (sourceIterator2.hasNext()) {
                        T2 item2 = sourceIterator2.next();
                        R key2 = expression2.apply(item2);
                        if (map2.containsKey(key2)) {
                            throw new JoinOnNotPrimaryKeyException();
                        }
                        map2.put(key2, item2);
                    }
                    List<Tuple<T, T2>> joinedList = new ArrayList<Tuple<T, T2>>();
                    for (Map.Entry<R, T> entry: map1.entrySet()) {
                        if (map2.containsKey(entry.getKey())) {
                            joinedList.add(new Tuple<T, T2>(entry.getValue(), map2.get(entry.getKey())));
                        }
                    }
                    return joinedList;
                }
            };
            joinedSource = listSupplierToStream(joinedSupplier);
        }

        public final FromStmt<Tuple<T, T2>> on(BiPredicate<T, T2> predicate) {
            predicateJoin(predicate);
            return new FromStmt<Tuple<T, T2>>(previousClass, previousSource, joinedSource);
        }

        public final <R> FromStmt<Tuple<T, T2>> on(Function<T, R> expression1, Function<T2, R> expression2) {
            expressionJoin(expression1, expression2);
            return new FromStmt<Tuple<T, T2>>(previousClass, previousSource, joinedSource);
        }

        @SafeVarargs
        public final <R> SelectStmt<Tuple<T, T2>, R> select(
                Class<R> resultClass, Function<Tuple<T, T2>, ?>... argumentFunctions)
                throws NullPointerException, CollectionUnionTypeException {
            if (resultClass == null) {
                throw new NullPointerException();
            }
            if (previousClass != null && !resultClass.equals(previousClass)) {
                throw new CollectionUnionTypeException();
            }
            if (joinedSource == null) {
                predicateJoin((o1, o2) -> true);
            }
            return new SelectStmt<Tuple<T, T2>, R>(joinedSource, previousSource,
                    isNeedDistinct, resultClass, argumentFunctions);
        }

        @SafeVarargs
        public final <R> SelectStmt<Tuple<T, T2>, R> selectDistinct(
                Class<R> clazz, Function<Tuple<T, T2>, ?>... argumentFunctions)
                throws NullPointerException, CollectionUnionTypeException {
            isNeedDistinct = true;
            return select(clazz, argumentFunctions);
        }

        JoinStmt(Stream<T2> source2Arg) {
            source2 = source2Arg;
        }
    }
}
