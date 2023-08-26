package ru.fivegst.speedtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.*
import kotlin.random.Random

class Wave(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private var normalizedSpeed = 0f
    private var redrawJob: Job? = null

    private val rotationMatrix: Matrix by lazy {
        Matrix().apply {
            setRotate(180f, width.toFloat() / 2, height.toFloat() / 2)
        }
    }

    private val topBackgroundHarmonics = _root_ide_package_.ru.fivegst.speedtest.Wave.HarmonicSum()
    private val bottomBackgroundHarmonics =
        _root_ide_package_.ru.fivegst.speedtest.Wave.HarmonicSum()
    private val topForegroundHarmonics = _root_ide_package_.ru.fivegst.speedtest.Wave.HarmonicSum()
    private val bottomForegroundHarmonics =
        _root_ide_package_.ru.fivegst.speedtest.Wave.HarmonicSum()

    init {
        paint.strokeWidth = 1f
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
    }

    fun start() {
        stop()
        redrawJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000 / _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.FPS)
                this@Wave.postInvalidate()
            }
        }
    }

    fun stop() {
        redrawJob?.let {
            runBlocking {
                it.cancel()
                it.cancelAndJoin()
            }
        }
    }

    private inline fun buildFunctionPath(path: Path = Path(), f: (Float) -> Float): Path {
        val points = (0..width).map { it.toFloat() to f(_root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_X * it.toFloat() / width) }

        if (path.isEmpty) {
            path.moveTo(points[0].first, points[0].second)
        }
        points.forEach { (x, y) -> path.lineTo(x, y) }
        return path
    }

    private fun drawHarmonics(canvas: Canvas, alpha: Int, top: _root_ide_package_.ru.fivegst.speedtest.Wave.HarmonicSum, bottom: _root_ide_package_.ru.fivegst.speedtest.Wave.HarmonicSum) {
        paint.alpha = alpha
        top.update(normalizedSpeed)
        bottom.update(normalizedSpeed)
        val path = buildFunctionPath(f = top)
        path.transform(rotationMatrix)
        canvas.drawPath(buildFunctionPath(path, bottom), paint)
    }

    public override fun onDraw(canvas: Canvas) {
        drawHarmonics(canvas,
            _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.BACKGROUND_ALPHA, topBackgroundHarmonics, bottomBackgroundHarmonics)
        drawHarmonics(canvas,
            _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.FOREGROUND_ALPHA, topForegroundHarmonics, bottomForegroundHarmonics)
    }

    fun attachSpeed(speed: Int) {
        normalizedSpeed = log2(speed.toFloat() + 1) * _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.NORMALIZED_SPEED_SCALE
    }

    fun attachColor(color: Int) {
        paint.color = color
    }

    private class Harmonic(
        val amplitude: Float,
        var frequency: Float,
        var initialPhase: Float,
        var amplitudeCyclicScale: Float = 0f,
    ): (Float) -> Float {
        val amplitudeScale: Float
            get() = amplitudeCyclicScale.mod(_root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_AMPLITUDE_SCALE * 4)
                .minus(_root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_AMPLITUDE_SCALE * 2)
                .absoluteValue
                .minus(_root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_AMPLITUDE_SCALE)

        override fun invoke(p1: Float): Float {
            return amplitudeScale * amplitude * sin(frequency * p1 + initialPhase)
        }
    }

    private class HarmonicSum : (Float) -> Float {
        private val harmonics = _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.FREQUENCIES.map { frequency ->
            _root_ide_package_.ru.fivegst.speedtest.Wave.Harmonic(
                _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_AMPLITUDE,
                frequency,
                Random.nextFloat() * _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_STARTING_INITIAL_PHASE,
                Random.nextFloat() * _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_AMPLITUDE_SCALE * 4
            )
        }

        override fun invoke(p1: Float): Float {
            return harmonics.sumOf {
                it(p1).toDouble() + it.amplitude * _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.OFFSET_AMPLITUDE_SCALE
            }.toFloat()
        }

        fun update(normalizedSpeed: Float) {
            harmonics.forEachIndexed { index, harmonic ->
                harmonic.amplitudeCyclicScale += _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.AMPLITUDE_CYCLIC_SCALE_STEP
                    .times(normalizedSpeed + _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.AMPLITUDE_CYCLIC_SCALE_STEP_MIN_SCALE)
                if (abs(harmonic.amplitudeScale) < _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MUTATION_AMPLITUDE_SCALE_THRESHOLD) {
                    harmonic.initialPhase += Random.nextFloat()
                        .times(_root_ide_package_.ru.fivegst.speedtest.Wave.Companion.MAX_INITIAL_PHASE_STEP)
                        .times(if (index % 2 == 0) -1 else 1)
                    harmonic.frequency = _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.FREQUENCIES[index]
                        .plus(Random.nextFloat() * _root_ide_package_.ru.fivegst.speedtest.Wave.Companion.FREQUENCY_SEGMENT_LENGTH)
                        .minus(_root_ide_package_.ru.fivegst.speedtest.Wave.Companion.FREQUENCY_SEGMENT_LENGTH / 2)
                        .times(normalizedSpeed)
                }
            }
        }
    }

    companion object {
        private const val FPS = 30L
        private const val MAX_AMPLITUDE = 13f
        private val FREQUENCIES = List(8) { 1 - it.toFloat() / 10 }
        private const val MAX_STARTING_INITIAL_PHASE = 5f
        private const val MAX_AMPLITUDE_SCALE = 1f
        private const val MUTATION_AMPLITUDE_SCALE_THRESHOLD = 0.08f
        private const val AMPLITUDE_CYCLIC_SCALE_STEP = 0.05f
        private const val MAX_INITIAL_PHASE_STEP = 0.4f
        private const val FREQUENCY_SEGMENT_LENGTH = 0.08f
        private const val MAX_X = 25f
        private const val BACKGROUND_ALPHA = 128
        private const val FOREGROUND_ALPHA = 255
        private const val OFFSET_AMPLITUDE_SCALE = 0.5f
        private const val NORMALIZED_SPEED_SCALE = 0.2f
        private const val AMPLITUDE_CYCLIC_SCALE_STEP_MIN_SCALE = 1f
    }
}
