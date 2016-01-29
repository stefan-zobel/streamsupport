/*
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @summary flat-map operations
 * @bug 8044047 8076458
 */
package org.openjdk.tests.java.util.stream;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import java8.util.function.Function;
import java8.util.function.Supplier;
import java8.util.stream.DoubleStreamTestDataProvider;
import java8.util.stream.IntStreams;
import java8.util.stream.LongStreams;
import java8.util.stream.DoubleStreams;
import java8.util.stream.IntStreamTestDataProvider;
import java8.util.stream.LongStreamTestDataProvider;
import java8.util.stream.OpTestCase;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;
import java8.util.stream.StreamTestDataProvider;
import java8.util.stream.TestData;
import static java8.util.stream.LambdaTestHelpers.*;
import static java8.util.stream.ThrowableHelper.checkNPE;

@Test
public class FlatMapOpTest extends OpTestCase {

    public void testNullMapper() {
        checkNPE(() -> RefStreams.of(1).flatMap(null));
        checkNPE(() -> IntStreams.of(1).flatMap(null));
        checkNPE(() -> LongStreams.of(1).flatMap(null));
        checkNPE(() -> DoubleStreams.of(1).flatMap(null));
    }

    static final Function<Integer, Stream<Integer>> integerRangeMapper
            = e -> IntStreams.range(0, e).boxed();

    public void testFlatMap() {
        String[] stringsArray = {"hello", "there", "", "yada"};
        Stream<String> strings = StreamSupport.stream(Arrays.asList(stringsArray));
        assertConcat(strings.flatMap(flattenChars).iterator(), "hellothereyada");

        assertCountSum(StreamSupport.stream(countTo(10)).flatMap(mfId), 10, 55);
        assertCountSum(StreamSupport.stream(countTo(10)).flatMap(mfNull), 0, 0);
        assertCountSum(StreamSupport.stream(countTo(3)).flatMap(mfLt), 6, 4);

        exerciseOps(TestData.Factory.ofArray("stringsArray", stringsArray), s -> s.flatMap(flattenChars));
        exerciseOps(TestData.Factory.ofArray("LONG_STRING", new String[] {LONG_STRING}), s -> s.flatMap(flattenChars));
    }

    @Test
    public void testClose() {
        AtomicInteger before = new AtomicInteger();
        AtomicInteger onClose = new AtomicInteger();

        Supplier<Stream<Integer>> s = () -> {
            before.set(0); onClose.set(0);
            return RefStreams.of(1, 2).peek(e -> before.getAndIncrement());
        };

        s.get().flatMap(i -> RefStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());

        s.get().flatMapToInt(i -> IntStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());

        s.get().flatMapToLong(i -> LongStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());

        s.get().flatMapToDouble(i -> DoubleStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());
    }

    @Test
    public void testIntClose() {
        AtomicInteger before = new AtomicInteger();
        AtomicInteger onClose = new AtomicInteger();

        IntStreams.of(1, 2).peek(e -> before.getAndIncrement()).
                flatMap(i -> IntStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());
    }

    @Test
    public void testLongClose() {
        AtomicInteger before = new AtomicInteger();
        AtomicInteger onClose = new AtomicInteger();

        LongStreams.of(1, 2).peek(e -> before.getAndIncrement()).
                flatMap(i -> LongStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());
    }

    @Test
    public void testDoubleClose() {
        AtomicInteger before = new AtomicInteger();
        AtomicInteger onClose = new AtomicInteger();

        DoubleStreams.of(1, 2).peek(e -> before.getAndIncrement()).
                flatMap(i -> DoubleStreams.of(i, i).onClose(onClose::getAndIncrement)).count();
        assertEquals(before.get(), onClose.get());
    }

    @Test(dataProvider = "StreamTestData<Integer>", dataProviderClass = StreamTestDataProvider.class)
    public void testOps(String name, TestData.OfRef<Integer> data) {
        Collection<Integer> result = exerciseOps(data, s -> s.flatMap(mfId));
        assertEquals(data.size(), result.size());

        result = exerciseOps(data, s -> s.flatMap(mfNull));
        assertEquals(0, result.size());

        result = exerciseOps(data, s-> s.flatMap(e -> RefStreams.empty()));
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "StreamTestData<Integer>.small", dataProviderClass = StreamTestDataProvider.class)
    public void testOpsX(String name, TestData.OfRef<Integer> data) {
        exerciseOps(data, s -> s.flatMap(mfLt));
        exerciseOps(data, s -> s.flatMap(integerRangeMapper));
        exerciseOps(data, s -> s.flatMap((Integer e) -> IntStreams.range(0, e).boxed().limit(10)));
    }

    //

    @Test(dataProvider = "IntStreamTestData", dataProviderClass = IntStreamTestDataProvider.class)
    public void testIntOps(String name, TestData.OfInt data) {
        Collection<Integer> result = exerciseOps(data, s -> s.flatMap(i -> StreamSupport.stream(Collections.singleton(i)).mapToInt(j -> j)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> s.flatMap(i -> IntStreams.empty()));
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "IntStreamTestData.small", dataProviderClass = IntStreamTestDataProvider.class)
    public void testIntOpsX(String name, TestData.OfInt data) {
        exerciseOps(data, s -> s.flatMap(e -> IntStreams.range(0, e)));
        exerciseOps(data, s -> s.flatMap(e -> IntStreams.range(0, e).limit(10)));
    }

    //

    @Test(dataProvider = "LongStreamTestData", dataProviderClass = LongStreamTestDataProvider.class)
    public void testLongOps(String name, TestData.OfLong data) {
        Collection<Long> result = exerciseOps(data, s -> s.flatMap(i -> StreamSupport.stream(Collections.singleton(i)).mapToLong(j -> j)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> LongStreams.empty());
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "LongStreamTestData.small", dataProviderClass = LongStreamTestDataProvider.class)
    public void testLongOpsX(String name, TestData.OfLong data) {
        exerciseOps(data, s -> s.flatMap(e -> LongStreams.range(0, e)));
        exerciseOps(data, s -> s.flatMap(e -> LongStreams.range(0, e).limit(10)));
    }

    //

    @Test(dataProvider = "DoubleStreamTestData", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDoubleOps(String name, TestData.OfDouble data) {
        Collection<Double> result = exerciseOps(data, s -> s.flatMap(i -> StreamSupport.stream(Collections.singleton(i)).mapToDouble(j -> j)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> DoubleStreams.empty());
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "DoubleStreamTestData.small", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDoubleOpsX(String name, TestData.OfDouble data) {
        exerciseOps(data, s -> s.flatMap(e -> IntStreams.range(0, (int) e).asDoubleStream()));
        exerciseOps(data, s -> s.flatMap(e -> IntStreams.range(0, (int) e).limit(10).asDoubleStream()));
    }
}
