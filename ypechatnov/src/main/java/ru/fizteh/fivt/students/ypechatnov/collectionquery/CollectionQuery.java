package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.exceptions.CollectionException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.function.Function;

import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.avg;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.count;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Aggregates.max;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.CollectionQuery.Student.student;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Conditions.rlike;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.OrderByConditions.asc;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.OrderByConditions.desc;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.Sources.list;
import static ru.fizteh.fivt.students.ypechatnov.collectionquery.impl.FromStmt.from;

/**
 * @author akormushin
 */
public class CollectionQuery {

    /**
     * Make this code work!
     *
     * @param args
     */
    public static void main(String[] args) {
        Function<Student, ?> name = Student::getName, date = Student::getDateOfBith,
                group = Student::getGroup, maxGroup = max(Student::getGroup),
                countGroup = count(Student::getGroup), avgGroup = avg(Student::age);
        try {
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
            System.out.println(statistics);
        } catch (Exception e) {
            System.err.println(e);
        }
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
