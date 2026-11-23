package com.github.nrfr.compat

import org.junit.Assert.*
import org.junit.Test

class AuthorizationProbeTest {
    private class Fake : AuthorizationGateway {
        var phone = true; var alive = true; var granted = false; var blocked = false
        var failPermission = false; var permissionReads = 0
        override fun phoneAllowed() = phone
        override fun binderAlive() = alive
        override fun permissionGranted(): Boolean {
            permissionReads++
            if (failPermission) throw IllegalStateException("Binder died")
            return granted
        }
        override fun requestBlocked() = blocked
    }
    @Test fun phoneDenialDoesNotPreventShizukuInspection() {
        val fake = Fake().apply { phone = false; granted = true }
        val state = AuthorizationProbe.inspect(fake)
        assertEquals(ShizukuAccess.GRANTED, state.shizuku)
        assertEquals(1, fake.permissionReads)
        assertFalse(state.ready)
    }
    @Test fun disconnectedDoesNotCallPermissionApi() {
        val fake = Fake().apply { alive = false }
        assertEquals(ShizukuAccess.DISCONNECTED, AuthorizationProbe.inspect(fake).shizuku)
        assertEquals(0, fake.permissionReads)
    }
    @Test fun unauthorizedAndBlockedAreDistinct() {
        val fake = Fake()
        assertEquals(ShizukuAccess.NOT_GRANTED, AuthorizationProbe.inspect(fake).shizuku)
        fake.blocked = true
        assertEquals(ShizukuAccess.BLOCKED, AuthorizationProbe.inspect(fake).shizuku)
    }
    @Test fun binderRaceDoesNotReportAuthorized() {
        val fake = Fake().apply { granted = true; failPermission = true }
        val state = AuthorizationProbe.inspect(fake)
        assertEquals(ShizukuAccess.ERROR, state.shizuku)
        assertFalse(state.ready)
        assertTrue(state.detail.contains("Binder died"))
    }
    @Test fun revokedGrantIsNotCached() {
        val fake = Fake().apply { granted = true }
        assertTrue(AuthorizationProbe.inspect(fake).ready)
        fake.granted = false
        assertFalse(AuthorizationProbe.inspect(fake).ready)
        fake.alive = false
        assertEquals(ShizukuAccess.DISCONNECTED, AuthorizationProbe.inspect(fake).shizuku)
    }
}
