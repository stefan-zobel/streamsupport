/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java8.util.concurrent.CountedCompleter;
import java8.util.function.Predicate;
import java8.util.function.Supplier;
import java8.util.Optional;
import java8.util.OptionalDouble;
import java8.util.OptionalInt;
import java8.util.OptionalLong;
import java8.util.Spliterator;

/**
 * Factory for instances of a short-circuiting {@code TerminalOp} that searches
 * for an element in a stream pipeline, and terminates when it finds one.
 * Supported variants include find-first (find the first element in the
 * encounter order) and find-any (find any element, may not be the first in
 * encounter order.)
 *
 * @since 1.8
 */
final class FindOps {

    private FindOps() { }

    private static final Predicate<Optional<Object>> REF_IS_PRESENT = Optional::isPresent;
    private static final Predicate<OptionalInt> INT_IS_PRESENT = OptionalInt::isPresent;
    private static final Predicate<OptionalLong> LONG_IS_PRESENT = OptionalLong::isPresent;
    private static final Predicate<OptionalDouble> DOUBLE_IS_PRESENT = OptionalDouble::isPresent;
    private static final Supplier<TerminalSink<Object, Optional<Object>>> REF_SINK_SUPP = FindSink.OfRef::new;
    private static final Supplier<TerminalSink<Integer, OptionalInt>> INT_SINK_SUPP = FindSink.OfInt::new;
    private static final Supplier<TerminalSink<Long, OptionalLong>> LONG_SINK_SUPP = FindSink.OfLong::new;
    private static final Supplier<TerminalSink<Double, OptionalDouble>> DOUBLE_SINK_SUPP = FindSink.OfDouble::new;

    @SuppressWarnings("rawtypes")
    private static final TerminalOp FIRST_REF = new FindOp<>(true, StreamShape.REFERENCE, Optional.empty(),
            REF_IS_PRESENT, REF_SINK_SUPP);
    @SuppressWarnings("rawtypes")
    private static final TerminalOp ANY_REF = new FindOp<>(false, StreamShape.REFERENCE, Optional.empty(),
            REF_IS_PRESENT, REF_SINK_SUPP);
    private static final TerminalOp<Integer, OptionalInt> FIRST_INT = new FindOp<>(true, StreamShape.INT_VALUE,
            OptionalInt.empty(), INT_IS_PRESENT, INT_SINK_SUPP);
    private static final TerminalOp<Integer, OptionalInt> ANY_INT = new FindOp<>(false, StreamShape.INT_VALUE,
            OptionalInt.empty(), INT_IS_PRESENT, INT_SINK_SUPP);
    private static final TerminalOp<Long, OptionalLong> FIRST_LONG = new FindOp<>(true, StreamShape.LONG_VALUE,
            OptionalLong.empty(), LONG_IS_PRESENT, LONG_SINK_SUPP);
    private static final TerminalOp<Long, OptionalLong> ANY_LONG = new FindOp<>(false, StreamShape.LONG_VALUE,
            OptionalLong.empty(), LONG_IS_PRESENT, LONG_SINK_SUPP);
    private static final TerminalOp<Double, OptionalDouble> FIRST_DOUBLE = new FindOp<>(true, StreamShape.DOUBLE_VALUE,
            OptionalDouble.empty(), DOUBLE_IS_PRESENT, DOUBLE_SINK_SUPP);
    private static final TerminalOp<Double, OptionalDouble> ANY_DOUBLE = new FindOp<>(false, StreamShape.DOUBLE_VALUE,
            OptionalDouble.empty(), DOUBLE_IS_PRESENT, DOUBLE_SINK_SUPP);

    /**
     * Constructs a {@code TerminalOp} for streams of objects.
     *
     * @param <T> the type of elements of the stream
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    @SuppressWarnings("unchecked")
    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean mustFindFirst) {
        return mustFindFirst ? FIRST_REF : ANY_REF;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of ints.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Integer, OptionalInt> makeInt(boolean mustFindFirst) {
        return mustFindFirst ? FIRST_INT : ANY_INT;
    }

    /**
     * Constructs a {@code TerminalOp} for streams of longs.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Long, OptionalLong> makeLong(boolean mustFindFirst) {
        return mustFindFirst ? FIRST_LONG : ANY_LONG;
    }

    /**
     * Constructs a {@code FindOp} for streams of doubles.
     *
     * @param mustFindFirst whether the {@code TerminalOp} must produce the
     *        first element in the encounter order
     * @return a {@code TerminalOp} implementing the find operation
     */
    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean mustFindFirst) {
        return mustFindFirst ? FIRST_DOUBLE : ANY_DOUBLE;
    }

    /**
     * A short-circuiting {@code TerminalOp} that searches for an element in a
     * stream pipeline, and terminates when it finds one.  Implements both
     * find-first (find the first element in the encounter order) and find-any
     * (find any element, may not be the first in encounter order.)
     *
     * @param <T> the output type of the stream pipeline
     * @param <O> the result type of the find operation, typically an optional
     *        type
     */
    private static final class FindOp<T, O> implements TerminalOp<T, O> {
        private final StreamShape shape;
        final int opFlags;
        final O emptyValue;
        final Predicate<O> presentPredicate;
        final Supplier<TerminalSink<T, O>> sinkSupplier;

        /**
         * Constructs a {@code FindOp}.
         *
         * @param mustFindFirst if true, must find the first element in
         *        encounter order, otherwise can find any element
         * @param shape stream shape of elements to search
         * @param emptyValue result value corresponding to "found nothing"
         * @param presentPredicate {@code Predicate} on result value
         *        corresponding to "found something"
         * @param sinkSupplier supplier for a {@code TerminalSink} implementing
         *        the matching functionality
         */
        FindOp(boolean mustFindFirst,
                       StreamShape shape,
                       O emptyValue,
                       Predicate<O> presentPredicate,
                       Supplier<TerminalSink<T, O>> sinkSupplier) {
            this.opFlags = StreamOpFlag.IS_SHORT_CIRCUIT | (mustFindFirst ? 0 : StreamOpFlag.NOT_ORDERED);
            this.shape = shape;
            this.emptyValue = emptyValue;
            this.presentPredicate = presentPredicate;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return opFlags;
        }

        @Override
        public StreamShape inputShape() {
            return shape;
        }

        @Override
        public <S> O evaluateSequential(PipelineHelper<T> helper,
                                        Spliterator<S> spliterator) {
            O result = helper.wrapAndCopyInto(sinkSupplier.get(), spliterator).get();
            return result != null ? result : emptyValue;
        }

        @Override
        public <P_IN> O evaluateParallel(PipelineHelper<T> helper,
                                         Spliterator<P_IN> spliterator) {
            // This takes into account the upstream ops flags and the terminal
            // op flags and therefore takes into account findFirst or findAny
            boolean mustFindFirst = StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags());
            return new FindTask<>(this, mustFindFirst, helper, spliterator).invoke();
        }
    }

    /**
     * Implementation of {@code TerminalSink} that implements the find
     * functionality, requesting cancellation when something has been found
     *
     * @param <T> The type of input element
     * @param <O> The result type, typically an optional type
     */
    private abstract static class FindSink<T, O> implements TerminalSink<T, O> {
        boolean hasValue;
        T value;

        FindSink() {} // Avoid creation of special accessor

        @Override
        public void accept(T value) {
            if (!hasValue) {
                hasValue = true;
                this.value = value;
            }
        }

        @Override
        public boolean cancellationRequested() {
            return hasValue;
        }

        @Override
        public void begin(long size) {
        }

        @Override
        public void end() {
        }

        @Override
        public void accept(int value) {
            SinkDefaults.reject();
        }

        @Override
        public void accept(long value) {
            SinkDefaults.reject();
        }

        @Override
        public void accept(double value) {
            SinkDefaults.reject();
        }

        /** Specialization of {@code FindSink} for reference streams */
        static final class OfRef<T> extends FindSink<T, Optional<T>> {
            @Override
            public Optional<T> get() {
                return hasValue ? Optional.of(value) : null;
            }
        }

        /** Specialization of {@code FindSink} for int streams */
        static final class OfInt extends FindSink<Integer, OptionalInt>
                implements Sink.OfInt {
            @Override
            public void accept(int value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Integer) value);
            }

            @Override
            public OptionalInt get() {
                return hasValue ? OptionalInt.of(value) : null;
            }
        }

        /** Specialization of {@code FindSink} for long streams */
        static final class OfLong extends FindSink<Long, OptionalLong>
                implements Sink.OfLong {
            @Override
            public void accept(long value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Long) value);
            }

            @Override
            public OptionalLong get() {
                return hasValue ? OptionalLong.of(value) : null;
            }
        }

        /** Specialization of {@code FindSink} for double streams */
        static final class OfDouble extends FindSink<Double, OptionalDouble>
                implements Sink.OfDouble {
            @Override
            public void accept(double value) {
                // Boxing is OK here, since few values will actually flow into the sink
                accept((Double) value);
            }

            @Override
            public OptionalDouble get() {
                return hasValue ? OptionalDouble.of(value) : null;
            }
        }
    }

    /**
     * {@code ForkJoinTask} implementing parallel short-circuiting search
     * @param <P_IN> Input element type to the stream pipeline
     * @param <P_OUT> Output element type from the stream pipeline
     * @param <O> Result type from the find operation
     */
    @SuppressWarnings("serial")
    private static final class FindTask<P_IN, P_OUT, O>
            extends AbstractShortCircuitTask<P_IN, P_OUT, O, FindTask<P_IN, P_OUT, O>> {
        private final FindOp<P_OUT, O> op;
        private final boolean mustFindFirst;

        FindTask(FindOp<P_OUT, O> op,
                 boolean mustFindFirst,
                 PipelineHelper<P_OUT> helper,
                 Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.mustFindFirst = mustFindFirst;
            this.op = op;
        }

        FindTask(FindTask<P_IN, P_OUT, O> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.mustFindFirst = parent.mustFindFirst;
            this.op = parent.op;
        }

        @Override
        protected FindTask<P_IN, P_OUT, O> makeChild(Spliterator<P_IN> spliterator) {
            return new FindTask<>(this, spliterator);
        }

        @Override
        protected O getEmptyResult() {
            return op.emptyValue;
        }

        private void foundResult(O answer) {
            if (isLeftmostNode())
                shortCircuit(answer);
            else
                cancelLaterNodes();
        }

        @Override
        protected O doLeaf() {
            O result = helper.wrapAndCopyInto(op.sinkSupplier.get(), spliterator).get();
            if (!mustFindFirst) {
                if (result != null)
                    shortCircuit(result);
                return null;
            }
            else {
                if (result != null) {
                    foundResult(result);
                    return result;
                }
                else
                    return null;
            }
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (mustFindFirst) {
                    for (FindTask<P_IN, P_OUT, O> child = leftChild, p = null; child != p;
                         p = child, child = rightChild) {
                    O result = child.getLocalResult();
                    if (result != null && op.presentPredicate.test(result)) {
                        setLocalResult(result);
                        foundResult(result);
                        break;
                    }
                }
            }
            super.onCompletion(caller);
        }
    }
}

