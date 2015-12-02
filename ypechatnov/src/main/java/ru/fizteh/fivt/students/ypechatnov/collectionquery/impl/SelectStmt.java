package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;


import ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.AggregatoComporato;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.DataFormatException;

/**
 * Created by kormushin on 06.10.15.
 */
public class SelectStmt<T, R> {
    FromStmt<T> fromStmt;
    Stream <T> source;
    Stream <R> result = null;
    Class<R> resultClass;
    List<Function<T, ?>> aggregates, groupByExpressions = null;
    List<AggregatoComporato<R, ?>> groupComparators = null;
    Set<Function<T, ?>> groupByExpressionsSet;

    class Group extends ArrayList<T> implements Comparable<Group> {
        R result = null;
        List<Object> argumentsValue, sortParameters;

        public int compareTo(Group g) {
            for (int i = 0; i < groupComparators.size(); i++) {
                if (sortParameters.get(i) == null) {
                    return -1;
                }
                if (g.sortParameters.get(i) == null) {
                    return 1;
                }
                int compareResult = groupComparators.get(i).compareObjects(
                        sortParameters.get(i), g.sortParameters.get(i));
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return 0;
        }
        Group(T item) {
            add(item);
        }
        Group() {
        }
    }

    class ComputingResultIterator implements Iterator<R> {
        Iterator <T> sourceIterator = null;
        List<Group> groups;
        List<R> result;
        Iterator<R> resultIterator = null;
        List<Constructor<R>> constructors;
        @SuppressWarnings("unchecked")
        private void precalculateLists() {

            System.out.println("Start");
            if (groupByExpressions != null) {
                Map<List<Object>, Group> groupsMap = new HashMap<List<Object>, Group>();
                sourceIterator = source.iterator();
                while (sourceIterator.hasNext()) {
                    T sourceItem = sourceIterator.next();
                    List<Object> key = Arrays.asList(
                            groupByExpressions.stream().map(func -> func.apply(sourceItem)).toArray());
                    if (groupsMap.get(key) == null) {
                        groupsMap.put(key, new Group());
                    }
                    groupsMap.get(key).add(sourceItem);
                }
                groups = new ArrayList<Group>(groupsMap.values());
            } else {
                groups = new ArrayList<Group>();
                source.forEach(item -> groups.add(new Group(item)));
            }
            constructors = Arrays.asList((Constructor<R>[])resultClass.getConstructors());
            System.out.println(constructors);
            groups.stream().forEach(item -> {
                item.argumentsValue = new ArrayList<Object>();
                for (Function<T, ?> function : aggregates) {
                    if (function instanceof Aggregator) {
                        Aggregator<T, R> listFunction = (Aggregator<T, R>)function;
                        item.argumentsValue.add(listFunction.applyList(item));
                    } else {
                        item.argumentsValue.add(function.apply(item.get(0)));
                    }
                }
                System.out.println(constructors);
                List<Constructor<R>> oldConstructors = constructors;
                constructors = new ArrayList<Constructor<R>>();
                oldConstructors.stream().forEach(constructor -> {
                    Class<?>[] parameters = constructor.getParameterTypes();
                    if (parameters.length != aggregates.size()) {
                        return;
                    }
                    for (int i = 0; i < parameters.length; i++) {
                        if (!(item.argumentsValue.get(i) == null ||
                                parameters[i].isInstance(item.argumentsValue.get(i)))) {
                            return;
                        }
                    }
                    System.out.println("yep");
                    constructors.add(constructor);
                });
            });
            if (constructors.size() == 0) {
                resultIterator = null;
                return;
            }
            Constructor<R> constructor = constructors.get(0);
            groups.stream().forEach(item -> {
                try {
                    item.result = constructor.newInstance(item.argumentsValue.toArray());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    item.result = null;
                }
            });
            if (groupComparators != null) {
                groups.stream().forEach(item -> {
                    item.sortParameters = new ArrayList<Object>();
                    for (AggregatoComporato<R, ?> comparator : groupComparators) {
                        Function<R, ?> function = comparator.getExpression();
                        if (function instanceof Aggregator) {
                            throw new Error();
                        } else {
                            item.sortParameters.add(function.apply(item.result));
                        }
                    }
                });
                groups.sort(Group::compareTo);
            }
            result = new ArrayList<R>(Arrays.asList((R[])groups.stream().map(group -> group.result).toArray()));
            System.out.println(result);
            resultIterator = result.iterator();
        }
        private void checkAndPrecalculate() {
            if (resultIterator == null) {
                precalculateLists();
            }
        }
        @Override
        public boolean hasNext() {
            checkAndPrecalculate();
            if (resultIterator == null) {
                return false;
            }
            return resultIterator.hasNext();
        }
        @Override
        public R next() {
            checkAndPrecalculate();
            return resultIterator.next();
        }
    };

    @SafeVarargs
    public SelectStmt(FromStmt<T> fromStmtArg, Class<R> clazzArg, Function<T, ?>... s) {
        fromStmt = fromStmtArg;
        resultClass = clazzArg;
        aggregates = Arrays.asList(s);
        aggregates.stream().forEach(x -> {System.out.println(x.getClass());});
        source = fromStmtArg.getSource();
        result = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new ComputingResultIterator(),
                        Spliterator.ORDERED), false
        );
        if (fromStmt.needDistinct()) {
            result = result.distinct();
        }
    }

    public WhereStmt where(Predicate<T> predicate) {
        source = source.filter(predicate);
        return new WhereStmt();
    }

    public Iterable<R> execute() throws CollectionException {
        return new WhereStmt().execute();
    }
    public Stream<R> stream() throws CollectionException {
        return new WhereStmt().stream();
    }
    @SafeVarargs
    public final WhereStmt orderBy(Comparator<R>... comparators) throws CollectionException {
        return new WhereStmt().orderBy(comparators);
    }
    public final UnionStmt<R> union() throws CollectionException {
        return new WhereStmt().union();
    }

    public class WhereStmt {
        @SafeVarargs
        public final WhereStmt groupBy(Function<T, ?>... expressions) throws CollectionBadAggregatesException {
            groupByExpressionsSet = new HashSet<Function<T, ?>>(Arrays.asList(expressions));
            for (Function<T, ?> function: aggregates) {
                if (!(function instanceof Aggregator || groupByExpressionsSet.contains(function))) {
                    throw new CollectionBadAggregatesException();
                }
            }
            groupByExpressions = Arrays.asList(expressions);
            return this;
        }

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public final WhereStmt orderBy(Comparator<R>... comparators) throws CollectionException {
            groupComparators = new ArrayList<AggregatoComporato<R, ?>>();
            for (Comparator<R> comparator : comparators) {
                if (comparator instanceof AggregatoComporato) {
                    groupComparators.add((AggregatoComporato<R, ?>)comparator);
                } else {
                    throw new CollectionBadAggregatesException();
                }
            }
            return this;
        }

        public WhereStmt having(Predicate<R> condition) throws CollectionNoGroupByBeforeHavingException {
            if (groupByExpressions == null) {
                throw new CollectionNoGroupByBeforeHavingException();
            }
            result = result.filter(condition);
            return this;
        }

        public WhereStmt limit(int count) {
            result = result.limit(count);
            return this;
        }

        public UnionStmt<R> union() throws CollectionException {
            return new UnionStmt<R>(resultClass, stream());
        }

        public Iterable<R> execute() throws CollectionException {
            List<R> list = new ArrayList<R>();
            Iterator<R> iterator = stream().iterator();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
            return list;
        }

        @SuppressWarnings("unchecked")
        public Stream<R> stream() throws CollectionException {
            try {
                result = Stream.concat((Stream<R>)fromStmt.getPreviousSource(), result);
            } catch (ClassCastException e) {
                throw new CollectionUnionTypeException();
            }
            return result;
        }

        WhereStmt() {
        }
    }

}
