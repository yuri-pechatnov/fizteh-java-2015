package ru.fizteh.fivt.students.ypechatnov.Threads;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ura on 08.12.15.
 */
public class Counter {
    private static Integer amount;
    private static Integer currentId;
    private static Lock lock = new ReentrantLock(true);
    private static Condition needWrite = lock.newCondition();
    private static Thread[] threads;

    public static void main(String[] args) throws Exception {
        amount = Integer.valueOf(args[0]);
        threads = new Thread[amount];
        currentId = 0;
        for (int i = 0; i < amount; i++) {
            threads[i] = new Thread(new Runnable() {
                final Integer id = currentId++;
                Integer count = 0;
                @Override
                public void run() {
                    while (true) {
                        lock.lock();
                        try {
                            while (!currentId.equals(id)) {
                                needWrite.await();
                                if (Thread.interrupted()) {
                                    System.err.println("Interrupting thread #" + id);
                                    return;
                                }
                            }
                            System.out.println("Thread-" + id);
                            currentId = (currentId + 1) % amount;
                            needWrite.signalAll();
                        } catch (InterruptedException e) {
                            System.err.println("Interrupted error in thread #" + id);
                            return;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }
        currentId = -1;
        for (Thread thread : threads) {
            thread.start();
        }
        lock.lock();
        currentId = 0;
        needWrite.signalAll();
        lock.unlock();
        Thread.sleep(1000);
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

}
