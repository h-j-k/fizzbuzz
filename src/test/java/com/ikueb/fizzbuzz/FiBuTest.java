package com.ikueb.fizzbuzz;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FiBuTest {

    private static final long MAGIC = 23;
    private static final String FIZZ = FiBuEnum.THREE.getOutput();
    private static final String BUZZ = FiBuEnum.FIVE.getOutput();

    /**
     * Syntactic sugar to replace lambda's {@link String} representation with something
     * more understandable.
     */
    private enum SourceWrapper {
        CLASS(FiBuMain.CLASS), ENUM(FiBuMain.ENUM), COMBINED(FiBuMain.COMBINED);

        private final Supplier<Stream<? extends FiBu>> source;

        private SourceWrapper(final Supplier<Stream<? extends FiBu>> source) {
            this.source = source;
        }
    }

    @DataProvider(name = "default")
    public Iterator<Object[]> getDefaultCases() {
        final Collection<String> transformed = Arrays.asList("1", "2", FIZZ, "4", BUZZ,
                FIZZ, "7", "8", FIZZ, BUZZ, "11", FIZZ, "13", "14", FIZZ + BUZZ, "16",
                "17", FIZZ, "19", BUZZ, FIZZ, "22");
        final Collection<String> noTransform = LongStream.range(1, MAGIC)
                .mapToObj(Long::toString).collect(Collectors.toList());
        return Stream.of(wrap(SourceWrapper.ENUM, transformed),
                wrap(SourceWrapper.CLASS, noTransform),
                wrap(SourceWrapper.COMBINED, transformed)).iterator();
    }

    /**
     * Test for the default set-up.
     *
     * @param stream the {@link Stream} to derive the output with
     * @param expected the expected output
     * @see #getDefaultCases()
     */
    @Test(dataProvider = "default")
    public void testDefault(final SourceWrapper stream,
            final Collection<String> expected) {
        test(stream, expected);
    }

    /**
     * Test for getting {@link FiBuEnum} values.
     */
    @Test
    public void testFiBuEnumOperations() {
        assertBoolean(FiBuEnum.get(0).isPresent(), false);
        final Optional<FiBu> value = FiBuEnum.get(FiBuEnum.THREE.getFactor());
        assertBoolean(value.isPresent(), true);
        assertThat(value.get(), equalTo(FiBuEnum.THREE));
        final LongStream stream = FiBuEnum.valueStream().mapToLong(FiBu::getFactor);
        assertThat(FiBuEnum.getAll(stream.toArray()), equalTo(FiBuEnum.valueStream()
                .collect(Collectors.toList())));
    }

    /**
     * Test for the addition and removal of {@link FiBuClass} instances.
     */
    @Test
    public void testFiBuClassOperations() {
        FiBuClass.reset();
        final long[] newFactors = new long[] { 7, 11, 13 };
        final String[] newOutputs = new String[] { "Jazz", "Fuzz", "Bazz" };
        final FiBu newValue = FiBuClass.add(newFactors[0], newOutputs[0]);
        final Map<Long, String> map = new HashMap<>();
        for (int i = 1; i < newFactors.length; i++) {
            map.put(Long.valueOf(newFactors[i]), newOutputs[i]);
        }
        final Collection<FiBu> newValues = FiBuClass.addAll(map);
        FiBuUtils.validate(FiBuMain.CLASS, FiBuMain.COMBINED);
        final Iterator<FiBu> newIterator = newValues.iterator();
        final FiBu firstValue = newIterator.next();
        final FiBu secondValue = newIterator.next();
        assertThat(firstValue, not(equalTo(secondValue)));
        assertBoolean(firstValue.hashCode() == secondValue.hashCode(), false);
        assertBoolean(newIterator.hasNext(), false);
        final Iterator<FiBu> streamIterator = FiBuClass.valueStream().iterator();
        assertThat(streamIterator.next(), equalTo(newValue));
        assertBoolean(FiBuClass.remove(newValue.getFactor()), true);
        assertBoolean(FiBuClass.get(newValue.getFactor()).isPresent(), false);
        assertBoolean(FiBuClass.remove(newValue.getFactor()), false);
        assertThat(FiBuClass.getAll(newFactors), equalTo(newValues));
        FiBuClass.reset();
        assertThat(Long.valueOf(FiBuClass.valueStream().count()),
                equalTo(Long.valueOf(0)));
    }

    @DataProvider(name = "updated")
    public Iterator<Object[]> getUpdatedTestCases() {
        final long newFactor = 7;
        final String newOutput = "Jazz";
        FiBuClass.reset();
        FiBuClass.add(newFactor, newOutput);
        final Collection<String> enumOutput = Arrays.asList("1", "2", FIZZ, "4", BUZZ,
                FIZZ, "7", "8", FIZZ, BUZZ, "11", FIZZ, "13", "14", FIZZ + BUZZ, "16",
                "17", FIZZ, "19", BUZZ, FIZZ, "22");
        final Collection<String> classOutput = LongStream.range(1, MAGIC)
                .mapToObj(v -> v % newFactor == 0 ? newOutput : Long.toString(v))
                .collect(Collectors.toList());
        final Collection<String> combined = Arrays.asList("1", "2", FIZZ, "4", BUZZ,
                FIZZ, newOutput, "8", FIZZ, BUZZ, "11", FIZZ, "13", newOutput, FIZZ
                        + BUZZ, "16", "17", FIZZ, "19", BUZZ, FIZZ + newOutput, "22");
        return Stream.of(wrap(SourceWrapper.ENUM, enumOutput),
                wrap(SourceWrapper.CLASS, classOutput),
                wrap(SourceWrapper.COMBINED, combined)).iterator();
    }

    /**
     * Test for the updated set-up.
     *
     * @param stream the {@link Stream} to derive the output with
     * @param expected the expected output
     * @see #getUpdatedTestCases()
     */
    @Test(dataProvider = "updated")
    public void testUpdated(final SourceWrapper stream,
            final Collection<String> expected) {
        test(stream, expected);
    }

    private static void test(final SourceWrapper stream,
            final Collection<String> expected) {
        assertThat(FiBuUtils.process(1, MAGIC, stream.source), equalTo(expected));
    }

    private static final String BAD_OUTPUT_MESSAGE = "output null, empty or all whitespaces";
    private static final String FACTOR_OF_MESSAGE = "same/factor/multiple of ";
    private static final String SAME_OUTPUT_MESSAGE = "same output as ";

    /**
     * A wrapper for a test payload that will generate a {@link IllegalStateException}.
     */
    private enum ExceptionWrapper {
        INVALID_FACTOR(true, 1, "ONE", "factor < 2"),
        NULL_OUTPUT(true, 2, null, BAD_OUTPUT_MESSAGE),
        EMPTY_OUTPUT(true, 2, "", BAD_OUTPUT_MESSAGE),
        WHITESPACE_OUTPUT(true, 2, "  ", BAD_OUTPUT_MESSAGE),
        MULTI_WITH_FACTOR(true, FiBuEnum.THREE.getFactor(), "THREE", FACTOR_OF_MESSAGE),
        MULTI_WITH_SAME_OUTPUT(true, MAGIC, FiBuEnum.THREE.getOutput(), SAME_OUTPUT_MESSAGE),
        FACTOR(false, FiBuEnum.THREE.getFactor(), "THREE", FACTOR_OF_MESSAGE),
        SAME_OUTPUT(false, MAGIC, FiBuEnum.THREE.getOutput(), SAME_OUTPUT_MESSAGE);

        private final boolean failOnAdd;
        private final long newFactor;
        private final String newOutput;
        private final String expectedMessage;

        /**
         * @param failOnAdd {@code true} if we should not be able to create the new
         *            instance
         * @param newFactor the {@code factor} value that will throw
         *            {@link IllegalStateException}
         * @param newOutput the {@code output} value that will throw
         *            {@link IllegalStateException}
         * @param expectedExceptionMessage the {@link IllegalStateException} message is
         *            expected to contain this
         */
        private ExceptionWrapper(boolean failOnAdd, long newFactor,
                final String newOutput, final String expectedExceptionMessage) {
            this.failOnAdd = failOnAdd;
            this.newFactor = newFactor;
            this.newOutput = newOutput;
            this.expectedMessage = expectedExceptionMessage;
        }

        /**
         * @return a {@link Map} to (unsuccessfully) create new {@link FiBuClass}
         *         instances
         */
        private Map<Long, String> getValues() {
            final Map<Long, String> map = new HashMap<>();
            map.put(Long.valueOf(newFactor), newOutput);
            if (equals(MULTI_WITH_FACTOR)) {
                map.put(Long.valueOf(newFactor * 2), newOutput + newOutput);
            } else if (equals(MULTI_WITH_SAME_OUTPUT)) {
                map.put(Long.valueOf(Long.MAX_VALUE), newOutput);
            }
            return map;
        }
    }

    @DataProvider(name = "exception")
    public Iterator<Object[]> getExceptionCases() {
        return Stream.of(ExceptionWrapper.values()).map(FiBuTest::wrap).iterator();
    }

    /**
     * Test for expected errors:
     * <ul>
     * <li>adding a {@code factor} of 1</li>
     * <li>adding with {@code null} {@code output}</li>
     * <li>adding with an empty {@code output}</li>
     * <li>adding {@code output} consisting of whitespaces</li>
     * <li>adding multiple values, which are factors of each other</li>
     * <li>adding multiple values, which have the same {@code output}</li>
     * <li>adding a same {@code factor}</li>
     * <li>adding a same {@code output}</li>
     * </ul>
     * The last two tests are done by validating against {@link FiBuEnum}'s values.
     *
     * @param test the test payload that will generate a {@link IllegalStateException},
     *            and then assert for the expected message
     * @see ExceptionWrapper
     * @see #getExceptionCases()
     */
    @Test(dataProvider = "exception")
    public void testExceptionCase(final ExceptionWrapper test) {
        try {
            FiBuClass.addAll(test.getValues());
            if (test.failOnAdd) {
                throw new AssertionError();
            }
        } catch (final IllegalStateException e) {
            assertThat(e.getMessage(), containsString(test.expectedMessage));
            return;
        }
        try {
            FiBuUtils.validate(FiBuMain.CLASS, FiBuMain.COMBINED);
            throw new AssertionError();
        } catch (final IllegalStateException e) {
            assertThat(e.getMessage(), containsString(test.expectedMessage));
            FiBuClass.reset();
        }
    }

    private static void assertBoolean(boolean actual, boolean expected) {
        assertThat(Boolean.valueOf(actual), equalTo(Boolean.valueOf(expected)));
    }

    private static Object[] wrap(Object... values) {
        return values;
    }
}
