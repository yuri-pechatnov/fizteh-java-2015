package main.java.ru.fizteh.fivt.students.ypechatnov;

//import java.io.IOException;
import java.lang.Thread.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import twitter4j.*;

public class TwitterStream {
    private static TwitterOptions opt;
    private static TwitterFactory tf;
    private static Twitter twitter;
    private static Query query;
    private static FilterQuery filterQuery;
    private static QueryResult queryResult;
    private static twitter4j.TwitterStream ts;

    public enum ShowTime {
        yes, no
    };

    public static void main(String[] args) throws TwitterException {
        opt = new TwitterOptions(args);
        if (opt.isNeedToExit()) {
            System.exit(0);
        }

        /*There is a class TwitterConfigurationBuilder, NOW IT'S DISABLED
            which have method,
            which returns ConfigurationBuilder class with authentication data */
        tf = new TwitterFactory(/*TwitterConfigurationBuilder.getConfig().build()*/);
        twitter = tf.getInstance();

        if (opt.isStreaming()) {
            startStreaming();
        } else {
            genAndShowResult();
        }
        System.exit(0);
    }

    private static void startStreaming() {
        ts = new TwitterStreamFactory().getInstance();
        TwitterListener tl = new TwitterListener();
        byte[] smb = {'g'};
        tl.init();
        ts.addListener(tl);
        makeFilterQuery();
        ts.filter(filterQuery);
        final Integer ms2s = new Integer(1000);
        try {
            while (true) {
                Thread.sleep(ms2s);
                System.err.println("Iteration");
                String outStr = tl.pollTweetStr();
                if (outStr != null) {
                    System.out.println(outStr);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupt occured");
            System.exit(-1);
        }
    }


    private static void makeFilterQuery() {
        filterQuery = new FilterQuery();
        filterQuery.track(opt.getQuery());
        if (opt.isSetPlace()) {
            YandexPlaces places = new YandexPlaces(opt.getPlace());
            if (!places.isSmthFailed()) {
                filterQuery.locations(places.calcBounds());
            } else {
                System.err.println("Problem with --place option. Program will be continued without it.");
            }
        }
    }

    public static void genAndShowResult() {
        makeQuery();
        try {
            queryResult = twitter.search(query);
        } catch (TwitterException e) {
            System.err.println("Some problem with query");
            System.exit(-1);
        }
        showTweets(queryResult.getTweets());
    }

    public static void showTweets(List<Status> tweets) {
        System.out.println("Ururu");
        if (opt.isHidingRetweets()) {
            tweets = tweets.stream()
                    .filter(p -> !p.isRetweet()).collect(Collectors.toList());
        }
        System.out.println(tweets.size());
        if (opt.isSetLimit()) {
            tweets = tweets.subList(0, Math.min(tweets.size(), opt.getLimit()));
        }
        if (tweets.size() > 0) {
            for (Status t : tweets) {
                showOneTweet(t, ShowTime.yes);
            }
        } else {
            System.out.println("There are not any tweets.");
        }
    }

    public static String oneTweetToStr(Status tweet, ShowTime showTime) {
        final String dateHighlightBegin = "\u001B[32m", dateHighlightEnd = "\u001B[0m",
                userHighlightBegin = "\u001B[33m", userHighlightEnd = "\u001B[0m";
        String retweetPart = "";
        if (tweet.isRetweet()) {
            retweetPart = userHighlightBegin
                    + "ретвитнул @" + tweet.getRetweetedStatus().getUser().getScreenName()
                    + userHighlightEnd + ": ";
            if (retweetPart == null) {
                retweetPart = "";
            }
        }
        return clauseStr(showTime == ShowTime.yes, dateHighlightBegin + "["
                        + timeInReadableFormat(tweet.getCreatedAt()) + "]" + dateHighlightEnd + " ")
                        + userHighlightBegin + "@" + tweet.getUser().getScreenName()
                        + userHighlightEnd + ": " + retweetPart + tweet.getText()
                        + clauseStr(tweet.isRetweeted(), "(" + tweet.getRetweetCount() + " ретвит"
                        + calcNumEnding(new Long(tweet.getRetweetCount()), "", "а", "ов"));
    }

    private static void showOneTweet(Status tweet, ShowTime showTime) {
        System.out.println(oneTweetToStr(tweet, showTime));
    }

    private static String timeInReadableFormat(Date date) {
        final long ms2s = 1000, s2m = 60, m2h = 60, h2d = 24;
        long delta = (System.currentTimeMillis() - date.getTime());
        delta /= ms2s; // Now delta in seconds
        delta /= s2m; // Now delta in minutes
        if (delta < new Long(2L)) {
            return "только что";
        }
        if (delta < m2h) {
            return String.valueOf(delta) + " минут" + calcNumEnding(delta, "у", "ы", "") + " назад";
        }
        delta /= m2h; // Now in hours
        if (delta < h2d) {
            return String.valueOf(delta) + " час" + calcNumEnding(delta, "", "а", "ов") + " назад";
        }
        delta /= h2d; // Now in days
        return String.valueOf(delta) + " " + calcNumEnding(delta, "день", "дня", "дней") + " назад";
    }

    public static String calcNumEnding(Long number, String p1, String p24, String p50) {
        final Long ten = 10L, one = 1L, five = 5L;
        number = number % ten;
        if (number == one) {
            return p1;
        }
        if (one < number && number < five) {
            return p24;
        }
        return p50;
    }

    public static void makeQuery() {
        System.err.println("QueryStr is: " + opt.getQuery() + clauseStr(opt.isHidingRetweets(),
                clauseStr(opt.getQuery().length() > 0, "+") + "exclude:retweets"));
        System.err.println(opt.getQuery().length());
        query = new Query(opt.getQuery() + clauseStr(opt.isHidingRetweets(),
                clauseStr(opt.getQuery().length() > 0, "+") + "exclude:retweets"));
        if (opt.isSetPlace()) {
            YandexPlaces places = new YandexPlaces(opt.getPlace());
            double[] coord = places.calcCoord();
            if (!places.isSmthFailed()) {
                System.err.println("Query geo is: lat = " + String.valueOf(coord[1])
                                + " long = " + String.valueOf(coord[0])
                                + " radius = " + String.valueOf(places.calcRadiusKm()));
                query.setGeoCode(new GeoLocation(coord[1], coord[0]), places.calcRadiusKm(), Query.Unit.mi);
            } else {
                System.err.println("Sorry but --place option is failed, and run will be continued without it");
            }
            System.out.print(coord[0]);
            System.out.print(" ");
            System.out.println(coord[1]);
        }
    }

    private static String clauseStr(boolean clause, String str) {
        if (clause) {
            return str;
        } else {
            return "";
        }
    }
}
/*
    twitter4j.properties класть в src/main/resources
    Добавить в git.ignore

    @Override
    public void onStatus(Status status) {
    Что такое @Override?

    В java можно рантайм получать метаинформацию о классе

 */
