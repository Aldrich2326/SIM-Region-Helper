// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.compat;

/** Shell-UID service used only for the carrier-country instrumentation command. */
interface ICountryOverrideService {
    String inspectRuntime();
    String applyCountryIso(int subId, String iso, String expectedOperatorNumeric);
    String restoreCountryIso(int subId, String iso, String expectedOperatorNumeric);
}
