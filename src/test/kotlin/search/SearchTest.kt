package search

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.assertEquals

class SearchTest {
    @Test
    fun testBasicSearch() = runBlocking {
        val tempDir = Files.createTempDirectory("searchTest")
        val file1 = tempDir.resolve("file1.txt")
        val file2 = tempDir.resolve("file2.txt")

        file1.writeText("hello world\nfoo bar\nworld peace")
        file2.writeText("another world here")

        val results = searchForTextOccurrences("world", tempDir).toList()

        // Matching without considering order
        val filesSet = results.map { it.file }.toSet()
        assertEquals(setOf(file1, file2), filesSet)
    }

    @Test
    fun testEmptyStringThrows() {
        val tempDir = Files.createTempDirectory("searchTest")
        try {
            searchForTextOccurrences("", tempDir)
        } catch (e: IllegalArgumentException) {
            assertEquals("Search string must not be empty", e.message)
        }
    }
}
