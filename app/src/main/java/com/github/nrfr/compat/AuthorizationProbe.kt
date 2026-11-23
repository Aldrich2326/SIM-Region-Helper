package com.github.nrfr.compat

/** Authorization only: never reads or writes a SIM/carrier configuration. */
enum class ShizukuAccess { DISCONNECTED, NOT_GRANTED, BLOCKED, GRANTED, ERROR }

data class AuthorizationState(
    val phoneAllowed: Boolean = false,
    val shizuku: ShizukuAccess = ShizukuAccess.DISCONNECTED,
    val detail: String = ""
) {
    val ready: Boolean get() = phoneAllowed && shizuku == ShizukuAccess.GRANTED
}

interface AuthorizationGateway {
    fun phoneAllowed(): Boolean
    fun binderAlive(): Boolean
    fun permissionGranted(): Boolean
    fun requestBlocked(): Boolean
}

object AuthorizationProbe {
    fun inspect(gateway: AuthorizationGateway): AuthorizationState {
        var phone = false
        return try {
            phone = gateway.phoneAllowed()
            val access = when {
                !gateway.binderAlive() -> ShizukuAccess.DISCONNECTED
                gateway.permissionGranted() -> ShizukuAccess.GRANTED
                gateway.requestBlocked() -> ShizukuAccess.BLOCKED
                else -> ShizukuAccess.NOT_GRANTED
            }
            AuthorizationState(phone, access)
        } catch (e: Exception) {
            // A dead Binder or inspection failure must never retain a stale "authorized" state.
            AuthorizationState(phone, ShizukuAccess.ERROR, "${e.javaClass.simpleName}: ${e.message.orEmpty()}")
        }
    }
}
