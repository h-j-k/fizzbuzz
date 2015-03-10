package com.ikueb.fizzbuzz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A defensive {@code class}-based implementation for doing the FizzBuzz.
 * What that means is that new objects are validated against the current
 * objects to make sure each object is not divisble by another, or has
 * duplicate properties.
 */
public final class FiBuClass implements FiBu {

    // this must preceed any public static final instances
    private static final List<FiBu> values = new ArrayList<>();

    private final long factor;
    private final String output;

    private FiBuClass(long factor, final String output) {
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
        return FiBuUtils.toString(getClass().getSimpleName(), this);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof FiBuClass &&
                Long.compare(factor, ((FiBuClass)o).factor) == 0 &&
                output.equals(((FiBuClass)o).output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Long.valueOf(factor), output);
    }

    /**
     * Creates and validates a new instance of {@link FiBuClass},
     * before adding an internal reference to it.
     *
     * @param factor the number to use as a factor
     * @param output the output to append when processing
     * @return a new instance
     * @see #addAll(Map)
     */
    public static FiBu add(long factor, final String output) {
        final Map<Long, String> map = new HashMap<>();
        map.put(Long.valueOf(factor), output);
        return addAll(map).iterator().next();
    }

    /**
     * Creates and validates new instances of {@link FiBuClass},
     * before adding internal references to them.
     *
     * @param map the {@link Map} containing the number-and-output pairings
     * @return a {@link Collection} of new instances
     * @see FiBuUtils#validate(java.util.function.Supplier)
     * @see FiBuUtils#validate(java.util.function.Supplier, java.util.function.Supplier)
     */
    public static Collection<FiBu> addAll(final Map<Long, String> map) {
        final Collection<FiBu> result = map.entrySet().stream()
                .map(v -> new FiBuClass(v.getKey().longValue(), v.getValue()))
                .collect(Collectors.toList());
        FiBuUtils.validate(() -> { return result.stream(); });
        FiBuUtils.validate(() -> { return result.stream(); }, FiBuClass::valueStream);
        values.addAll(result);
        return result;
    }

    /**
     * @param factor the value to check
     * @return an {@link Optional} container over a {@link FiBuClass} instance
     * @see FiBuUtils#get(Supplier, long)
     */
    public static Optional<FiBu> get(long factor) {
        return FiBuUtils.get(FiBuClass::valueStream, factor);
    }

    /**
     * @param factors the values to check
     * @return a {@link Collection} of found instances, which may be less than the
     *         number of {@code factors}
     * @see FiBuUtils#getAll(Supplier, long...)
     */
    public static Collection<FiBu> getAll(long... factors) {
        return FiBuUtils.getAll(FiBuClass::valueStream, factors);
    }

    /**
     * Removes the internal reference to {@code value}.
     *
     * @param value the value to remove
     * @return {@true} if {@code value} was removed successfully
     */
    public static boolean remove(long value) {
        final Optional<FiBu> element = get(value);
        return element.isPresent() && values.remove(element.get());
    }

    /**
     * Removes all internal references.
     */
    public static void reset() {
        values.clear();
    }

    public static Stream<FiBu> valueStream() {
        return values.stream();
    }
}
