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

/* Adapted from Dougs CVS test/jsr166e/LongAdderDemo.java
 *
 * The demo is a micro-benchmark to compare AtomicLong and LongAdder (run
 * without any args), this restricted version simply exercises the basic
 * functionality of LongAdder, suitable for automated testing (-shortrun).
 */

/*
 * @test
 * @bug 8005311
 * @run main LongAdderDemo -shortrun
 * @summary Basic test for LongAdder
 */
package org.openjdk.other.tests.java.util.concurrent.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import java8.util.concurrent.Phaser;
import java8.util.concurrent.atomic.LongAdder;

public class LongAdderDemo {
    static final int INCS_PER_THREAD = 10000000;
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    static final int SHORT_RUN_MAX_THREADS = NCPU > 1 ? NCPU / 2 : 1;
    static final int LONG_RUN_MAX_THREADS = NCPU * 2;
    static final ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public static void test() {
        main(new String[]{});
    }

    public static void main(String[] args) {
        boolean shortRun = args.length > 0 && args[0].equals("-shortrun");
        int maxNumThreads = shortRun ? SHORT_RUN_MAX_THREADS : LONG_RUN_MAX_THREADS;

        System.out.println("Warmup...");
        int half = NCPU > 1 ? NCPU / 2 : 1;
        if (!shortRun)
            casTest(half, 1000);
        adderTest(half, 1000);

        for (int reps = 0; reps < 2; ++reps) {
            System.out.println("Running...");
            for (int i = 1; i <= maxNumThreads; i <<= 1) {
                if (!shortRun)
                    casTest(i, INCS_PER_THREAD);
                adderTest(i, INCS_PER_THREAD);
            }
        }
        pool.shutdown();
    }

    static void casTest(int nthreads, int incs) {
        System.out.print("AtomicLong ");
        Phaser phaser = new Phaser(nthreads + 1);
        AtomicLong a = new AtomicLong();
        for (int i = 0; i < nthreads; ++i)
            pool.execute(new CasTask(a, phaser, incs));
        report(nthreads, incs, timeTasks(phaser), a.get());
    }

    static void adderTest(int nthreads, int incs) {
        System.out.print("LongAdder  ");
        Phaser phaser = new Phaser(nthreads + 1);
        LongAdder a = new LongAdder();
        for (int i = 0; i < nthreads; ++i)
            pool.execute(new AdderTask(a, phaser, incs));
        report(nthreads, incs, timeTasks(phaser), a.sum());
    }

    static void report(int nthreads, int incs, long time, long sum) {
        long total = (long) nthreads * incs;
        if (sum != total)
            throw new Error(sum + " != " + total);
        time = (time == 0L) ? 1L : time;
        double secs = (double) time / (1000L * 1000 * 1000);
        long rate = total * (1000L) / time;
        System.out.printf("threads:%3d  Time: %7.3fsec  Incs per microsec: %4d\n",
                          nthreads, secs, rate);
    }

    static long timeTasks(Phaser phaser) {
        phaser.arriveAndAwaitAdvance();
        long start = System.nanoTime();
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        return System.nanoTime() - start;
    }

    static final class AdderTask implements Runnable {
        final LongAdder adder;
        final Phaser phaser;
        final int incs;
        volatile long result;
        AdderTask(LongAdder adder, Phaser phaser, int incs) {
            this.adder = adder;
            this.phaser = phaser;
            this.incs = incs;
        }

        public void run() {
            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndAwaitAdvance();
            LongAdder a = adder;
            for (int i = 0; i < incs; ++i)
                a.increment();
            result = a.sum();
            phaser.arrive();
        }
    }

    static final class CasTask implements Runnable {
        final AtomicLong adder;
        final Phaser phaser;
        final int incs;
        volatile long result;
        CasTask(AtomicLong adder, Phaser phaser, int incs) {
            this.adder = adder;
            this.phaser = phaser;
            this.incs = incs;
        }

        public void run() {
            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndAwaitAdvance();
            AtomicLong a = adder;
            for (int i = 0; i < incs; ++i)
                a.getAndIncrement();
            result = a.get();
            phaser.arrive();
        }
    }
}
