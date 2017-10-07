/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java8.util.function;

import java8.util.Objects;

/**
 * A place for static default implementations of the new Java 8
 * default interface methods and static interface methods in the
 * {@link Predicate} interface. 
 */
public final class Predicates {
    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * AND of the {@code this_} predicate and another.  When evaluating the composed
     * predicate, if the {@code this_} predicate is {@code false}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of the {@code this_} predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param <T> the type of the input to the predicates
     * @param this_ a predicate that will be logically-ANDed with the {@code other} predicate
     * @param other a predicate that will be logically-ANDed with the {@code this_}
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * AND of the {@code this_} predicate and the {@code other} predicate
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if other is null
     */
    public static <T> Predicate<T> and(Predicate<? super T> this_, Predicate<? super T> other) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(other);
        return (t) -> this_.test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of the {@code this_}
     * predicate.
     * @param <T> the type of the input to the predicate
     * @param this_ the predicate to be negated
     * @return a predicate that represents the logical negation of the {@code this_}
     * predicate
     * @throws NullPointerException if {@code this_} is null
     */
    public static <T> Predicate<T> negate(Predicate<? super T> this_) {
        Objects.requireNonNull(this_);
        return (t) -> !this_.test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * OR of the {@code this_} predicate and another.  When evaluating the composed
     * predicate, if the {@code this_} predicate is {@code true}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of the {@code this_} predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param <T> the type of the input to the predicates
     * @param this_ a predicate that will be logically-ORed with the {@code other} predicate
     * @param other a predicate that will be logically-ORed with the {@code this_}
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * OR of the {@code this_} predicate and the {@code other} predicate
     * @throws NullPointerException if {@code this_} is null
     * @throws NullPointerException if other is null
     */
    public static <T> Predicate<T> or(Predicate<? super T> this_, Predicate<? super T> other) {
        Objects.requireNonNull(this_);
        Objects.requireNonNull(other);
        return (t) -> this_.test(t) || other.test(t);
    }

    /**
     * Returns a predicate that tests if two arguments are equal according
     * to {@link Objects#equals(Object, Object)}.
     *
     * @param <T> the type of arguments to the predicate
     * @param targetRef the object reference with which to compare for equality,
     *               which may be {@code null}
     * @return a predicate that tests if two arguments are equal according
     * to {@link Objects#equals(Object, Object)}
     */
    public static <T> Predicate<T> isEqual(Object targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }

    private Predicates() {
    }
}
