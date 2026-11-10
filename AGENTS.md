# Sudoku

## Project goal

Build a native Android Sudoku game in Kotlin with a simple, minimal design.
Prioritize a clear board, intuitive controls, and a calm playing experience.
Communicate with the user in German.
Use Sudoku - The Clean One by Dustland Design as the visual and navigation reference:
https://play.google.com/store/apps/details?id=ee.dustland.android.dustlandsudoku
The user owns the core algorithms unless they explicitly request implementation.
The solution counter and generator integration were explicitly requested and are implemented.
Do not implement or replace the remaining difficulty-grading algorithms without a new request.

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
- Let the player select a puzzle level within each difficulty and persist that selection.
- Draw original vector assets rather than bundling assets from the reference app.

## Initial playable scope

- Display a valid Sudoku puzzle with fixed starting numbers.
- Let the player select an editable cell, enter a number from 1 to 9, and erase an entry.
- Prevent changes to starting numbers.
- Validate rows, columns, and boxes and recognize a correctly completed puzzle.
- Allow starting a new game; confirm before discarding an unfinished game with player entries.
- Preserve the active puzzle and player entries across activity recreation and app restarts.
- Support manual notes, undo, conflict highlighting, and saved progress per puzzle version, difficulty, and level.
- Generate uniquely solvable puzzles for new games and preserve saved sample games.
- Label generated puzzles as ungraded until the technique-based difficulty analyzer is implemented.
- Track active play time per game. Pause in the background and after completion; persist elapsed time.
- Keep the adaptive launcher icon consistent with the home screen's Sudoku symbol and Aurum colors.
- Defer hints and technique-based difficulty grading.
- Generate puzzles in the background and retain in-flight work across activity recreation.

## Architecture

- `app`: Android application startup and the manual dependency container.
- `domain.board`: board values, row/column/box cell groups, duplicate validation, and digit counts.
- `domain.game`: game state, editing, notes, timing, new-game creation, and the repository contract.
- `domain.puzzle`: stable puzzle identity, difficulty IDs, and generator version.
- `domain.generation`: seeded full-board generation and uniqueness-preserving clue removal.
- `domain.solver`: bounded solution counting and reference-solution resolution.
- `domain.grading`: the user's logical-technique contracts and grading policy.
- `domain.grading.techniques`: one detector file per technique; the user implements their detection algorithms.
- `domain.settings`: platform-independent option values and color-theme identifiers.
- `domain.common`: cancellation shared by generation and solution counting.
- `data.game`: SharedPreferences game repository and JSON encoding.
- `domain.statistics` and `data.statistics`: completed-game records and their persisted leaderboard.
- `data.preferences`: persisted app choices, gameplay options, and board layout.
- `data.puzzle`: the bundled sample used for previews and test fixtures.
- `ui.home`, `ui.game`, `ui.settings`, `ui.statistics`, and `ui.about`: feature screens.
- `ui.game.board` and `ui.game.controls`: board rendering/gestures and number-pad controls.
- `ui.game.result` and `ui.navigation`: game-over/victory presentation and home navigation.
- `ui.settings.layout`: board-layout preview and its dialog.
- `ui.theme` and `ui.common`: Android theme resources, palette, formatting, and activity helpers.
- Domain code must not import Android, AndroidX, `data`, `ui`, or `app` classes.
- Data code must not import UI or application wiring. Resolve Android resources in UI code.
- Activities obtain shared repositories and preferences from `SudokuApplication.container`.
- Tests mirror the production packages; shared fixtures live in `sudoku.fixtures` and `sudoku.testing`.
- Keep the application ID, preference filenames/keys, and saved enum names stable.
- `.MainActivity` remains a launcher alias for `.ui.home.MainActivity` for launcher compatibility.
- Defaults reflect the Pixel settings captured on 2026-11-09: Aurum, Easy, centered/full-width board,
  timer and assistance enabled, notes prefill and erase gestures disabled, given-cell circles disabled.

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
- Run Espresso tests with system animations temporarily disabled and restore their original values afterward.
- Report what was verified and any checks that could not run.

## Next milestone

New games use `NewGameFactory` and `PuzzleGenerator`; saved games still load their original puzzle.
`SudokuGenerator.generate()` fills an empty board using seeded randomness and an explicit decision stack.
It chooses cells with the fewest candidates, breaking ties randomly.
`SolutionCounter.count(SudokuBoard)` distinguishes no solution, one solution, and multiple solutions.
Its private search copies the input, uses row/column/box masks, and stops after finding two solutions.
`ClueRemover` tries cells in seeded random order and keeps a removal only for `SolutionCount.UNIQUE`.
Its result reports whether the clue target was reached. V1 retains its original shared target of 36 givens.
V2 selects a seeded random target from `GeneratorConfig.clueRange`: Beginner 40–45, Easy 36–39,
Medium 32–35, Hard 28–31, and Extreme 24–27. These are generation targets, not difficulty grades.
V2 retries clue removal up to 100 times and reports failure rather than starting with an unreached target.
Do not start games from unchecked removals or fully solved boards.
`PuzzleRandom` derives a fresh random stream from the fixed app seed and `PuzzleId.key` using SHA-256.
`GeneratorVersion.V1` fixes the original encoding and generation behavior; V2 adds difficulty-specific clue targets.
New games use V2. Home resumes older saved versions when no newer save exists for the selected difficulty and level.
Keep saved boards and generator behavior stable within each version. Version behavior changes.
`GamePreparationModel` runs generation off the main thread, retains it across activity recreation,
and interrupts it when cleared. Generation and solution counting must keep checking cancellation.
`GameActivity` shows loading/retry states, starts its timer after preparation, and preserves existing saves.
Each game has three lives and a persisted reference solution. Wrong number submissions cost one life,
including repeated submissions; notes, givens, erase, and undo do not cost lives or refund lost lives.
The third mistake resets state/history/time, deletes only that puzzle's current and applicable legacy save,
and shows a non-cancellable game-over dialog before returning home. Failed sessions must never be saved again.
`GameStateCodec` retains the solution, mistakes, game ID, and activated assistance across restarts;
older saves resolve the original puzzle and start with three lives without retroactive penalties.
Restarting a level clears entries, notes, history, time, lives, and assistance while retaining its starting puzzle.
Each of the five activated assistance options contributes one point, at most once per game (0–5).
Previously recorded assistance remains counted after switching it off. Freeze the score on completion.
A completed game opens `VictoryActivity` with recorded active play time and assistance points.
Record each unique game ID once; leaderboard ordering is elapsed time first, assistance points as a tie-breaker.
The local leaderboard includes level and difficulty and preserves previous victories when another attempt fails.
The five difficulty values live in `domain.puzzle.Difficulty`; their Android string mapping lives in `ui.common`.
The highest value remains `EXTREME` for save compatibility, has stable ID `expert`, and displays "Hard".
The grading package contains technique contracts, policy, and the user's implemented detectors.
All fourteen detector classes live in `domain.grading.techniques` and are registered by default.
`FullHouseDetector`, `NakedSingleDetector`, `HiddenSingleDetector`, and `LockedCandidatesPointingDetector` are implemented.
The remaining ten detectors return `NotImplemented`.
`SudokuUnits` provides ordered cell groups: rows, then columns, then boxes, each in reading order.
Detectors return the first applicable step without mutating `LogicalState` or recomputing its candidates.
Board validation only detects duplicates and completion; it must not solve puzzles.
The next algorithm milestone is technique-based difficulty grading, implemented by the user.
