package com.example.alarmboss.ui.alarms

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Drag-to-delete for a list row, written by hand instead of using SwipeToDismissBox for one reason:
 * the screen lives in a pager, and Material's swipe handler claims drags in BOTH directions even
 * when one is disabled, so a leftward swipe that happened to start on a row never reached the
 * pager and tab swiping silently stopped working wherever an alarm sat.
 *
 * This only claims drags toward the end of the layout (rightward in LTR). A drag the other way is
 * left unconsumed, so the pager takes it. Releasing past [threshold] of the row's width asks to
 * delete; the row always springs back, since the caller confirms before anything is removed.
 */
@Composable
fun SwipeToDeleteRow(
    enabled: Boolean,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier,
    threshold: Float = 0.4f,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var widthPx by remember { mutableFloatStateOf(0f) }
    val latestOnRequestDelete by rememberUpdatedState(onRequestDelete)
    // Pointer coordinates are physical, so "toward the end" flips in right-to-left layouts.
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f

    Box(modifier.onSizeChanged { widthPx = it.width.toFloat() }) {
        if (offsetX.value != 0f) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        Box(
            Modifier
                .offset { IntOffset((offsetX.value * direction).roundToInt(), 0) }
                .pointerInput(enabled, direction) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                            // Claim only the delete direction. Anything else stays unconsumed and
                            // goes on to the pager (which then cancels this gesture).
                            if (overSlop * direction > 0f) change.consume()
                        }
                        if (drag != null) {
                            // Accumulate locally: reading offsetX.value right after launching
                            // snapTo would return a stale value and drop distance on fast drags.
                            var travelled = 0f
                            horizontalDrag(drag.id) { change ->
                                travelled = (travelled + change.positionChange().x * direction).coerceAtLeast(0f)
                                scope.launch { offsetX.snapTo(travelled) }
                                change.consume()
                            }
                            val passed = widthPx > 0f && travelled > widthPx * threshold
                            scope.launch { offsetX.animateTo(0f) }
                            if (passed) latestOnRequestDelete()
                        }
                    }
                }
        ) {
            content()
        }
    }
}
