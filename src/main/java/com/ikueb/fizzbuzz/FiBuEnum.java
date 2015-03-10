package com.ikueb.fizzbuzz;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A defensive {@code enum}-based implementation for doing the FizzBuzz.
 * What that means is that a {@code static} code block checks that each
 * {@code enum} is not divisble by another, or has duplicate properties.
 */
public enum FiBuEnum implements FiBu {
    THREE(3, "Fizz"), FIVE(5, "Buzz");

    static {
        FiBuUtils.validate(FiBuEnum::valueStream);
    }

    private final long factor;
    private final String output;

    private FiBuEnum(long factor, final String output) {
        this.factor = factor;
        this.output = output;
    }

    @Override
    public long getFactor() {
        return factor;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return FiBuUtils.toString(name(), this);
    }

    /**
     * @param factor the value to check
     * @return an {@link Optional} container over a {@link FiBuClass} instance
     * @see FiBuUtils#get(Supplier, long)
     */
    public static Optional<FiBu> get(long factor) {
        return FiBuUtils.get(FiBuEnum::valueStream, factor);
    }

    /**
     * @param factors the values to check
     * @return a {@link Collection} of found instances, which may be less than the
     *         number of {@code factors}
     * @see FiBuUtils#getAll(Supplier, long...)
     */
    public static Collection<FiBu> getAll(long... factors) {
        return FiBuUtils.getAll(FiBuEnum::valueStream, factors);
    }

    public static Stream<FiBu> valueStream() {
        return Stream.of(values());
    }
}
