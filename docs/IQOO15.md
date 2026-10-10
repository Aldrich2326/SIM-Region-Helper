# iQOO 15 / Android 16 local test build

This is a local derivative of JHR-deve/Nrfr-Android17 at
`c87e24c2341bf1c334f5794ac59d2acde865dafe`, itself based on Ackites/Nrfr.
It is not an official release from either upstream author.

Target supplied by the owner: iQOO 15 (V2505A), Android 16,
PD2505D_A_16.0.30.8.W10.V000L1, security patch 2026-08-01.
The device was connected on 2026-10-10. Startup, runtime phone-read permission,
Shizuku authorization and the read-only instrumentation probe were verified.
The owner requested startup and permission checks only; country writes and restoration
were not included in the controlled validation. These checks do not establish full compatibility.

## Changes

- Separate package `io.github.nrfr.iqoo15` and launcher name, so installation does not replace
  either previously downloaded app. Shizuku permission must be granted again for this package.
- Retain upstream's instrumentation-based permission delegation, instead of calling
  `overrideConfig` as the shell UID. The write remains country-only and non-persistent.
- Add a read-only environment check that exercises instrumentation and confirms temporary
  phone permissions and method availability. It never calls `overrideConfig`, and a successful
  check does not prove that the vendor's service will accept the subsequent write.
- Register the protected telephony reload broadcast with explicit receiver visibility on API 33+,
  because telephony may broadcast from its own privileged UID.
- Allow up to 15 seconds for SIM readiness and country propagation after the command, and
  require three consecutive matching reads of the public SIM country and original operator.
  A single early matching value is insufficient. No automatic retry writes are performed.
- A missing carrier reload broadcast does not alone fail restoration. Restoration must still
  pass public-country verification before its snapshot is removed.
- For mainland China operator MCC 460, recover the original CN country even when the current
  reported country is temporarily blank. Other cards still require a reliable original value.
- Refresh SIM display when the app returns to the foreground, and track Shizuku binder death.
- Explicitly check phone-read permission before reading subscriptions and handle revocation.
  The manifest minimum is Android 10/API 29, matching the inherited subscription APIs;
  the previous upstream Android 8 claim did not match those calls.
- Add a local diagnostic report containing model, firmware, patch level and latest operation.
  No phone numbers, IMSI, ICCID or serial number are collected. Copying is user initiated.
- Compress DEX files in the APK to reduce transfer size. With minSdk 29 the default packaging
  stores DEX uncompressed, which inflated this build to approximately 18 MB. This does not
  remove runtime code or enable code shrinking on the reflection/instrumentation classes.
- Use stable AGP 8.7.3 and Kotlin 2.0.21 with Gradle 8.9/JDK 21/SDK 35, instead of the old
  AGP release candidate. Update Compose BOM to 2024.12.01 because the inherited 2023 Compose
  lint detectors crash with Kotlin 2. The build SDK is independent of the phone's Android version;
  targetSdk remains 34, so this build does not opt into additional target-SDK behavior changes.

## Validation

Host tasks: `:app:testDebugUnitTest :app:assembleRelease :app:lintRelease`.
Additional tests cover delayed vendor reloads, one-off matches that revert, SIM operator
changes during verification, and a not-ready SIM. APK signing and manifest inspection are
performed after the build. See the delivered validation report for actual outcomes.

All 12 host tests passed; release lint completed with 0 errors and 22 warnings.
The phone's probe returned RESULT_OK after permission delegation and runtime API inspection.
Reported country values were US,CN before and after inspection, so these values are not evidence
that this build changed either SIM successfully. Country write/readback, restoration, and normal
cellular calling/data still require controlled on-device validation.

The phone contains the initial 18,345,703-byte APK. Updating to the 6,598,375-byte compressed
APK was rejected by the phone and was not forced. Uncompressed hashes of both DEX files,
AndroidManifest.xml and resources.arsc match between the installed and delivered packages.
The smaller package has not itself been installed on this phone. No root or system modification
is attempted. See the delivered Chinese validation report for the exact scope and limitations.

## Build

Set `sdk.dir` in an untracked `local.properties` or set `ANDROID_HOME` to Android SDK 35.
Run the host tasks above using JDK 21. Release output is unsigned until signed with a local
key using Android build-tools `zipalign` and `apksigner`. Signing keys and their passwords
are deliberately excluded from the delivered source archive.

Upstream Apache-2.0 and inherited MIT notices remain in LICENSE, NOTICE-ANDROID17.md,
and licenses/. Those notices are also packaged into the APK by the upstream build tasks.
