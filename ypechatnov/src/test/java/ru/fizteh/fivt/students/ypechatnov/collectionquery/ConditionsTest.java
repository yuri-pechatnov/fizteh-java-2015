package ru.fizteh.fivt.students.ypechatnov.collectionquery;


import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.Conditions;
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
import java.util.stream.StreamSupport;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import java.util.function.Predicate;

/**
 * Created by ura on 04.12.15.
 */
public class ConditionsTest {

    @Test
    public void testRlike() {
        java.util.function.Predicate<Statistics> pred =
                Conditions.rlike(Statistics::toString, "1.*");
        assertTrue(pred.test(new Statistics("111", 0L, 0.0)));
        assertFalse(pred.test(new Statistics("211", 0L, 0.0)));
    }
    @Test
    public void testLike() {
        java.util.function.Predicate<Statistics> pred =
                Conditions.like(Statistics::toString, "111");
        assertTrue(pred.test(new Statistics("111", 0L, 0.0)));
        assertFalse(pred.test(new Statistics("211", 0L, 0.0)));
    }

    public static class Statistics {

        private final String group;
        private final Long count;
        private final Double age;

        public String getGroup() {
            return group;
        }

        public Long getCount() {
            return count;
        }

        public Double getAge() {
            return age;
        }

        public Statistics(String group, Long count, Double age) {
            this.group = group;
            this.count = count;
            this.age = age;
        }

        @Override
        public String toString() {
            return "" + group;
        }
    }
}
