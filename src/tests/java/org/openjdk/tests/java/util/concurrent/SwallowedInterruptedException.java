/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.tests.java.util.concurrent;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.DAYS;

import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import java8.util.concurrent.CompletableFuture;
import java8.util.concurrent.ThreadLocalRandom;

/*
 * @test
 * @bug 8254350
 * @run main SwallowedInterruptedException
 * @key randomness
 */
@Test
public class SwallowedInterruptedException {
    static final int ITERATIONS = 100;

    public void test() {
        try {
            main_(new String[]{});
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            throw new AssertionError("test failed");
        }
    }

    private static void main_(String[] args) throws Throwable {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 1; i <= ITERATIONS; i++) {
            boolean timed = rnd.nextBoolean();
            long sleepMillis = rnd.nextLong(10);

            CompletableFuture<Void> future = new CompletableFuture<>();
            CountDownLatch threadRunning = new CountDownLatch(1);
            AtomicReference<Throwable> fail = new AtomicReference<>();

            Thread thread = new Thread(() -> {
                threadRunning.countDown();

                try {
                    @SuppressWarnings("unused")
                    Void result = (timed) ? future.get(1, DAYS) : future.get();

                    if (!Thread.currentThread().isInterrupted()) {
                        fail.set(new AssertionError(
                            "Future.get completed with interrupt status not set"));
                    }
                } catch (InterruptedException ex) {
                    if (Thread.currentThread().isInterrupted()) {
                        fail.set(new AssertionError(
                            "InterruptedException with interrupt status set"));
                    }
                } catch (Throwable ex) {
                    fail.set(ex);
                }
            });
            thread.start();
            threadRunning.await();

            // interrupt thread, then set result after an optional (random) delay
            thread.interrupt();
            if (sleepMillis > 0)
                Thread.sleep(sleepMillis);
            future.complete(null);

            thread.join();
            if (fail.get() != null) {
                AssertionError e = new AssertionError(
                        String.format(
                                "Test failed at iteration %d with [timed=%s sleepMillis=%d]",
                                i, timed, sleepMillis));
                e.initCause(fail.get());
                throw e;
            }
        }
    }
}
