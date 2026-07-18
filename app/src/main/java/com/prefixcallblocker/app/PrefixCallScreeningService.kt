package com.prefixcallblocker.app

import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * The core of the app. The Telecom system binds this service for the duration of
 * a single screening decision on each incoming call, invokes [onScreenCall], then
 * tears it down. There is no persistent/always-on component.
 *
 * Design rule: FAIL OPEN. If anything is uncertain (null number, empty prefix
 * list, unexpected error) we ALLOW the call. A bug must never silently block a
 * legitimate call.
 *
 * Note on contacts: Android only invokes this callback for numbers that are NOT
 * already in the user's contacts, so saved contacts are inherently exempt from
 * blocking — which is why this app requests neither READ_CONTACTS nor an
 * allowlist (SPEC §6.1). Verify this on-device (SPEC §11).
 */
class PrefixCallScreeningService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        try {
            val isIncoming = details.callDirection == Call.Details.DIRECTION_INCOMING
            // details.handle is a `tel:` Uri; schemeSpecificPart is the raw number.
            val rawNumber = details.handle?.schemeSpecificPart
            val prefixes = PrefixStore(this).getPrefixes()

            // All matching/normalization lives in the pure, unit-tested core.
            if (BlockDecision.shouldBlock(rawNumber, isIncoming, prefixes)) {
                block(details)
            } else {
                allow(details)
            }
        } catch (t: Throwable) {
            // Fail open on any unexpected error.
            allow(details)
        }
    }

    /** Default response: do not disallow — the call proceeds normally. */
    private fun allow(details: Call.Details) {
        respondToCall(details, CallResponse.Builder().build())
    }

    /**
     * Silently reject: no ring, no notification. We keep the call in the system
     * call log (setSkipCallLog(false)) so the user can audit what was blocked and
     * confirm there were no false positives (SPEC §3, §6.1).
     */
    private fun block(details: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(true)
            .setSkipCallLog(false)
            .setSilenceCall(false)
            .build()
        respondToCall(details, response)
    }
}
