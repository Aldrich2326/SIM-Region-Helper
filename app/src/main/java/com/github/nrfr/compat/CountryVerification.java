// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.compat;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** A returned Binder call alone does not establish that a vendor ROM kept the country. */
final class CountryVerification {
    private static final long TIMEOUT_MILLIS = 15_000L;
    private static final long INTERVAL_MILLIS = 500L;
    private static final int REQUIRED_SAMPLES = 3;

    interface Sleeper { void sleep(long millis) throws InterruptedException; }

    private CountryVerification() {}

    static void awaitStable(String expectedIso, String expectedNumeric,
            Supplier<String> country, Supplier<String> numeric, BooleanSupplier ready,
            LongSupplier clock, Sleeper sleeper) throws InterruptedException {
        long started = clock.getAsLong();
        int consecutive = 0;
        String actualCountry = "";
        while (true) {
            String actualNumeric = numeric.get();
            // A SIM reload can temporarily return no operator. Never accept another operator.
            if (actualNumeric != null && !actualNumeric.isEmpty()
                    && !expectedNumeric.equals(actualNumeric)) {
                throw new IllegalStateException("SIM operator changed during verification; "
                        + "no further configuration was written");
            }
            actualCountry = country.get();
            boolean matches = ready.getAsBoolean() && expectedNumeric.equals(actualNumeric)
                    && expectedIso.equalsIgnoreCase(actualCountry);
            consecutive = matches ? consecutive + 1 : 0;
            if (consecutive >= REQUIRED_SAMPLES) return;
            long remaining = TIMEOUT_MILLIS - (clock.getAsLong() - started);
            if (remaining <= 0) {
                throw new IllegalStateException("SIM country verification failed: expected="
                        + expectedIso + ", actual=" + actualCountry
                        + "; the original-country snapshot was retained");
            }
            sleeper.sleep(Math.min(INTERVAL_MILLIS, remaining));
        }
    }
}
