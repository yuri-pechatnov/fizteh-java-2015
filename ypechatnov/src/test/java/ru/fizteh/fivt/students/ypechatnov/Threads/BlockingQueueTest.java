package ru.fizteh.fivt.students.ypechatnov.Threads;

import org.junit.Test;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.*;
import org.mockito.runners.*;


import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.StreamSupport;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import java.util.Arrays;

/**
 * Created by ura on 08.12.15.
 */
public class BlockingQueueTest {
    @Test
    public void testOneThread() {
        BlockingQueue<Integer> queue = new BlockingQueue<Integer>(3);
        queue.offer(Arrays.asList(1));
        queue.offer(Arrays.asList(2, 3));
        assertTrue(queue.take(1).get(0) == 1);
        assertTrue(queue.take(1).get(0) == 2);
        assertTrue(queue.take(1).get(0) == 3);
        queue.offer(Arrays.asList(1, 2, 3));
        assertTrue(queue.take(3).get(1) == 2);
    }

    @Test(timeout = 150)
    public void testTwoThreads() {
        BlockingQueue<Integer> queue = new BlockingQueue<Integer>(3);
        queue.offer(Arrays.asList(1));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    queue.offer(Arrays.asList());
                }
                queue.offer(Arrays.asList(2, 3));
            }
        }).start();
        queue.take(2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    queue.offer(Arrays.asList());
                }
                queue.offer(Arrays.asList(4, 5));
                queue.take(3);
            }
        }).start();
        queue.offer(Arrays.asList(7, 7, 7));
    }

    @Test(timeout = 50)
    public void testTimeout() {
        BlockingQueue<Integer> queue = new BlockingQueue<Integer>(3);
        queue.offer(Arrays.asList(7, 7, 7, 7), 10);
        queue.take(4, 10);
    }


}
