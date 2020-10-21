/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/*
 * Any changes or additions made by the maintainers of the
 * streamsupport (https://github.com/stefan-zobel/streamsupport)
 * or retrostreams (https://github.com/retrostreams) libraries are
 * also released to the public domain, as explained at
 * https://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util.concurrent;

import java.util.concurrent.Executor;
import java8.util.function.BiFunction;
import java8.util.function.Function;
import java8.util.function.Functions;

/**
 * A place for static default implementations of the new Java 12
 * default interface methods in the {@link CompletionStage} interface. 
 */
public final class CompletionStages {
// CVS rev. 1.44

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is executed with {@code thisStage}'s exception as the
     * argument to the supplied function, using {@code thisStage}'s default
     * asynchronous execution facility.  Otherwise, if {@code thisStage}
     * completes normally, then the returned stage also completes
     * normally with the same value.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes {@link CompletionStage#handle}, relaying to
     * {@link CompletionStage#handleAsync} on exception, then
     * {@link CompletionStage#thenCompose} for result.
     *
     * @param <T> the CompletionStage's element type
     * @param thisStage the CompletionStage to decorate
     * @param fn the function to use to compute the value of the
     * returned CompletionStage if the {@code thisStage} CompletionStage
     * completed exceptionally
     * @return the new CompletionStage
     * @since 12
     */
    public static <T> CompletionStage<T> exceptionallyAsync
        (final CompletionStage<T> thisStage,
         final Function<Throwable, ? extends T> fn) {
        return thisStage.handle(new BiFunction<T, Throwable, CompletionStage<T>>() {
            @Override
            public CompletionStage<T> apply(T r, Throwable ex) {
                return (ex == null) ? thisStage : thisStage.<T>handleAsync(new BiFunction<T, Throwable, T>() {
                    @Override
                    public T apply(T r1, Throwable ex1) {
                        return fn.apply(ex1);
                    }
                });
            }
        }).thenCompose(Functions.<CompletionStage<T>>identity());
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is executed with {@code thisStage}'s exception as the
     * argument to the supplied function, using the supplied
     * Executor.  Otherwise, if {@code thisStage} completes normally, then
     * the returned stage also completes normally with the same value.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes {@link CompletionStage#handle}, relaying to
     * {@link CompletionStage#handleAsync} on exception, then
     * {@link CompletionStage#thenCompose} for result.
     *
     * @param <T> the CompletionStage's element type
     * @param thisStage the CompletionStage to decorate
     * @param fn the function to use to compute the value of the
     * returned CompletionStage if the {@code thisStage} CompletionStage
     * completed exceptionally
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     * @since 12
     */
    public static <T> CompletionStage<T> exceptionallyAsync
        (final CompletionStage<T> thisStage,
         final Function<Throwable, ? extends T> fn,
         final Executor executor) {
        return thisStage.handle(new BiFunction<T, Throwable, CompletionStage<T>>() {
            @Override
            public CompletionStage<T> apply(T r, Throwable ex) {
                return (ex == null) ? thisStage : thisStage.<T>handleAsync(new BiFunction<T, Throwable, T>() {
                    @Override
                    public T apply(T r1, Throwable ex1) {
                        return fn.apply(ex1);
                    }
                }, executor);
            }
        }).thenCompose(Functions.<CompletionStage<T>>identity());
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is composed using the results of the supplied
     * function applied to {@code thisStage}'s exception.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes {@link CompletionStage#handle}, invoking the
     * given function on exception, then {@link CompletionStage#thenCompose}
     * for result.
     *
     * @param <T> the CompletionStage's element type
     * @param thisStage the CompletionStage to compose with
     * @param fn the function to use to compute the returned
     * CompletionStage if the {@code thisStage} CompletionStage completed
     * exceptionally
     * @return the new CompletionStage
     * @since 12
     */
    public static <T> CompletionStage<T> exceptionallyCompose
        (final CompletionStage<T> thisStage,
         final Function<Throwable, ? extends CompletionStage<T>> fn) {
        return thisStage.handle(new BiFunction<T, Throwable, CompletionStage<T>>() {
            @Override
            public CompletionStage<T> apply(T r, Throwable ex) {
                return (ex == null) ? thisStage : fn.apply(ex);
            }
        }).thenCompose(Functions.<CompletionStage<T>>identity());
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is composed using the results of the supplied
     * function applied to {@code thisStage}'s exception, using
     * {@code thisStage}'s default asynchronous execution facility.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes {@link CompletionStage#handle}, relaying to
     * {@link CompletionStage#handleAsync} on exception, then
     * {@link CompletionStage#thenCompose} for result.
     *
     * @param <T> the CompletionStage's element type
     * @param thisStage the CompletionStage to compose with
     * @param fn the function to use to compute the returned
     * CompletionStage if the {@code thisStage} CompletionStage completed
     * exceptionally
     * @return the new CompletionStage
     * @since 12
     */
    public static <T> CompletionStage<T> exceptionallyComposeAsync
        (final CompletionStage<T> thisStage,
         final Function<Throwable, ? extends CompletionStage<T>> fn) {
        return thisStage.handle(new BiFunction<T, Throwable, CompletionStage<T>>() {
            @Override
            public CompletionStage<T> apply(T r, Throwable ex) {
                return (ex == null) ? thisStage
                        : thisStage.handleAsync(new BiFunction<T, Throwable, CompletionStage<T>>() {
                            @Override
                            public CompletionStage<T> apply(T r1, Throwable ex1) {
                                return fn.apply(ex1);
                            }
                        }).thenCompose(Functions.<CompletionStage<T>>identity());
            }
        }).thenCompose(Functions.<CompletionStage<T>>identity());
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is composed using the results of the supplied
     * function applied to {@code thisStage}'s exception, using the
     * supplied Executor.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes {@link CompletionStage#handle}, relaying to
     * {@link CompletionStage#handleAsync} on exception, then
     * {@link CompletionStage#thenCompose} for result.
     *
     * @param <T> the CompletionStage's element type
     * @param thisStage the CompletionStage to compose with
     * @param fn the function to use to compute the returned
     * CompletionStage if the {@code thisStage} CompletionStage completed
     * exceptionally
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     * @since 12
     */
    public static <T> CompletionStage<T> exceptionallyComposeAsync
        (final CompletionStage<T> thisStage,
         final Function<Throwable, ? extends CompletionStage<T>> fn,
         final Executor executor) {
        return thisStage.handle(new BiFunction<T, Throwable, CompletionStage<T>>() {
            @Override
            public CompletionStage<T> apply(T r, Throwable ex) {
                return (ex == null) ? thisStage
                        : thisStage.handleAsync(new BiFunction<T, Throwable, CompletionStage<T>>() {
                            @Override
                            public CompletionStage<T> apply(T r1, Throwable ex1) {
                                return fn.apply(ex1);
                            }
                        }, executor).thenCompose(Functions.<CompletionStage<T>>identity());
            }
        }).thenCompose(Functions.<CompletionStage<T>>identity());
    }

    private CompletionStages() {}
}
