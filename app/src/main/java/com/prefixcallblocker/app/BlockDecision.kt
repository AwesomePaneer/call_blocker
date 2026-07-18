package com.prefixcallblocker.app

/**
 * The pure, framework-free blocking decision, extracted from the screening
 * service so it can be unit-tested on the plain JVM (no device/emulator needed).
 *
 * [PrefixCallScreeningService] maps the Android [android.telecom.Call.Details]
 * into the primitives this function takes, keeping all Android dependencies out
 * of the testable core.
 */
object BlockDecision {

    /**
     * Returns true iff the call should be blocked.
     *
     * Fail-open contract: any uncertainty returns false (allow).
     *
     * @param rawNumber   number as delivered by the network (may be null/blank)
     * @param isIncoming  true only for incoming calls; outgoing are never blocked
     * @param prefixes    already-normalized stored prefixes (see [PrefixStore.normalize])
     */
    fun shouldBlock(rawNumber: String?, isIncoming: Boolean, prefixes: Set<String>): Boolean {
        if (!isIncoming) return false
        if (rawNumber.isNullOrBlank()) return false
        if (prefixes.isEmpty()) return false
        val number = PrefixStore.normalize(rawNumber)
        return prefixes.any { number.startsWith(it) }
    }
}
