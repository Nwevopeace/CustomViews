package com.peacecodes.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import java.util.Collections.min
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int){
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = 8

class DialView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
//    for the circular shape of the circle
    private var radius = 0.0f
//    for the speed of the fan which is set to off by default
    private var fanSpeed = FanSpeed.OFF
//    for the x and why position of the control
    private var pointPosition: PointF = PointF(0.0f, 0.0f)

    private var lowColor = 0
    private var mediumColor = 0
    private var highColor = 0

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 32.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }
    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.DialView){
            lowColor = getColor(R.styleable.DialView_color1,0)
            mediumColor = getColor(R.styleable.DialView_color2,0)
            highColor = getColor(R.styleable.DialView_color3,0)
        }

        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat(){
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(AccessibilityNodeInfo.ACTION_CLICK,
                    context.getString(if (fanSpeed != FanSpeed.HIGH) R.string.change else R.string.reset))
                info?.addAction(customClick)
            }
        })
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()

        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(w,h)/ 2.0*0.8).toFloat()
    }

    private fun PointF.computeXYFanSpeed(position: FanSpeed, radius: Float){
        val startAngle = Math.PI * (9/8.0)
        val angle = startAngle + position.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width/2
        y = (radius * sin(angle)).toFloat() + height/2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        paint.color = if (fanSpeed == FanSpeed.OFF) Color.DKGRAY else Color.YELLOW
        paint.color = when(fanSpeed) {
            FanSpeed.OFF -> Color.DKGRAY
            FanSpeed.LOW -> lowColor
            FanSpeed.MEDIUM -> mediumColor
            FanSpeed.HIGH -> highColor
        }
        canvas?.drawCircle((width/2).toFloat(), (height/2).toFloat(), radius, paint)

        val marker = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYFanSpeed(fanSpeed, marker)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius/20, paint)

        val label = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()){
            pointPosition.computeXYFanSpeed(i, label)
            val labelText = resources.getString(i.label)
            canvas?.drawText(labelText, pointPosition.x, pointPosition.y, paint)
        }
    }
}