package main.java.ru.fizteh.fivt.students.ypechatnov;


import java.util.*;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusDeletionNotice;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by ura on 27.09.15.
 */
public class TwitterListener extends StatusAdapter {
    private ConcurrentLinkedQueue<String> outs;
    private static final int LIMIT = 1000000;

    public TwitterListener init() {
        outs = new ConcurrentLinkedQueue<String>();
        return this;
    }

    public void addTweetStr(String str) {
        // There is no need to store more
        // And memory can run out
        if (outs.size() < LIMIT) {
            outs.add(str);
        }
    }

    public String pollTweetStr() {
        return outs.poll();
    }

    @Override
    public void onStatus(Status status) {
        addTweetStr(TwitterStream.oneTweetToStr(status, TwitterStream.ShowTime.no));
    }
    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        return;
    }
    @Override
    public void onStallWarning(StallWarning warning) {
        System.err.println("Warning:" + warning);
    }
    @Override
    public void onException(Exception ex) {
        System.err.println("Exception: " + ex.getMessage());
    }
    @Override
    public void onScrubGeo(long a, long b) { }
    @Override
    public void onTrackLimitationNotice(int a) { }
}
