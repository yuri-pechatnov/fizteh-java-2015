package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Created by ura on 02.12.15.
 */
public class AggregatoComparato<T, R> implements Comparator<T> {
    private Function<T, R> expression;
    private Comparator<R> comparator;
    private final Boolean needCheckWithGroupBy;
    public boolean getNeedCheckWithGroupBy() {
        return needCheckWithGroupBy;
    }
    public Function<T, R> getExpression() {
        return expression;
    }
    public int compare(T a, T b) {
        return 0;
    }
    @SuppressWarnings("unchecked")
    public int compareObjects(Object a, Object b) {
        return comparator.compare((R) a, (R) b);
    }
    public AggregatoComparato(Function<T, R> expressionArg,
                              Comparator<R> comparatorArg, Boolean needCheckWithGroupByArg) {
        expression = expressionArg;
        comparator = comparatorArg;
        needCheckWithGroupBy = needCheckWithGroupByArg;
    }
}
