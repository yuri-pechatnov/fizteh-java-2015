package test.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library;

import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.TweetFormatter;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.TwitterListener;

import org.junit.*;
import junit.framework.Assert;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;
import twitter4j.*;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class TwitterListenerTest {
    @Test
    public void testListener() {
        TwitterListener listener = new TwitterListener();
        listener.init();
        Status status = mock(Status.class);
        User user = mock(User.class);
        when(status.isRetweet()).thenReturn(false);
        when(status.getCreatedAt()).thenReturn(new Date());
        when(status.getUser()).thenReturn(user);
        when(user.getScreenName()).thenReturn("Vasya Pupkin");
        when(status.getText()).thenReturn("Petya is the best friend of mine");
        when(status.isRetweeted()).thenReturn(false);
        when(status.getRetweetCount()).thenReturn(-1);
        listener.onStatus(status);
        String result = listener.pollTweetStr();
        Assert.assertEquals(result,
                TweetFormatter.USER_HIGHLIGHT_BEGIN + "@Vasya Pupkin" + TweetFormatter.USER_HIGHLIGHT_END
                        + ": Petya is the best friend of mine");
        Assert.assertEquals(listener.pollTweetStr(), null);
    }
}
