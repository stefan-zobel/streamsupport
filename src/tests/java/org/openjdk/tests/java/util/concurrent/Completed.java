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

/**
 * @test
 * @run testng Completed
 * @summary Basic tests for CompletableFuture.completed
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java8.util.Sets;
import java8.util.concurrent.CompletableFuture;
import java8.util.stream.Collectors;
import java8.util.stream.Stream;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test
public class Completed {

    /**
     * Test the stream returned by the completed method when elements are
     * immediately available.
     */
    public void testBasic1() {
        CompletableFuture<String> cf1 = CompletableFuture.completedFuture("foo");
        CompletableFuture<String> cf2 = CompletableFuture.completedFuture("bar");

        long count = CompletableFuture.completed(cf1, cf2).mapToLong(e -> 1L).sum();
        assertTrue(count == 2);

        Set<String> results = CompletableFuture.completed(cf1, cf2)
                .map(CompletableFuture::join)
                .collect(Collectors.toSet());
        assertEquals(results, Sets.of("foo", "bar"));
    }

    /**
     * Test waiting on the stream with the interrupt status set.
     */
    public void testInterrupt1() {
        CompletableFuture<String> cf = new CompletableFuture<String>();
        Stream<CompletableFuture<String>> stream = CompletableFuture.completed(cf);
        Thread.currentThread().interrupt();
        try {
            stream.forEach(System.out::println);
            assertTrue(false);
        } catch (CancellationException e) {
            // interrupt status should be set
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear interrupt
        }
    }

    public void testEmpty1() {
        long count = CompletableFuture.completed().mapToLong(e -> 1L).sum();
        assertTrue(count == 0);
    }

    public void testEmpty2() {
        long count = CompletableFuture.completed(Sets.of()).mapToLong(e -> 1L).sum();
        assertTrue(count == 0);
    }

    @Test(expectedExceptions = { NullPointerException.class })
    public void testNull1() {
        Collection<? extends CompletableFuture<String>> cfs = null;
        CompletableFuture.completed(cfs);
    }

    @Test(expectedExceptions = { NullPointerException.class })
    public void testNull2() {
        List<CompletableFuture<String>> cfs = new ArrayList<CompletableFuture<String>>();
        cfs.add(CompletableFuture.completedFuture("foo"));
        cfs.add(null);
        CompletableFuture.completed(cfs);
    }

    @Test(expectedExceptions = { NullPointerException.class })
    public void testNull3() {
        CompletableFuture<String>[] cfs = null;
        CompletableFuture.completed(cfs);
    }

    @Test(expectedExceptions = { NullPointerException.class })
    public void testNull4() {
        CompletableFuture<String>[] cfs = new CompletableFuture[2];
        cfs[0] = CompletableFuture.completedFuture("foo");
        cfs[1] = null;
        CompletableFuture.completed(cfs);
    }
}
