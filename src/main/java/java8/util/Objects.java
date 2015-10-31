/*
 * Copyright (c) 2009, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package java8.util;

import java.util.Arrays;
import java.util.Comparator;

import java8.util.function.BiFunction;
import java8.util.function.Supplier;

/**
 * This class consists of {@code static} utility methods for operating
 * on objects, or checking certain conditions before operation.  These utilities
 * include {@code null}-safe or {@code null}-tolerant methods for computing the
 * hash code of an object, returning a string for an object, comparing two
 * objects, and checking if indexes or sub-range values are out of bounds.
 *
 * <p><b>API Note:</b><br>
 * Static methods such as {@link Objects#checkIndex},
 * {@link Objects#checkFromToIndex}, and {@link Objects#checkFromIndexSize} are
 * provided for the convenience of checking if values corresponding to indexes
 * and sub-ranges are out of bounds.
 * Variations of these static methods support customization of the runtime
 * exception, and corresponding exception detail message, that is thrown when
 * values are out of bounds.  Such methods accept a functional interface
 * argument, instances of {@code BiFunction}, that maps out of bound values to a
 * runtime exception.  Care should be taken when using such methods in
 * combination with an argument that is a lambda expression, method reference or
 * class that capture values.  In such cases the cost of capture, related to
 * functional interface allocation, may exceed the cost of checking bounds.
 *
 * @since 1.7
 */
public final class Objects {
    private Objects() {
        throw new AssertionError();
    }

    /**
     * Returns {@code true} if the arguments are equal to each other
     * and {@code false} otherwise.
     * Consequently, if both arguments are {@code null}, {@code true}
     * is returned and if exactly one argument is {@code null}, {@code
     * false} is returned.  Otherwise, equality is determined by using
     * the {@link Object#equals equals} method of the first
     * argument.
     *
     * @param a an object
     * @param b an object to be compared with {@code a} for equality
     * @return {@code true} if the arguments are equal to each other
     * and {@code false} otherwise
     * @see Object#equals(Object)
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

   /**
    * Returns {@code true} if the arguments are deeply equal to each other
    * and {@code false} otherwise.
    *
    * Two {@code null} values are deeply equal.  If both arguments are
    * arrays, the algorithm in {@link Arrays#deepEquals(Object[],
    * Object[]) Arrays.deepEquals} is used to determine equality.
    * Otherwise, equality is determined by using the {@link
    * Object#equals equals} method of the first argument.
    *
    * @param a an object
    * @param b an object to be compared with {@code a} for deep equality
    * @return {@code true} if the arguments are deeply equal to each other
    * and {@code false} otherwise
    * @see Arrays#deepEquals(Object[], Object[])
    * @see Objects#equals(Object, Object)
    */
    public static boolean deepEquals(Object a, Object b) {
        if (a == b)
            return true;
        else if (a == null || b == null)
            return false;
        else
            return deepEquals0(a, b);
    }

    private static boolean deepEquals0(Object e1, Object e2) {
        boolean eq;
        if (e1 instanceof Object[] && e2 instanceof Object[])
            eq = Arrays.deepEquals ((Object[]) e1, (Object[]) e2);
        else if (e1 instanceof byte[] && e2 instanceof byte[])
            eq = equals((byte[]) e1, (byte[]) e2);
        else if (e1 instanceof short[] && e2 instanceof short[])
            eq = equals((short[]) e1, (short[]) e2);
        else if (e1 instanceof int[] && e2 instanceof int[])
            eq = equals((int[]) e1, (int[]) e2);
        else if (e1 instanceof long[] && e2 instanceof long[])
            eq = equals((long[]) e1, (long[]) e2);
        else if (e1 instanceof char[] && e2 instanceof char[])
            eq = equals((char[]) e1, (char[]) e2);
        else if (e1 instanceof float[] && e2 instanceof float[])
            eq = equals((float[]) e1, (float[]) e2);
        else if (e1 instanceof double[] && e2 instanceof double[])
            eq = equals((double[]) e1, (double[]) e2);
        else if (e1 instanceof boolean[] && e2 instanceof boolean[])
            eq = equals((boolean[]) e1, (boolean[]) e2);
        else
            eq = e1.equals(e2);
        return eq;
    }

    /**
     * Returns the hash code of a non-{@code null} argument and 0 for
     * a {@code null} argument.
     *
     * @param o an object
     * @return the hash code of a non-{@code null} argument and 0 for
     * a {@code null} argument
     * @see Object#hashCode
     */
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

   /**
    * Generates a hash code for a sequence of input values. The hash
    * code is generated as if all the input values were placed into an
    * array, and that array were hashed by calling {@link
    * Arrays#hashCode(Object[])}.
    *
    * <p>This method is useful for implementing {@link
    * Object#hashCode()} on objects containing multiple fields. For
    * example, if an object that has three fields, {@code x}, {@code
    * y}, and {@code z}, one could write:
    *
    * <blockquote><pre>
    * &#064;Override public int hashCode() {
    *     return Objects.hash(x, y, z);
    * }
    * </pre></blockquote>
    *
    * <b>Warning: When a single object reference is supplied, the returned
    * value does not equal the hash code of that object reference.</b> This
    * value can be computed by calling {@link #hashCode(Object)}.
    *
    * @param values the values to be hashed
    * @return a hash value of the sequence of input values
    * @see Arrays#hashCode(Object[])
    * @see java.util.List#hashCode
    */
    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    /**
     * Returns the result of calling {@code toString} for a non-{@code
     * null} argument and {@code "null"} for a {@code null} argument.
     *
     * @param o an object
     * @return the result of calling {@code toString} for a non-{@code
     * null} argument and {@code "null"} for a {@code null} argument
     * @see Object#toString
     * @see String#valueOf(Object)
     */
    public static String toString(Object o) {
        return String.valueOf(o);
    }

    /**
     * Returns the result of calling {@code toString} on the first
     * argument if the first argument is not {@code null} and returns
     * the second argument otherwise.
     *
     * @param o an object
     * @param nullDefault string to return if the first argument is
     *        {@code null}
     * @return the result of calling {@code toString} on the first
     * argument if it is not {@code null} and the second argument
     * otherwise.
     * @see Objects#toString(Object)
     */
    public static String toString(Object o, String nullDefault) {
        return (o != null) ? o.toString() : nullDefault;
    }

    /**
     * Returns 0 if the arguments are identical and {@code
     * c.compare(a, b)} otherwise.
     * Consequently, if both arguments are {@code null} 0
     * is returned.
     *
     * <p>Note that if one of the arguments is {@code null}, a {@code
     * NullPointerException} may or may not be thrown depending on
     * what ordering policy, if any, the {@link Comparator Comparator}
     * chooses to have for {@code null} values.
     *
     * @param <T> the type of the objects being compared
     * @param a an object
     * @param b an object to be compared with {@code a}
     * @param c the {@code Comparator} to compare the first two arguments
     * @return 0 if the arguments are identical and {@code
     * c.compare(a, b)} otherwise.
     * @see Comparable
     * @see Comparator
     */
    public static <T> int compare(T a, T b, Comparator<? super T> c) {
        return (a == b) ? 0 :  c.compare(a, b);
    }

    /**
     * Checks that the specified object reference is not {@code null}. This
     * method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar) {
     *     this.bar = Objects.requireNonNull(bar);
     * }
     * </pre></blockquote>
     *
     * @param obj the object reference to check for nullity
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

    /**
     * Checks that the specified object reference is not {@code null} and
     * throws a customized {@link NullPointerException} if it is. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar, Baz baz) {
     *     this.bar = Objects.requireNonNull(bar, "bar must not be null");
     *     this.baz = Objects.requireNonNull(baz, "baz must not be null");
     * }
     * </pre></blockquote>
     *
     * @param obj     the object reference to check for nullity
     * @param message detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    /**
     * Returns {@code true} if the provided reference is {@code null} otherwise
     * returns {@code false}.
     *
     * <p><b>API Note:</b><br> This method exists to be used as a
     * {@link java8.util.function.Predicate}, {@code filter(Objects::isNull)}
     *
     * @param obj a reference to be checked against {@code null}
     * @return {@code true} if the provided reference is {@code null} otherwise
     * {@code false}
     *
     * @see java.util.function.Predicate
     * @since 1.8
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Returns {@code true} if the provided reference is non-{@code null}
     * otherwise returns {@code false}.
     *
     * <p><b>API Note:</b><br> This method exists to be used as a
     * {@link java8.util.function.Predicate}, {@code filter(Objects::nonNull)}
     *
     * @param obj a reference to be checked against {@code null}
     * @return {@code true} if the provided reference is non-{@code null}
     * otherwise {@code false}
     *
     * @see java.util.function.Predicate
     * @since 1.8
     */
    public static boolean nonNull(Object obj) {
        return obj != null;
    }

//    /**
//     * Returns the first argument if it is non-{@code null} and
//     * otherwise returns the non-{@code null} second argument.
//     *
//     * @param obj an object
//     * @param defaultObj a non-{@code null} object to return if the first argument
//     *                   is {@code null}
//     * @param <T> the type of the reference
//     * @return the first argument if it is non-{@code null} and
//     *        otherwise the second argument if it is non-{@code null}
//     * @throws NullPointerException if both {@code obj} is null and
//     *        {@code defaultObj} is {@code null}
//     * @since 9
//     */
//    public static <T> T nonNullElse(T obj, T defaultObj) {
//        return (obj != null) ? obj : requireNonNull(defaultObj, "defaultObj");
//    }
//
//    /**
//     * Returns the first argument if it is non-{@code null} and otherwise
//     * returns the non-{@code null} value of {@code supplier.get()}.
//     *
//     * @param obj an object
//     * @param supplier of a non-{@code null} object to return if the first argument
//     *                 is {@code null}
//     * @param <T> the type of the first argument and return type
//     * @return the first argument if it is non-{@code null} and otherwise
//     *         the value from {@code supplier.get()} if it is non-{@code null}
//     * @throws NullPointerException if both {@code obj} is null and
//     *        either the {@code supplier} is {@code null} or
//     *        the {@code supplier.get()} value is {@code null}
//     * @since 9
//     */
//    public static <T> T nonNullElseGet(T obj, Supplier<? extends T> supplier) {
//        return (obj != null) ? obj : requireNonNull(requireNonNull(supplier, "supplier").get(), "supplier.get()");
//    }

    /**
     * Checks that the specified object reference is not {@code null} and
     * throws a customized {@link NullPointerException} if it is.
     *
     * <p>Unlike the method {@link #requireNonNull(Object, String)},
     * this method allows creation of the message to be deferred until
     * after the null check is made. While this may confer a
     * performance advantage in the non-null case, when deciding to
     * call this method care should be taken that the costs of
     * creating the message supplier are less than the cost of just
     * creating the string message directly.
     *
     * @param obj     the object reference to check for nullity
     * @param messageSupplier supplier of the detail message to be
     * used in the event that a {@code NullPointerException} is thrown
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     * @since 1.8
     */
    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null)
            throw new NullPointerException(messageSupplier.get());
        return obj;
    }
    /**
     * Maps out of bounds values to a runtime exception.
     *
     * @param a the first out of bound value
     * @param b the second out of bound value
     * @param oobe the exception mapping function that when applied with out of
     *        bounds arguments returns a runtime exception.  If {@code null}
     *        then, it's as if an exception mapping function was supplied that
     *        returns {@link IndexOutOfBoundsException} for any given arguments.
     * @return the runtime exception
     */
    private static RuntimeException outOfBounds(
            int a, int b, BiFunction<Integer, Integer, ? extends RuntimeException> oobe) {
        return oobe == null
               ? new IndexOutOfBoundsException("Indexed access out of bounds: " + a + ", " + b)
               : oobe.apply(a, b);
    }

    /**
     * Checks if the {@code index} is within the bounds of the range from
     * {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * <p>The {@code index} is defined to be out of bounds if any of the
     * following inequalities is true:
     * <ul>
     *  <li>{@code index < 0}</li>
     *  <li>{@code index >= length}</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * @param index the index
     * @param length the upper-bound (exclusive) of the range
     * @return {@code index} if it is within bounds of the range
     * @throws IndexOutOfBoundsException if the {@code index} is out of bounds
     * @since 9
     */
    public static
    int checkIndex(int index, int length) throws IndexOutOfBoundsException {
        return checkIndex(index, length, null);
    }

    /**
     * Checks if the {@code index} is within the bounds of the range from
     * {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * <p>The {@code index} is defined to be out of bounds if any of the
     * following inequalities is true:
     * <ul>
     *  <li>{@code index < 0}</li>
     *  <li>{@code index >= length}</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * <p>If the {@code index} is out of bounds, then a runtime exception is
     * thrown that is the result of applying the arguments {@code index} and
     * {@code length} to the given exception mapping function.
     *
     * @param <T> the type of runtime exception to throw if the arguments are
     *        out of bounds
     * @param index the index
     * @param length the upper-bound (exclusive) of the range
     * @param oobe the exception mapping function that when applied with out
     *        of bounds arguments returns a runtime exception.  If {@code null}
     *        then, it's as if an exception mapping function was supplied that
     *        returns {@link IndexOutOfBoundsException} for any given arguments.
     * @return {@code index} if it is within bounds of the range
     * @throws T if the {@code index} is out of bounds, then a runtime exception
     *         is thrown that is the result of applying the out of bounds
     *         arguments to the exception mapping function.
     * @throws IndexOutOfBoundsException if the {@code index} is out of bounds
     *         and the exception mapping function is {@code null}
     * @since 9
     */
    public static <T extends RuntimeException>
    int checkIndex(int index, int length,
                   BiFunction<Integer, Integer, T> oobe) throws T, IndexOutOfBoundsException {
        if (index < 0 || index >= length)
            throw outOfBounds(index, length, oobe);
        return index;
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to
     * {@code toIndex} (exclusive) is within the bounds of range from {@code 0}
     * (inclusive) to {@code length} (exclusive).
     *
     * <p>The sub-range is defined to be out of bounds if any of the following
     * inequalities is true:
     * <ul>
     *  <li>{@code fromIndex < 0}</li>
     *  <li>{@code fromIndex > toIndex}</li>
     *  <li>{@code toIndex > length}</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-range
     * @param toIndex the upper-bound (exclusive) of the sub-range
     * @param length the upper-bound (exclusive) the range
     * @return {@code fromIndex} if the sub-range within bounds of the range
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     * @since 9
     */
    public static
    int checkFromToIndex(int fromIndex, int toIndex, int length) throws IndexOutOfBoundsException {
        return checkFromToIndex(fromIndex, toIndex, length, null);
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to
     * {@code toIndex} (exclusive) is within the bounds of range from {@code 0}
     * (inclusive) to {@code length} (exclusive).
     *
     * <p>The sub-range is defined to be out of bounds if any of the following
     * inequalities is true:
     * <ul>
     *  <li>{@code fromIndex < 0}</li>
     *  <li>{@code fromIndex > toIndex}</li>
     *  <li>{@code toIndex > length}</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * <p>If the sub-range is out of bounds, then a runtime exception is thrown
     * that is the result of applying the arguments {@code fromIndex} and
     * {@code toIndex} to the given exception mapping function.
     *
     * @param <T> the type of runtime exception to throw if the arguments are
     *        out of bounds
     * @param fromIndex the lower-bound (inclusive) of the sub-range
     * @param toIndex the upper-bound (exclusive) of the sub-range
     * @param length the upper-bound (exclusive) the range
     * @param oobe the exception mapping function that when applied with out
     *        of bounds arguments returns a runtime exception.  If {@code null}
     *        then, it's as if an exception mapping function was supplied that
     *        returns {@link IndexOutOfBoundsException} for any given arguments.
     * @return {@code fromIndex} if the sub-range within bounds of the range
     * @throws T if the sub-range is out of bounds, then a runtime exception is
     *         thrown that is the result of applying the out of bounds arguments
     *         to the exception mapping function.
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds and
     *         the exception mapping function is {@code null}
     * @since 9
     */
    public static <T extends RuntimeException>
    int checkFromToIndex(int fromIndex, int toIndex, int length,
                         BiFunction<Integer, Integer, T> oobe) throws T, IndexOutOfBoundsException {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > length)
            throw outOfBounds(fromIndex, toIndex, oobe);
        return fromIndex;
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to
     * {@code fromIndex + size} (exclusive) is within the bounds of range from
     * {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * <p>The sub-range is defined to be out of bounds if any of the following
     * inequalities is true:
     * <ul>
     *  <li>{@code fromIndex < 0}</li>
     *  <li>{@code size < 0}</li>
     *  <li>{@code fromIndex + size > length}, taking into account integer overflow</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-interval
     * @param size the size of the sub-range
     * @param length the upper-bound (exclusive) of the range
     * @return {@code fromIndex} if the sub-range within bounds of the range
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     * @since 9
     */
    public static
    int checkFromIndexSize(int fromIndex, int size, int length) throws IndexOutOfBoundsException {
        return checkFromIndexSize(fromIndex, size, length, null);
    }

    /**
     * Checks if the sub-range from {@code fromIndex} (inclusive) to
     * {@code fromIndex + size} (exclusive) is within the bounds of range from
     * {@code 0} (inclusive) to {@code length} (exclusive).
     *
     * <p>The sub-range is defined to be out of bounds if any of the following
     * inequalities is true:
     * <ul>
     *  <li>{@code fromIndex < 0}</li>
     *  <li>{@code size < 0}</li>
     *  <li>{@code fromIndex + size > length}, taking into account integer overflow</li>
     *  <li>{@code length < 0}, which is implied from the former inequalities</li>
     * </ul>
     *
     * <p>If the sub-range is out of bounds then, a runtime exception is thrown
     * that is the result of applying the arguments {@code fromIndex} and
     * {@code size} to the given exception mapping function.
     *
     * @param <T> the type of runtime exception to throw if the arguments are
     *        out of bounds
     * @param fromIndex the lower-bound (inclusive) of the sub-interval
     * @param size the size of the sub-range
     * @param length the upper-bound (exclusive) of the range
     * @param oobe the exception mapping function that when applied with out
     *        of bounds arguments returns a runtime exception.  If {@code null}
     *        then, it's as if an exception mapping function was supplied that
     *        returns {@link IndexOutOfBoundsException} for any given arguments.
     * @return {@code fromIndex} if the sub-range within bounds of the range
     * @throws T if the sub-range is out of bounds, then a runtime exception is
     *         thrown that is the result of applying the out of bounds arguments
     *         to the exception mapping function.
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds and
     *         the exception mapping function is {@code null}
     * @since 9
     */
    public static <T extends RuntimeException>
    int checkFromIndexSize(int fromIndex, int size, int length,
                           BiFunction<Integer, Integer, T> oobe) throws T, IndexOutOfBoundsException {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex)
            throw outOfBounds(fromIndex, size, oobe);
        return fromIndex;
    }
}
