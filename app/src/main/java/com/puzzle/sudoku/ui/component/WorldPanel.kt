package com.puzzle.sudoku.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.puzzle.sudoku.service.TouchEventService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class WorldPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope {
    companion object {
        const val TAG = "WorldPanel"
    }

    private var job: Job? = null

    // CoroutineScope context
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private val circlePaint = Paint().apply {
        color = Color.BLUE
    }
    private val itemPaint = Paint()
    private val boundaryPaint = Paint().apply {
        color = Color.argb(60, 128, 128, 128)
    }
    private val linePaint = Paint().apply {
        val configStrokeWidth = 4F
        val configDashLength = 10F
        val configGapLength = 5F
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = configStrokeWidth
        pathEffect = DashPathEffect(floatArrayOf(configDashLength, configGapLength), 0F)
    }
    private val textPaint = TextPaint().apply {
        color = Color.rgb(10, 161, 27)
        isAntiAlias = true
        textSize = 48F
        typeface = Typeface.DEFAULT_BOLD
    }


    val circleRadius = 50F
    var center = Pair(50F, 50F)
    val boundary by lazy { floatArrayOf(circleRadius, circleRadius, width - circleRadius, height - circleRadius) }
    var vector = Pair(5F, 135) // (magnitude, angle)
    val good by lazy { MutableList(5) { Pair(Random.nextInt(circleRadius.toInt(), width - circleRadius.toInt()).toFloat(), Random.nextInt(circleRadius.toInt(), height - circleRadius.toInt()).toFloat()) } }
    val bad by lazy { MutableList(5) { Pair(Random.nextInt(circleRadius.toInt(), width - circleRadius.toInt()).toFloat(), Random.nextInt(circleRadius.toInt(), height - circleRadius.toInt()).toFloat()) } }

    var score = 50

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawColor(Color.WHITE)
//        canvas.drawRect(boundary[0], boundary[1], boundary[2], boundary[3], boundaryPaint)
//        canvas.drawLines(
//            floatArrayOf(
//                width / 2F, 0F, width / 2F, height.toFloat(),
//                0F, height / 2F, width.toFloat(), height / 2F
//            ), linePaint
//        )
        val scoreInfo = "Score: $score"
        canvas.drawText(scoreInfo, width - textPaint.measureText(scoreInfo) - 8F, height.toFloat() - 8F, textPaint)

        good.forEach {
            itemPaint.color = Color.argb(255, 218, 165, 32)
            canvas.drawCircle(it.first, it.second, 10F, itemPaint)
        }

        bad.forEach {
            itemPaint.color = Color.RED
            canvas.drawCircle(it.first, it.second, 10F, itemPaint)
        }

        canvas.drawCircle(center.first, center.second, 50F, circlePaint)
    }

    fun start() {
        job = launch {
            Log.d(TAG, "boundary: ${boundary.toList()}")
            while (true) {
                if (center.first !in boundary[0]..boundary[2]) {
                    Log.d(TAG, "abnormal - center: $center")
                    break
                }
                if (center.second !in boundary[1]..boundary[3]) {
                    Log.d(TAG, "abnormal - center: $center")
                    break
                }
                val updatedX = center.first + vector.first * sin(vector.second.toRadians())
                val updatedY = center.second - vector.first * cos(vector.second.toRadians())
                if (updatedX in boundary[0]..boundary[2] && updatedY in boundary[1]..boundary[3]) {
                    center = Pair(updatedX, updatedY)
                } else {
                    Log.d(TAG, "collision at center: $center")
                    val angle = when {
                        updatedX < boundary[2] / 2 && updatedY < boundary[3] / 2 -> Random.nextInt(90, 180)
                        updatedX > boundary[2] / 2 && updatedY < boundary[3] / 2 -> Random.nextInt(180, 270)
                        updatedX > boundary[2] / 2 && updatedY > boundary[3] / 2 -> Random.nextInt(270, 360)
                        updatedX < boundary[2] / 2 && updatedY > boundary[3] / 2 -> Random.nextInt(0, 90)
                        else -> Random.nextInt(0, 360)
                    }
//                    Log.d(TAG, "angle: $angle")
                    vector = Pair(vector.first, angle)
                    center = Pair(center.first + vector.first * sin(vector.second.toRadians()), center.second - vector.first * cos(vector.second.toRadians()))
                }

                good.filter { calculateDistance(it, center) < circleRadius }.forEach {
                    TouchEventService.instance?.simulateTap(it.first, it.second)
                    score++
                    good.remove(it)
                    good.add(Pair(Random.nextInt(circleRadius.toInt(), width - circleRadius.toInt()).toFloat(), Random.nextInt(circleRadius.toInt(), height - circleRadius.toInt()).toFloat()))
                }

                bad.filter { calculateDistance(it, center) < circleRadius }.forEach {
                    TouchEventService.instance?.simulateTap(it.first, it.second)
                    score--
                    bad.remove(it)
                    bad.add(Pair(Random.nextInt(circleRadius.toInt(), width - circleRadius.toInt()).toFloat(), Random.nextInt(circleRadius.toInt(), height - circleRadius.toInt()).toFloat()))
                }
                Log.d(TAG, "score: $score")

                if (score == 0 || score == 100) {
                    break
                }

                invalidate()
                delay(1000 / 60)
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (event.rawX in boundary[0]..boundary[2] && event.rawY in boundary[1]..boundary[3]) {
                    center = Pair(event.rawX, event.rawY)
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {

            }
        }
        return true
    }

    fun Int.toRadians(): Float {
        return this * (PI / 180).toFloat()
    }

    fun calculateDistance(pointA: Pair<Float, Float>, pointB: Pair<Float, Float>): Float {
        return sqrt(((pointB.first - pointA.first) * (pointB.first - pointA.first) + (pointB.second - pointA.second) * (pointB.second - pointA.second))).toFloat()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel() // Cancel the coroutine job
        job = null
    }
}