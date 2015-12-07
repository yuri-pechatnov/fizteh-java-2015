package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Aggregator;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.AggregatoComparato;

import java.util.Comparator;
import java.util.function.Function;

/**
 * OrderBy sort order helper methods.
 *
 * @author akormushin
 */
public class OrderByConditions {

    /**
     * Ascending comparator.
     *
     * @param expression
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R extends Comparable<R>> Comparator<T> asc(Function<T, R> expression) {
        return new AggregatoComparato<T, R>(expression,
                Comparable::compareTo, !(expression instanceof Aggregator));
    }

    /**
     * Descending comparator.
     *
     * @param expression
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R extends Comparable<R>> Comparator<T> desc(Function<T, R> expression) {
        return new AggregatoComparato<T, R>(expression,
                (o1, o2) -> -o1.compareTo(o2), !(expression instanceof Aggregator));
    }

}
