package com.patatos.sac.kaabii

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.preference.PreferenceManager
import android.service.wallpaper.WallpaperService
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.SurfaceHolder

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MyWallpaperEngine()
    }

    private inner class MyWallpaperEngine : Engine() {

        private val handler = Handler()
        private val updateThread = Runnable { this.update() }
        private val boredThread = Runnable {
            val off = this.size / 2

            this.moveTo(
                off + Math.random().toFloat() * (this.width - off) - this.width / 2,
                off + Math.random().toFloat() * (this.height - off) - this.height / 2,
                (Math.random() * this.stepCount).toInt() + 2
            )

            this.delay(250)

            for (k in 2..4)
                if (Math.random() < .5) {
                    this.moveTo(
                        this.x + Math.random().toFloat() * off,
                        this.y + Math.random().toFloat() * off,
                        (Math.random() * this.stepCount / k).toInt() + 1
                    )
                    this.delay(250)
                } else break

            this.boring = false
        }

        private val brushes = arrayOf(
            this.fill(R.color.colorPrimary),
            this.fill(R.color.colorPrimaryDark),
            this.fill(R.color.colorSecondary),
            this.fill(R.color.colorSecondaryDark),
            this.fill(R.color.colorAccent),
            this.stroke(R.color.colorPrimaryDark)
        )

        private var visible = true
        private var boring = false

        private var stepCount: Int
        private var enableTouch: Boolean
        private var distanceCoefficient: Float
        private var sizeCoefficient: Float
        private var translateCoefficient: Float
        private var scaleCoefficient: Float
        private var skewCoefficient: Float
        private var enableBored: Boolean
        private var boredMillis: Long

        private var width = 0f
        private var height = 0f

        private var distance = 0f
        private var size = 0f

        private var x = 0f
        private var y = 0f

        init {
            PreferenceManager.getDefaultSharedPreferences(this@MyWallpaperService).also {
                this.stepCount = it.getString(this@MyWallpaperService.getString(R.string.step_count_key), "8")!!.toInt()
                this.enableTouch = it.getBoolean(this@MyWallpaperService.getString(R.string.enable_touch_key), true)
                this.distanceCoefficient = it.getString(this@MyWallpaperService.getString(R.string.distance_coefficient_key), "1.5")!!.toFloat()
                this.sizeCoefficient = it.getString(this@MyWallpaperService.getString(R.string.size_coefficient_key), "0.64")!!.toFloat()
                this.translateCoefficient = it.getString(this@MyWallpaperService.getString(R.string.translate_coefficient_key), "0.42")!!.toFloat()
                this.scaleCoefficient = it.getString(this@MyWallpaperService.getString(R.string.scale_coefficient_key), "1")!!.toFloat()
                this.skewCoefficient = it.getString(this@MyWallpaperService.getString(R.string.skew_coefficient_key), "0.24")!!.toFloat()
                this.enableBored = it.getBoolean(this@MyWallpaperService.getString(R.string.enable_bored_key), true)
                this.boredMillis = (it.getString(this@MyWallpaperService.getString(R.string.bored_delay_key), "4.2")!!.toFloat() * 1000).toLong()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible

            if (visible) this.handler.post(this.updateThread)
            else this.removeCallbacks()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)

            this.visible = false
            this.removeCallbacks()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)

            this.width = 1f * w
            this.height = 1f * h

            Math.min(w, h).also {
                this.distance = this.distanceCoefficient * it
                this.size = this.sizeCoefficient * it
            }
        }

        override fun onTouchEvent(event: MotionEvent) {
            if (this.enableTouch) {
                this.moveTo(
                    event.x - this.width / 2,
                    event.y - this.height / 2,
                    (Math.random() * this.stepCount).toInt() + 2
                )

                if (this.boring) {
                    this.handler.removeCallbacks(this.boredThread)
                    this.boring = false
                }

                super.onTouchEvent(event)
            }
        }

        private fun fill(@ColorRes id: Int): Paint {
            return Paint().let {
                it.isAntiAlias = true
                it.color = ContextCompat.getColor(this@MyWallpaperService, id)
                it
            }
        }

        private fun stroke(@ColorRes id: Int): Paint {
            return Paint().let {
                it.isAntiAlias = true
                it.color = ContextCompat.getColor(this@MyWallpaperService, id)
                it.style = Paint.Style.STROKE
                it.strokeWidth = .01f
                it
            }
        }

        private fun ease(a: Float, b: Float, k: Int, n: Int): Float {
            return a + (b - a) * k / n
        }

        private fun moveTo(px:  Float, py: Float, steps: Int) {
            val sx = this.x
            val sy = this.y

            for (k in 0..steps)
                this.target(this.ease(sx, px, k + 1, steps), this.ease(sy, py, k + 1, steps))
        }

        private fun delay(millis: Long) {
            try { Thread.sleep(millis) }
            catch (e: InterruptedException) { }
        }

        private fun removeCallbacks() {
            this.handler.removeCallbacks(this.updateThread)
            this.handler.removeCallbacks(this.boredThread)
            this.boring = false
        }

        private fun target(px: Float, py: Float) {
            this.x = px
            this.y = py

            this.update()
        }

        private fun update() {
            val holder = this.surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(ContextCompat.getColor(this@MyWallpaperService, R.color.colorAccentDark))

                    canvas.translate(this.width / 2, this.height / 2)
                    canvas.scale(this.size, this.size)

                    this.draw(canvas, this.x, this.y)
                }
            } catch (e: Exception) {
                this.removeCallbacks()
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
                return
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }

            this.handler.removeCallbacks(this.updateThread)

            if (this.visible)
                this.handler.postDelayed(this.updateThread, 5000)

            if (this.enableBored && !this.boring) {
                this.handler.removeCallbacks(this.boredThread)
                this.handler.postDelayed(this.boredThread, ((.5 + Math.random()) * this.boredMillis).toLong())
                this.boring = true
            }
        }

        private fun draw(canvas: Canvas, px: Float, py: Float) {
            canvas.drawCircle(0f, 0f, .5f, this.brushes[1])
            canvas.drawCircle(0f, 0f, .44f, this.brushes[0])

            val rx = Math.atan(1.0 * px / this.distance)
            val ry = Math.atan(1.0 * py / this.distance)

            val satX = Math.sin(rx).toFloat()
            val satY = Math.sin(ry).toFloat()
            val catX = Math.cos(rx).toFloat()
            val catY = Math.cos(ry).toFloat()

            canvas.skew(-this.skewCoefficient * (rx * ry).toFloat(), 0f)
            canvas.translate(this.translateCoefficient * satX, this.translateCoefficient * satY)
            canvas.scale(this.scaleCoefficient * catX, this.scaleCoefficient * catY)

            this.ellipse(canvas, -.11f, -.14f, .06f, .14f, this.brushes[1])
            this.ellipse(canvas, -.11f, -.21f, .03f, .05f, this.brushes[4])
            this.ellipse(canvas, .11f, -.14f, .06f, .14f, this.brushes[1])
            this.ellipse(canvas, .11f, -.21f, .03f, .05f, this.brushes[4])

            this.ellipse(canvas, -.23f, -.03f, .07f, .05f, this.brushes[2])
            this.ellipse(canvas, -.23f, -.03f, .06f, .04f, this.brushes[3])
            this.ellipse(canvas, .23f, -.03f, .07f, .05f, this.brushes[2])
            this.ellipse(canvas, .23f, -.03f, .06f, .04f, this.brushes[3])

            canvas.drawPath(Path().let {
                it.moveTo(-.06f, .04f)
                it.cubicTo(-.04f, .09f, .04f, .09f, .06f, .04f)
                it
            }, this.brushes[5])
            canvas.drawCircle(-.06f, .04f, .01f, this.brushes[1])
            canvas.drawCircle(.06f, .04f, .01f, this.brushes[1])
        }

        private fun ellipse(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, paint: Paint) {
            canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, paint)
        }

    }

}
