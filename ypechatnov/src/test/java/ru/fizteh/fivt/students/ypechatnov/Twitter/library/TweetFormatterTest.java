package ru.fizteh.fivt.students.ypechatnov.Twitter.library;

/**
 * Created by ura on 05.10.15.
 */


import org.junit.*;
import junit.framework.Assert;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;
import ru.fizteh.fivt.students.ypechatnov.Twitter.library.TweetFormatter;
import twitter4j.*;

import java.util.Date;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class TweetFormatterTest {
    TweetFormatter tweetFormatter;

    @Before
    public void init() {
        tweetFormatter = new TweetFormatter();
    }

    @Test
    public void testClauseStr() {
        assertEquals(tweetFormatter.clauseStr(false, null), "");
        assertEquals(tweetFormatter.clauseStr(false, ""), "");
        assertEquals(tweetFormatter.clauseStr(false, "Uti"), "");
        assertEquals(tweetFormatter.clauseStr(true, null), null);
        assertEquals(tweetFormatter.clauseStr(true, ""), "");
        assertEquals(tweetFormatter.clauseStr(true, "Ohehe"), "Ohehe");
    }
    @Test
    @Ignore
    public void testTimeInReadableFormat() {
        final long zero = 0, one = 1, two = 2, four = 4, five = 5,
                ten = 10, twenty = 20, s2m = 60, s2h = 3600, h2d = 24;
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(zero))),
                "только что");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(ten))),
                "только что");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * two))),
                "2 минуты назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * four))),
                "4 минуты назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * five))),
                "5 минут назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * (ten + one)))),
                "11 минут назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(
                        Date.from(new Date().toInstant().minusSeconds(s2m * (ten * two + one)))),
                "21 минуту назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h))),
                "1 час назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * two))),
                "2 часа назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * five))),
                "5 часов назад");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * h2d))),
                "вчера");
        assertEquals(
                tweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * h2d * two))),
                "2 дня назад");
    }
    @Test
    public void testCalcNumEnding() {
        final long zero = 0, one = 1, two = 2, four = 4, five = 5, ten = 10, twenty = 20;
        assertEquals(tweetFormatter.calcNumEnding(zero, "день", "дня", "дней"), "дней");
        assertEquals(tweetFormatter.calcNumEnding(one, "день", "дня", "дней"), "день");
        for (long i = two; i <= four; i++) {
            assertEquals(tweetFormatter.calcNumEnding(i, "день", "дня", "дней"), "дня");
        }
        for (long i = five; i <= twenty; i++) {
            assertEquals(tweetFormatter.calcNumEnding(i, "день", "дня", "дней"), "дней");
        }
        for (long i = two; i < ten; i++) {
            assertEquals(tweetFormatter.calcNumEnding(i * ten + one, "день", "дня", "дней"), "день");
            for (long j = two; j <= four; j++) {
                assertEquals(tweetFormatter.calcNumEnding(i * ten + j, "день", "дня", "дней"), "дня");
            }

            for (long j = five; j <= ten; j++) {
                assertEquals(tweetFormatter.calcNumEnding(i * ten + j, "день", "дня", "дней"), "дней");
            }
        }
    }

    @Test
    public void testOneTweetToStr() {
        Status status = mock(Status.class), retstatus = mock(Status.class);
        User user = mock(User.class), retuser = mock(User.class);
        String result;
        final Integer greatInteger = 100500;
        // Minimal test
        when(status.isRetweet()).thenReturn(false);
        when(status.getCreatedAt()).thenReturn(new Date());
        when(status.getUser()).thenReturn(user);
        when(user.getScreenName()).thenReturn("Vasya Pupkin");
        when(status.getText()).thenReturn("Petya is the best friend of mine");
        when(status.isRetweeted()).thenReturn(false);
        when(status.getRetweetCount()).thenReturn(-1);
        result = tweetFormatter.oneTweetToStr(status, TweetFormatter.ShowTime.no);
        assertEquals(result,
                tweetFormatter.USER_HIGHLIGHT_BEGIN + "@Vasya Pupkin" + tweetFormatter.USER_HIGHLIGHT_END
                        + ": Petya is the best friend of mine");
        // Maximal test
        Date curDate = new Date();
        when(status.isRetweet()).thenReturn(true);
        when(status.getRetweetedStatus()).thenReturn(retstatus);
        when(retstatus.getUser()).thenReturn(retuser);
        when(retuser.getScreenName()).thenReturn("Lisa");
        when(status.getCreatedAt()).thenReturn(curDate);
        when(status.getUser()).thenReturn(user);
        when(user.getScreenName()).thenReturn("Volk");
        when(status.getText()).thenReturn("Medved is the best friend of us");
        when(status.isRetweeted()).thenReturn(true);
        when(status.getRetweetCount()).thenReturn(greatInteger);
        result = tweetFormatter.oneTweetToStr(status, TweetFormatter.ShowTime.yes);
        assertEquals(result,
                tweetFormatter.DATE_HIGHLIGHT_BEGIN + "[только что]" + tweetFormatter.DATE_HIGHLIGHT_END + " "
                        + tweetFormatter.USER_HIGHLIGHT_BEGIN + "@Volk" + tweetFormatter.USER_HIGHLIGHT_END
                        + ": ретвитнул "
                        + tweetFormatter.USER_HIGHLIGHT_BEGIN + "@Lisa" + tweetFormatter.USER_HIGHLIGHT_END
                        + ": Medved is the best friend of us (100500 ретвитов)");


    }
}
