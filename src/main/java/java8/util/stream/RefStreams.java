/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates. All rights reserved.
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
package java8.util.stream;

import java8.util.Objects;
import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.Consumer;
import java8.util.function.Predicate;
import java8.util.function.Supplier;
import java8.util.function.UnaryOperator;
import java8.util.stream.Stream.Builder;

/**
 * A place for the implementations of the new Java 8/9 static interface methods
 * in the {@link Stream} interface.
 */
public final class RefStreams {
    // Static factories

    /**
     * Returns a builder for a {@link Stream}.
     *
     * @param <T> type of elements
     * @return a stream builder
     */
    public static <T> Builder<T> builder() {
        return new Streams.StreamBuilderImpl<>();
    }

    /**
     * Returns an empty sequential {@link Stream}.
     *
     * @param <T> the type of stream elements
     * @return an empty sequential stream
     */
    public static <T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.<T>emptySpliterator(), false);
    }

    /**
     * Returns a sequential {@link Stream} containing a single element.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a singleton sequential stream
     */
    public static <T> Stream<T> of(T t) {
        return StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    /**
     * Returns a sequential {@link Stream} containing a single element, if
     * non-null, otherwise returns an empty {@code Stream}.
     *
     * @param t the single element
     * @param <T> the type of stream elements
     * @return a stream with a single element if the specified element
     *         is non-null, otherwise an empty stream
     * @since 9
     */
    public static <T> Stream<T> ofNullable(T t) {
        return t == null ? empty()
                : StreamSupport.stream(new Streams.StreamBuilderImpl<>(t), false);
    }

    /**
     * Returns a sequential ordered {@link Stream} whose elements are the
     * specified values.
     *
     * @param <T> the type of stream elements
     * @param values the elements of the new stream
     * @return the new stream
     */
    public static <T> Stream<T> of(@SuppressWarnings("unchecked") T... values) {
        return java8.util.J8Arrays.stream(values);
    }

    /**
     * Returns an infinite sequential ordered {@link Stream} produced by iterative
     * application of a function {@code f} to an initial element {@code seed},
     * producing a {@code Stream} consisting of {@code seed}, {@code f(seed)},
     * {@code f(f(seed))}, etc.
     *
     * <p>The first element (position {@code 0}) in the {@code Stream} will be
     * the provided {@code seed}.  For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * <p>The action of applying {@code f} for one element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying {@code f} for subsequent elements.  For any given
     * element the action may be performed in whatever thread the library
     * chooses.
     *
     * @param <S> the type of the operand and seed, a subtype of T
     * @param <T> the type of stream elements
     * @param seed the initial element
     * @param f a function to be applied to the previous element to produce
     *          a new element
     * @return a new sequential {@code Stream}
     */
    public static <T, S extends T> Stream<T> iterate(S seed, UnaryOperator<S> f) {
        Objects.requireNonNull(f);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, 
               Spliterator.ORDERED | Spliterator.IMMUTABLE) {
            S prev;
            boolean started;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                S s;
                if (started) {
                    s = f.apply(prev);
                } else {
                    s = seed;
                    started = true;
                }
                action.accept(prev = s);
                return true;
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Returns a sequential ordered {@code Stream} produced by iterative
     * application of the given {@code next} function to an initial element,
     * conditioned on satisfying the given {@code hasNext} predicate.  The
     * stream terminates as soon as the {@code hasNext} predicate returns false.
     *
     * <p>{@code RefStreams.iterate} should produce the same sequence of elements as
     * produced by the corresponding for-loop:
     * <pre>{@code
     *     for (T index=seed; hasNext.test(index); index = next.apply(index)) { 
     *         ... 
     *     }
     * }</pre>
     *
     * <p>The resulting sequence may be empty if the {@code hasNext} predicate
     * does not hold on the seed value.  Otherwise the first element will be the
     * supplied {@code seed} value, the next element (if present) will be the
     * result of applying the {@code next} function to the {@code seed} value,
     * and so on iteratively until the {@code hasNext} predicate indicates that
     * the stream should terminate.
     *
     * <p>The action of applying the {@code hasNext} predicate to an element
     * <a href="../concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * the action of applying the {@code next} function to that element.  The
     * action of applying the {@code next} function for one element
     * <i>happens-before</i> the action of applying the {@code hasNext}
     * predicate for subsequent elements.  For any given element an action may
     * be performed in whatever thread the library chooses.
     *
     * @param <S> the type of the operand, predicate input and seed, a subtype of T
     * @param <T> the type of stream elements
     * @param seed the initial element
     * @param hasNext a predicate to apply to elements to determine when the 
     *                stream must terminate
     * @param next a function to be applied to the previous element to produce
     *             a new element
     * @return a new sequential {@code Stream}
     * @since 9
     */
    public static <T, S extends T> Stream<T> iterate(S seed, Predicate<? super S> hasNext, UnaryOperator<S> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, 
               Spliterator.ORDERED | Spliterator.IMMUTABLE) {
            S prev;
            boolean started, finished;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return false;
                }
                S s;
                if (started) {
                    s = next.apply(prev);
                } else {
                    s = seed;
                    started = true;
                }
                if (!hasNext.test(s)) {
                    prev = null;
                    finished = true;
                    return false;
                }
                action.accept(prev = s);
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return;
                }
                finished = true;
                S s = started ? next.apply(prev) : seed;
                prev = null;
                while (hasNext.test(s)) {
                    action.accept(s);
                    s = next.apply(s);
                }
            }
        };
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Returns an infinite sequential unordered {@link Stream} where each
     * element is generated by the provided {@code Supplier}.  This is
     * suitable for generating constant streams, streams of random elements,
     * etc.
     *
     * @param <T> the type of stream elements
     * @param s the {@code Supplier} of generated elements
     * @return a new infinite sequential unordered {@code Stream}
     */
    public static <T> Stream<T> generate(Supplier<? extends T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(
                new StreamSpliterators.InfiniteSupplyingSpliterator.OfRef<>(Long.MAX_VALUE, s), false);
    }

    /**
     * Creates a lazily concatenated {@link Stream} whose elements are all the
     * elements of the first stream followed by all the elements of the
     * second stream.  The resulting stream is ordered if both
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for both input streams are invoked.
     * 
     * <p>This method operates on the two input streams and binds each stream
     * to its source.  As a result subsequent modifications to an input stream
     * source may not be reflected in the concatenated stream result.
     * 
     * <p><b>Implementation Note:</b><br>
     * Use caution when constructing streams from repeated concatenation.
     * Accessing an element of a deeply concatenated stream can result in deep
     * call chains, or even {@code StackOverflowError}.
     * <p>Subsequent changes to the sequential/parallel execution mode of the
     * returned stream are not guaranteed to be propagated to the input streams.
     * 
     * <p><b>API Note:</b><br>
     * To preserve optimization opportunities this method binds each stream to
     * its source and accepts only two streams as parameters.  For example, the
     * exact size of the concatenated stream source can be computed if the exact
     * size of each input stream source is known.
     * To concatenate more streams without binding, or without nested calls to
     * this method, try creating a stream of streams and flat-mapping with the
     * identity function, for example:
     * <pre>{@code
     *     Stream<T> concat = RefStreams.of(s1, s2, s3, s4).flatMap(s -> s);
     * }</pre>
     * 
     * @param <T> The type of stream elements
     * @param a the first stream
     * @param b the second stream
     * @return the concatenation of the two input streams
     */
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        @SuppressWarnings("unchecked")
        Spliterator<T> split = new Streams.ConcatSpliterator.OfRef<>(
                (Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator());
        Stream<T> stream = StreamSupport.stream(split, a.isParallel() || b.isParallel());
        return stream.onClose(Streams.composedClose(a, b));
    }

    private RefStreams() {
    }
}
