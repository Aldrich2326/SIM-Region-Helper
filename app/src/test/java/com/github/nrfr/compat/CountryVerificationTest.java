// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.compat;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CountryVerificationTest {
    @Test public void waitsThroughVendorReloadAndRequiresStableReadback() throws Exception {
        AtomicLong time = new AtomicLong();
        AtomicInteger samples = new AtomicInteger();
        CountryVerification.awaitStable("us", "46001",
                () -> samples.incrementAndGet() <= 2 ? "cn" : "us",
                () -> time.get() < 500 ? "" : "46001", () -> true,
                time::get, time::addAndGet);
        assertEquals(5, samples.get());
        assertEquals(2000, time.get());
    }

    @Test public void aSingleMatchingValueBeforeRevertIsNotSuccess() throws Exception {
        AtomicLong time = new AtomicLong();
        AtomicInteger samples = new AtomicInteger();
        try {
            CountryVerification.awaitStable("us", "46001",
                    () -> samples.incrementAndGet() == 1 ? "us" : "cn",
                    () -> "46001", () -> true, time::get, time::addAndGet);
            fail("A transient match must not be reported as success");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("actual=cn"));
            assertTrue(expected.getMessage().contains("snapshot was retained"));
        }
        assertEquals(15000, time.get());
    }

    @Test public void stopsWhenTheActiveSimOperatorChanges() throws Exception {
        AtomicLong time = new AtomicLong();
        try {
            CountryVerification.awaitStable("us", "46001", () -> "us",
                    () -> "44010", () -> true, time::get, time::addAndGet);
            fail("Must not verify another SIM's country");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("operator changed"));
        }
        assertEquals(0, time.get());
    }

    @Test public void countryIsNotAcceptedWhileSimIsNotReady() throws Exception {
        AtomicLong time = new AtomicLong();
        CountryVerification.awaitStable("us", "46001", () -> "us", () -> "46001",
                () -> time.get() >= 2000, time::get, time::addAndGet);
        assertEquals(3000, time.get());
    }
}
