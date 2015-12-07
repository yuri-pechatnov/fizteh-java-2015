package ru.fizteh.fivt.students.ypechatnov.collectionquery;


import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.Conditions;
import org.mockito.*;
import org.mockito.runners.*;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Aggregator;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.aggregators.AggregatoComparato;


import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.*;

import java.util.function.Predicate;

/**
 * Created by ura on 04.12.15.
 */
public class AggregatesTest {
    @Test
    @SuppressWarnings("unchecked")
    public void testAll() {
        Function<Student, ?> minf = min(Student::age), maxf = max(Student::age),
                avgf = avg(Student::age), countf = count(Student::age);
        List<Student> list = Arrays.asList(new Student("A", LocalDate.parse("1996-08-01"), "123"),
                new Student("B", LocalDate.parse("1997-08-01"), "123"));
        assertTrue(minf instanceof Aggregator);
        assertEquals(((Aggregator<Student, ?>) minf).applyList(list), 1L);
        assertTrue(maxf instanceof Aggregator);
        assertEquals(((Aggregator<Student, ?>) maxf).applyList(list), 2L);
        assertTrue(avgf instanceof Aggregator);
        assertEquals(((Aggregator<Student, ?>) avgf).applyList(list), 1.5);
        assertTrue(countf instanceof Aggregator);
        assertEquals(((Aggregator<Student, ?>) countf).applyList(list), 2L);
    }

    public static class Student {
        private final String name;

        private final LocalDate dateOfBith;

        private final String group;

        public String getName() {
            return name;
        }

        public Student(String name, LocalDate dateOfBith, String group) {
            this.name = name;
            this.dateOfBith = dateOfBith;
            this.group = group;
        }

        public LocalDate getDateOfBith() {
            return dateOfBith;
        }

        public String getGroup() {
            return group;
        }

        public long age() {
            return ChronoUnit.YEARS.between(getDateOfBith(), LocalDate.parse("1998-09-01"));
        }

        public String toString() {
            return name + " date of birth:" + dateOfBith + " group:" + group;
        }

        public static Student student(String name, LocalDate dateOfBith, String group) {
            return new Student(name, dateOfBith, group);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Student student = (Student) obj;
            return (name.equals(student.name) && dateOfBith.equals(student.dateOfBith)
                    && group.equals(student.group));
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }


    }


}
