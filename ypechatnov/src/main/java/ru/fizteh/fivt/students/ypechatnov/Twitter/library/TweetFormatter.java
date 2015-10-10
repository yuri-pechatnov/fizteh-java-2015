package ru.fizteh.fivt.students.ypechatnov.Twitter.library;

import twitter4j.Status;

import java.util.Date;

/**
 * Created by ura on 05.10.15.
 */
public class TweetFormatter {
    public enum ShowTime {
        yes, no
    };

    public final String
            DATE_HIGHLIGHT_BEGIN = "\u001B[32m",
            DATE_HIGHLIGHT_END = "\u001B[0m",
            USER_HIGHLIGHT_BEGIN = "\u001B[33m",
            USER_HIGHLIGHT_END = "\u001B[0m";

    public String oneTweetToStr(Status tweet, ShowTime showTime) {
        String retweetPart = "";
        if (tweet.isRetweet()) {
            retweetPart = "ретвитнул " + USER_HIGHLIGHT_BEGIN
                    + "@" + tweet.getRetweetedStatus().getUser().getScreenName()
                    + USER_HIGHLIGHT_END + ": ";
            if (retweetPart == null) {
                retweetPart = "";
            }
        }
        return clauseStr(showTime == ShowTime.yes, DATE_HIGHLIGHT_BEGIN + "["
                    + timeInReadableFormat(tweet.getCreatedAt()) + "]" + DATE_HIGHLIGHT_END + " ")
                + USER_HIGHLIGHT_BEGIN + "@" + tweet.getUser().getScreenName() + USER_HIGHLIGHT_END
                + ": " + retweetPart + tweet.getText()
                + clauseStr(tweet.isRetweeted(), " (" + tweet.getRetweetCount() + " ретвит"
                    + calcNumEnding(new Long(tweet.getRetweetCount()), "", "а", "ов") + ")");
    }

    public String timeInReadableFormat(Date date) {
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
        if (delta == 1L) {
            return "вчера";
        }
        return String.valueOf(delta) + " д" + calcNumEnding(delta, "ень", "ня", "ней") + " назад";
    }

    public String calcNumEnding(Long number, String p1, String p24, String p50) {
        final Long ten = 10L, one = 1L, five = 5L;
        if (((number / ten) % ten) == one) {
            return p50;
        }
        number = number % ten;
        if (number.equals(one)) {
            return p1;
        }
        if (one < number && number < five) {
            return p24;
        }
        return p50;
    }

    public String clauseStr(boolean clause, String str) {
        if (clause) {
            return str;
        } else {
            return "";
        }
    }
}
