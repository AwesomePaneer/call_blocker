package com.prefixcallblocker.app

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for PrefixStore.normalize() — pure, runs on the JVM. */
class NormalizeTest {

    @Test fun keepsE164AsIs() {
        assertEquals("+91140", PrefixStore.normalize("+91140"))
    }

    @Test fun trimsSurroundingWhitespace() {
        assertEquals("+91140", PrefixStore.normalize("  +91140  "))
    }

    @Test fun stripsSpacesDashesParens() {
        assertEquals("+911402345", PrefixStore.normalize("+91 140-23 (45)"))
    }

    @Test fun keepsSingleLeadingPlusOnly() {
        // internal '+' signs are dropped
        assertEquals("+91140", PrefixStore.normalize("+91+140"))
    }

    @Test fun noLeadingPlusIsFine() {
        assertEquals("0114023", PrefixStore.normalize("(011) 4023"))
    }

    @Test fun lettersProduceEmpty() {
        assertEquals("", PrefixStore.normalize("abc"))
    }

    @Test fun lonesPlusStaysPlus() {
        assertEquals("+", PrefixStore.normalize("+"))
    }

    @Test fun emptyStaysEmpty() {
        assertEquals("", PrefixStore.normalize("   "))
    }
}
