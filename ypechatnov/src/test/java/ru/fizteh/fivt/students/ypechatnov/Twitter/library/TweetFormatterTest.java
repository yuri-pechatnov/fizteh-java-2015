package test.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library;

/**
 * Created by ura on 05.10.15.
 */


import org.junit.*;
import junit.framework.Assert;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.TweetFormatter;
import twitter4j.*;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class TweetFormatterTest {
    @Test
    public void testClauseStr() {
        Assert.assertEquals(TweetFormatter.clauseStr(false, null), "");
        Assert.assertEquals(TweetFormatter.clauseStr(false, ""), "");
        Assert.assertEquals(TweetFormatter.clauseStr(false, "Uti"), "");
        Assert.assertEquals(TweetFormatter.clauseStr(true, null), null);
        Assert.assertEquals(TweetFormatter.clauseStr(true, ""), "");
        Assert.assertEquals(TweetFormatter.clauseStr(true, "Ohehe"), "Ohehe");
    }
    @Test
    public void testTimeInReadableFormat() {
        final long zero = 0, one = 1, two = 2, four = 4, five = 5,
                ten = 10, twenty = 20, s2m = 60, s2h = 3600, h2d = 24;
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(zero))),
                "только что");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(ten))),
                "только что");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * two))),
                "2 минуты назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * four))),
                "4 минуты назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * five))),
                "5 минут назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2m * (ten + one)))),
                "11 минут назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(
                        Date.from(new Date().toInstant().minusSeconds(s2m * (ten * two + one)))),
                "21 минуту назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h))),
                "1 час назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * two))),
                "2 часа назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * five))),
                "5 часов назад");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * h2d))),
                "вчера");
        Assert.assertEquals(
                TweetFormatter.timeInReadableFormat(Date.from(new Date().toInstant().minusSeconds(s2h * h2d * two))),
                "2 дня назад");
    }
    @Test
    public void testCalcNumEnding() {
        final long zero = 0, one = 1, two = 2, four = 4, five = 5, ten = 10, twenty = 20;
        Assert.assertEquals(TweetFormatter.calcNumEnding(zero, "день", "дня", "дней"), "дней");
        Assert.assertEquals(TweetFormatter.calcNumEnding(one, "день", "дня", "дней"), "день");
        for (long i = two; i <= four; i++) {
            Assert.assertEquals(TweetFormatter.calcNumEnding(i, "день", "дня", "дней"), "дня");
        }
        for (long i = five; i <= twenty; i++) {
            Assert.assertEquals(TweetFormatter.calcNumEnding(i, "день", "дня", "дней"), "дней");
        }
        for (long i = two; i < ten; i++) {
            Assert.assertEquals(TweetFormatter.calcNumEnding(i * ten + one, "день", "дня", "дней"), "день");
            for (long j = two; j <= four; j++) {
                Assert.assertEquals(TweetFormatter.calcNumEnding(i * ten + j, "день", "дня", "дней"), "дня");
            }

            for (long j = five; j <= ten; j++) {
                Assert.assertEquals(TweetFormatter.calcNumEnding(i * ten + j, "день", "дня", "дней"), "дней");
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
        result = TweetFormatter.oneTweetToStr(status, TweetFormatter.ShowTime.no);
        Assert.assertEquals(result,
                TweetFormatter.USER_HIGHLIGHT_BEGIN + "@Vasya Pupkin" + TweetFormatter.USER_HIGHLIGHT_END
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
        result = TweetFormatter.oneTweetToStr(status, TweetFormatter.ShowTime.yes);
        Assert.assertEquals(result,
                TweetFormatter.DATE_HIGHLIGHT_BEGIN + "[только что]" + TweetFormatter.DATE_HIGHLIGHT_END + " "
                        + TweetFormatter.USER_HIGHLIGHT_BEGIN + "@Volk" + TweetFormatter.USER_HIGHLIGHT_END
                        + ": ретвитнул "
                        + TweetFormatter.USER_HIGHLIGHT_BEGIN + "@Lisa" + TweetFormatter.USER_HIGHLIGHT_END
                        + ": Medved is the best friend of us (100500 ретвитов)");


    }
}
