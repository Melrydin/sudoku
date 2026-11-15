package sudoku.domain.generation

import sudoku.domain.puzzle.PuzzleId
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.random.Random

object PuzzleRandom {
    fun forPuzzle(puzzle: PuzzleId): Random {
        val input = "${GeneratorConfig.APP_SEED}:${puzzle.key}".toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(input)
        return Random(ByteBuffer.wrap(digest).long)
    }
}
