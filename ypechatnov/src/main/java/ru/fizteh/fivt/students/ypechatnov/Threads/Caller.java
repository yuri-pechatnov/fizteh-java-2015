package ru.fizteh.fivt.students.ypechatnov.Threads;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ura on 08.12.15.
 */
public class Caller {
    private static Integer amount;
    private static Integer currentIteration;
    private static Boolean currentAnswer;
    private static Lock lock = new ReentrantLock(true);
    private static Condition needWrite = lock.newCondition();
    private static CountDownLatch countDownLatch;
    private static Thread[] threads;

    public static void main(String[] args) throws Exception {
        amount = Integer.valueOf(args[0]);
        threads = new Thread[amount];
        for (int i = 0; i < amount; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    Integer iteration = 0;
                    Random random = new Random();
                    while (true) {
                        lock.lock();
                        try {
                            while (!currentIteration.equals(iteration)) {
                                needWrite.await();
                                if (Thread.interrupted()) {
                                    System.err.println("Interrupting thread");
                                    return;
                                }
                            }
                            if (random.nextInt() % 10 == 0) {
                                synchronized (System.out) {
                                    System.out.print("No(" + iteration + ") ");
                                    currentAnswer = false;
                                }
                            }
                            else {
                                synchronized (System.out) {
                                    System.out.print("Yes(" + iteration + ") ");
                                }
                            }
                            countDownLatch.countDown();
                            ++iteration;
                        } catch (InterruptedException e) {
                            System.err.println("Interrupted error in thread" );
                            return;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            });
        }
        currentIteration = -1;
        for (Thread thread : threads) {
            thread.start();
        }
        while (true) {
            synchronized (System.out) {
                System.out.println("Are you ready?");
            }
            lock.lock();
            countDownLatch = new CountDownLatch(amount);
            currentAnswer = true;
            currentIteration++;
            needWrite.signalAll();
            lock.unlock();
            countDownLatch.await();
            synchronized (System.out) {
                System.out.println("");
            }
            if (currentAnswer) {
                break;
            }
        }
        System.out.println("Mission complete");
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

}
