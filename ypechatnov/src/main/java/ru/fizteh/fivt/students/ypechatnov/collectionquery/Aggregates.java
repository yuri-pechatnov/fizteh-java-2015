package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import java.util.function.Function;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.*;

/**
 * Aggregate functions.
 *
 * @author akormushin
 */
public class Aggregates {

    /**
     * Maximum value for expression for elements of given collecdtion.
     *
     * @param expression
     * @param <C>
     * @param <T>
     * @return
     */
    public static <C, T extends Comparable<T>> Function<C, T> max(Function<C, T> expression) {
        return new GroupMax<C, T>(expression);
    }

    /**
     * Minimum value for expression for elements of given collecdtion.
     *
     * @param expression
     * @param <C>
     * @param <T>
     * @return
     */
    public static <C, T extends Comparable<T>> Function<C, T> min(Function<C, T> expression) {
        return new GroupMin<C, T>(expression);
    }

    /**
     * Number of items in source collection that turns this expression into not null.
     *
     * @param expression
     * @param <C>
     * @param <T>
     * @return
     */
    public static <C, T> Function<C, T> count(Function<C, T> expression) {
        return new GroupCount<C, T>(expression);
    }

    /**
     * Average value for expression for elements of given collection.
     *
     * @param expression
     * @param <C>
     * @param <T>
     * @return
     */
    public static <C, T extends Number> Function<C, T> avg(Function<C, T> expression) {
        return new GroupAvg<C, T>(expression);
    }

}
