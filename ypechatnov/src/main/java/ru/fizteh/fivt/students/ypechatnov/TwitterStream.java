package main.java.ru.fizteh.fivt.students.ypechatnov;

//import java.io.IOException;
import java.lang.Thread.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import main.java.ru.fizteh.fivt.students.ypechatnov.exceptions.PlaceNotFoundException;
import main.java.ru.fizteh.fivt.students.ypechatnov.exceptions.TwitterParameterException;
import twitter4j.*;

public class TwitterStream {
    private static TwitterOptions opt;
    private static Twitter twitter;
    private static Query query;
    private static FilterQuery filterQuery;
    private static QueryResult queryResult;
    private static twitter4j.TwitterStream ts;

    public enum ShowTime {
        yes, no
    };

    public static void main(String[] args) {
        opt = new TwitterOptions();
        try {
            opt.parse(args);
        } catch (TwitterParameterException e) {
            opt.usage();
            return;
        }
        if (opt.isNeedToShowHelp()) {
            opt.usage();
            return;
        }

        if (opt.isStreaming()) {
            try {
                startStreaming();
            } catch (InterruptedException e) {
                System.err.println("Interrupt occured\n"
                    + "Streaming finished =(");
                System.err.println(e);
                System.exit(-1);
            }
        } else {
            genAndShowResult();
        }
    }

    private static void startStreaming() throws InterruptedException {
        ts = new TwitterStreamFactory().getInstance();
        TwitterListener tl = new TwitterListener();
        ts.addListener(tl.init());
        makeFilterQuery();
        ts.filter(filterQuery);
        final Integer ms2s = new Integer(1000);
        while (true) {
            Thread.sleep(ms2s);
            System.err.println("Iteration");
            String outStr = tl.pollTweetStr();
            if (outStr != null) {
                System.out.println(outStr);
            }
        }
    }


    private static void makeFilterQuery() {
        filterQuery = new FilterQuery();
        filterQuery.track(opt.getQuery());
        if (opt.isSetPlace()) {
            try {
                YandexPlaces places = new YandexPlaces();
                places.setPlaceQuery(opt.getPlace());
                filterQuery.locations(places.calcBounds());
            } catch (PlaceNotFoundException e) {
                System.err.println("Problem with --place option. Program will be continued without it.");
            }
        }
    }

    public static void genAndShowResult() {
        makeQuery();
        try {
            /*There is a class TwitterConfigurationBuilder (NOW IT'S DISABLED),
                which have method,
                which returns ConfigurationBuilder class with authentication data

                new TwitterFactory(TwitterConfigurationBuilder.getConfig().build())
            */
            twitter = new TwitterFactory().getInstance();
            queryResult = twitter.search(query);
        } catch (TwitterException e) {
            System.err.println("Some problem with query");
            System.exit(-1);
        }
        showTweets(queryResult.getTweets());
    }

    public static void showTweets(List<Status> tweets) {
        if (opt.isHidingRetweets()) {
            tweets = tweets.stream()
                    .filter(p -> !p.isRetweet()).collect(Collectors.toList());
        }
        System.out.println("There are " + String.valueOf(tweets.size())
                + " tweets about your query without limitation");
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
        final long ms2s = 1000L, s2m = 60L, m2h = 60L, h2d = 24L;
        long delta = (System.currentTimeMillis() - date.getTime());
        delta /= ms2s; // Now delta in seconds
        delta /= s2m; // Now delta in minutes
        if (delta < 2L) {
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
        return String.valueOf(delta) + " д" + calcNumEnding(delta, "ень", "ня", "ней") + " назад";
    }

    public static String calcNumEnding(Long number, String p1, String p24, String p50) {
        final Long ten = 10L, one = 1L, five = 5L;
        number = number % ten;
        if (number.equals(one)) {
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
        if (opt.isSetLimit()) {
            query.setCount(opt.getLimit());
        }
        if (opt.isSetPlace()) {
            try {
                YandexPlaces places = new YandexPlaces();
                places.setPlaceQuery(opt.getPlace());
                double[] coord = places.calcCoord();
                System.err.println("Query geo is: lat = " + String.valueOf(coord[1])
                        + " long = " + String.valueOf(coord[0])
                        + " radius = " + String.valueOf(places.calcRadiusKm()));
                query.setGeoCode(new GeoLocation(coord[1], coord[0]), places.calcRadiusKm(), Query.Unit.mi);
            } catch (PlaceNotFoundException e) {
                System.err.println("Sorry but --place option is failed, and run will be continued without it");
            }
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
