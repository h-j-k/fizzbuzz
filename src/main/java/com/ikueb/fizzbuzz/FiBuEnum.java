package com.ikueb.fizzbuzz;

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

    public static Stream<FiBuEnum> valueStream() {
        return Stream.of(values());
    }
}
