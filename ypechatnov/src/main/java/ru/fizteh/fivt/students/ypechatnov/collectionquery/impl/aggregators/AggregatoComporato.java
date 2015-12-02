package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by ura on 02.12.15.
 */
public class AggregatoComporato<T, R> implements Comparator<T> {
    Function<T, R> expression;
    Comparator<R> comparator;
    public final Boolean needCheckWithGroupBy;
    public Function<T, R> getExpression() {
        return expression;
    }
    public int compare(T a, T b) {
        return 0;
    }
    @SuppressWarnings("unchecked")
    public int compareObjects(Object a, Object b) {
        return comparator.compare((R)a, (R)b);
    }
    public AggregatoComporato(Function<T, R> expressionArg, Comparator<R> comparatorArg, Boolean needCheckWithGroupByArg) {
        expression = expressionArg;
        comparator = comparatorArg;
        needCheckWithGroupBy = needCheckWithGroupByArg;
    }
}
