# Sudoku

## Project goal

Build a native Android Sudoku game in Kotlin with a simple, minimal design.
Prioritize a clear board, intuitive controls, and a calm playing experience.
Communicate with the user in German.
Use Sudoku - The Clean One by Dustland Design as the visual and navigation reference:
https://play.google.com/store/apps/details?id=ee.dustland.android.dustlandsudoku
The user owns implementation of the core solver algorithm. Do not implement or replace it.

## Coding rules

- Write code, identifiers, and code comments in English.
- Keep every function at 20 lines or fewer, including its declaration and closing brace.
- Keep every code line at 120 characters or fewer.
- Follow DRY. Extract repeated knowledge and logic instead of copying it.
- Follow SRP. Give each class, file, and function one clear responsibility.
- Keep one abstraction level per function. Extract lower-level details into well-named helpers.
- Add clear, concise comments only for complex or non-obvious decisions. Do not restate the code.
- Use explicit Kotlin types when they clarify public APIs or non-obvious values.
  Allow inference for obvious local values.

## Game design

- Keep the interface simple, uncluttered, and easy to understand.
- Make the 9 by 9 board the main focus, with clear boundaries between the 3 by 3 boxes.
- Use readable numbers, generous touch targets, and consistent spacing.
- Clearly distinguish given numbers, player entries, and the selected cell.
- Use a restrained color palette and sufficient contrast. Do not rely on color alone for meaning.
- Keep primary controls close to the board: number selection, erase, and new game.
- Use Android string resources for user-facing text.
- Keep additional screens, animations, and settings out of the initial scope unless needed.
- Match the reference home structure: theme arrows at the top, a central circular Sudoku symbol,
  difficulty arrows, an outlined new-game button, and settings, best-times, and info icons at the bottom.
- Offer five difficulty levels and persist the selected difficulty and color theme.
- Draw original vector assets rather than bundling assets from the reference app.

## Initial playable scope

- Display a valid Sudoku puzzle with fixed starting numbers.
- Let the player select an editable cell, enter a number from 1 to 9, and erase an entry.
- Prevent changes to starting numbers.
- Validate rows, columns, and boxes and recognize a correctly completed puzzle.
- Allow starting a new game; confirm before discarding an unfinished game with player entries.
- Preserve the active puzzle and player entries across activity recreation and app restarts.
- Support manual notes, undo, conflict highlighting, and saved progress per selected difficulty.
- Use the bundled sample puzzle until a real puzzle source is added. Label it clearly; the sample is
  shared across difficulty selections and does not represent five graded puzzle sets.
- Track active play time per game. Pause in the background and after completion; persist elapsed time.
- Keep the adaptive launcher icon consistent with the home screen's Sudoku symbol and Aurum colors.
- Defer hints, recorded statistics, and puzzle generation until the basics work.

## Implementation guidance

- The current project uses Kotlin, an Android app module named `app`, and XML layouts with AppCompat.
- Keep the existing UI stack unless a change is agreed with the user.
- Keep Sudoku rules and game state independent of Android UI classes so they can be unit-tested.
- Keep activities and views focused on rendering state and forwarding player actions.
- Prefer small, focused components and add dependencies only when they solve a concrete need.
- Treat the Gradle configuration as the source of truth for SDK and dependency versions.
- Preserve unrelated work and avoid unnecessary formatting changes.

## Validation

- Add focused unit tests for Sudoku rules, immutable starting cells, editing, and completion detection
  when implementing those behaviors.
- Run `./gradlew testDebugUnitTest` for game logic changes.
- Run `./gradlew assembleDebug` and `./gradlew lintDebug` for Android integration or UI changes.
- When a device or emulator is available, check board readability, cell selection, number entry,
  erase, state restoration, and new-game behavior.
- Report what was verified and any checks that could not run.

## Next milestone

The home screen and playable board use XML layouts and the selected color theme.
The solver contract is `SudokuSolver.solve(SudokuBoard): SolverResult` in the `solver` package.
`UserSudokuSolver` is deliberately unimplemented and returns `SolverResult.NotImplemented`.
Board validation only detects duplicates and completion; it must not solve puzzles.
Integrate the user's algorithm later, then add real puzzle sets and difficulty grading.
