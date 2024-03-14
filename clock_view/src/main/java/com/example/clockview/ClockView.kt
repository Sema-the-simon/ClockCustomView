package com.example.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val REFRESH_PERIOD = 180L

private const val DEFAULT_WIDTH_IN_DP = 240
private const val DEFAULT_HEIGHT_IN_DP = 240
private const val START_ANGLE = -Math.PI / 2

private const val BUNDLE_KEY_SUPERSTATE = "BUNDLE_KEY_SUPERSTATE"
private const val BUNDLE_KEY_COLORS = "BUNDLE_KEY_COLORS"

class ClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var clockColors: DefaultClockColors = DefaultClockColors()

    private var clockRadius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private val borderWidth get() = clockRadius / 12
    private val dialTextSize get() = clockRadius * 3 / 11
    private val smallDotRadius get() = clockRadius / 90
    private val bigDotRadius get() = clockRadius / 130
    private val hourHandWidth get() = clockRadius / 15
    private val minuteHandWidth get() = clockRadius / 30
    private val secondHandEndLineWidth get() = clockRadius / 40
    private val secondHandBaseLineWidth get() = clockRadius / 120

    private val hourHandStart get() = clockRadius * 1 / 4
    private val hourHandEnd get() = clockRadius * 1 / 2

    private val minuteHandStart get() = clockRadius * 1 / 4
    private val minuteHandEnd get() = clockRadius * 2 / 3

    private val secondHandStart get() = clockRadius * 1 / 4
    private val secondHandMiddle get() = clockRadius * 1 / 20
    private val secondHandEnd get() = clockRadius * 3 / 4

    private val borderCircleRadius get() = clockRadius - borderWidth / 2
    private val dotsCircleRadius get() = clockRadius * 5 / 6
    private val dialCircleRadius get() = clockRadius * 11 / 16

    private val utilPosition: PointF = PointF(0.0f, 0.0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        letterSpacing = -0.15f
    }
    private val calendar: Calendar get() = Calendar.getInstance()

    init {
        context.withStyledAttributes(attrs, R.styleable.ClockView) {
            clockColors.apply {
                backColor = getColor(
                    R.styleable.ClockView_backColor,
                    clockColors.backColor
                )
                borderColor = getColor(
                    R.styleable.ClockView_borderColor,
                    clockColors.borderColor
                )
                dotsColor = getColor(
                    R.styleable.ClockView_dotsColor,
                    clockColors.dotsColor
                )
                dialColor = getColor(
                    R.styleable.ClockView_dialColor,
                    clockColors.dialColor
                )
                hourHandColor = getColor(
                    R.styleable.ClockView_hourHandColor,
                    clockColors.hourHandColor
                )
                minuteHandColor = getColor(
                    R.styleable.ClockView_minuteHandColor,
                    clockColors.minuteHandColor
                )
                secondHandColor = getColor(
                    R.styleable.ClockView_secondHandColor,
                    clockColors.secondHandColor
                )
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        clockRadius = min(width, height) / 2f
        centerX = width / 2f
        centerY = height / 2f
    }

    private fun PointF.calculateDigitPosition(digit: Int, radius: Float) {
        val angle = (START_ANGLE + digit * (Math.PI / 6)).toFloat()
        val textBaselineToCenter = (paint.descent() + paint.ascent()) / 2

        x = radius * cos(angle) + centerX
        y = radius * sin(angle) + centerY - textBaselineToCenter
    }

    private fun PointF.calculateDotPosition(dotNumber: Int, radius: Float) {
        val angle = (dotNumber * (Math.PI / 30)).toFloat()

        x = radius * cos(angle) + centerX
        y = radius * sin(angle) + centerY
    }

    private fun drawClockBack(canvas: Canvas) {
        paint.color = clockColors.backColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, clockRadius, paint)
    }

    private fun drawClockBorder(canvas: Canvas) {
        paint.color = clockColors.borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth

        canvas.drawCircle(centerX, centerY, borderCircleRadius, paint)
        paint.strokeWidth = 0f
    }

    private fun drawSecondDots(canvas: Canvas) {
        paint.color = clockColors.dotsColor
        paint.style = Paint.Style.FILL

        for (i in 0 until 60) {
            utilPosition.calculateDotPosition(i, dotsCircleRadius)
            val dotRadius = if (i % 5 == 0) smallDotRadius else bigDotRadius
            canvas.drawCircle(utilPosition.x, utilPosition.y, dotRadius, paint)
        }
    }

    private fun drawDial(canvas: Canvas) {
        paint.strokeWidth = 0f
        paint.textSize = dialTextSize
        paint.color = clockColors.dialColor

        for (i in 1..12) {
            utilPosition.calculateDigitPosition(i, dialCircleRadius)
            canvas.drawText(i.toString(), utilPosition.x, utilPosition.y, paint)
        }
    }

    private fun drawHourHand(canvas: Canvas, hourAndMinutes: Float) {
        paint.color = clockColors.hourHandColor
        paint.strokeWidth = hourHandWidth
        val angle = (START_ANGLE + hourAndMinutes * Math.PI / 6).toFloat()

        canvas.drawLine(
            centerX - cos(angle) * hourHandStart,
            centerY - sin(angle) * hourHandStart,
            centerX + cos(angle) * hourHandEnd,
            centerY + sin(angle) * hourHandEnd,
            paint
        )
    }

    private fun drawMinuteHand(canvas: Canvas, minutes: Int) {
        paint.color = clockColors.minuteHandColor
        paint.strokeWidth = minuteHandWidth
        val angle = (START_ANGLE + minutes * Math.PI / 30).toFloat()

        canvas.drawLine(
            centerX - cos(angle) * minuteHandStart,
            centerY - sin(angle) * minuteHandStart,
            centerX + cos(angle) * minuteHandEnd,
            centerY + sin(angle) * minuteHandEnd,
            paint
        )
    }

    private fun drawSecondHand(canvas: Canvas, seconds: Int) {
        paint.color = clockColors.secondHandColor
        val angle = (START_ANGLE + seconds * Math.PI / 30).toFloat()

        paint.strokeWidth = secondHandEndLineWidth
        canvas.drawLine(
            centerX - cos(angle) * secondHandStart,
            centerY - sin(angle) * secondHandStart,
            centerX + cos(angle) * -secondHandMiddle,
            centerY + sin(angle) * -secondHandMiddle,
            paint
        )
        paint.strokeWidth = secondHandBaseLineWidth

        canvas.drawLine(
            centerX - cos(angle) * secondHandMiddle,
            centerY - sin(angle) * secondHandMiddle,
            centerX + cos(angle) * secondHandEnd,
            centerY + sin(angle) * secondHandEnd,
            paint
        )
    }

    private fun drawHands(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        hour = if (hour > 12) hour - 12 else hour
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        drawHourHand(canvas, hour + minute / 60f)
        drawMinuteHand(canvas, minute)
        drawSecondHand(canvas, second)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultWidth = (DEFAULT_WIDTH_IN_DP * resources.displayMetrics.density).toInt()
        val defaultHeight = (DEFAULT_HEIGHT_IN_DP * resources.displayMetrics.density).toInt()

        val widthToSet = resolveSize(defaultWidth, widthMeasureSpec)
        val heightToSet = resolveSize(defaultHeight, heightMeasureSpec)

        setMeasuredDimension(widthToSet, heightToSet)
    }

    override fun onDraw(canvas: Canvas) {
        drawClockBack(canvas)
        drawClockBorder(canvas)
        drawSecondDots(canvas)
        drawDial(canvas)
        drawHands(canvas)
        postInvalidateDelayed(REFRESH_PERIOD)
        super.onDraw(canvas)
    }


    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(BUNDLE_KEY_SUPERSTATE, super.onSaveInstanceState())
        bundle.putParcelable(BUNDLE_KEY_COLORS, clockColors)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            if (Build.VERSION.SDK_INT >= 33) {
                superState = state.getParcelable(
                    BUNDLE_KEY_SUPERSTATE, Parcelable::class.java
                )
                clockColors = state.getParcelable(
                    BUNDLE_KEY_COLORS, DefaultClockColors::class.java
                ) ?: DefaultClockColors()
            } else @Suppress("DEPRECATION") {
                superState = state.getParcelable(BUNDLE_KEY_SUPERSTATE)
                clockColors = state.getParcelable(BUNDLE_KEY_COLORS) ?: DefaultClockColors()
            }
        }
        super.onRestoreInstanceState(superState)
    }

    @Parcelize
    data class DefaultClockColors(
        var backColor: Int = Color.WHITE,
        var borderColor: Int = Color.BLACK,
        var dotsColor: Int = Color.BLACK,
        var dialColor: Int = Color.BLACK,
        var hourHandColor: Int = Color.BLACK,
        var minuteHandColor: Int = Color.BLACK,
        var secondHandColor: Int = Color.BLACK
    ) : Parcelable
}