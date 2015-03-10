package com.ikueb.fizzbuzz;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Utilities class for performing on {@link FiBu} elements.
 */
public enum FiBuUtils {
    INSTANCE;

    /**
     * Creates a sequence of numbers between {@code a} and {@code b}
     * and process them using the {@code source} of {@link Stream}
     * @param a one of the sequence's boundary
     * @param b the sequence's other boundary
     * @param source the {@link Supplier} supplying the {@link Stream}
     * @return the outcome
     * @see #process(long, Supplier)
     */
    public static Collection<String> process(long a, long b,
            final Supplier<Stream<? extends FiBu>> source) {
        return LongStream.range(Math.min(a, b), Math.max(a, b))
                .mapToObj(i -> process(i, source)).collect(Collectors.toList());
    }

    /**
     * @param i the number to handle
     * @param source the {@link Supplier} supplying the {@link Stream}
     * @return a concatenation of outputs of {@code i}'s factors, or {@code i}
     */
    private static String process(long i,
            final Supplier<Stream<? extends FiBu>> source) {
        final String temp = source.get().filter(v -> i % v.getFactor() == 0)
                .map(v -> v.getOutput()).collect(Collectors.joining());
        return temp.isEmpty() ? Long.toString(i) : temp;
    }

    /**
     * Validates a single {@link Stream}.
     *
     * @param source the {@link Stream} to validate on
     * @return {@code true} if the validation passes
     * @see #validate(Supplier, Supplier)
     */
    public static boolean validate(final Supplier<Stream<? extends FiBu>> source) {
        return validate(source, source);
    }


    /**
     * Validates the incoming {@link Stream} according to the following rules:
     * <ul>
     * <li>{@code factor} cannot be less than 2</li>
     * <li>{@code output} cannot be null, empty, or all whitespaces</li>
     * <li>All {@code factor}s cannot be repeated or factor of another</li>
     * <li>All {@code ouput}s cannot be repeated</li>
     * </ul>
     *
     * @param incoming the {@link Supplier} supplying the incoming {@link Stream}
     * @param current the {@link Supplier} supplying the current {@link Stream}
     * @return {@code true} if the incoming {@link Stream} passes validation
     * @see #throwIf(Supplier, Predicate, String)
     * @throws IllegalStateException if validation fails
     */
    public static boolean validate(final Supplier<Stream<? extends FiBu>> incoming,
            final Supplier<Stream<? extends FiBu>> current) {
        throwIf(incoming, v -> v.getFactor() < 2, "factor < 2");
        throwIf(incoming, v -> Objects.toString(v.getOutput(), "").trim().isEmpty(),
                "output null, empty or all whitespaces");
        incoming.get().forEach(a -> throwIf(current, b -> isFactor(a, b),
                "same/factor/multiple of " + a));
        incoming.get().forEach(a -> throwIf(current, b -> isSameOutput(a, b),
                "same output as " + a));
        return true;
    }

    /**
     * @param source the {@link Stream} to check
     * @param factor the value to check
     * @return an {@link Optional} container over an instance of {@link FiBu}
     */
    public static Optional<FiBu> get(final Supplier<Stream<? extends FiBu>> source,
            long factor) {
        return getAll(source, factor).stream().findFirst();
    }

    /**
     * @param source the {@link Stream} to check
     * @param factors the values to check
     * @return a {@link Collection} of found instances, which may be less than the
     *         number of {@code factors}
     */
    public static Collection<FiBu> getAll(final Supplier<Stream<? extends FiBu>> source,
            long... factors) {
        return source.get().filter(v -> Arrays.stream(factors)
                .anyMatch(f -> v.getFactor() == f))
                .collect(Collectors.toList());
    }

    /**
     * @param a one {@link FiBu}
     * @param b other {@link FiBu}
     * @return true if one is a factor of the other
     */
    private static boolean isFactor(final FiBu a, final FiBu b) {
        final long a1 = a.getFactor();
        final long b1 = b.getFactor();
        return a != b && Math.max(a1, b1) % Math.min(a1, b1) == 0;
    }

    /**
     * @param a one {@link FiBu}
     * @param b other {@link FiBu}
     * @return true if one's {@code output} is the same as the other's
     */
    private static boolean isSameOutput(final FiBu a, final FiBu b) {
        return a != b && a.getOutput().equals(b.getOutput());
    }

    /**
     * Syntatic sugar to throw {@link IllegalStateException}
     * from any filtered value of a {@link Stream}.
     *
     * @param source the {@link Supplier} supplying the {@link Stream}
     * @param filter the {@link Predicate} to apply the filter with
     * @param suffix the {@link String} to append to the thrown {@link Exception}
     * @throws IllegalStateException if there are any filtered values
     */
    private static <T> void throwIf(final Supplier<Stream<? extends T>> source,
            final Predicate<T> filter, final String suffix) {
        source.get().filter(filter).findAny().ifPresent(x -> {
            throw new IllegalStateException(x + ": " + suffix); });
    }

    /**
     * @param prefix the prefix
     * @param value the value
     * @return the {@link String} representation
     */
    public static String toString(final String prefix, final FiBu value) {
        return prefix + "[" + value.getFactor() + "; " + value.getOutput() + "]";
    }
}
