package main.java.ru.fizteh.fivt.students.ypechatnov.validators;


import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class NonNegativeInteger implements IParameterValidator {
    public void validate(String name, String value)
            throws ParameterException {
        int n;
        try {
            n = Integer.parseInt(value);
        } catch (Exception e) {
            throw new ParameterException("Parameter " + name
                    + " should be non-negative (found " + value + ")");
        }
        if (n < 0) {
            throw new ParameterException("Parameter " + name
                    + " should be non-negative (found " + value + ")");
        }
    }
}
