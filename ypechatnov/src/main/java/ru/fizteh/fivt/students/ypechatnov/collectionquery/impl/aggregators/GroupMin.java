package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Aggregator;

import java.util.List;
import java.util.function.Function;

/**
 * Created by ura on 01.12.15.
 */
public class GroupMin<T, R extends Comparable<R>> extends Aggregator<T, R> {
    Function<T, R> expression;
    R result;
    public R applyList(List<T> list) {
        result = null;
        list.stream().map(expression).forEach(value -> {
            if (result == null || result.compareTo(value) > 0) {
                result = value;
            }
        });
        return result;
    }
    public GroupMin(Function<T, R> expressionArg) {
        expression = expressionArg;
    }
}
