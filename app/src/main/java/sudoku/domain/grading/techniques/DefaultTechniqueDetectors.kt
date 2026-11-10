package sudoku.domain.grading.techniques

import sudoku.domain.grading.TechniqueDetector

fun defaultTechniqueDetectors(): List<TechniqueDetector> = listOf(
    FullHouseDetector(),
    NakedSingleDetector(),
    HiddenSingleDetector(),
    LockedCandidatesPointingDetector(),
    LockedCandidatesClaimingDetector(),
    NakedPairDetector(),
    HiddenPairDetector(),
    NakedTripleDetector(),
    HiddenTripleDetector(),
    XWingDetector(),
    NakedQuadDetector(),
    HiddenQuadDetector(),
    SwordfishDetector(),
    XYWingDetector(),
)
