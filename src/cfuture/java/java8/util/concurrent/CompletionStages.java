/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java8.util.concurrent;

import java.util.concurrent.Executor;
import java8.util.function.Function;

/**
 * A place for static default implementations of the new Java 12
 * default interface methods in the {@link CompletionStage} interface. 
 */
public final class CompletionStages {
// CVS rev. 1.41

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is executed with {@code thisStage}'s exception as the
     * argument to the supplied function, using {@code thisStage}'s default
     * asynchronous execution facility.  Otherwise, if {@code thisStage}
     * completes normally, then the returned stage also completes
     * normally with the same value.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes the {@link CompletionStage#toCompletableFuture}
     * version.
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
        (CompletionStage<T> thisStage,
         Function<Throwable, ? extends T> fn) {
        return thisStage.toCompletableFuture().exceptionallyAsync(fn);
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is executed with {@code thisStage}'s exception as the
     * argument to the supplied function, using the supplied
     * Executor.  Otherwise, if {@code thisStage} completes normally, then
     * the returned stage also completes normally with the same value.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes the {@link CompletionStage#toCompletableFuture}
     * version.
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
        (CompletionStage<T> thisStage,
         Function<Throwable, ? extends T> fn,
         Executor executor) {
        return thisStage.toCompletableFuture().exceptionallyAsync(fn, executor);
    }        

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is composed using the results of the supplied
     * function applied to {@code thisStage}'s exception.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes the {@link CompletionStage#toCompletableFuture}
     * version.
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
        (CompletionStage<T> thisStage,
         Function<Throwable, ? extends CompletionStage<T>> fn) {
        return thisStage.toCompletableFuture().exceptionallyCompose(fn);
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is composed using the results of the supplied
     * function applied to {@code thisStage}'s exception, using
     * {@code thisStage}'s default asynchronous execution facility.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes the {@link CompletionStage#toCompletableFuture}
     * version.
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
        (CompletionStage<T> thisStage,
         Function<Throwable, ? extends CompletionStage<T>> fn) {
        return thisStage.toCompletableFuture().exceptionallyComposeAsync(fn);
    }

    /**
     * Returns a new CompletionStage that, when {@code thisStage} completes
     * exceptionally, is composed using the results of the supplied
     * function applied to {@code thisStage}'s exception, using the
     * supplied Executor.
     *
     * <p><b>Implementation Requirements:</b><br> The default
     * implementation invokes the {@link CompletionStage#toCompletableFuture}
     * version.
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
        (CompletionStage<T> thisStage,
         Function<Throwable, ? extends CompletionStage<T>> fn,
         Executor executor) {
        return thisStage.toCompletableFuture().exceptionallyComposeAsync(fn, executor);
    }

    private CompletionStages() {}
}
