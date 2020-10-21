/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/*
 * Any changes or additions made by the maintainers of the
 * streamsupport (https://github.com/stefan-zobel/streamsupport)
 * or retrostreams (https://github.com/retrostreams) libraries are
 * also released to the public domain, as explained at
 * https://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * @test
 * @summary stress test for register/arriveAndDeregister
 * @run main FickleRegister 300
 */
package org.openjdk.other.tests.phaser;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import java8.util.concurrent.Phaser;

import org.testng.annotations.Test;

public class FickleRegister {
    final AtomicLong count = new AtomicLong(0);
    final long testDurationMillisDefault = 10_000L;
    final long testDurationMillis;
    final long quittingTimeNanos;
    final int chunkSize = 1000;

    FickleRegister(String[] args) {
        testDurationMillis = (args.length > 0) ?
            Long.valueOf(args[0]) : testDurationMillisDefault;
        quittingTimeNanos = System.nanoTime() +
            testDurationMillis * 1000L * 1000L;
    }

    class Runner extends CheckedRunnable {
        final Phaser p;
        Runner(Phaser phaser) { p = phaser; }
        public void realRun() {
            int prevPhase = -1;
            for (int k = 1;; k++) {
                for (int i = 0; i < chunkSize; i++) {
                    int phase = p.register();
                    if (phase < 0) break;
                    check(phase > prevPhase);
                    prevPhase = phase;
                    equal(phase, p.arriveAndDeregister());
                    check(phase < p.awaitAdvance(phase));
                }
                if (System.nanoTime() - quittingTimeNanos > 0) {
                    count.getAndAdd(k * chunkSize);
                    break;
                }
            }
        }
    }

    void test(String[] args) throws Throwable {
        final Phaser parent = new Phaser() {
                protected boolean onAdvance(int phase, int parties) {
                    return false;
                }
            };

        final Phaser child1 = new Phaser(parent);
        final Phaser child2 = new Phaser(parent);
        final Phaser subchild1 = new Phaser(child1);
        final Phaser subchild2 = new Phaser(child2);
        final Phaser[] phasers = {
            parent, child1, child2, subchild1, subchild2
        };

        int reps = 4;
        ArrayList<Thread> threads = new ArrayList<>();
        for (int j = 0; j < reps; ++j) {
            threads.add(new Thread(new Runner(subchild1)));
            threads.add(new Thread(new Runner(child1)));
            threads.add(new Thread(new Runner(parent)));
            threads.add(new Thread(new Runner(child2)));
            threads.add(new Thread(new Runner(subchild2)));
        }

        for (Thread thread : threads)
            thread.start();

        for (Thread thread : threads)
            thread.join();

        System.out.println("Parent:    " + parent);
        System.out.println("Child1:    " + child1);
        System.out.println("Child2:    " + child2);
        System.out.println("Subchild1: " + subchild1);
        System.out.println("Subchild2: " + subchild2);
        System.out.println("Iterations:" + count.get());

        for (Phaser phaser : phasers) {
            check(phaser.getPhase() > 0);
            equal(0, phaser.getRegisteredParties());
            equal(0, phaser.getUnarrivedParties());
            equal(parent.getPhase(), phaser.getPhase());
        }
    }

    //--------------------- Infrastructure ---------------------------
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}

    @Test
    public static void test() {
        main(new String[]{});
    }

    public static void main(String[] args) {
        new FickleRegister(args).instanceMain(args);}

    public void instanceMain(String[] args) {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError(failed + " tests failed");}

    abstract class CheckedRunnable implements Runnable {
        protected abstract void realRun() throws Throwable;

        public final void run() {
            try {realRun();} catch (Throwable t) {unexpected(t);}
        }
    }
}
