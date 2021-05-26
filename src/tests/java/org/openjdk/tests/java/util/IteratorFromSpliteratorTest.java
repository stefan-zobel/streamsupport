/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.tests.java.util;

/**
 * @test
 * @summary Spliterator.iterator traversing tests
 * @library /lib/testlibrary/bootlib
 * @run testng IteratorFromSpliteratorTest
 * @bug 8267452
 */
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java8.util.J8Arrays;
import java8.util.Iterators;
import java8.util.Lists;
import java8.util.PrimitiveIterator;
import java8.util.Spliterators;
import java8.util.function.DoubleConsumer;
import java8.util.function.IntConsumer;
import java8.util.function.LongConsumer;
import java8.util.stream.StreamSupport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.testng695.Assert.assertThrows;

public class IteratorFromSpliteratorTest {
    @Test
    public void testIteratorFromSpliterator() {
        List<Integer> input = Lists.of(1, 2, 3, 4, 5);
        for (int i = 0; i < input.size(); i++) {
            Iterator<Integer> iterator = Spliterators.iterator(Spliterators.spliterator(input));
            List<Integer> result = new ArrayList<>();
            int j = i;
            while (j++ < input.size() && iterator.hasNext()) {
                result.add(iterator.next());
            }
            // While SpliteratorTraversingAndSplittingTest tests some scenarios with Spliterators.iterator
            // it always wraps the resulting iterator into spliterator again, and this limits the used patterns.
            // In particular, calling hasNext() right before forEachRemaining() is not tested.
            // Here we cover such a scenario.
            assertEquals(iterator.hasNext(), result.size() < input.size());
            Iterators.forEachRemaining(iterator, result::add);
            Iterators.forEachRemaining(iterator, x -> fail("Should not be called"));
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
            Iterators.forEachRemaining(iterator, x -> fail("Should not be called"));
            assertEquals(result, input);
        }
    }

    @Test
    public void testIteratorFromSpliteratorInt() {
        int[] input = { 1, 2, 3, 4, 5 };
        for (int i = 0; i < input.length; i++) {
            PrimitiveIterator.OfInt iterator = Spliterators.iterator(J8Arrays.spliterator(input));
            List<Integer> result = new ArrayList<>();
            int j = i;
            while (j++ < input.length && iterator.hasNext()) {
                result.add(iterator.nextInt());
            }
            assertEquals(iterator.hasNext(), result.size() < input.length);
            iterator.forEachRemaining((IntConsumer) result::add);
            iterator.forEachRemaining((IntConsumer) (x -> fail("Should not be called")));
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
            iterator.forEachRemaining((IntConsumer) (x -> fail("Should not be called")));
            assertEquals(StreamSupport.stream(result).mapToInt(x -> x).toArray(), input);
        }
    }

    @Test
    public void testIteratorFromSpliteratorLong() {
        long[] input = { 1L, 2L, 3L, 4L, 5L };
        for (int i = 0; i < input.length; i++) {
            PrimitiveIterator.OfLong iterator = Spliterators.iterator(J8Arrays.spliterator(input));
            List<Long> result = new ArrayList<>();
            int j = i;
            while (j++ < input.length && iterator.hasNext()) {
                result.add(iterator.nextLong());
            }
            assertEquals(iterator.hasNext(), result.size() < input.length);
            iterator.forEachRemaining((LongConsumer) result::add);
            iterator.forEachRemaining((LongConsumer) (x -> fail("Should not be called")));
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
            iterator.forEachRemaining((LongConsumer) (x -> fail("Should not be called")));
            assertEquals(StreamSupport.stream(result).mapToLong(x -> x).toArray(), input);
        }
    }

    @Test
    public void testIteratorFromSpliteratorDouble() {
        double[] input = { 1.0, 2.0, 3.0, 4.0, 5.0 };
        for (int i = 0; i < input.length; i++) {
            PrimitiveIterator.OfDouble iterator = Spliterators.iterator(J8Arrays.spliterator(input));
            List<Double> result = new ArrayList<>();
            int j = i;
            while (j++ < input.length && iterator.hasNext()) {
                result.add(iterator.nextDouble());
            }
            assertEquals(iterator.hasNext(), result.size() < input.length);
            iterator.forEachRemaining((DoubleConsumer) result::add);
            iterator.forEachRemaining((DoubleConsumer) (x -> fail("Should not be called")));
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
            iterator.forEachRemaining((DoubleConsumer) (x -> fail("Should not be called")));
            assertEquals(StreamSupport.stream(result).mapToDouble(x -> x).toArray(), input);
        }
    }

    @Test
    public void testIteratorFromSpliteratorEmpty() {
        Iterator<?> it = Spliterators.iterator(Spliterators.emptySpliterator());
        Iterators.forEachRemaining(it, x -> fail("Should not be called"));
        assertFalse(it.hasNext());
        Iterators.forEachRemaining(it, x -> fail("Should not be called"));
        assertThrows(NoSuchElementException.class, it::next);

        PrimitiveIterator<?, ?>[] iterators = {
            Spliterators.iterator(Spliterators.emptyIntSpliterator()),
            Spliterators.iterator(Spliterators.emptyLongSpliterator()),
            Spliterators.iterator(Spliterators.emptyDoubleSpliterator())
        };
        for (PrimitiveIterator<?, ?> iterator : iterators) {
            Iterators.forEachRemaining(iterator, x -> fail("Should not be called"));
            assertFalse(iterator.hasNext());
            Iterators.forEachRemaining(iterator, x -> fail("Should not be called"));
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }
}
