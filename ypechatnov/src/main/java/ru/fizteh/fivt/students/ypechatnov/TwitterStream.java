package main.java.ru.fizteh.fivt.students.ypechatnov;

import java.util.*;
import java.util.concurrent.*;
import twitter4j.*;

public class TwitterStream {
    private static TwitterOptions opt;
    private static TwitterFactory tf;
    private static Twitter twitter;
    private static Query query;
    private static QueryResult queryResult;

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

        makeQuery();
        if (opt.isStreaming()) {
            startStreaming();
        } else {
            genAndShowResult();
        }
        System.exit(0);
    }

    public static void startStreaming() {

    }

    public static void genAndShowResult() {
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
        System.out.println(tweets.size());
        if (opt.isSetLimit()) {
            tweets = tweets.subList(0, Math.min(tweets.size(), opt.getLimit()));
        }
        if (tweets.size() > 0) {
            for (Status t : tweets) {
                showOneTweetWithTime(t);
            }
        }
    }

    public static void showOneTweetWithTime(Status tweet) {
        String out = "[" + timeInReadableFormat(tweet.getCreatedAt()) + "] + @"
                + tweet.getUser().getScreenName() + ": ";
        if (tweet.isRetweet()) {
            out += "ретвитнул @" + tweet.getRetweetedStatus().getUser().getScreenName() + ": ";
        }
        out += tweet.getText();
        if (tweet.isRetweeted()) {
            String retweetWordEnd = "ов";
            final Integer ten = 10, five = 5, one = 1;
            if (tweet.getRetweetCount() % ten == one) {
                retweetWordEnd = "";
            }
            if (tweet.getRetweetCount() % ten > one
                    && tweet.getRetweetCount() % ten < five) {
                retweetWordEnd = "а";
            }
            out += "(" + tweet.getRetweetCount() + " ретвит" + retweetWordEnd + ")";
        }
        System.out.println(out);
    }

    public static String timeInReadableFormat(Date date) {
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
        String iAlwaysHateCheckstyle = ""; // I like (clause ? val1 : val2) in obvious cases
        if (opt.isHidingRetweets()) {
            iAlwaysHateCheckstyle = "+exclude:retweets";
        }
        System.err.println("Query str is: " + opt.getQuery() + iAlwaysHateCheckstyle);
        query = new Query("физтех"/*opt.getQuery() + iAlwaysHateCheckstyle*/);
        if (opt.isSetPlace()) {
            YandexPlaces places = new YandexPlaces(opt.getPlace());
            double[] coord = places.calcCoord();
            if (!places.isSmthFailed()) {
                System.err.println("Query geo is: lt = " + String.valueOf(coord[0])
                                + " at = " + String.valueOf(coord[1])
                                + " radius = " + String.valueOf(places.calcRadiusKm()));
                query.setGeoCode(new GeoLocation(coord[0], coord[1]), places.calcRadiusKm(), Query.Unit.km);
            } else {
                System.err.println("Sorry but --place option is failed, and run will be continued without it");
            }
            System.out.print(coord[0]);
            System.out.print(" ");
            System.out.println(coord[1]);
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
