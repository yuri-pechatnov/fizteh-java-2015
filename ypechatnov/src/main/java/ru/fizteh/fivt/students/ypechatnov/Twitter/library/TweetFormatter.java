package ru.fizteh.fivt.students.ypechatnov.Twitter.library;

import twitter4j.Status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by ura on 05.10.15.
 */
public class TweetFormatter {
    public enum ShowTime {
        yes, no
    };

    public static final String
            DATE_HIGHLIGHT_BEGIN = "\u001B[32m",
            DATE_HIGHLIGHT_END = "\u001B[0m",
            USER_HIGHLIGHT_BEGIN = "\u001B[33m",
            USER_HIGHLIGHT_END = "\u001B[0m";

    public String oneTweetToStr(Status tweet, ShowTime showTime) {
        String retweetPart = "";
        if (tweet.isRetweet()) {
            retweetPart = new StringBuilder().append("ретвитнул ").append(USER_HIGHLIGHT_BEGIN)
                    .append("@").append(tweet.getRetweetedStatus().getUser().getScreenName())
                    .append(USER_HIGHLIGHT_END).append(": ").toString();
            if (retweetPart == null) {
                retweetPart = "";
            }
        }
        return new StringBuilder().append(
                clauseStr(showTime == ShowTime.yes, DATE_HIGHLIGHT_BEGIN + "["
                    + timeInReadableFormat(tweet.getCreatedAt()) + "]" + DATE_HIGHLIGHT_END + " "))
                .append(USER_HIGHLIGHT_BEGIN).append("@").append(tweet.getUser().getScreenName())
                .append(USER_HIGHLIGHT_END).append(": ").append(retweetPart).append(tweet.getText())
                .append(
                    clauseStr(tweet.isRetweeted(), " (" + tweet.getRetweetCount() + " ретвит"
                    + calcNumEnding(new Long(tweet.getRetweetCount()), "", "а", "ов") + ")"))
                .toString();
    }

    public String timeInReadableFormat(Date date) {
        final long ms2s = 1000L, s2m = 60L, m2h = 60L, h2d = 24L;

        LocalDateTime tweetTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                nowTime = LocalDateTime.now();

        long minuteDifference = ChronoUnit.MINUTES.between(tweetTime, nowTime);
        long hourDifference = ChronoUnit.HOURS.between(tweetTime, nowTime);
        long daysDifference = tweetTime.toLocalDate().until(nowTime.toLocalDate(), ChronoUnit.DAYS);

        long deltaMinutes = ChronoUnit.MINUTES.between(tweetTime, nowTime);
        if (deltaMinutes < 2L) {
            return "только что";
        }
        long deltaHours = ChronoUnit.HOURS.between(tweetTime, nowTime);
        if (deltaHours == 0L) {
            return new StringBuilder().append(deltaMinutes).append(" минут")
                    .append(calcNumEnding(deltaMinutes, "у", "ы", "")).append(" назад").toString();
        }
        long deltaDays = tweetTime.toLocalDate().until(nowTime.toLocalDate(), ChronoUnit.DAYS);
        if (deltaDays != 0L) {
            if (deltaDays == 1L) {
                return "вчера";
            } else {
                return new StringBuilder().append(deltaDays).append(" д")
                        .append(calcNumEnding(deltaDays, "ень", "ня", "ней")).append(" назад").toString();
            }
        }
        return new StringBuilder().append(deltaHours).append(" час").append(calcNumEnding(deltaHours, "", "а", "ов"))
                    .append(" назад").toString();
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
