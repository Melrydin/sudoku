package sudoku.domain.generation

import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.random.Random
import sudoku.domain.puzzle.PuzzleId

object PuzzleRandom {
    fun forPuzzle(puzzle: PuzzleId): Random {
        val input: ByteArray = "${GeneratorConfig.APP_SEED}:${puzzle.key}".toByteArray(Charsets.UTF_8)
        val digest: ByteArray = MessageDigest.getInstance("SHA-256").digest(input)
        return Random(ByteBuffer.wrap(digest).long)
    }
}
