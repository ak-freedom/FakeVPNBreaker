package com.akfreedom.fakevpnbreaker

import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceLocalizationTest {
    private val defaultStrings = readStrings(Path.of("src", "main", "res", "values", "strings.xml"))
    private val russianStrings = readStrings(Path.of("src", "main", "res", "values-ru", "strings.xml"))

    @Test
    fun russianResourcesCoverLocalizableUiStrings() {
        val manifestOnlyStrings = setOf("foreground_service_subtype")
        val expectedRussianKeys = defaultStrings.keys - manifestOnlyStrings

        assertEquals(expectedRussianKeys, russianStrings.keys)
    }

    @Test
    fun manifestReferencedSubtypeDoesNotVaryByLocale() {
        assertTrue(defaultStrings.containsKey("foreground_service_subtype"))
        assertFalse(russianStrings.containsKey("foreground_service_subtype"))
        assertNotNull(
            readStringNode(Path.of("src", "main", "res", "values", "strings.xml"), "foreground_service_subtype")
                .attributes
                .getNamedItem("translatable")
                ?.takeIf { it.nodeValue == "false" },
        )
    }

    @Test
    fun languageAndEmptyLogStringsAreLocalized() {
        assertEquals("Russian", defaultStrings.getValue("language_russian"))
        assertEquals("Русский", russianStrings.getValue("language_russian"))
        assertEquals("No events yet.", defaultStrings.getValue("no_events_yet"))
        assertEquals("Событий пока нет.", russianStrings.getValue("no_events_yet"))
    }

    private fun readStrings(path: Path): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(path.toFile())
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    private fun readStringNode(path: Path, name: String) =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(path.toFile())
            .getElementsByTagName("string")
            .let { nodes ->
                (0 until nodes.length)
                    .map { index -> nodes.item(index) }
                    .first { node -> node.attributes.getNamedItem("name").nodeValue == name }
            }
}
