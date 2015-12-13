package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.*;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.Tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.avg;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.count;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.max;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.CommonTest.Student.student;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Conditions.rlike;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.OrderByConditions.asc;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.OrderByConditions.desc;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Sources.list;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.FromStmt.*;

/**
 * @author akormushin
 */
public class CommonTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Function<Student, ?> name = Student::getName, date = Student::getDateOfBith,
            group = Student::getGroup, maxGroup = max(Student::getGroup),
            countGroup = count(Student::getGroup), avgGroup = avg(Student::age);

    @Test
    public void testSelect() throws Exception {
        Iterable<String> result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("A", LocalDate.parse("1986-08-06"), "491"),
                        student("A", LocalDate.parse("1986-08-06"), "491"),
                        student("A", LocalDate.parse("1986-08-06"), "492"),
                        student("d", LocalDate.parse("1986-08-06"), "491"),
                        student("babushkin", LocalDate.parse("1986-08-06"), "494"))
        ).select(String.class, name).execute();
        int len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 6);
        result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("A", LocalDate.parse("1986-08-06"), "491"),
                        student("A", LocalDate.parse("1986-08-06"), "491"),
                        student("A", LocalDate.parse("1986-08-06"), "492"),
                        student("d", LocalDate.parse("1986-08-06"), "491"),
                        student("babushkin", LocalDate.parse("1986-08-06"), "494"))
        ).selectDistinct(String.class, name).execute();
        len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 4);
    }

    @Test
    public void testConstructorException() throws Exception {
        thrown.expect(CollectionConstructorException.class);
        Iterable<String> result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"))
        ).select(String.class, name, group).execute();
    }

    @Test
    public void testFrom() throws Exception {
        Iterable<String> result1 = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"))
        ).select(String.class, name).execute();
        Iterable<String> result2 = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491")).stream()
        ).select(String.class, name).execute();
        assertEquals(result1, result2);
    }

    @Test
    public void testUnion() throws Exception {
        Iterable<String> result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491")))
                .select(String.class, name)
                .union()
                .from(list(
                        student("bykov", LocalDate.parse("1986-08-06"), "491")))
                .select(String.class, name)
                .execute();
        int len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 2);
        thrown.expect(CollectionUnionTypeException.class);
        Iterable<?> result2 = from(list(
                student("ivanov", LocalDate.parse("1986-08-06"), "491")))
                .select(String.class, name)
                .union()
                .from(list(
                        student("bykov", LocalDate.parse("1986-08-06"), "491")))
                .select(Statistics.class, s -> "all", s -> 1, s-> 4.0)
                .execute();
    }

    @Test
    public void testLimit() throws Exception {
        Iterable<String> result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"))
        ).select(String.class, name).limit(1).execute();
        int len = 0;
        for (String str : result) {
            len++;
        }
        assertEquals(len, 1);
    }

    @Test
    public void testGroupBy() throws Exception {
        Iterable<Statistics> result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, group, count(name), avg(Student::age)).groupBy(group).execute();
        int[] cnt = new int[3];
        for (Statistics stat : result) {
            cnt[stat.count.intValue()] += 1;
        }
        assertEquals(2, cnt[1]);
        assertEquals(1, cnt[2]);
        thrown.expect(CollectionBadSelectExpressionsException.class);
        result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, name, count(name), avg(Student::age)).groupBy(group).execute();
    }
    @Test
    public void testHaving() throws Exception {
        Iterable<Statistics> result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, group, count(name), avg(Student::age)).groupBy(group)
                .having(stat -> (stat.count == 2)).execute();
        int[] cnt = new int[3];
        for (Statistics stat : result) {
            cnt[stat.count.intValue()] += 1;
        }
        assertEquals(0, cnt[1]);
        assertEquals(1, cnt[2]);
        thrown.expect(CollectionNoGroupByBeforeHavingException.class);
        result = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, group, count(name), avg(Student::age)).where(o -> true)
                .having(stat -> (stat.count == 2)).execute();
    }

    @Test
    public void testOrderBy() throws Exception {
        Iterator<Statistics> iter = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, group, count(name), avg(Student::age))
                .orderBy(asc(Statistics::getGroup)).execute().iterator();
        assertTrue("491".equals(iter.next().getGroup()));
        assertTrue("492".equals(iter.next().getGroup()));
        assertTrue("492".equals(iter.next().getGroup()));
        assertTrue("493".equals(iter.next().getGroup()));
        assertFalse(iter.hasNext());

        iter = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, group, count(name), avg(Student::age))
                .groupBy(group)
                .orderBy(asc(Statistics::getCount)).execute().iterator();
        assertTrue(1L == iter.next().getCount());
        assertTrue(1L == iter.next().getCount());
        assertTrue(2L == iter.next().getCount());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testNullCase() throws Exception {
        Iterator<Statistics> iter = from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                        student("shlykov", LocalDate.parse("1986-08-06"), "493"),
                        student("ivanov", LocalDate.parse("1986-08-06"), "492"),
                        student("chernov", LocalDate.parse("1986-08-06"), "492"))
        ).select(Statistics.class, (s -> (s.getGroup().equals("492") ? null : s.getGroup()))
                , count(name), avg(Student::age))
                .orderBy(asc(Statistics::getGroup)).execute().iterator();
        assertTrue(null == iter.next().getGroup());
        assertTrue(null == iter.next().getGroup());
        assertTrue("491".equals(iter.next().getGroup()));
        assertTrue("493".equals(iter.next().getGroup()));
        assertFalse(iter.hasNext());

    }


    @Test
    public void mainTest() throws Exception {
        Iterable<Student> stat =
                from(list(
                                student("ivanov", LocalDate.parse("1986-08-06"), "491"),
                                student("A", LocalDate.parse("1986-08-06"), "491"),
                                student("A", LocalDate.parse("1986-08-06"), "491"),
                                student("A", LocalDate.parse("1986-08-06"), "492"),
                                student("d", LocalDate.parse("1986-08-06"), "491"),
                                student("babushkin", LocalDate.parse("1986-08-06"), "494"))
                ).selectDistinct(
                        Student.class, name, date, maxGroup
                        //).where(
                        //       student -> true || ("A".equals(student.name))
                ).where(x -> true).groupBy(name, date).execute();
        Iterable<Statistics> statistics =
                from(list(
                        student("ivanov", LocalDate.parse("1986-08-06"), "494"),
                        student("sidorov", LocalDate.parse("1986-08-06"), "495"),
                        student("smith", LocalDate.parse("1986-08-06"), "495"),
                        student("petrov", LocalDate.parse("2006-08-06"), "494")))
                        .select(Statistics.class, group, countGroup, avgGroup)
                        .where(rlike(Student::getName, ".*ov").and(s -> s.age() > 20))
                        .groupBy(group)
                        .having(s -> s.getCount() > 0)
                        .orderBy(asc(Statistics::getGroup), desc(Statistics::getCount))
                        .limit(100)
                        .union()
                        .from(list(student("ivanov", LocalDate.parse("1985-08-06"), "494")))
                        .selectDistinct(Statistics.class, s -> "all", count(s -> 1), avg(Student::age))
                        .execute();

    }

    @Test
    public void testJoin() throws Exception {
        Tuple<String, String> tup = new Tuple<String, String>("", "");
        Iterable<Tuple> names =
                from(list(
                        student("ivanova", LocalDate.parse("1985-08-06"), "494"),
                        student("ivanov", LocalDate.parse("1985-08-06"), "495")
                ))
                        .join(list(
                                student("katya", LocalDate.parse("1985-08-06"), "494"),
                                student("ivashka", LocalDate.parse("1985-08-06"), "495")))
                        .on((s, g) -> Objects.equals(s.getGroup(), g.getGroup()))
                        .select(Tuple.class, sg -> sg.getFirst().getName(), sg -> sg.getSecond().getName())
                        .execute();
        int katya = 0, ivashka = 0;
        for (Tuple item : names) {
            katya += (item.toString().equals("(ivanova, katya)") ? 1 : 0);
            ivashka += (item.toString().equals("(ivanov, ivashka)") ? 1 : 0);
        }
        assertTrue(katya == 1 && ivashka == 1);
    }

    @Test
    public void testJoin2() throws Exception {
        Iterable<Tuple> names =
                from(list(
                        student("ivanova", LocalDate.parse("1985-08-06"), "494"),
                        student("ivanov", LocalDate.parse("1985-08-06"), "495")
                ))
                        .join(list(
                                student("katya", LocalDate.parse("1985-08-06"), "494"),
                                student("ivashka", LocalDate.parse("1985-08-06"), "495")))
                        .on(Student::getGroup, Student::getGroup)
                        .select(Tuple.class, sg -> sg.getFirst().getName(), sg -> sg.getSecond().getName())
                        .execute();
        int katya = 0, ivashka = 0;
        for (Tuple item : names) {
            katya += (item.toString().equals("(ivanova, katya)") ? 1 : 0);
            ivashka += (item.toString().equals("(ivanov, ivashka)") ? 1 : 0);
        }
        assertTrue(katya == 1 && ivashka == 1);
    }

    @Test
    public void testJoinException() throws Exception {
        thrown.expect(JoinOnNotPrimaryKeyException.class);
        Iterable<Tuple> names =
                from(list(
                        student("ivanova", LocalDate.parse("1985-08-06"), "494"),
                        student("ivanov", LocalDate.parse("1985-08-06"), "494")
                ))
                        .join(list(
                                student("katya", LocalDate.parse("1985-08-06"), "494"),
                                student("ivashka", LocalDate.parse("1985-08-06"), "495")))
                        .on(Student::getGroup, Student::getGroup)
                        .select(Tuple.class, sg -> sg.getFirst().getName(), sg -> sg.getSecond().getName())
                        .execute();
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
            return ChronoUnit.YEARS.between(getDateOfBith(), LocalDateTime.now());
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
            return "Statistics{"
                    + "group='" + group + '\''
                    + ", count=" + count
                    + ", age=" + age
                    + '}';
        }
    }

}
