package com.ikueb.fizzbuzz;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
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

    private static void assertBoolean(boolean actual, boolean expected) {
        assertThat(Boolean.valueOf(actual), equalTo(Boolean.valueOf(expected)));
    }

    /**
     * Tests for getting {@link FiBuEnum} values.
     */
    @Test
    public void testFiBuEnumOperations() {
        assertBoolean(FiBuEnum.get(0).isPresent(), false);
        assertThat(FiBuEnum.get(FiBuEnum.THREE.getFactor()).get(),
                equalTo(FiBuEnum.THREE));
        final LongStream stream = FiBuEnum.valueStream().mapToLong(FiBu::getFactor);
        assertThat(FiBuEnum.getAll(stream.toArray()),
                equalTo(FiBuEnum.valueStream().collect(Collectors.toList())));
    }

    /**
     * Tests for the addition and removal of {@link FiBuClass} instances.
     */
    @Test
    public void testFiBuClassOperations() {
        FiBuClass.reset();
        final long[] newFactors = new long[] { 7, 11, 13 };
        final String[] newOutputs = new String[] { "Jazz", "Fuzz", "Bazz" };
        final Map<Long, String> map = new HashMap<>();
        for (int i = 1; i < newFactors.length; i++) {
            map.put(Long.valueOf(newFactors[i]), newOutputs[i]);
        }
        final FiBu newValue = FiBuClass.add(newFactors[0], newOutputs[0]);
        final Collection<FiBu> newValues = FiBuClass.addAll(map);
        FiBuUtils.validate(FiBuMain.CLASS, FiBuMain.COMBINED);
        final Iterator<FiBu> newIterator = newValues.iterator();
        final FiBu first = newIterator.next();
        final FiBu second = newIterator.next();
        assertBoolean(newIterator.hasNext(), false);
        assertThat(first, not(equalTo(second)));
        assertBoolean(first.hashCode() == second.hashCode(), false);
        final Iterator<FiBu> streamIterator = FiBuClass.valueStream().iterator();
        assertThat(streamIterator.next(), equalTo(newValue));
        assertBoolean(FiBuClass.remove(newValue.getFactor()), true);
        assertBoolean(FiBuClass.get(newValue.getFactor()).isPresent(), false);
        assertBoolean(FiBuClass.remove(newValue.getFactor()), false);
        assertThat(FiBuClass.getAll(newFactors), equalTo(newValues));
        FiBuClass.reset();
        assertBoolean(FiBuClass.valueStream().count() == 0, true);
    }

    /**
     * Syntactic sugar to make {@link Supplier} instances more accessible.
     */
    private enum Source {
        ENUM(FiBuMain.ENUM), CLASS(FiBuMain.CLASS), COMBINED(FiBuMain.COMBINED);

        private final Supplier<Stream<? extends FiBu>> supplier;

        private Source(final Supplier<Stream<? extends FiBu>> supplier) {
            this.supplier = supplier;
        }
    }

    private static interface TestCase {
        static final long END = 23;

        void doTest();
    }

    /**
     * Implementations of this will assert a processed
     * {@link Stream} against the expected result.
     *
     * @see FiBuUtils#process(long, long, Supplier)
     */
    private static interface ProcessingTestCase extends TestCase {
        static final String FIZZ = FiBuEnum.THREE.getOutput();
        static final String BUZZ = FiBuEnum.FIVE.getOutput();

        Supplier<Stream<? extends FiBu>> getSource();
        Collection<String> getResult();

        @Override
        default void doTest() {
            assertThat(FiBuUtils.process(1, END, getSource()), equalTo(getResult()));
        }
    }

    /**
     * Test payloads for processing with the default state.
     */
    private enum ResultPayload implements ProcessingTestCase {
        ENUM, CLASS, COMBINED;

        @Override
        public Supplier<Stream<? extends FiBu>> getSource() {
            return Source.valueOf(name()).supplier;
        }

        @Override
        public Collection<String> getResult() {
            final Collection<String> transformed = Arrays.asList("1", "2", FIZZ, "4",
                    BUZZ, FIZZ, "7", "8", FIZZ, BUZZ, "11", FIZZ, "13", "14", FIZZ
                            + BUZZ, "16", "17", FIZZ, "19", BUZZ, FIZZ, "22");
            switch (this) {
            case ENUM:
                return transformed;
            case CLASS:
                return LongStream.range(1, END).mapToObj(Long::toString)
                        .collect(Collectors.toList());
            case COMBINED:
                return transformed;
            default:
                throw new RuntimeException("Unexpected enum value.");
            }
        }
    }

    /**
     * Test payloads for processing after creating a new {@link FiBuClass} instance.
     */
    private enum EnrichedResultPayload implements ProcessingTestCase {
        ENUM, CLASS, COMBINED;

        private static final long FACTOR = 7;
        private static final String OUTPUT = "Jazz";

        @Override
        public void doTest() {
            FiBuClass.reset();
            FiBuClass.add(FACTOR, OUTPUT);
            ProcessingTestCase.super.doTest();
        }

        @Override
        public Supplier<Stream<? extends FiBu>> getSource() {
            return Source.valueOf(name()).supplier;
        }

        @Override
        public Collection<String> getResult() {
            switch (this) {
            case ENUM:
                return Arrays.asList("1", "2", FIZZ, "4", BUZZ, FIZZ, "7", "8", FIZZ,
                        BUZZ, "11", FIZZ, "13", "14", FIZZ + BUZZ, "16", "17", FIZZ,
                        "19", BUZZ, FIZZ, "22");
            case CLASS:
                return LongStream.range(1, END)
                        .mapToObj(v -> v % FACTOR == 0 ? OUTPUT : Long.toString(v))
                        .collect(Collectors.toList());
            case COMBINED:
                return Arrays.asList("1", "2", FIZZ, "4", BUZZ, FIZZ, OUTPUT, "8",
                        FIZZ, BUZZ, "11", FIZZ, "13", OUTPUT, FIZZ + BUZZ, "16",
                        "17", FIZZ, "19", BUZZ, FIZZ + OUTPUT, "22");
            default:
                throw new RuntimeException("Unexpected enum value.");
            }
        }
    }

    private static final String BAD_OUTPUT = "output null, empty or all whitespaces";
    private static final String FACTOR_OF = "same/factor/multiple of ";
    private static final String SAME_OUTPUT_AS = "same output as ";

    /**
     * Asserting that {@link IllegalStateException} is thrown for:
     * <ul>
     * <li>adding a {@code factor} less than 2</li>
     * <li>adding a {@code null} {@code output}</li>
     * <li>adding an empty {@code output}</li>
     * <li>adding {@code output} consisting of whitespaces</li>
     * <li>adding multiple values, which are factors of each other</li>
     * <li>adding multiple values, which have the same {@code output}</li>
     * <li>adding an existing {@code factor}</li>
     * <li>adding an existing {@code output}</li>
     * </ul>
     * The last two tests are done by validating {@link FiBuMain#CLASS} against
     * {@link FiBuMain#COMBINED}.
     */
    private enum ExceptionPayload implements TestCase {
        INVALID_FACTOR(true, 1, "ONE", "factor < 2"),
        NULL_OUTPUT(true, 2, null, BAD_OUTPUT),
        EMPTY_OUTPUT(true, 2, "", BAD_OUTPUT),
        WHITESPACE_OUTPUT(true, 2, "  ", BAD_OUTPUT),
        MULTI_FACTOR(true, FiBuEnum.THREE.getFactor(), "THREE", FACTOR_OF),
        MULTI_SAME_OUTPUT(true, END, FiBuEnum.THREE.getOutput(), SAME_OUTPUT_AS),
        FACTOR(false, FiBuEnum.THREE.getFactor(), "THREE", FACTOR_OF),
        SAME_OUTPUT(false, END, FiBuEnum.THREE.getOutput(), SAME_OUTPUT_AS);

        private final boolean failOnAdd;
        private final long factor;
        private final String output;
        private final String expectedMessage;

        /**
         * @param failOnAdd {@code true} if we should not be able to create the new
         *            instance
         * @param factor the {@code factor} value that will throw
         *            {@link IllegalStateException}
         * @param output the {@code output} value that will throw
         *            {@link IllegalStateException}
         * @param expectedMessage the {@link IllegalStateException} message is
         *            expected to contain this
         */
        private ExceptionPayload(boolean failOnAdd, long factor,
                final String output, final String expectedMessage) {
            this.failOnAdd = failOnAdd;
            this.factor = factor;
            this.output = output;
            this.expectedMessage = expectedMessage;
        }

        @Override
        public void doTest() {
            try {
                FiBuClass.reset();
                FiBuClass.addAll(getValues());
                if (failOnAdd) {
                    throw new AssertionError();
                }
                FiBuUtils.validate(FiBuMain.CLASS, FiBuMain.COMBINED);
                throw new AssertionError();
            } catch (final IllegalStateException e) {
                assertThat(e.getMessage(), containsString(expectedMessage));
            }
        }

        /**
         * @return a {@link Map} to (unsuccessfully) create new {@link FiBuClass}
         *         instances
         */
        private Map<Long, String> getValues() {
            final Map<Long, String> map = new HashMap<>();
            map.put(Long.valueOf(factor), output);
            if (equals(MULTI_FACTOR)) {
                map.put(Long.valueOf(factor * 2), output + output);
            } else if (equals(MULTI_SAME_OUTPUT)) {
                map.put(Long.valueOf(Long.MAX_VALUE), output);
            }
            return map;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private static @interface Case {
        Class<? extends TestCase> provider();
    }

    @DataProvider(name = "test-cases")
    public static Iterator<Object[]> getTestCases(final Method method) {
        return getCases(method).map(v -> { return new Object[]{ v }; }).iterator();
    }

    private static Stream<TestCase> getCases(final Method method) {
        if (!method.isAnnotationPresent(Case.class)) {
            throw new RuntimeException(String.format("Missing @%s on %s",
                    Case.class.getSimpleName(), method.getName()));
        }
        final Class<? extends TestCase> clazz =
                method.getAnnotation(Case.class).provider();
        if (!clazz.isEnum()) {
            throw new RuntimeException("Expecting an enum implementation of "
                    + TestCase.class.getSimpleName());
        }
        return Arrays.stream(clazz.getEnumConstants());
    }

    @Test(dataProvider = "test-cases")
    @Case(provider = ResultPayload.class)
    public void testDefault(final TestCase testCase) {
        testCase.doTest();
    }

    @Test(dataProvider = "test-cases")
    @Case(provider = EnrichedResultPayload.class)
    public void testEnriched(final TestCase testCase) {
        testCase.doTest();
    }

    @Test(dataProvider = "test-cases")
    @Case(provider = ExceptionPayload.class)
    public void testException(final TestCase testCase) {
        testCase.doTest();
    }
}
