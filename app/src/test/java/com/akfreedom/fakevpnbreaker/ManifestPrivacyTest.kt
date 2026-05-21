package com.akfreedom.fakevpnbreaker

import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestPrivacyTest {
    private val manifest = Path.of("src", "main", "AndroidManifest.xml").readText()

    @Test
    fun manifestDoesNotRequestInternetPermission() {
        assertFalse(
            "Privacy invariant violated: android.permission.INTERNET must not be requested.",
            manifest.contains("android.permission.INTERNET"),
        )
    }

    @Test
    fun fakeVpnServiceKeepsMinimalVpnBoundary() {
        val service = manifestBlock("<service\\s+android:name=\"\\.vpn\\.FakeVpnService\"", "service")

        assertTrue(
            "VPN invariant violated: FakeVpnService must require android.permission.BIND_VPN_SERVICE.",
            service.contains("android:permission=\"android.permission.BIND_VPN_SERVICE\""),
        )
        assertTrue(
            "VPN invariant violated: FakeVpnService must remain non-exported.",
            service.contains("android:exported=\"false\""),
        )
        assertTrue(
            "VPN invariant violated: FakeVpnService must keep foregroundServiceType=\"specialUse\".",
            service.contains("android:foregroundServiceType=\"specialUse\""),
        )
        assertTrue(
            "VPN invariant violated: FakeVpnService must keep the Android VpnService action.",
            service.contains("android:name=\"android.net.VpnService\""),
        )
    }

    @Test
    fun triggerReceiverKeepsDocumentedBroadcastSurface() {
        val receiver = manifestBlock("<receiver\\s+android:name=\"\\.TriggerReceiver\"", "receiver")

        assertTrue(
            "MacroDroid invariant violated: TriggerReceiver must remain exported for explicit Broadcast setup.",
            receiver.contains("android:exported=\"true\""),
        )
        assertTrue(
            "MacroDroid invariant violated: TriggerReceiver must keep the documented BREAK_VPN action.",
            receiver.contains("android:name=\"com.akfreedom.fakevpnbreaker.BREAK_VPN\""),
        )
    }

    @Test
    fun triggerActivityKeepsIsolatedFallbackSurface() {
        val activity = manifestBlock("<activity\\s+android:name=\"\\.TriggerActivity\"", "activity")

        assertTrue(
            "MacroDroid invariant violated: TriggerActivity must remain exported for Activity fallback setup.",
            activity.contains("android:exported=\"true\""),
        )
        assertTrue(
            "MacroDroid invariant violated: TriggerActivity must keep singleTask replacement handling.",
            activity.contains("android:launchMode=\"singleTask\""),
        )
        assertTrue(
            "MacroDroid invariant violated: TriggerActivity must use an isolated task affinity.",
            activity.contains("android:taskAffinity=\"com.akfreedom.fakevpnbreaker.trigger\""),
        )
        assertTrue(
            "MacroDroid invariant violated: TriggerActivity must stay excluded from recents.",
            activity.contains("android:excludeFromRecents=\"true\""),
        )
        assertTrue(
            "MacroDroid invariant violated: TriggerActivity must keep the documented BREAK_VPN action.",
            activity.contains("android:name=\"com.akfreedom.fakevpnbreaker.BREAK_VPN\""),
        )
        assertFalse(
            "MacroDroid invariant violated: TriggerActivity must not use android:noHistory because VPN consent result delivery depends on history.",
            activity.contains("android:noHistory"),
        )
    }

    private fun manifestBlock(startPattern: String, tagName: String): String {
        val start = Regex(startPattern).find(manifest)?.range?.first
            ?: error("Manifest invariant setup failed: expected component declaration is missing.")
        val closingTag = "</$tagName>"
        val end = manifest.indexOf(closingTag, start).takeIf { it >= 0 }
            ?: error("Manifest invariant setup failed: expected component closing tag is missing.")
        return manifest.substring(start, end + closingTag.length)
    }
}
