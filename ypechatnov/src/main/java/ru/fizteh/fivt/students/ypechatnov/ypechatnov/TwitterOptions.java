package main.java.ru.fizteh.fivt.students.ypechatnov.ypechatnov;

/**
 * Created by ura on 27.09.15.
 */


import com.beust.jcommander.*;
import main.java.ru.fizteh.fivt.students.ypechatnov.ypechatnov.NonNegativeInteger;
import main.java.ru.fizteh.fivt.students.ypechatnov.ypechatnov.TwitterParameterException;
import main.java.ru.fizteh.fivt.students.ypechatnov.ypechatnov.exceptions.TwitterParameterException;
import main.java.ru.fizteh.fivt.students.ypechatnov.ypechatnov.validators.NonNegativeInteger;


public class TwitterOptions {
    @Parameter(names = {"-q", "--query"}, description = "Query"
            + "Example \"Физтех\"")
    private String twquery;

    public boolean isSetQuery() {
        return twquery != null;
    }
    public String getQuery() {
        if (twquery != null) {
            return twquery;
        } else {
            return "";
        }
    }

    @Parameter(names = {"-p", "--place"}, description = "Place of tweets")
    private String place;

    public boolean isSetPlace() {
        return place != null;
    }
    public String getPlace() {
        return place;
    }

    @Parameter(names = {"-s", "--stream"}, description = "Is streaming or not"
            + "Incompatible with --limit option")
    private boolean asStream;

    public boolean isStreaming() {
        return asStream;
    }

    @Parameter(names = {"--hideRetweets"}, description = "Hide retweets")
    private boolean hideRetweets;

    public boolean isHidingRetweets() {
        return hideRetweets;
    }

    @Parameter(names = {"-l", "--limit"}, description = "Amount tweets"
            + "Incompatible with --stream option.",
            validateWith = NonNegativeInteger.class)
    private Integer limitint = -1;

    public boolean isSetLimit() {
        return limitint != null && limitint != -1;
    }
    public int getLimit() {
        return limitint;
    }

    @Parameter(names = {"-h", "--help"}, description = "Help. "
            + "For extra information see: "
            + "https://github.com/akormushin/fizteh-java-2015/blob/master/tasks/01-TwitterStream.md",
            help = true)
    private boolean showHelp;

    public boolean isNeedToShowHelp() {
        return showHelp;
    }

    private JCommander jc;

    public void usage() {
        jc.usage();
    }

    public TwitterOptions parse(String[] args) throws TwitterParameterException {
        jc = new JCommander(this);
        jc.setProgramName("TwitterStream");
        try {
            jc.parse(args);
        } catch (Exception exc) {
            throw new TwitterParameterException();
        }
        if (isSetLimit() && isStreaming()) {
            throw new TwitterParameterException();
        }
        return this;
    }

    TwitterOptions() { }
}
