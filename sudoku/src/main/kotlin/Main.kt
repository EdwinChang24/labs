infix fun Int.through(end: Int) = (this..end).toList()
val Int.c get() = listOf(this, this + 4, this + 8, this + 12)
val Int.r get() = this through this + 3

const val groupSize = 16
const val cellCount = 96

val groups = listOf(
    // faces
    1 through 16,
    17 through 32,
    33 through 48,
    49 through 64,
    65 through 80,
    81 through 96,
    // green
    1.c + 17.r + 68.c + 61.r,
    2.c + 21.r + 67.c + 57.r,
    3.c + 25.r + 66.c + 53.r,
    4.c + 29.r + 65.c + 49.r,
    // red
    17.c + 33.c + 49.c + 84.c,
    18.c + 34.c + 50.c + 83.c,
    19.c + 35.c + 51.c + 82.c,
    20.c + 36.c + 52.c + 81.c,
    // blue
    1.r + 33.r + 65.r + 81.r,
    5.r + 37.r + 69.r + 85.r,
    9.r + 41.r + 73.r + 89.r,
    13.r + 45.r + 77.r + 93.r,
)

fun <T> List<T>.allPairs() = map { i -> map { i to it } }.flatten().filter { (l, r) -> l != r }

fun main() {
    val atLeastOne =
        (1..cellCount).map { cell -> ((cell * groupSize + 1)..(cell * groupSize + groupSize)).joinToString(" ") }
    val atMostOne = (1..cellCount).map { cell ->
        ((cell * groupSize + 1)..(cell * groupSize + groupSize)).toList().allPairs().map { (l, r) -> "-$l -$r" }
    }.flatten()
    val groupsSatisfied = groups.map { group ->
        (1..groupSize).map { num ->
            group.map { cell -> cell * groupSize + num }.joinToString(" ")
        }
    }.flatten()
    val everything = atLeastOne + atMostOne + groupsSatisfied
    println("p cnf ${cellCount * groupSize + groupSize} ${everything.size}")
    everything.forEach {
        println("$it 0")
    }
}
