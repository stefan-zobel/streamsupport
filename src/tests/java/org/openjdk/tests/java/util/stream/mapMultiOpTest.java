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

/*
 * @test
 * @summary Test mapMulti(BiConsumer) and primitive stream operations
 */

package org.openjdk.tests.java.util.stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

import java8.util.J8Arrays;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import java8.util.function.Function;
import java8.util.stream.DefaultMethodStreams;
import java8.util.stream.DoubleStream;
import java8.util.stream.DoubleStreams;
import java8.util.stream.DoubleStreamTestDataProvider;
import java8.util.stream.IntStream;
import java8.util.stream.IntStreams;
import java8.util.stream.IntStreamTestDataProvider;
import java8.util.stream.LongStream;
import java8.util.stream.LongStreams;
import java8.util.stream.LongStreamTestDataProvider;
import java8.util.stream.OpTestCase;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;
import java8.util.stream.StreamTestDataProvider;
import java8.util.stream.TestData;

import static java8.util.stream.DefaultMethodStreams.delegateTo;
import static java8.util.stream.LambdaTestHelpers.LONG_STRING;
import static java8.util.stream.LambdaTestHelpers.assertConcat;
import static java8.util.stream.LambdaTestHelpers.assertContents;
import static java8.util.stream.LambdaTestHelpers.assertCountSum;
import static java8.util.stream.LambdaTestHelpers.countTo;
import static java8.util.stream.LambdaTestHelpers.flattenChars;
import static java8.util.stream.LambdaTestHelpers.mfId;
import static java8.util.stream.LambdaTestHelpers.mfLt;
import static java8.util.stream.LambdaTestHelpers.mfNull;
import static java8.util.stream.ThrowableHelper.checkNPE;

@Test
public class mapMultiOpTest extends OpTestCase {

    BiConsumer<Integer, Consumer<Integer>> nullConsumer =
            (e, sink) -> mfNull.apply(e).forEach(sink);
    BiConsumer<Integer, Consumer<Integer>> idConsumer =
            (e, sink) -> mfId.apply(e).forEach(sink);
    BiConsumer<Integer, Consumer<Integer>> listConsumer =
            (e, sink) -> mfLt.apply(e).forEach(sink);
    BiConsumer<String, Consumer<Character>> charConsumer =
            (e, sink) -> flattenChars.apply(e).forEach(sink);
    BiConsumer<Integer, Consumer<Integer>> emptyStreamConsumer =
            (e, sink) -> RefStreams.empty().forEach(i -> sink.accept((Integer) i));
    BiConsumer<Integer, Consumer<Integer>> intRangeConsumer =
            (e, sink) -> IntStreams.range(0, e).boxed().forEach(sink);
    BiConsumer<Integer, Consumer<Integer>> rangeConsumerWithLimit =
            (e, sink) -> IntStreams.range(0, e).boxed().limit(10).forEach(sink);

    @DataProvider(name = "Stream<Integer>")
    public Object[][] streamProvider() {
        return new Object[][]{
                {RefStreams.of(0, 1, 2)},
                {DefaultMethodStreams.delegateTo(RefStreams.of(0, 1, 2))}
        };
    }

    @Test(dataProvider = "Stream<Integer>")
    public void testNullMapper(Stream<Integer> s) {
        checkNPE(() -> s.mapMulti(null));
        checkNPE(() -> s.mapMultiToInt(null));
        checkNPE(() -> s.mapMultiToDouble(null));
        checkNPE(() -> s.mapMultiToLong(null));
    }

    @Test
    public void testMapMulti() {
        String[] stringsArray = {"hello", "there", "", "yada"};
        Stream<String> strings = StreamSupport.stream(Arrays.asList(stringsArray));

        assertConcat(strings.mapMulti(charConsumer)
                .iterator(), "hellothereyada");
        assertCountSum((StreamSupport.stream(countTo(10)).mapMulti(idConsumer)),
                10, 55);
        assertCountSum(StreamSupport.stream(countTo(10)).mapMulti(nullConsumer),
                0, 0);
        assertCountSum(StreamSupport.stream(countTo(3)).mapMulti(listConsumer),
                6, 4);

        exerciseOps(TestData.Factory.ofArray("stringsArray",
                stringsArray), s -> s.mapMulti(charConsumer));
        exerciseOps(TestData.Factory.ofArray("LONG_STRING",
                new String[]{LONG_STRING}), s -> s.mapMulti(charConsumer));
    }

    @Test
    public void testDefaultMapMulti() {
        String[] stringsArray = {"hello", "there", "", "yada"};
        Stream<String> strings = J8Arrays.stream(stringsArray);

        assertConcat(delegateTo(strings)
                .mapMulti(charConsumer).iterator(), "hellothereyada");
        assertCountSum(delegateTo(StreamSupport.stream(countTo(10)))
                .mapMulti(idConsumer), 10, 55);
        assertCountSum(delegateTo(StreamSupport.stream(countTo(10)))
                .mapMulti(nullConsumer), 0, 0);
        assertCountSum(delegateTo(StreamSupport.stream(countTo(3)))
                .mapMulti(listConsumer), 6, 4);

        exerciseOps(TestData.Factory.ofArray("stringsArray",
                stringsArray), s -> delegateTo(s).mapMulti(charConsumer));
        exerciseOps(TestData.Factory.ofArray("LONG_STRING",
                new String[]{LONG_STRING}), s -> delegateTo(s).mapMulti(charConsumer));
    }

    @Test(dataProvider = "StreamTestData<Integer>",
            dataProviderClass = StreamTestDataProvider.class)
    public void testOps(String name, TestData.OfRef<Integer> data) {
        testOps(name, data, s -> s);
        testOps(name, data, s -> delegateTo(s));
    }

    private void testOps(String name,
                         TestData.OfRef<Integer> data,
                         Function<Stream<Integer>, Stream<Integer>> sf) {
        Collection<Integer> result;
        result = exerciseOps(data, s -> sf.apply(s).mapMulti(idConsumer));
        assertEquals(data.size(), result.size());

        result = exerciseOps(data, s -> sf.apply(s).mapMulti(nullConsumer));
        assertEquals(0, result.size());

        result = exerciseOps(data, s -> sf.apply(s).mapMulti(emptyStreamConsumer));
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "StreamTestData<Integer>.small",
            dataProviderClass = StreamTestDataProvider.class)
    public void testOpsX(String name, TestData.OfRef<Integer> data) {
        exerciseOps(data, s -> s.mapMulti(listConsumer));
        exerciseOps(data, s -> s.mapMulti(intRangeConsumer));
        exerciseOps(data, s -> s.mapMulti(rangeConsumerWithLimit));
    }

    @Test(dataProvider = "StreamTestData<Integer>.small",
            dataProviderClass = StreamTestDataProvider.class)
    public void testDefaultOpsX(String name, TestData.OfRef<Integer> data) {
        exerciseOps(data, s -> delegateTo(s).mapMulti(listConsumer));
        exerciseOps(data, s -> delegateTo(s).mapMulti(intRangeConsumer));
        exerciseOps(data, s -> delegateTo(s).mapMulti(rangeConsumerWithLimit));
    }

    // Int

    @DataProvider(name = "IntStream")
    public Object[][] intStreamProvider() {
        return new Object[][]{
                {IntStreams.of(0, 1, 2)},
                {DefaultMethodStreams.delegateTo(IntStreams.of(0, 1, 2))}
        };
    }

    @Test(dataProvider = "IntStream")
    public void testIntNullMapper(IntStream s) {
        checkNPE(() -> s.mapMulti(null));
    }

    @Test(dataProvider = "IntStreamTestData", dataProviderClass = IntStreamTestDataProvider.class)
    public void testIntOps(String name, TestData.OfInt data) {
        testIntOps(name, data, s -> s);
        testIntOps(name, data, s -> delegateTo(s));
    }

    private void testIntOps(String name,
                            TestData.OfInt data,
                            Function<IntStream, IntStream> sf) {
        Collection<Integer> result = exerciseOps(data, s -> sf.apply(s).mapMulti((i, sink) -> IntStreams.of(i).forEach(sink)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> sf.apply(s).boxed().mapMultiToInt((i, sink) -> IntStreams.of(i).forEach(sink)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> sf.apply(s).mapMulti((i, sink) -> IntStreams.empty().forEach(sink)));
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "IntStreamTestData.small", dataProviderClass = IntStreamTestDataProvider.class)
    public void testIntOpsX(String name, TestData.OfInt data) {
        exerciseOps(data, s -> s.mapMulti((e, sink) -> IntStreams.range(0, e).forEach(sink)));
        exerciseOps(data, s -> s.mapMulti((e, sink) -> IntStreams.range(0, e).limit(10).forEach(sink)));

        exerciseOps(data, s -> s.boxed().mapMultiToInt((e, sink) -> IntStreams.range(0, e).forEach(sink)));
        exerciseOps(data, s -> s.boxed().mapMultiToInt((e, sink) -> IntStreams.range(0, e).limit(10).forEach(sink)));
    }

    @Test(dataProvider = "IntStreamTestData.small", dataProviderClass = IntStreamTestDataProvider.class)
    public void testDefaultIntOpsX(String name, TestData.OfInt data) {
        exerciseOps(data, s -> delegateTo(s).mapMulti((e, sink) -> IntStreams.range(0, e).forEach(sink)));
        exerciseOps(data, s -> delegateTo(s).mapMulti((e, sink) -> IntStreams.range(0, e).limit(10).forEach(sink)));

        exerciseOps(data, s -> delegateTo(s).boxed().mapMultiToInt((e, sink) -> IntStreams.range(0, e).forEach(sink)));
        exerciseOps(data, s -> delegateTo(s).boxed().mapMultiToInt((e, sink) -> IntStreams.range(0, e).limit(10).forEach(sink)));
    }

    // Double

    @DataProvider(name = "DoubleStream")
    public Object[][] doubleStreamProvider() {
        return new Object[][]{
                {DoubleStreams.of(0, 1, 2)},
                {DefaultMethodStreams.delegateTo(DoubleStreams.of(0, 1, 2))}
        };
    }

    @Test(dataProvider = "DoubleStream")
    public void testDoubleNullMapper(DoubleStream s) {
        checkNPE(() -> s.mapMulti(null));
    }

    @Test(dataProvider = "DoubleStreamTestData", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDoubleOps(String name, TestData.OfDouble data) {
        testDoubleOps(name, data, s -> s);
        testDoubleOps(name, data, s -> delegateTo(s));
    }

    private void testDoubleOps(String name,
                               TestData.OfDouble data,
                               Function<DoubleStream, DoubleStream> sf) {
        Collection<Double> result = exerciseOps(data, s -> sf.apply(s).mapMulti((i, sink) -> DoubleStreams.of(i).forEach(sink)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> sf.apply(s).boxed().mapMultiToDouble((i, sink) -> DoubleStreams.of(i).forEach(sink)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> sf.apply(s).mapMulti((i, sink) -> DoubleStreams.empty().forEach(sink)));
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "DoubleStreamTestData.small", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDoubleOpsX(String name, TestData.OfDouble data) {
        exerciseOps(data, s -> s.mapMulti((e, sink) -> IntStreams.range(0, (int) e).asDoubleStream().forEach(sink)));
        exerciseOps(data, s -> s.mapMulti((e, sink) -> IntStreams.range(0, (int) e).limit(10).asDoubleStream().forEach(sink)));
    }

    @Test(dataProvider = "DoubleStreamTestData.small", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDefaultDoubleOpsX(String name, TestData.OfDouble data) {
        exerciseOps(data, s -> delegateTo(s).mapMulti((e, sink) -> IntStreams.range(0, (int) e).asDoubleStream().forEach(sink)));
        exerciseOps(data, s -> delegateTo(s).mapMulti((e, sink) -> IntStreams.range(0, (int) e).limit(10).asDoubleStream().forEach(sink)));
    }

    // Long

    @DataProvider(name = "LongStream")
    public Object[][] longStreamProvider() {
        return new Object[][]{
                {LongStreams.of(0, 1, 2)},
                {DefaultMethodStreams.delegateTo(LongStreams.of(0, 1, 2))}
        };
    }

    @Test(dataProvider = "LongStream")
    public void testLongNullMapper(LongStream s) {
        checkNPE(() -> s.mapMulti(null));
    }

    @Test(dataProvider = "LongStreamTestData", dataProviderClass = LongStreamTestDataProvider.class)
    public void testLongOps(String name, TestData.OfLong data) {
        testLongOps(name, data, s -> s);
        testLongOps(name, data, s -> delegateTo(s));
    }

    private void testLongOps(String name,
                             TestData.OfLong data,
                             Function<LongStream, LongStream> sf) {
        Collection<Long> result = exerciseOps(data, s -> sf.apply(s).mapMulti((i, sink) -> LongStreams.of(i).forEach(sink)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> sf.apply(s).boxed().mapMultiToLong((i, sink) -> LongStreams.of(i).forEach(sink)));
        assertEquals(data.size(), result.size());
        assertContents(data, result);

        result = exerciseOps(data, s -> sf.apply(s).mapMulti((i, sink) -> LongStreams.empty().forEach(sink)));
        assertEquals(0, result.size());
    }

    @Test(dataProvider = "LongStreamTestData.small", dataProviderClass = LongStreamTestDataProvider.class)
    public void testLongOpsX(String name, TestData.OfLong data) {
        exerciseOps(data, s -> s.mapMulti((e, sink) -> LongStreams.range(0, e).forEach(sink)));
        exerciseOps(data, s -> s.mapMulti((e, sink) -> LongStreams.range(0, e).limit(10).forEach(sink)));
    }

    @Test(dataProvider = "LongStreamTestData.small", dataProviderClass = LongStreamTestDataProvider.class)
    public void testDefaultLongOpsX(String name, TestData.OfLong data) {
        exerciseOps(data, s -> delegateTo(s).mapMulti((e, sink) -> LongStreams.range(0, e).forEach(sink)));
        exerciseOps(data, s -> delegateTo(s).mapMulti((e, sink) -> LongStreams.range(0, e).limit(10).forEach(sink)));
    }
}
