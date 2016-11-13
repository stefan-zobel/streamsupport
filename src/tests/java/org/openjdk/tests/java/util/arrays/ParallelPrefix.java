/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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

/**
 * @test 8014076 8025067
 * @summary unit test for Arrays.ParallelPrefix().
 * @author Tristan Yan
 * @run testng ParallelPrefix
 */
package org.openjdk.tests.java.util.arrays;

import java8.util.J8Arrays;
import java8.util.function.BinaryOperator;
import java8.util.function.DoubleBinaryOperator;
import java8.util.function.Function;
import java8.util.function.IntBinaryOperator;
import java8.util.function.LongBinaryOperator;
import java8.util.stream.IntStreams;
import java8.util.stream.LongStreams;
import static org.testng.Assert.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParallelPrefix {
    //Array size less than MIN_PARTITION
    private static final int SMALL_ARRAY_SIZE = 1 << 3;

    //Array size equals MIN_PARTITION
    private static final int THRESHOLD_ARRAY_SIZE = 1 << 4;

    //Array size greater than MIN_PARTITION
    private static final int MEDIUM_ARRAY_SIZE = 1 << 8;

    //Array size much greater than MIN_PARTITION
    private static final int LARGE_ARRAY_SIZE = 1 << 12; // 2^14 doesn't work on Android  
    //bump the LARGE_ARRAY_SIZE from 2^12 to 2^14, for better test coverage on systems with a lot of parallelism.
    //private final static int LARGE_ARRAY_SIZE = 1 << 14; // JDK-8085858

    private static final int[] ARRAY_SIZE_COLLECTION  = new int[]{
        SMALL_ARRAY_SIZE,
        THRESHOLD_ARRAY_SIZE,
        MEDIUM_ARRAY_SIZE,
        LARGE_ARRAY_SIZE
    };

    @DataProvider(name = "intSet")
    public static Object[][] intSet(){
        return genericData(size -> IntStreams.range(0, size).toArray(),
                new IntBinaryOperator[]{
                    java8.lang.Integers::sum,
                    java8.lang.Integers::min});
    }

    @DataProvider(name = "longSet")
    public static Object[][] longSet(){
        return genericData(size -> LongStreams.range(0, size).toArray(),
                new LongBinaryOperator[]{
                    java8.lang.Longs::sum,
                    java8.lang.Longs::min});
    }

    @DataProvider(name = "doubleSet")
    public static Object[][] doubleSet(){
        return genericData(size -> IntStreams.range(0, size).mapToDouble(i -> (double)i).toArray(),
                new DoubleBinaryOperator[]{
                    java8.lang.Doubles::sum,
                    java8.lang.Doubles::min});
    }

    @DataProvider(name = "stringSet")
    public static Object[][] stringSet(){
        Function<Integer, String[]> stringsFunc = size ->
                IntStreams.range(0, size).mapToObj(Integer::toString).toArray(String[]::new);
        BinaryOperator<String> concat = String::concat;
        return genericData(stringsFunc,
                (BinaryOperator<String>[]) new BinaryOperator[]{
                    concat });
    }

    private static <T, OPS> Object[][] genericData(Function<Integer, T> generateFunc, OPS[] ops) {
        //test arrays which size is equals n-1, n, n+1, test random data
        Object[][] data = new Object[ARRAY_SIZE_COLLECTION.length * 3 * ops.length][4];
        for(int n = 0; n < ARRAY_SIZE_COLLECTION.length; n++ ) {
            for(int testValue = -1 ; testValue <= 1; testValue++) {
                int array_size = ARRAY_SIZE_COLLECTION[n] + testValue;
                for(int opsN = 0; opsN < ops.length; opsN++) {
                    int index = n * 3 * ops.length + (testValue + 1) * ops.length + opsN;
                    data[index][0] = generateFunc.apply(array_size);
                    data[index][1] = array_size / 3;
                    data[index][2] = 2 * array_size / 3;
                    data[index][3] = ops[opsN];
                }
            }
        }
        return data;
    }

    @Test(dataProvider="intSet")
    public void testParallelPrefixForInt(int[] data, int fromIndex, int toIndex, IntBinaryOperator op) {
        int[] sequentialResult = data.clone();
        for (int index = fromIndex + 1; index < toIndex; index++) {
            sequentialResult[index ] = op.applyAsInt(sequentialResult[index  - 1], sequentialResult[index]);
        }

        int[] parallelResult = data.clone();
        J8Arrays.parallelPrefix(parallelResult, fromIndex, toIndex, op);
        assertArraysEqual(parallelResult, sequentialResult);

        int[] parallelRangeResult = java.util.Arrays.copyOfRange(data, fromIndex, toIndex);
        J8Arrays.parallelPrefix(parallelRangeResult, op);
        assertArraysEqual(parallelRangeResult, java.util.Arrays.copyOfRange(sequentialResult, fromIndex, toIndex));
    }

    @Test(dataProvider="longSet")
    public void testParallelPrefixForLong(long[] data, int fromIndex, int toIndex, LongBinaryOperator op) {
        long[] sequentialResult = data.clone();
        for (int index = fromIndex + 1; index < toIndex; index++) {
            sequentialResult[index ] = op.applyAsLong(sequentialResult[index  - 1], sequentialResult[index]);
        }

        long[] parallelResult = data.clone();
        J8Arrays.parallelPrefix(parallelResult, fromIndex, toIndex, op);
        assertArraysEqual(parallelResult, sequentialResult);

        long[] parallelRangeResult = java.util.Arrays.copyOfRange(data, fromIndex, toIndex);
        J8Arrays.parallelPrefix(parallelRangeResult, op);
        assertArraysEqual(parallelRangeResult, java.util.Arrays.copyOfRange(sequentialResult, fromIndex, toIndex));
    }

    @Test(dataProvider="doubleSet")
    public void testParallelPrefixForDouble(double[] data, int fromIndex, int toIndex, DoubleBinaryOperator op) {
        double[] sequentialResult = data.clone();
        for (int index = fromIndex + 1; index < toIndex; index++) {
            sequentialResult[index ] = op.applyAsDouble(sequentialResult[index  - 1], sequentialResult[index]);
        }

        double[] parallelResult = data.clone();
        J8Arrays.parallelPrefix(parallelResult, fromIndex, toIndex, op);
        assertArraysEqual(parallelResult, sequentialResult);

        double[] parallelRangeResult = java.util.Arrays.copyOfRange(data, fromIndex, toIndex);
        J8Arrays.parallelPrefix(parallelRangeResult, op);
        assertArraysEqual(parallelRangeResult, java.util.Arrays.copyOfRange(sequentialResult, fromIndex, toIndex));
    }

    @Test(dataProvider="stringSet")
    public void testParallelPrefixForStringr(String[] data , int fromIndex, int toIndex, BinaryOperator<String> op) {
        String[] sequentialResult = data.clone();
        for (int index = fromIndex + 1; index < toIndex; index++) {
            sequentialResult[index ] = op.apply(sequentialResult[index  - 1], sequentialResult[index]);
        }

        String[] parallelResult = data.clone();
        J8Arrays.parallelPrefix(parallelResult, fromIndex, toIndex, op);
        assertArraysEqual(parallelResult, sequentialResult);

        String[] parallelRangeResult = java.util.Arrays.copyOfRange(data, fromIndex, toIndex);
        J8Arrays.parallelPrefix(parallelRangeResult, op);
        assertArraysEqual(parallelRangeResult, java.util.Arrays.copyOfRange(sequentialResult, fromIndex, toIndex));
    }

    @Test
    public void testNPEs() {
        // null array
        assertThrows( () -> J8Arrays.parallelPrefix((int[]) null, java8.lang.Integers::max),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix((long []) null, java8.lang.Longs::max),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix((double []) null, java8.lang.Doubles::max),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix((String []) null, String::concat),
                NullPointerException.class, "should throw NPE");

        // null array w/ range
        assertThrows( () -> J8Arrays.parallelPrefix((int[]) null, 0, 0, java8.lang.Integers::max),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix((long []) null, 0, 0, java8.lang.Longs::max),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix((double []) null, 0, 0, java8.lang.Doubles::max),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix((String []) null, 0, 0, String::concat),
                NullPointerException.class, "should throw NPE");

        // null op
        assertThrows( () -> J8Arrays.parallelPrefix(new int[] {}, null),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix(new long[] {}, null),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix(new double[] {}, null),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix(new String[] {}, null),
                NullPointerException.class, "should throw NPE");

        // null op w/ range
        assertThrows( () -> J8Arrays.parallelPrefix(new int[] {}, 0, 0, null),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix(new long[] {}, 0, 0, null),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix(new double[] {}, 0, 0, null),
                NullPointerException.class, "should throw NPE");
        assertThrows( () -> J8Arrays.parallelPrefix(new String[] {}, 0, 0, null),
                NullPointerException.class, "should throw NPE");
    }

    @Test
    public void testIAEs() {
        assertThrows( () -> J8Arrays.parallelPrefix(new int[] {}, 1, 0, java8.lang.Integers::max),
                IllegalArgumentException.class, "should throw IAE");
        assertThrows( () -> J8Arrays.parallelPrefix(new long[] {}, 1, 0, java8.lang.Longs::max),
                IllegalArgumentException.class, "should throw IAE");
        assertThrows( () -> J8Arrays.parallelPrefix(new double[] {}, 1, 0, java8.lang.Doubles::max),
                IllegalArgumentException.class, "should throw IAE");
        assertThrows( () -> J8Arrays.parallelPrefix(new String[] {}, 1, 0, String::concat),
                IllegalArgumentException.class, "should throw IAE");
    }

    @Test
    public void testAIOBEs() {
        // bad "fromIndex"
        assertThrows( () -> J8Arrays.parallelPrefix(new int[] {}, -1, 0, java8.lang.Integers::max),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
        assertThrows( () -> J8Arrays.parallelPrefix(new long[] {}, -1, 0, java8.lang.Longs::max),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
        assertThrows( () -> J8Arrays.parallelPrefix(new double[] {}, -1, 0, java8.lang.Doubles::max),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
        assertThrows( () -> J8Arrays.parallelPrefix(new String[] {}, -1, 0, String::concat),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");

        // bad "toIndex"
        assertThrows( () -> J8Arrays.parallelPrefix(new int[] {}, 0, 1, java8.lang.Integers::max),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
        assertThrows( () -> J8Arrays.parallelPrefix(new long[] {}, 0, 1, java8.lang.Longs::max),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
        assertThrows( () -> J8Arrays.parallelPrefix(new double[] {}, 0, 1, java8.lang.Doubles::max),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
        assertThrows( () -> J8Arrays.parallelPrefix(new String[] {}, 0, 1, String::concat),
                ArrayIndexOutOfBoundsException.class, "should throw AIOBE");
    }

    // "library" code

    public interface Thrower<T extends Throwable> {

        public void run() throws T;
    }


    public static <T extends Throwable> void assertThrows(Thrower<T> thrower, Class<T> throwable) {
        assertThrows(thrower, throwable, null);
    }

    public static <T extends Throwable> void assertThrows(Thrower<T> thrower, Class<T> throwable, String message) {
        Throwable thrown;
        try {
            thrower.run();
            thrown = null;
        } catch (Throwable caught) {
            thrown = caught;
        }

        assertInstance(thrown, throwable,
            ((null != message) ? message : "") +
            " Failed to throw " + throwable.getCanonicalName());
    }

    //@SafeVarargs
    public static <T extends Throwable> void assertThrows(Class<T> throwable, String message, Thrower<T>... throwers) {
        for(Thrower<T> thrower : throwers) {
            assertThrows(thrower, throwable, message);
        }
    }

    public static void assertInstance(Object actual, Class<?> expected) {
        assertInstance(expected.isInstance(actual), null);
    }

    public static void assertInstance(Object actual, Class<?> expected, String message) {
        assertTrue(expected.isInstance(actual), message);
    }

    static void assertArraysEqual(int[] actual, int[] expected) {
        try {
            assertEquals(actual, expected, "");
        } catch (AssertionError x) {
            AssertionError ae = new AssertionError(String.format("Expected:%s, actual:%s",
                    java.util.Arrays.toString(expected), java.util.Arrays.toString(actual)));
            ae.initCause(x);
            throw ae;
        }
    }

    static void assertArraysEqual(long[] actual, long[] expected) {
        try {
            assertEquals(actual, expected, "");
        } catch (AssertionError x) {
            AssertionError ae = new AssertionError(String.format("Expected:%s, actual:%s",
                    java.util.Arrays.toString(expected), java.util.Arrays.toString(actual)));
            ae.initCause(x);
            throw ae;
        }
    }

    static void assertArraysEqual(double[] actual, double[] expected) {
        try {
            assertEquals(actual, expected, "");
        } catch (AssertionError x) {
            AssertionError ae = new AssertionError(String.format("Expected:%s, actual:%s",
                    java.util.Arrays.toString(expected), java.util.Arrays.toString(actual)));
            ae.initCause(x);
            throw ae;
        }
    }

    static void assertArraysEqual(String[] actual, String[] expected) {
        try {
            assertEquals(actual, expected, "");
        } catch (AssertionError x) {
            AssertionError ae = new AssertionError(String.format("Expected:%s, actual:%s",
                    java.util.Arrays.toString(expected), java.util.Arrays.toString(actual)));
            ae.initCause(x);
            throw ae;
        }
    }
}

