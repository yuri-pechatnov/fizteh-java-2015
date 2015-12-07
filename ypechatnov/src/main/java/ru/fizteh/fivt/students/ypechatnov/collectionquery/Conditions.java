package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Where clause conditions.
 *
 * @author akormushin
 */
public class Conditions<T> {

    /**
     * Matches string result of expression against regexp pattern.
     *
     * @param expression expression result to match
     * @param regexp     pattern to match to
     * @param <T>        source object type
     * @return
     */
    public static <T> Predicate<T> rlike(Function<T, String> expression, String regexp) {
        if (regexp == null) {
            return item -> false;
        }
        Pattern pattern = Pattern.compile(regexp);
        System.out.println();
        return item -> pattern.matcher(expression.apply(item)).matches();
    }

    /**
     * Matches string result of expression against SQL like pattern.
     *
     * @param expression expression result to match
     * @param pattern    pattern to match to
     * @param <T>        source object type
     * @return
     */
    public static <T> Predicate<T> like(Function<T, String> expression, String pattern) {
        if (pattern == null) {
            return item -> false;
        }
        return item -> pattern.equals(expression.apply(item));
    }

}
