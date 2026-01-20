package blbl.cat3399.feature.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import blbl.cat3399.R

class SegmentedSeekBar : AppCompatSeekBar {
    private val segmentPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.blbl_blue)
            alpha = 170
        }

    private var segments: List<SegmentMark> = emptyList()

    private val tmpRect = RectF()

    private var trackHeightPx: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setTrackHeightPx(heightPx: Int) {
        val v = heightPx.coerceAtLeast(0)
        if (trackHeightPx == v) return
        trackHeightPx = v
        updateTrackBounds()
        invalidate()
    }

    fun setSegments(segments: List<SegmentMark>) {
        this.segments = segments
        invalidate()
    }

    fun clearSegments() {
        setSegments(emptyList())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateTrackBounds()
    }

    override fun setProgressDrawable(d: android.graphics.drawable.Drawable?) {
        super.setProgressDrawable(d)
        updateTrackBounds()
    }

    private fun updateTrackBounds() {
        val d = progressDrawable ?: return
        if (width <= 0 || height <= 0) return

        val b = d.bounds
        val hasValidBounds = b.width() > 1 && b.height() > 1

        // Keep the same horizontal range as the framework computed (padding, RTL, thumb offset, etc).
        // Only adjust vertical bounds to control track thickness; changing left/right here can cause visual drift.
        val left =
            if (hasValidBounds) b.left
            else (paddingLeft + thumbOffset).coerceAtLeast(0)
        val right =
            if (hasValidBounds) b.right
            else (width - paddingRight - thumbOffset).coerceAtLeast(left + 1)

        val contentTop = paddingTop
        val contentBottom = (height - paddingBottom).coerceAtLeast(contentTop + 1)
        val contentHeight = (contentBottom - contentTop).coerceAtLeast(1)

        val desired = trackHeightPx.takeIf { it > 0 } ?: b.height().takeIf { it > 0 } ?: contentHeight
        val h = desired.coerceIn(1, contentHeight)

        val centerY =
            if (hasValidBounds) b.centerY()
            else (contentTop + contentHeight / 2)
        val top = (centerY - h / 2).coerceIn(contentTop, contentBottom - 1)
        val bottom = (top + h).coerceAtMost(contentBottom)

        if (b.left != left || b.top != top || b.right != right || b.bottom != bottom) {
            d.setBounds(left, top, right, bottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty()) return

        val bounds = progressDrawable?.bounds ?: return
        val width = bounds.width().toFloat()
        if (width <= 1f) return

        val top = bounds.top.toFloat()
        val bottom = bounds.bottom.toFloat()

        // AbsSeekBar draws the track inset by thumbOffset so the thumb doesn't overflow at the ends.
        // Use the same horizontal range for segment markers; otherwise markers won't align with the thumb/progress.
        val leftBase = bounds.left.toFloat()
        val rightBase = bounds.right.toFloat()
        if (rightBase - leftBase <= 1f) return

        for (seg in segments) {
            val start = seg.startFraction.coerceIn(0f, 1f)
            val end = seg.endFraction.coerceIn(0f, 1f)
            if (end <= start) continue
            val range = rightBase - leftBase
            val (l, r) =
                if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    val lRtl = rightBase - range * end
                    val rRtl = rightBase - range * start
                    lRtl to rRtl
                } else {
                    val lLtr = leftBase + range * start
                    val rLtr = leftBase + range * end
                    lLtr to rLtr
                }
            tmpRect.set(l, top, r, bottom)
            canvas.drawRect(tmpRect, segmentPaint)
        }
    }
}
