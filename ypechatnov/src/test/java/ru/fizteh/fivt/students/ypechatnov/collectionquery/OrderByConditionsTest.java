package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.Sources;
import org.mockito.*;
import org.mockito.runners.*;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.AggregatoComparato;


import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


/**
 * Created by ura on 04.12.15.
 */
public class OrderByConditionsTest {

    @Test
    public void testAsc() {
        Comparator<Statistics> smth = OrderByConditions.asc(Statistics::getCount);
        assertTrue(smth instanceof AggregatoComparato);
        if (smth instanceof AggregatoComparato) {
            AggregatoComparato<Statistics, Long> aggr = (AggregatoComparato) smth;
            assertTrue(aggr.compareObjects(aggr.getExpression().apply(new Statistics("", 1L, 0.0)),
                    aggr.getExpression().apply(new Statistics("", 0L, 0.0))) > 0);
        }
    }

    @Test
    public void testDesc() {
        Comparator<Statistics> smth = OrderByConditions.desc(Statistics::getCount);
        assertTrue(smth instanceof AggregatoComparato);
        if (smth instanceof AggregatoComparato) {
            AggregatoComparato<Statistics, Long> aggr = (AggregatoComparato) smth;
            assertTrue(aggr.compareObjects(aggr.getExpression().apply(new Statistics("", 1L, 0.0)),
                    aggr.getExpression().apply(new Statistics("", 0L, 0.0))) < 0);
        }
    }

    public static class Statistics {

        private final String group;
        private final Long count;
        private final Double age;

        public Long getCount() {
            return count;
        }

        public Statistics(String group, Long count, Double age) {
            this.group = group;
            this.count = count;
            this.age = age;
        }
    }

}
