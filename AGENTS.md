# Sudoku

## Project goal

Build a native Android Sudoku game in Kotlin with a simple, minimal design.
Prioritize a clear board, intuitive controls, and a calm playing experience.
Communicate with the user in German.
Use Sudoku - The Clean One by Dustland Design as the visual and navigation reference:
https://play.google.com/store/apps/details?id=ee.dustland.android.dustlandsudoku
The user owns the core algorithms unless they explicitly request implementation.
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
- Keep function return types non-nullable. Represent absence with an empty collection, an explicit result type,
  or a clearly defined sentinel outside the valid value range (for example, `0` when valid digits are `1..9`).

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
- Keep the adaptive launcher icon consistent with the home screen's Sudoku symbol and Aurum colors.

## Gameplay rules

- Keep givens immutable; support digit entry, erase, manual notes, undo, and conflict highlighting.
  Board validation detects row/column/box duplicates and completion; it must not solve puzzles.
- Confirm before discarding unfinished games with player entries. Restart retains the starting puzzle
  and resets entries, notes, history, time, lives, and assistance.
- Persist progress per puzzle version, difficulty, and level across activity recreation and app restarts,
  including the reference solution, mistakes, game ID, and activated assistance. Preserve saved sample games.
- Each game has three lives. Every wrong number submission costs one, including repeats;
  notes, givens, erase, and undo neither cost lives nor refund them.
- The third mistake clears state/history/time, deletes only that puzzle's current and applicable legacy save,
  and shows a non-cancellable game-over dialog before returning home. Never save failed sessions again.
- Older saves retain their original puzzle, resolve its solution, and start with three lives
  without retroactive penalties.
- Count each of the five activated assistance options once per game, even after disabling it;
  freeze the score on completion.
- Persist active play time; pause in the background and after completion. Show time and assistance points on victory.
- Record each game ID once. Rank victories by elapsed time, then assistance points; include difficulty and level.
  Preserve previous victories when another attempt fails.
- Hints remain out of scope for now.

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

## How the work goes

- **Trunk based.** Every commit goes straight to `main` in small focused steps. No
  feature branch, no pull request — not even "to be safe". CI on `main` is the safety
  net. Should more contributors join, the branch and pull request workflow returns.
- **Commit only when asked.** Conventional commits (`feat`, `fix`, `refactor`, `perf`,
  `style`, `test`, `docs`, `build`, `ops`, `chore`, optional scope, imperative,
  lowercase, no trailing dot).
- **Never push.** Clemens pushes himself, always. Do not run `git push`, do not offer
  it, do not ask whether it should happen, and do not mention it as an open step when
  reporting on a commit.
- A code change and a documentation update are **separate commits** — the fix as
  `fix(...)`, the document as `docs(...)`.

## Generation and grading rules

- Keep saved boards and deterministic generator behavior stable within each version;
  behavior changes require a new version.
- New games use V2; resume older saved versions when no newer save exists for the selected difficulty and level.
- Only start uniquely solvable, incomplete puzzles. V2 must reach its clue target or report failure.
  Clue counts are generation targets, not difficulty grades; `GeneratorConfig` defines their ranges and retry limit.
- Generate off the main thread, retain in-flight work across activity recreation, and cancel it when cleared.
  Generation and solution counting must check cancellation; start the timer only after preparation.
- Keep generated puzzles labeled as ungraded until technique-based grading is integrated.
- Keep difficulty enum names and IDs stable: `EXTREME` has ID `expert` and displays "Hard".
  Resolve Android difficulty labels in `ui.common`.
- Keep all fourteen detectors registered. `SudokuUnits` orders rows, then columns, then boxes, each in reading order.
  Return the first effective step without mutating `LogicalState` or recomputing candidates.

## Open milestones

Technique-based difficulty grading is the current algorithm milestone; the user implements its algorithms.
Six detectors are implemented and tested: Full House, Naked Single, Hidden Single,
Locked Candidates Pointing, Locked Candidates Claiming, and Naked Pair.

1. Implement and test Hidden Pair next.
2. Implement and test the remaining detectors: Naked Triple, Hidden Triple, X-Wing, Naked Quad,
   Hidden Quad, Swordfish, and XY-Wing. These seven and Hidden Pair currently return `NotImplemented`.
3. Implement and test `DifficultyAnalyzer`: initialize candidates once, apply effective steps,
   restart technique priority after each step, and grade solved boards by the highest technique tier used.
   Preserve candidate eliminations and return `Unrated` when solving stalls; the analyzer is currently a placeholder.
4. Integrate grading into puzzle generation and the UI while preserving generator-version and save compatibility.
