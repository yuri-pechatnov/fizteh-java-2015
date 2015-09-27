package main.java.ru.fizteh.fivt.students.ypechatnov;

/**
 * Created by ura on 27.09.15.
 */


import com.beust.jcommander.*;

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
            + "Incompatible with --stream option.")
    private String limit;
    private int limitint;

    public boolean isSetLimit() {
        return limit != null;
    }
    public int getLimit() {
        return limitint;
    }

    @Parameter(names = {"-h", "--help"}, description = "Help. "
            + "For extra information see: "
            + "https://github.com/akormushin/fizteh-java-2015/blob/master/tasks/01-TwitterStream.md",
            help = true)
    private boolean showHelp;

    public boolean isNeedToExit() {
        return showHelp || incorrectOptions;
    }

    private boolean incorrectOptions;

    private JCommander jc;

    TwitterOptions(String[] args) {
        incorrectOptions = false;
        jc = new JCommander(this);
        jc.setProgramName("TwitterStream");
        try {
            jc.parse(args);
        } catch (com.beust.jcommander.ParameterException exc) {
            incorrectOptions = true;
        }
        if (isSetLimit()) {
            try {
                limitint = Integer.valueOf(limit);
            } catch (NumberFormatException e) {
                incorrectOptions = true;
            }
        }
        if (isSetLimit() && isStreaming()) {
            incorrectOptions = true;
        }
        if (incorrectOptions) {
            System.out.println("Incorrect options. Please, read the usage:");
        }
        if (showHelp || incorrectOptions) {
            jc.usage();
            System.exit(0);
        }
    }
}
