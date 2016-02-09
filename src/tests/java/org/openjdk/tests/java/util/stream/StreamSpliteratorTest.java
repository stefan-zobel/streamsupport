/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.tests.java.util.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.Consumer;
import java8.util.function.DoubleConsumer;
import java8.util.function.Function;
import java8.util.function.IntConsumer;
import java8.util.function.LongConsumer;
import java8.util.function.UnaryOperator;
import java8.util.stream.DoubleStream;
import java8.util.stream.DoubleStreams;
import java8.util.stream.DoubleStreamTestDataProvider;
import java8.util.stream.IntStream;
import java8.util.stream.IntStreamTestDataProvider;
import java8.util.stream.IntStreams;
import java8.util.stream.LambdaTestHelpers;
import java8.util.stream.LongStream;
import java8.util.stream.LongStreamTestDataProvider;
import java8.util.stream.LongStreams;
import java8.util.stream.OpTestCase;
import java8.util.stream.RefStreams;
import java8.util.stream.SpliteratorTestHelper;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;
import java8.util.stream.StreamTestDataProvider;
import java8.util.stream.TestData;

import org.testng.Assert;
import org.testng.annotations.Test;

import static java8.util.stream.LambdaTestHelpers.countTo;
import static java8.util.stream.LambdaTestHelpers.dpEven;
import static java8.util.stream.LambdaTestHelpers.ipEven;
import static java8.util.stream.LambdaTestHelpers.irDoubler;
import static java8.util.stream.LambdaTestHelpers.lpEven;
import static java8.util.stream.LambdaTestHelpers.mDoubler;
import static java8.util.stream.LambdaTestHelpers.pEven;
import static java8.util.stream.LambdaTestHelpers.permuteStreamFunctions;

/**
 * @test
 * @bug 8148838
 */
@Test
public class StreamSpliteratorTest extends OpTestCase {

    private static class ProxyNoExactSizeSpliterator<T> implements Spliterator<T> {
        final Spliterator<T> sp;
        final boolean proxyEstimateSize;
        int splits = 0;
        int prefixSplits = 0;

        long sizeOnTraversal = -1;

        ProxyNoExactSizeSpliterator(Spliterator<T> sp, boolean proxyEstimateSize) {
            this.sp = sp;
            this.proxyEstimateSize = proxyEstimateSize;
        }

        @Override
        public Spliterator<T> trySplit() {
            splits++;
            Spliterator<T> prefix = sp.trySplit();
            if (prefix != null)
                prefixSplits++;
            return prefix;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (sizeOnTraversal == -1)
                sizeOnTraversal = sp.getExactSizeIfKnown();
            return sp.tryAdvance(consumer);
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            sizeOnTraversal = sp.getExactSizeIfKnown();
            sp.forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            return proxyEstimateSize ? sp.estimateSize() : Long.MAX_VALUE;
        }

        @Override
        public Comparator<? super T> getComparator() {
            return sp.getComparator();
        }

        @Override
        public int characteristics() {
            if (proxyEstimateSize)
                return sp.characteristics();
            else
                return sp.characteristics() & ~(Spliterator.SUBSIZED | Spliterator.SIZED);
        }

        @Override
        public long getExactSizeIfKnown() {
            return Spliterators.getExactSizeIfKnown(this);
        }

        @Override
        public boolean hasCharacteristics(int i) {
            return Spliterators.hasCharacteristics(this, i);
        }

        private static class OfInt extends ProxyNoExactSizeSpliterator<Integer> implements Spliterator.OfInt {
            final Spliterator.OfInt psp;

            private OfInt(Spliterator.OfInt sp, boolean proxyEstimateSize) {
                super(sp, proxyEstimateSize);
                this.psp = sp;
            }

            @Override
            public Spliterator.OfInt trySplit() {
                splits++;
                Spliterator.OfInt prefix = psp.trySplit();
                if (prefix != null)
                    prefixSplits++;
                return prefix;
            }

            @Override
            public boolean tryAdvance(Consumer<? super Integer> consumer) {
                return Spliterators.OfInt.tryAdvance(this, consumer);
            }

            @Override
            public void forEachRemaining(Consumer<? super Integer> consumer) {
                Spliterators.OfInt.forEachRemaining(this, consumer);
            }

            @Override
            public boolean tryAdvance(IntConsumer consumer) {
                if (sizeOnTraversal == -1)
                    sizeOnTraversal = sp.getExactSizeIfKnown();
                return psp.tryAdvance(consumer);
            }

            @Override
            public void forEachRemaining(IntConsumer consumer) {
                sizeOnTraversal = sp.getExactSizeIfKnown();
                psp.forEachRemaining(consumer);
            }
        }

        private static class OfLong extends ProxyNoExactSizeSpliterator<Long> implements Spliterator.OfLong {
            final Spliterator.OfLong psp;

            private OfLong(Spliterator.OfLong sp, boolean proxyEstimateSize) {
                super(sp, proxyEstimateSize);
                this.psp = sp;
            }

            @Override
            public Spliterator.OfLong trySplit() {
                splits++;
                Spliterator.OfLong prefix = psp.trySplit();
                if (prefix != null)
                    prefixSplits++;
                return prefix;
            }

            @Override
            public boolean tryAdvance(Consumer<? super Long> consumer) {
                return Spliterators.OfLong.tryAdvance(this, consumer);
            }

            @Override
            public void forEachRemaining(Consumer<? super Long> consumer) {
                Spliterators.OfLong.forEachRemaining(this, consumer);
            }

            @Override
            public boolean tryAdvance(LongConsumer consumer) {
                if (sizeOnTraversal == -1)
                    sizeOnTraversal = sp.getExactSizeIfKnown();
                return psp.tryAdvance(consumer);
            }

            @Override
            public void forEachRemaining(LongConsumer consumer) {
                sizeOnTraversal = sp.getExactSizeIfKnown();
                psp.forEachRemaining(consumer);
            }
        }

        private static class OfDouble extends ProxyNoExactSizeSpliterator<Double>
                implements Spliterator.OfDouble {
            final Spliterator.OfDouble psp;

            private OfDouble(Spliterator.OfDouble sp, boolean proxyEstimateSize) {
                super(sp, proxyEstimateSize);
                this.psp = sp;
            }

            @Override
            public Spliterator.OfDouble trySplit() {
                splits++;
                Spliterator.OfDouble prefix = psp.trySplit();
                if (prefix != null)
                    prefixSplits++;
                return prefix;
            }

            @Override
            public boolean tryAdvance(Consumer<? super Double> consumer) {
                return Spliterators.OfDouble.tryAdvance(this, consumer);
            }

            @Override
            public void forEachRemaining(Consumer<? super Double> consumer) {
                Spliterators.OfDouble.forEachRemaining(this, consumer);
            }

            @Override
            public boolean tryAdvance(DoubleConsumer consumer) {
                if (sizeOnTraversal == -1)
                    sizeOnTraversal = sp.getExactSizeIfKnown();
                return psp.tryAdvance(consumer);
            }

            @Override
            public void forEachRemaining(DoubleConsumer consumer) {
                sizeOnTraversal = sp.getExactSizeIfKnown();
                psp.forEachRemaining(consumer);
            }
        }
    }

    public void testSplitting() {
        // Size is assumed to be larger than the target size for no splitting
        // @@@ Need way to obtain the target size
        List<Integer> l = countTo(1000);

        List<Consumer<Stream<Integer>>> terminalOps = Arrays.asList(
                s -> s.toArray(),
                s -> s.forEach(e -> { }),
                s -> s.reduce(java8.lang.Integers::sum)
        );

        List<UnaryOperator<Stream<Integer>>> intermediateOps = Arrays.asList(
                s -> s.parallel(),
                // The following ensures the wrapping spliterator is tested
                s -> s.map(LambdaTestHelpers.identity()).parallel()
        );

        for (int i = 0; i < terminalOps.size(); i++) {
            setContext("termOpIndex", i);
            Consumer<Stream<Integer>> terminalOp = terminalOps.get(i);
            for (int j = 0; j < intermediateOps.size(); j++) {
                setContext("intOpIndex", j);
                UnaryOperator<Stream<Integer>> intermediateOp = intermediateOps.get(j);
                for (boolean proxyEstimateSize : new boolean[] {false, true}) {
                    setContext("proxyEstimateSize", proxyEstimateSize);
                    Spliterator<Integer> sp = intermediateOp.apply(StreamSupport.stream(l)).spliterator();
                    ProxyNoExactSizeSpliterator<Integer> psp = new ProxyNoExactSizeSpliterator<>(sp, proxyEstimateSize);
                    Stream<Integer> s = StreamSupport.stream(psp, true);
                    terminalOp.accept(s);
                    Assert.assertTrue(psp.splits > 0,
                                      String.format("Number of splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.prefixSplits > 0,
                                      String.format("Number of non-null prefix splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.sizeOnTraversal < l.size(),
                                      String.format("Size on traversal of last split should be less than the size of the list, %d, when proxyEstimateSize is %s",
                                                    l.size(), proxyEstimateSize));
                }
            }
        }
    }

    @Test(dataProvider = "StreamTestData<Integer>.small",
          dataProviderClass = StreamTestDataProvider.class,
          groups = { "serialization-hostile" })
    public void testStreamSpliterators(String name, TestData.OfRef<Integer> data) {
        for (Function<Stream<Integer>, Stream<Integer>> f : streamFunctions()) {
            withData(data).
                    stream((Stream<Integer> in) -> {
                        Stream<Integer> out = f.apply(in);
                        return StreamSupport.stream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), false);
                    }).
                    exercise();

            withData(data).
                    stream((Stream<Integer> in) -> {
                        Stream<Integer> out = f.apply(in);
                        return StreamSupport.stream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), true);
                    }).
                    exercise();
        }
    }

    @Test(dataProvider = "StreamTestData<Integer>.small", dataProviderClass = StreamTestDataProvider.class)
    public void testSpliterators(String name, TestData.OfRef<Integer> data) {
        for (Function<Stream<Integer>, Stream<Integer>> f : streamFunctions()) {
            SpliteratorTestHelper.testSpliterator(() -> f.apply(data.stream()).spliterator());
        }
    }

    @Test(dataProvider = "StreamTestData<Integer>.small", dataProviderClass = StreamTestDataProvider.class)
    public void testParSpliterators(String name, TestData.OfRef<Integer> data) {
        for (Function<Stream<Integer>, Stream<Integer>> f : streamFunctions()) {
            SpliteratorTestHelper.testSpliterator(() -> f.apply(data.parallelStream()).spliterator());
        }
    }

    private List<Function<Stream<Integer>, Stream<Integer>>> streamFunctions;

    List<Function<Stream<Integer>, Stream<Integer>>> streamFunctions() {
        if (streamFunctions == null) {
            List<Function<Stream<Integer>, Stream<Integer>>> opFunctions = Arrays.asList(
                    s -> s.filter(pEven),
                    s -> s.flatMap(x -> RefStreams.of(x, x)),
                    // @@@ Add distinct once asserting results with or without order
                    //     is correctly supported
//                    s -> s.distinct(),
                    s -> s.sorted());

            streamFunctions = permuteStreamFunctions(opFunctions);
        }

        return streamFunctions;
    }

    //

    public void testIntSplitting() {
        List<Consumer<IntStream>> terminalOps = Arrays.asList(
                s -> s.toArray(),
                s -> s.forEach(e -> {}),
                s -> s.reduce(java8.lang.Integers::sum)
        );

        List<UnaryOperator<IntStream>> intermediateOps = Arrays.asList(
                s -> s.parallel(),
                // The following ensures the wrapping spliterator is tested
                s -> s.map(i -> i).parallel()
        );

        for (int i = 0; i < terminalOps.size(); i++) {
            setContext("termOpIndex", i);
            Consumer<IntStream> terminalOp = terminalOps.get(i);
            for (int j = 0; j < intermediateOps.size(); j++) {
                setContext("intOpIndex", j);
                UnaryOperator<IntStream> intermediateOp = intermediateOps.get(j);
                for (boolean proxyEstimateSize : new boolean[] {false, true}) {
                    setContext("proxyEstimateSize", proxyEstimateSize);
                    // Size is assumed to be larger than the target size for no splitting
                    // @@@ Need way to obtain the target size
                    Spliterator.OfInt sp = intermediateOp.apply(IntStreams.range(0, 1000)).spliterator();
                    ProxyNoExactSizeSpliterator.OfInt psp = new ProxyNoExactSizeSpliterator.OfInt(sp, proxyEstimateSize);
                    IntStream s = StreamSupport.intStream(psp, true);
                    terminalOp.accept(s);
                    Assert.assertTrue(psp.splits > 0,
                                      String.format("Number of splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.prefixSplits > 0,
                                      String.format("Number of non-null prefix splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.sizeOnTraversal < 1000,
                                      String.format("Size on traversal of last split should be less than the size of the list, %d, when proxyEstimateSize is %s",
                                                    1000, proxyEstimateSize));
                }
            }
        }
    }

    @Test(dataProvider = "IntStreamTestData.small",
          dataProviderClass = IntStreamTestDataProvider.class,
          groups = { "serialization-hostile" })
    public void testIntStreamSpliterators(String name, TestData.OfInt data) {
        for (Function<IntStream, IntStream> f : intStreamFunctions()) {
            withData(data).
                    stream(in -> {
                        IntStream out = f.apply(in);
                        return StreamSupport.intStream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), false);
                    }).
                    exercise();

            withData(data).
                    stream((in) -> {
                        IntStream out = f.apply(in);
                        return StreamSupport.intStream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), true);
                    }).
                    exercise();
        }
    }

    @Test(dataProvider = "IntStreamTestData.small", dataProviderClass = IntStreamTestDataProvider.class)
    public void testIntSpliterators(String name, TestData.OfInt data) {
        for (Function<IntStream, IntStream> f : intStreamFunctions()) {
            SpliteratorTestHelper.testIntSpliterator(() -> f.apply(data.stream()).spliterator());
        }
    }

    @Test(dataProvider = "IntStreamTestData.small", dataProviderClass = IntStreamTestDataProvider.class)
    public void testIntParSpliterators(String name, TestData.OfInt data) {
        for (Function<IntStream, IntStream> f : intStreamFunctions()) {
            SpliteratorTestHelper.testIntSpliterator(() -> f.apply(data.parallelStream()).spliterator());
        }
    }

    private List<Function<IntStream, IntStream>> intStreamFunctions;

    List<Function<IntStream, IntStream>> intStreamFunctions() {
        if (intStreamFunctions == null) {
            List<Function<IntStream, IntStream>> opFunctions = Arrays.asList(
                    s -> s.filter(ipEven),
                    s -> s.flatMap(x -> IntStreams.of(x, x)),
                    s -> s.sorted());

            intStreamFunctions = permuteStreamFunctions(opFunctions);
        }

        return intStreamFunctions;
    }

    //

    public void testLongSplitting() {
        List<Consumer<LongStream>> terminalOps = Arrays.asList(
                s -> s.toArray(),
                s -> s.forEach(e -> {}),
                s -> s.reduce(java8.lang.Longs::sum)
        );

        List<UnaryOperator<LongStream>> intermediateOps = Arrays.asList(
                s -> s.parallel(),
                // The following ensures the wrapping spliterator is tested
                s -> s.map(i -> i).parallel()
        );

        for (int i = 0; i < terminalOps.size(); i++) {
            Consumer<LongStream> terminalOp = terminalOps.get(i);
            setContext("termOpIndex", i);
            for (int j = 0; j < intermediateOps.size(); j++) {
                setContext("intOpIndex", j);
                UnaryOperator<LongStream> intermediateOp = intermediateOps.get(j);
                for (boolean proxyEstimateSize : new boolean[] {false, true}) {
                    setContext("proxyEstimateSize", proxyEstimateSize);
                    // Size is assumed to be larger than the target size for no splitting
                    // @@@ Need way to obtain the target size
                    Spliterator.OfLong sp = intermediateOp.apply(LongStreams.range(0, 1000)).spliterator();
                    ProxyNoExactSizeSpliterator.OfLong psp = new ProxyNoExactSizeSpliterator.OfLong(sp, proxyEstimateSize);
                    LongStream s = StreamSupport.longStream(psp, true);
                    terminalOp.accept(s);
                    Assert.assertTrue(psp.splits > 0,
                                      String.format("Number of splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.prefixSplits > 0,
                                      String.format("Number of non-null prefix splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.sizeOnTraversal < 1000,
                                      String.format("Size on traversal of last split should be less than the size of the list, %d, when proxyEstimateSize is %s",
                                                    1000, proxyEstimateSize));
                }
            }
        }
    }

    @Test(dataProvider = "LongStreamTestData.small",
          dataProviderClass = LongStreamTestDataProvider.class,
          groups = { "serialization-hostile" })
    public void testLongStreamSpliterators(String name, TestData.OfLong data) {
        for (Function<LongStream, LongStream> f : longStreamFunctions()) {
            withData(data).
                    stream(in -> {
                        LongStream out = f.apply(in);
                        return StreamSupport.longStream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), false);
                    }).
                    exercise();

            withData(data).
                    stream((in) -> {
                        LongStream out = f.apply(in);
                        return StreamSupport.longStream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), true);
                    }).
                    exercise();
        }
    }

    @Test(dataProvider = "LongStreamTestData.small", dataProviderClass = LongStreamTestDataProvider.class)
    public void testLongSpliterators(String name, TestData.OfLong data) {
        for (Function<LongStream, LongStream> f : longStreamFunctions()) {
            SpliteratorTestHelper.testLongSpliterator(() -> f.apply(data.stream()).spliterator());
        }
    }

    @Test(dataProvider = "LongStreamTestData.small", dataProviderClass = LongStreamTestDataProvider.class)
    public void testLongParSpliterators(String name, TestData.OfLong data) {
        for (Function<LongStream, LongStream> f : longStreamFunctions()) {
            SpliteratorTestHelper.testLongSpliterator(() -> f.apply(data.parallelStream()).spliterator());
        }
    }

    private List<Function<LongStream, LongStream>> longStreamFunctions;

    List<Function<LongStream, LongStream>> longStreamFunctions() {
        if (longStreamFunctions == null) {
            List<Function<LongStream, LongStream>> opFunctions = Arrays.asList(
                    s -> s.filter(lpEven),
                    s -> s.flatMap(x -> LongStreams.of(x, x)),
                    s -> s.sorted());

            longStreamFunctions = permuteStreamFunctions(opFunctions);
        }

        return longStreamFunctions;
    }

    //

    public void testDoubleSplitting() {
        List<Consumer<DoubleStream>> terminalOps = Arrays.asList(
                s -> s.toArray(),
                s -> s.forEach(e -> {}),
                s -> s.reduce(java8.lang.Doubles::sum)
        );

        List<UnaryOperator<DoubleStream>> intermediateOps = Arrays.asList(
                s -> s.parallel(),
                // The following ensures the wrapping spliterator is tested
                s -> s.map(i -> i).parallel()
        );

        for (int i = 0; i < terminalOps.size(); i++) {
            Consumer<DoubleStream> terminalOp = terminalOps.get(i);
            setContext("termOpIndex", i);
            for (int j = 0; j < intermediateOps.size(); j++) {
                UnaryOperator<DoubleStream> intermediateOp = intermediateOps.get(j);
                setContext("intOpIndex", j);
                for (boolean proxyEstimateSize : new boolean[] {false, true}) {
                    setContext("proxyEstimateSize", proxyEstimateSize);
                    // Size is assumed to be larger than the target size for no splitting
                    // @@@ Need way to obtain the target size
                    Spliterator.OfDouble sp = intermediateOp.apply(IntStreams.range(0, 1000).asDoubleStream()).spliterator();
                    ProxyNoExactSizeSpliterator.OfDouble psp = new ProxyNoExactSizeSpliterator.OfDouble(sp, proxyEstimateSize);
                    DoubleStream s = StreamSupport.doubleStream(psp, true);
                    terminalOp.accept(s);
                    Assert.assertTrue(psp.splits > 0,
                                      String.format("Number of splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.prefixSplits > 0,
                                      String.format("Number of non-null prefix splits should be greater that zero when proxyEstimateSize is %s",
                                                    proxyEstimateSize));
                    Assert.assertTrue(psp.sizeOnTraversal < 1000,
                                      String.format("Size on traversal of last split should be less than the size of the list, %d, when proxyEstimateSize is %s",
                                                    1000, proxyEstimateSize));
                }
            }
        }
    }

    @Test(dataProvider = "DoubleStreamTestData.small",
          dataProviderClass = DoubleStreamTestDataProvider.class,
          groups = { "serialization-hostile" })
    public void testDoubleStreamSpliterators(String name, TestData.OfDouble data) {
        for (Function<DoubleStream, DoubleStream> f : doubleStreamFunctions()) {
            withData(data).
                    stream(in -> {
                        DoubleStream out = f.apply(in);
                        return StreamSupport.doubleStream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), false);
                    }).
                    exercise();

            withData(data).
                    stream((in) -> {
                        DoubleStream out = f.apply(in);
                        return StreamSupport.doubleStream(() -> out.spliterator(), OpTestCase.getStreamFlags(out), true);
                    }).
                    exercise();
        }
    }

    @Test(dataProvider = "DoubleStreamTestData.small", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDoubleSpliterators(String name, TestData.OfDouble data) {
        for (Function<DoubleStream, DoubleStream> f : doubleStreamFunctions()) {
            SpliteratorTestHelper.testDoubleSpliterator(() -> f.apply(data.stream()).spliterator());
        }
    }

    @Test(dataProvider = "DoubleStreamTestData.small", dataProviderClass = DoubleStreamTestDataProvider.class)
    public void testDoubleParSpliterators(String name, TestData.OfDouble data) {
        for (Function<DoubleStream, DoubleStream> f : doubleStreamFunctions()) {
            SpliteratorTestHelper.testDoubleSpliterator(() -> f.apply(data.parallelStream()).spliterator());
        }
    }

    private List<Function<DoubleStream, DoubleStream>> doubleStreamFunctions;

    List<Function<DoubleStream, DoubleStream>> doubleStreamFunctions() {
        if (doubleStreamFunctions == null) {
            List<Function<DoubleStream, DoubleStream>> opFunctions = Arrays.asList(
                    s -> s.filter(dpEven),
                    s -> s.flatMap(x -> DoubleStreams.of(x, x)),
                    s -> s.sorted());

            doubleStreamFunctions = permuteStreamFunctions(opFunctions);
        }

        return doubleStreamFunctions;
    }
}
