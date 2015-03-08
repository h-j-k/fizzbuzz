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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FiBuTest {

    private static final long MAGIC = 23;

    /**
     * Syntatic sugar to replace lambda's {@link String} representation with something
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
        final Collection<String> transformed = Arrays.asList("1", "2", "Fizz", "4",
                "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14",
                "FizzBuzz", "16", "17", "Fizz", "19", "Buzz", "Fizz", "22");
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
     * Test for the addition and removal of {@link FiBuClass} instances.
     */
    @Test
    public void testAdditionAndRemoval() {
        FiBuClass.removeAll();
        final FiBuClass seven = FiBuClass.add(7, "Jazz");
        final FiBuClass eleven = FiBuClass.add(11, "Fuzz");
        assertThat(seven, not(equalTo(eleven)));
        assertThat(Integer.valueOf(seven.hashCode()),
                not(equalTo(Integer.valueOf(eleven.hashCode()))));
        final Iterator<FiBuClass> iterator = FiBuClass.valueStream().iterator();
        assertThat(iterator.next(), equalTo(seven));
        assertThat(iterator.next(), equalTo(eleven));
        assertThat(Boolean.valueOf(FiBuClass.remove(seven)), equalTo(Boolean.TRUE));
        assertThat(FiBuClass.valueStream().iterator().next(), equalTo(eleven));
        assertThat(Boolean.valueOf(FiBuClass.remove(eleven)), equalTo(Boolean.TRUE));
        final long newFactor = 13;
        final String newOutput = "Bazz";
        final FiBuClass newValue = FiBuClass.add(newFactor, newOutput);
        FiBuClass.remove(newValue);
        assertThat(FiBuClass.add(newFactor, newOutput), equalTo(newValue));
        FiBuClass.removeAll();
        assertThat(Long.valueOf(FiBuClass.valueStream().count()),
                equalTo(Long.valueOf(0)));
    }

    @DataProvider(name = "updated")
    public Iterator<Object[]> getUpdatedTestCases() {
        final long newFactor = 7;
        final String newOutput = "Jazz";
        FiBuClass.removeAll();
        FiBuClass.add(newFactor, newOutput);
        final Collection<String> enumOutput = Arrays.asList("1", "2", "Fizz", "4",
                "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14",
                "FizzBuzz", "16", "17", "Fizz", "19", "Buzz", "Fizz", "22");
        final Collection<String> classOutput = LongStream.range(1, MAGIC)
                .mapToObj(v -> v % newFactor == 0 ? newOutput : Long.toString(v))
                .collect(Collectors.toList());
        final Collection<String> combined = Arrays.asList("1", "2", "Fizz", "4",
                "Buzz", "Fizz", newOutput, "8", "Fizz", "Buzz", "11", "Fizz", "13",
                newOutput, "FizzBuzz", "16", "17", "Fizz", "19", "Buzz", "Fizz"
                        + newOutput, "22");
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

    /**
     * A wrapper for test payload that will generate a {@link IllegalStateException}.
     */
    private enum ExceptionWrapper {
        INVALID_FACTOR(true, 1, "ONE", "factor < 2"),
        NULL_OUTPUT(true, 2, null, "output null, empty or all whitespaces"),
        EMPTY_OUTPUT(true, 2, "", "output null, empty or all whitespaces"),
        WHITESPACE_OUTPUT(true, 2, "  ", "output null, empty or all whitespaces"),
        MULTI_WITH_FACTOR(true, FiBuEnum.THREE.getFactor(), "THREE", "same/factor/multiple of "),
        MULTI_WITH_SAME_OUTPUT(true, MAGIC, FiBuEnum.THREE.getOutput(), "same output as "),
        FACTOR(false, FiBuEnum.THREE.getFactor(), "THREE", "same/factor/multiple of "),
        SAME_OUTPUT(false, MAGIC, FiBuEnum.THREE.getOutput(), "same output as ");

        private final boolean failOnAdd;
        private final long newFactor;
        private final String newOutput;
        private final String expectedExceptionMessage;

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
            this.expectedExceptionMessage = expectedExceptionMessage;
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
     * <li>adding a same {@code factor}</li>
     * <li>adding a same {@code output}</li>
     * <li>adding multiple values, which are factors of each other</li>
     * <li>adding multiple values, which have the same {@code output}</li>
     * </ul>
     *
     * @param test the test payload that will generate a {@link IllegalStateException},
     *            and then assert for expected message
     * @see ExceptionWrapper
     * @see #getExceptionCases()
     */
    @Test(dataProvider = "exception")
    public void testExceptionCase(final ExceptionWrapper test) {
        try {
            final Collection<FiBuClass> newValues = FiBuClass.addAll(test.getValues());
            assertThat(FiBuClass.valueStream().collect(Collectors.toList()),
                    equalTo(newValues));
            FiBuClass.valueStream().forEach(
                    v -> assertThat(v, not(equalTo(FiBuEnum.THREE))));
        } catch (IllegalStateException e) {
            if (test.failOnAdd) {
                assertThat(e.getMessage(),
                        containsString(test.expectedExceptionMessage));
                return;
            }
            throw e;
        }
        try {
            FiBuUtils.validate(FiBuMain.CLASS, FiBuMain.COMBINED);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString(test.expectedExceptionMessage));
            FiBuClass.removeAll();
        }
    }

    private static Object[] wrap(Object... values) {
        return values;
    }
}
