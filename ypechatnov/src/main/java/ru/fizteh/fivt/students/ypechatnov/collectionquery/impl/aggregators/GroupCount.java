package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Aggregator;

import java.util.List;
import java.util.function.Function;

/**
 * Created by ura on 01.12.15.
 */
public class GroupCount<T, R> extends Aggregator<T, R> {
    Function<T, R> expression;
    R result;
    public Long applyList(List<T> list) {
        return list.stream().map(expression).count();
    }
    public GroupCount(Function<T, R> expressionArg) {
        expression = expressionArg;
    }
}
