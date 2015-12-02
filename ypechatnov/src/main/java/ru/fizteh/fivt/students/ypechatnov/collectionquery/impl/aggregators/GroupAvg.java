package ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Aggregator;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Function;

/**
 * Created by ura on 01.12.15.
 */
public class GroupAvg<T, R extends Number> extends Aggregator<T, R> {
    Function<T, R> expression;
    Double sum;
    public Double applyList(List<T> list) {
        sum = 0.0;
        list.stream().map(expression).forEach(value -> {
            sum = sum + value.doubleValue();
        });
        return sum / list.size();
    }
    public GroupAvg(Function<T, R> expressionArg) {
        expression = expressionArg;
    }
}
