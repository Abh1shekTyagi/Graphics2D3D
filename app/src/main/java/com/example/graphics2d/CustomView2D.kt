package com.example.graphics2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CustomView2D @JvmOverloads constructor(
    contextParam: Context,
    attr: AttributeSet,
    defStyleRes: Int = 0
) : View(contextParam, attr, defStyleRes) {

    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var lineGraph = Path()
    private val plotData = listOf(11,29, 10,20,12,5,31,24,21,13)
    private val gradient: LinearGradient

    private val width = resources.displayMetrics.widthPixels
    private val height = resources.displayMetrics.heightPixels + 70

    private val points = listOf(
        PointF(50f, 300f),
        PointF(150f, 400f),
        PointF(180f, 340f),
        PointF(240f, 420f),
        PointF(300f, 200f)
    )

    init {
        redPaint.color = Color.RED
        redPaint.style = Paint.Style.STROKE
        redPaint.strokeWidth = 5f //set to 5 pixels only
        bluePaint.color = Color.BLUE
        bluePaint.style = Paint.Style.STROKE // by default is fill
        bluePaint.strokeWidth = 5f
        greenPaint.setARGB(255, 0, 255, 0)
        greenPaint.style = Paint.Style.FILL_AND_STROKE
        gradient = LinearGradient(
            width / 12f, height / 4f,
            width / 1.2f, height / 4f,
            Color.BLUE,
            Color.RED,
            Shader.TileMode.MIRROR
        )
    }


    //list of draw function on canvas
    //https://developer.android.com/reference/android/graphics/Canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //we can use gradient fill and texture fill(from an image) to the paint as well.
        //we can use matrix to scale, rotate, translate or shear the object without changing it's
        //coordinates

        //translate
        // xti = xi + a //for each x point
        // yti = yi + b //for each y point

        /*
              | xi   |      |   a  |
              |      |  +   |      |    =
              | yi   |      |   b  |

              val points = listof( Point(x0,y0) , Point(x1, y1) , Point(x3, y3))

              it is a good idea to convert the vector into homogeneous coordinates by adding z in point as 1
              and making all these matrices 3D

               translation         point
                1   0   a           xi
                0   1   b           yi
                0   0   1           1
         */

        //rotation
        // xri = xi * cos(theta) - yi * sin(theta)
        // yri = xi * sin(theta) + yi * cos(theta)
        /*      Rotation matrix
                cos(theta)   -sin(theta)
                sin(theta)    cos(theta)

                if this is multiplied with a point we will get above result


                    Rotation                point
                cos     -sin    0           xi
                sin     cos     0           yi
                0       0       1           1
         */

        //scaling
        // xsi = cxi
        // ysi = dyi
        /*  Scaling matrix
                    c   0
                    0   d

                    scale
                   c    0   0
                   0    d   0
                   0    0   1
         */

        //shear
        // xhi = xi + eyi
        // yhi = fxi + yi
        /*  shearing matrix
                1   e
                f   1

                  Shear
                1   e   0
                f   1   0
                0   0   1
         */

        greenPaint.setShader(gradient)
//        canvas.drawRect(0f, 0f, width / 2f, height / 2f, redPaint)
//        canvas.drawCircle(width / 2f, height / 2f, width / 3f, bluePaint)
        path.moveTo(width / 12f, height / 4f)
        path.lineTo(width / 5f, height / 5f)
        path.lineTo(width / 2f, height / 5f)
        path.lineTo(width / 1.2f, height / 4f)
        path.close()//it will merge the start and end point of the path
//        canvas.drawPath(path, greenPaint)
        val newPoint = translatePoints(points,  120f,23f)
        updatePath(newPoint)
        canvas.drawPath(path,greenPaint)
        lineGraph = createLineGraph(listOfPlot = plotData, width,height)
        canvas.drawPath(lineGraph,redPaint)

        // we can use drawArc for creating a pie chart graph
        // we can use drawRect for creating a bar chart
        // we can use drawPath for a line graph eg ecg data

    }

    private fun translatePoints(points: List<PointF>, px: Float, py: Float): List<PointF> {
        /*
               translation
                1   0   px
                0   1   py
                0   0   1
         */
        val translationMatrix = Array(3) { Array<Double>(3) { 0.0 } }
        translationMatrix[0][0] = 1.0 ; translationMatrix[0][1] = 0.0 ; translationMatrix[0][2] = px.toDouble()
        translationMatrix[1][0] = 0.0 ; translationMatrix[1][1] = 1.0 ; translationMatrix[1][2] = py.toDouble()
        translationMatrix[2][0] = 0.0 ; translationMatrix[2][1] = 0.0 ; translationMatrix[2][2] = 1.0

        return affineTransformation(points, translationMatrix)
    }

    private fun affineTransformation(
        points: List<PointF>,
        translationMatrix: Array<Array<Double>>
    ): List<PointF> {
        /*
               translation              point           result
                1   0   px              xi              1 * xi + 0 * yi + px * 1
                0   1   py              yi
                0   0   1               1
         */
        val result = mutableListOf<PointF>()
        for (point in points) {
            val xt =
                translationMatrix[0][0] * point.x + translationMatrix[0][1] * point.y + translationMatrix[0][2]
            val yt =
                translationMatrix[1][0] * point.x + translationMatrix[1][1] * point.y + translationMatrix[1][2]
            result.add(PointF(xt.toFloat(), yt.toFloat()))
        }
        return result
    }

    private fun scalePoints(pointList: List<PointF>, xScale: Double, yScale: Double): List<PointF> {
        /*
                    scale
                   x    0   0
                   0    y   0
                   0    0   1
         */

        val scaleMatrix = Array(3){Array(3){0.0} }
        scaleMatrix[0][0] = xScale ; scaleMatrix[0][1] = 0.0 ; scaleMatrix[0][2] = 0.0
        scaleMatrix[1][0] = 0.0 ; scaleMatrix[1][1] = yScale ; scaleMatrix[1][2] = 0.0
        scaleMatrix[2][0] = 0.0 ; scaleMatrix[2][1] = 0.0 ; scaleMatrix[2][2] = 1.0

        return affineScale(pointList, scaleMatrix)

    }

    private fun affineScale(pointList: List<PointF>,scaleMatrix: Array<Array<Double>> ): List<PointF>{
        /*
                    scale               point           result
                   x    0   0           xi              c * xi + 0 * yi + 0 * 1
                   0    y   0           yi
                   0    0   1           1
         */
        val result = mutableListOf(PointF())
        for (point in pointList){
            val xi = scaleMatrix[0][0] * point.x + scaleMatrix[0][1] * point.y + scaleMatrix[0][2]
            val yi = scaleMatrix[1][0] * point.x + scaleMatrix[1][1] * point.y + scaleMatrix[0][2]
            result.add(PointF(xi.toFloat(),yi.toFloat()))
        }
        return result
    }

    private fun updatePath(points: List<PointF>){
        if(points.isEmpty()) return
        path.reset()
        for ((i, point) in points.withIndex()) {
            if (i == 0) path.moveTo(point.x, point.y)
            else {
                path.lineTo(point.x, point.y)
            }
        }
        path.close()
    }

    private fun createLineGraph(listOfPlot: List<Int>, width: Int, height: Int): Path{
        val result = Path()
        var pointList = mutableListOf<PointF>()
        var minValue = Int.MAX_VALUE
        var maxValue = Int.MIN_VALUE
        for ((i,point) in listOfPlot.withIndex()){
            pointList.add(PointF(i.toFloat(), point.toFloat()))
            minValue = min(minValue, point) ; maxValue = max(maxValue, point)
        }
        pointList = translatePoints(pointList.toList(), 0f,-minValue.toFloat()).toMutableList()
        val xScale = width.toDouble() / pointList.size
        val yScale = height.toDouble() / maxValue - minValue

        pointList = scalePoints(pointList, xScale, yScale).toMutableList()
        result.moveTo(pointList[0].x, pointList[0].y)
        for(point in pointList) result.lineTo(point.x, point.y)
        return result
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        when(event?.action){
            MotionEvent.ACTION_MOVE -> {
                val newPoint = translatePoints(points,  event.x,event.y)
                updatePath(newPoint)
//                invalidate()
            }
        }
        performClick()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}