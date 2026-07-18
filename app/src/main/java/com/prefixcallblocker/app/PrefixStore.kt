package com.prefixcallblocker.app

import android.content.Context

/**
 * Thin wrapper over SharedPreferences holding the user's blocked prefixes.
 *
 * Prefixes are stored already-normalized (see [normalize]) so the screening
 * service can do a plain `startsWith` on read without any per-call parsing.
 */
class PrefixStore(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Current prefixes. Always returns a fresh copy — the Set handed back by
     * SharedPreferences must never be mutated, and callers may want to edit it.
     */
    fun getPrefixes(): Set<String> =
        HashSet(prefs.getStringSet(KEY_PREFIXES, emptySet()) ?: emptySet())

    /**
     * Normalizes [raw] and adds it. Returns true if it was added, false if the
     * input was empty/whitespace-only or the prefix already existed.
     */
    fun addPrefix(raw: String): Boolean {
        val p = normalize(raw)
        if (p.isEmpty() || p == "+") return false
        val current = getPrefixes().toHashSet()
        if (!current.add(p)) return false
        prefs.edit().putStringSet(KEY_PREFIXES, current).apply()
        return true
    }

    /** Removes a prefix exactly as stored (already normalized). */
    fun removePrefix(prefix: String) {
        val current = getPrefixes().toHashSet()
        if (current.remove(prefix)) {
            prefs.edit().putStringSet(KEY_PREFIXES, current).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "prefixes"
        private const val KEY_PREFIXES = "prefix_set"

        /**
         * Normalization applied identically on write (here) and on read (in the
         * service), so `startsWith` matching is reliable (SPEC §7).
         *
         * Rules, deliberately simple and dependency-free:
         *   1. Trim surrounding whitespace.
         *   2. Keep a single leading '+' if present.
         *   3. Keep digits only; drop spaces, '-', '(', ')', and any other '+'.
         *
         * We do NOT attempt libphonenumber-style E.164 conversion or guess
         * country codes — matching is done on the string as the network
         * delivers it.
         */
        fun normalize(raw: String): String {
            val trimmed = raw.trim()
            val sb = StringBuilder(trimmed.length)
            var i = 0
            if (trimmed.startsWith("+")) {
                sb.append('+')
                i = 1
            }
            while (i < trimmed.length) {
                val c = trimmed[i]
                if (c in '0'..'9') sb.append(c)
                i++
            }
            return sb.toString()
        }
    }
}
