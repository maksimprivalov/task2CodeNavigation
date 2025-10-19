package search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.isRegularFile

interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}

data class OccurrenceImpl(
    override val file: Path,
    override val line: Int,
    override val offset: Int
) : Occurrence

fun searchForTextOccurrences(
    stringToSearch: String,
    directory: Path
): Flow<Occurrence> = channelFlow {
    require(stringToSearch.isNotEmpty()) { "Search string must not be empty" }
    require(Files.exists(directory)) { "Directory does not exist" }

    val files = Files.walk(directory)
        .filter { it.isRegularFile() }
        .collect(Collectors.toList())

    for (file in files) {
        launch(Dispatchers.IO) {
            try {
                file.toFile().useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        var startIndex = 0
                        while (true) {
                            val foundAt = line.indexOf(stringToSearch, startIndex)
                            if (foundAt == -1) break
                            send(OccurrenceImpl(file, index + 1, foundAt + 1))
                            startIndex = foundAt + stringToSearch.length
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore unreadable files
            }
        }
    }
}
