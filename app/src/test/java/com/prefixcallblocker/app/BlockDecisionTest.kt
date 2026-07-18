package com.prefixcallblocker.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the pure blocking decision (BlockDecision.shouldBlock). */
class BlockDecisionTest {

    private val prefixes = setOf("+91140")

    @Test fun blocksIncomingMatchingPrefix() {
        assertTrue(BlockDecision.shouldBlock("+911402345678", true, prefixes))
    }

    @Test fun blocksEvenWhenNetworkAddsSeparators() {
        // normalization inside shouldBlock makes "+91 140 234" match "+91140"
        assertTrue(BlockDecision.shouldBlock("+91 140 234", true, prefixes))
    }

    @Test fun allowsIncomingNonMatching() {
        assertFalse(BlockDecision.shouldBlock("+919998887777", true, prefixes))
    }

    @Test fun neverBlocksOutgoing() {
        assertFalse(BlockDecision.shouldBlock("+911402345678", false, prefixes))
    }

    @Test fun allowsWhenNumberNull() {
        assertFalse(BlockDecision.shouldBlock(null, true, prefixes))
    }

    @Test fun allowsWhenNumberBlank() {
        assertFalse(BlockDecision.shouldBlock("   ", true, prefixes))
    }

    @Test fun allowsWhenNoPrefixesConfigured() {
        assertFalse(BlockDecision.shouldBlock("+911402345678", true, emptySet()))
    }

    @Test fun matchesAnyOfSeveralPrefixes() {
        val many = setOf("+9199", "+91140", "+180")
        assertTrue(BlockDecision.shouldBlock("+911402345678", true, many))
    }

    @Test fun prefixMustBeAtStartNotMiddle() {
        // number contains "91140" but does not start with the prefix
        assertFalse(BlockDecision.shouldBlock("+18091140222", true, prefixes))
    }
}
