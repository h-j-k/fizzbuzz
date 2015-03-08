package com.ikueb.fizzbuzz;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class FiBuMain {

    public static final Supplier<Stream<? extends FiBu>> ENUM = FiBuEnum::valueStream;
    public static final Supplier<Stream<? extends FiBu>> CLASS = FiBuClass::valueStream;
    public static final Supplier<Stream<? extends FiBu>> COMBINED = () -> {
        return Stream.concat(FiBuEnum.valueStream(), FiBuClass.valueStream()); };
    private static final Supplier<Stream<? extends FiBu>> SOURCE = COMBINED;

    public static void main(String[] args) {
        FiBuUtils.process(parse(args.length > 0 ? args[0] : null, 1),
                parse(args.length > 1 ? args[1] : null, 100),
                SOURCE).forEach(System.out::println);
    }

    /**
     * @param input the {@link String} to parse
     * @param defaultValue the value to return if {@code input} is not parsable
     * @return the parsed value if not less than 1, or {@code defaultValue}
     */
    private static long parse(final String input, long defaultValue) {
        try {
            final long result = Long.parseLong(input);
            return result < 1 ? defaultValue : result;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
