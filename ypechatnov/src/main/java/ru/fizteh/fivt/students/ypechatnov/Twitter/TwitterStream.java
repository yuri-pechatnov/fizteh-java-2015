package main.java.ru.fizteh.fivt.students.ypechatnov.Twitter;

//import java.io.IOException;
import java.util.*;
import java.util.stream.*;

import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.TweetFormatter;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.TwitterListener;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.TwitterOptions;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.YandexPlaces;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.exceptions.PlaceNotFoundException;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.exceptions.TwitterParameterException;
import twitter4j.*;

public class TwitterStream {
    private static TwitterOptions opt;
    private static Twitter twitter;
    private static Query query;
    private static FilterQuery filterQuery;
    private static QueryResult queryResult;
    private static twitter4j.TwitterStream ts;

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
                double bnds[][] = places.calcBounds();
                double revBnds[][] = {{bnds[0][1], bnds[0][0]}, {bnds[1][1], bnds[1][0]}};
                filterQuery.locations(revBnds);
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
                showOneTweet(t, TweetFormatter.ShowTime.yes);
            }
        } else {
            System.out.println("There are not any tweets.");
        }
    }

    private static void showOneTweet(Status tweet, TweetFormatter.ShowTime showTime) {
        System.out.println(TweetFormatter.oneTweetToStr(tweet, showTime));
    }

    public static void makeQuery() {
        System.err.println("QueryStr is: " + opt.getQuery() + TweetFormatter.clauseStr(opt.isHidingRetweets(),
                TweetFormatter.clauseStr(opt.getQuery().length() > 0, "+") + "exclude:retweets"));
        System.err.println(opt.getQuery().length());
        query = new Query(opt.getQuery() + TweetFormatter.clauseStr(opt.isHidingRetweets(),
                TweetFormatter.clauseStr(opt.getQuery().length() > 0, "+") + "exclude:retweets"));
        if (opt.isSetLimit()) {
            query.setCount(opt.getLimit());
        }
        if (opt.isSetPlace()) {
            try {
                YandexPlaces places = new YandexPlaces();
                places.setPlaceQuery(opt.getPlace());
                double[] coord = places.calcCoord();
                /*System.err.println("Query geo is: lat = " + String.valueOf(coord[1])
                        + " long = " + String.valueOf(coord[0])
                        + " radius = " + String.valueOf(places.calcRadiusKm()));*/
                query.setGeoCode(new GeoLocation(coord[1], coord[0]), places.calcRadiusKm(), Query.Unit.mi);
            } catch (PlaceNotFoundException e) {
                System.err.println("Sorry but --place option is failed, and run will be continued without it");
            }
        }
    }

}
