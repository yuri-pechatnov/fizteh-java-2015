package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.AggregatoComporato;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
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
    List<Comparator<R>> groupComparators = null;

    class SourceItemToResultMapper implements Function<T, R> {
        Constructor<R> constructor = null;
        @Override
        public R apply(T sourceItem) {
            try {
                Object[] argumentsValue = aggregates.stream().map(
                        function -> function.apply(sourceItem)).toArray();
                if (constructor == null) {
                    Class<?>[] argumentsType = new Class<?>[argumentsValue.length];
                    for (int i = 0; i < argumentsValue.length; i++) {
                        argumentsType[i] = argumentsValue[i].getClass();
                    }
                    constructor = resultClass.getConstructor(argumentsType);
                }
                return constructor.newInstance(argumentsValue);
            } catch (Throwable e) {
                return null;
                // throw new CollectionConstructingException();
            }
        }
    };

    class SourceListToResultMapper implements Function<List<T>, R> {
        Constructor<R> constructor = null;
        @Override
        @SuppressWarnings("unchecked")
        public R apply(List<T> list) {
            if (list.isEmpty()) {
                return null;
            }
            try {
                Object[] argumentsValue = new Object[aggregates.size()];
                for (int i = 0; i < aggregates.size(); i++) {
                    if (aggregates.get(i) instanceof Aggregator) {
                        Aggregator<T, R> listFunction = (Aggregator<T, R>)aggregates.get(i);
                        argumentsValue[i] = listFunction.applyList(list);
                        System.out.println(argumentsValue[i]);
                    } else {
                        argumentsValue[i] = aggregates.get(i).apply(list.get(0));
                    }
                }
                if (constructor == null) {
                    Class<?>[] argumentsType = new Class<?>[argumentsValue.length];
                    for (int i = 0; i < argumentsValue.length; i++) {
                        argumentsType[i] = argumentsValue[i].getClass();
                    }
                    constructor = resultClass.getConstructor(argumentsType);
                }
                return constructor.newInstance(argumentsValue);
            } catch (Throwable e) {
                return null;
                // throw new CollectionConstructingException();
            }
        }
    };

    class Group extends ArrayList<T> {
        R result;
    }

    class ComputingResultIterator implements Iterator<R> {
        SourceItemToResultMapper itemMapper;
        Iterator <T> sourceIterator = null;
        List<Group> groups;
        List<R> result;
        Iterator<R> resultIterator;
        @SuppressWarnings("unchecked")
        private void precalculateLists() {
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
            SourceListToResultMapper listMapper = new SourceListToResultMapper();
            groups.stream().forEach(item -> { item.result = listMapper.apply(item); });

            System.out.println(groups);
            result = new ArrayList<R>(Arrays.asList((R[])groups.stream().map(group -> group.result).toArray()));
            System.out.println(result);
            resultIterator = result.iterator();
        }
        private void checkAndPrecalculate() {
            if (groupByExpressions == null) {
                if (sourceIterator == null) {
                    sourceIterator = source.iterator();
                    itemMapper = new SourceItemToResultMapper();
                }
            } else {
                if (groups == null) {
                    precalculateLists();
                }
            }
        }
        @Override
        public boolean hasNext() {
            checkAndPrecalculate();
            if (groupByExpressions == null) {
                return sourceIterator.hasNext();
            } else {
                return resultIterator.hasNext();
            }
        }
        @Override
        public R next() {
            checkAndPrecalculate();
            if (groupByExpressions == null) {
                return itemMapper.apply(sourceIterator.next());
            } else {
                return resultIterator.next();
            }
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
    public final WhereStmt orderBy(Comparator<R>... comparators) {
        return new WhereStmt().orderBy(comparators);
    }
    public final UnionStmt<R> union() throws CollectionException {
        return new WhereStmt().union();
    }

    public class WhereStmt {
        @SafeVarargs
        public final WhereStmt groupBy(Function<T, ?>... expressions) throws CollectionBadAggregatesException {
            Set<Function<T, ?>> set = new HashSet<Function<T, ?>>(Arrays.asList(expressions));
            for (Function<T, ?> function: aggregates) {
                if (!(function instanceof Aggregator || set.contains(function))) {
                    throw new CollectionBadAggregatesException();
                }
            }
            groupByExpressions = Arrays.asList(expressions);
            return this;
        }

        @SafeVarargs
        public final WhereStmt orderBy(Comparator<R>... comparators) {
            if (groupByExpressions == null) {
                result = result.sorted(new Comparator<R>() {
                    @Override
                    public int compare(R o1, R o2) {
                        for (Comparator<R> comparator : comparators) {
                            int result = comparator.compare(o1, o2);
                            if (result != 0) {
                                return result;
                            }
                        }
                        return 0;
                    }
                });
            } else {
                groupComparators = Arrays.asList(comparators);
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
