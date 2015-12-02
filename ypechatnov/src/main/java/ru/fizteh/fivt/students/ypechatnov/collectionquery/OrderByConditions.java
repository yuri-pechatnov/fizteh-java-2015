package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Aggregator;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.AggregatoComporato;

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
        if (expression instanceof Aggregator) {
            return (o1, o2) -> 0;
        } else {
            return (o1, o2) -> expression.apply(o1).compareTo(expression.apply(o2));
        }
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
        if (expression instanceof Aggregator) {
            return (o1, o2) -> 0;
        } else {
            return (o1, o2) -> -expression.apply(o1).compareTo(expression.apply(o2));
        }
    }

}
