import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlin.math.roundToInt

sealed interface MinutesSelectorState {
    data object Idle : MinutesSelectorState
    data class Held(val pointer: Offset) : MinutesSelectorState
}

val unitLength = 100.dp
val minutesDpOffset = DpOffset(x = 0.dp, y = -unitLength)
const val minutesRepetition = 5

fun main() =
    singleWindowApplication(title = "Time Picker", state = WindowState(placement = WindowPlacement.Maximized)) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(modifier = Modifier.fillMaxSize().scale(2f)) {
                var minutes by remember { mutableStateOf(0) }
                var minutesSelectorState: MinutesSelectorState by remember { mutableStateOf(MinutesSelectorState.Idle) }
                val unitLengthFloat = with(LocalDensity.current) { unitLength.roundToPx().toFloat() }
                val minutesOffset = with(LocalDensity.current) {
                    Offset(minutesDpOffset.x.roundToPx().toFloat(), minutesDpOffset.y.roundToPx().toFloat())
                }
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val minutesOffsetAbs =
                        Offset(constraints.maxWidth.toFloat() / 2, constraints.maxHeight.toFloat() / 2) + minutesOffset

                    fun calcMinutesCoordsAt(offset: Offset): Pair<Int, Int> {
                        val gridOffsetX =
                            (if (offset.x >= minutesOffsetAbs.x) (offset.x - minutesOffsetAbs.x) / unitLengthFloat else (offset.x - minutesOffsetAbs.x) / unitLengthFloat - 1).toInt()
                        val gridOffsetY =
                            (if (offset.y >= minutesOffsetAbs.y) (offset.y - minutesOffsetAbs.y) / unitLengthFloat else (offset.y - minutesOffsetAbs.y) / unitLengthFloat - 1).toInt()
                        return gridOffsetX to gridOffsetY
                    }

                    fun calcMinutesNumAt(coords: Pair<Int, Int>) =
                        (coords.first + minutes).mod(10) + 10 * (coords.second + minutes / 10).mod(6)

                    fun calcMinutesNumAt(offset: Offset) = calcMinutesNumAt(calcMinutesCoordsAt(offset))

                    SubcomposeLayout(modifier = Modifier.fillMaxSize().pointerInput(constraints) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            if (down.position.x !in minutesOffsetAbs.x..(minutesOffsetAbs.x + unitLengthFloat) || down.position.y !in minutesOffsetAbs.y..(minutesOffsetAbs.y + unitLengthFloat)) return@awaitEachGesture
                            minutesSelectorState = MinutesSelectorState.Held(down.position)
                            var change = awaitTouchSlopOrCancellation(down.id) { change, over ->
                                minutesSelectorState = when (val state = minutesSelectorState) {
                                    MinutesSelectorState.Idle -> MinutesSelectorState.Idle
                                    is MinutesSelectorState.Held -> MinutesSelectorState.Held(state.pointer + over)
                                }
                                change.consume()
                            }
                            while (change != null && change.pressed) {
                                change = awaitDragOrCancellation(change.id)
                                if (change != null && change.pressed) {
                                    minutesSelectorState = when (val state = minutesSelectorState) {
                                        MinutesSelectorState.Idle -> MinutesSelectorState.Idle
                                        is MinutesSelectorState.Held -> MinutesSelectorState.Held(state.pointer + change.positionChange())
                                    }
                                    change.consume()
                                }
                                if (change == null || change.changedToUp()) {
                                    when (val state = minutesSelectorState) {
                                        MinutesSelectorState.Idle -> {}
                                        is MinutesSelectorState.Held -> {
                                            minutes = calcMinutesNumAt(state.pointer)
                                            minutesSelectorState = MinutesSelectorState.Idle
                                        }
                                    }
                                }
                            }
                        }
                    }) { constraints ->
                        var slotId = 0
                        val currentSelection = subcompose(slotId++) {
                            Box(
                                contentAlignment = Alignment.Center, modifier = Modifier.border(4.dp, Color.Green)
                            ) {
                                Text(minutes.toString().padStart(2, '0'))
                            }
                        }.first().measure(Constraints.fixed(unitLengthFloat.roundToInt(), unitLengthFloat.roundToInt()))
                        val xRange = (minutesRepetition * -10)..(minutesRepetition * 10)
                        val xAxis = when (val state = minutesSelectorState) {
                            MinutesSelectorState.Idle -> emptyList()
                            is MinutesSelectorState.Held -> subcompose(slotId++) {
                                val heldCoords = calcMinutesCoordsAt(state.pointer)
                                for (x in xRange) {
                                    Box(
                                        contentAlignment = Alignment.Center, modifier = Modifier.border(2.dp, Color.Red)
                                    ) {
                                        Text(
                                            calcMinutesNumAt(heldCoords.first + x to heldCoords.second).toString()
                                                .padStart(2, '0')
                                        )
                                    }
                                }
                            }
                        }.mapIndexed { index, measurable ->
                            xRange.elementAt(index) to measurable.measure(
                                Constraints.fixed(unitLengthFloat.roundToInt(), unitLengthFloat.roundToInt())
                            )
                        }.toMap()
                        val yRange = (minutesRepetition * -6)..(minutesRepetition * 6)
                        val yAxis = when (val state = minutesSelectorState) {
                            MinutesSelectorState.Idle -> emptyList()
                            is MinutesSelectorState.Held -> subcompose(slotId++) {
                                val heldCoords = calcMinutesCoordsAt(state.pointer)
                                for (y in yRange) {
                                    Box(
                                        contentAlignment = Alignment.Center, modifier = Modifier.border(2.dp, Color.Red)
                                    ) {
                                        Text(
                                            calcMinutesNumAt(heldCoords.first to heldCoords.second + y).toString()
                                                .padStart(2, '0')
                                        )
                                    }
                                }
                            }
                        }.mapIndexed { index, measurable ->
                            yRange.elementAt(index) to measurable.measure(
                                Constraints.fixed(unitLengthFloat.roundToInt(), unitLengthFloat.roundToInt())
                            )
                        }.toMap()
                        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
                            val intersectionCoords = calcMinutesCoordsAt(
                                when (val state = minutesSelectorState) {
                                    MinutesSelectorState.Idle -> Offset.Zero
                                    is MinutesSelectorState.Held -> state.pointer
                                }
                            )
                            xAxis.forEach { (x, placeable) ->
                                placeable.place(
                                    minutesOffsetAbs.x.roundToInt() + (intersectionCoords.first + x) * unitLengthFloat.roundToInt(),
                                    minutesOffsetAbs.y.roundToInt() + intersectionCoords.second * unitLengthFloat.roundToInt()
                                )
                            }
                            yAxis.forEach { (y, placeable) ->
                                placeable.place(
                                    minutesOffsetAbs.x.roundToInt() + intersectionCoords.first * unitLengthFloat.roundToInt(),
                                    minutesOffsetAbs.y.roundToInt() + (intersectionCoords.second + y) * unitLengthFloat.roundToInt()
                                )
                            }
                            currentSelection.place(minutesOffsetAbs.x.roundToInt(), minutesOffsetAbs.y.roundToInt())
                        }
                    }
                }
            }
        }
    }
