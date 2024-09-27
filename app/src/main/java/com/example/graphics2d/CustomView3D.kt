package com.example.graphics2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

//use https://www.coursera.org/learn/intro-android-graphics/supplement/Hs2Ti/quaternion-multiplication-and-rotational-matrix
//to solve gimbal lock problem we use Quaternion multiplication and rotational matrix

class CustomView3D @JvmOverloads constructor(
    contextParam: Context,
    attr: AttributeSet,
    defStyleRes: Int = 0
) : View(contextParam, attr, defStyleRes)  {
    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG) //paint object for drawing the lines
    private var cube_vertices: MutableList<Coordinate>//the vertices of a 3D cube
    private var draw_cube_vertices: List<Coordinate> //the vertices for drawing a 3D cube

    private var angle = 0
    private var moveX = width
    init {
        redPaint.style = Paint.Style.STROKE //Stroke
        redPaint.color = Color.RED
        redPaint.strokeWidth = 2f

        //create a 3D cube
        cube_vertices = mutableListOf()
        cube_vertices.add( Coordinate(-1.0, -1.0, -1.0, 1.0))
        cube_vertices.add(Coordinate(-1.0, -1.0, 1.0, 1.0))
        cube_vertices.add(Coordinate(-1.0, 1.0, -1.0, 1.0))
        cube_vertices.add(Coordinate(-1.0, 1.0, 1.0, 1.0))
        cube_vertices.add(Coordinate(1.0, -1.0, -1.0, 1.0))
        cube_vertices.add(Coordinate(1.0, -1.0, 1.0, 1.0))
        cube_vertices.add(Coordinate(1.0, 1.0, -1.0, 1.0))
        cube_vertices.add(Coordinate(1.0, 1.0, 1.0, 1.0))
        draw_cube_vertices = cube_vertices
//        draw_cube_vertices = translate(cube_vertices, 2.0, 2.0, 2.0)
//        draw_cube_vertices = scale(draw_cube_vertices, 40.0, 40.0, 40.0)
//        draw_cube_vertices = translate(draw_cube_vertices, 200.0, 200.0, 40.0)
        // left=-1, right= 1, top=-1, bottom= 1, near=1 and far =1.1
        //aspectRation = (right - left) / (top - bottom)
//        draw_cube_vertices = perspectiveTransformation(draw_cube_vertices, -1.0,  PI/4, 1.1, 1.0)


        runInScope()

    }

    private fun runInScope(){
        CoroutineScope(Dispatchers.Main).launch{
            var positionX = 2.0
            var direction = 1.0
            while (true){
                angle += 10
                delay(100)
//                if (positionX + 2 >= width/40 ){ //divide by scale factor = 40
//                    direction = -1.0
//                }else if(positionX <= 2){
//                    direction = 1.0
//                }
//                positionX += direction
//                draw_cube_vertices = translate(cube_vertices, positionX, 2.0, 0.0)
                draw_cube_vertices = translate(cube_vertices, 0.0, 0.0, 0.0)
                draw_cube_vertices = scale(draw_cube_vertices, 40.0, 40.0, 40.0)
                draw_cube_vertices = rotateX(draw_cube_vertices, PI/180 * angle)
                draw_cube_vertices = rotateY(draw_cube_vertices, PI/180 * 45)
                draw_cube_vertices = rotateZ(draw_cube_vertices, PI/180 * 25)
                draw_cube_vertices = translate(draw_cube_vertices, width/2.0, height/2.0, 0.0)
//                draw_cube_vertices = shear(draw_cube_vertices, 2.0, 1.0)
                invalidate()
            }
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCube(canvas)
    }
    private fun drawLinePairs(
        canvas: Canvas,
        vertices: List<Coordinate>,
        start: Int,
        end: Int,
        paint: Paint
    ) {
        //draw a line connecting 2 points
        //canvas - canvas of the view
        //points - array of points
        //start - index of the starting point
        //end - index of the ending point
        //paint - the paint of the line
        canvas.drawLine(
            vertices[start].x.toInt().toFloat(),
            vertices[start].y.toInt().toFloat(),
            vertices[end].x.toInt()
                .toFloat(),
            vertices[end].y.toInt().toFloat(),
            paint
        )
    }

    private fun drawCube(canvas: Canvas) { //draw a cube on the screen
        drawLinePairs(canvas, draw_cube_vertices, 0, 1, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 1, 3, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 3, 2, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 2, 0, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 4, 5, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 5, 7, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 7, 6, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 6, 4, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 0, 4, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 1, 5, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 2, 6, redPaint)
        drawLinePairs(canvas, draw_cube_vertices, 3, 7, redPaint)
    }

    //*********************************
    //matrix and transformation functions
    private fun getIdentityMatrix(): Array<Array<Double>> { //return an 4x4 identity matrix
        val matrix = Array(4){ row -> Array<Double>(4){ col->
            if(row == col) 1.0
            else 0.0
        }}
        return matrix
    }

    private fun transformation(
        vertex: Coordinate,
        matrix: Array<Array<Double>>
    ): Coordinate { //affine transformation with homogeneous coordinates
        //i.e. a vector (vertex) multiply with the transformation matrix
        // vertex - vector in 3D
        // matrix - transformation matrix
        val x = matrix[0][0] * vertex.x + matrix[0][1] * vertex.y + matrix[0][2] * vertex.z + matrix[0][3]
        val y = matrix[1][0] * vertex.x + matrix[1][1] * vertex.y + matrix[1][2] * vertex.z + matrix[1][3]
        val z = matrix[2][0] * vertex.x + matrix[2][1] * vertex.y + matrix[2][2] * vertex.z + matrix[2][3]
        val w = matrix[3][0] * vertex.x + matrix[3][1] * vertex.y + matrix[3][2] * vertex.z + matrix[3][3]
        return Coordinate(x,y,z,w)
    }

    private fun transformation(
        vertices: List<Coordinate>,
        matrix: Array<Array<Double>>
    ): List<Coordinate> {
        //Affine transform a 3D object with vertices
        // vertices - vertices of the 3D object.
        // matrix - transformation matrix
        val result = mutableListOf<Coordinate>()
        for (vertex in vertices) {
            val coordinate = transformation(vertex, matrix)
            coordinate.normalise()
            result.add(coordinate)
        }
        return result
    }

    //***********************************************************
    //Affine transformation
    private fun translate(
        vertices: List<Coordinate>,
        tx: Double,
        ty: Double,
        tz: Double
    ): List<Coordinate> {
        val matrix = getIdentityMatrix()
        matrix[0][3] = tx
        matrix[1][3] = ty
        matrix[2][3] = tz
        return transformation(vertices, matrix)
    }

    private fun scale(
        vertices: List<Coordinate>,
        sx: Double,
        sy: Double,
        sz: Double
    ): List<Coordinate> {
        val matrix = getIdentityMatrix()
        matrix[0][0] = sx
        matrix[1][1] = sy
        matrix[2][2] = sz
        return transformation(vertices, matrix)
    }

    private fun shear(
        vertex: List<Coordinate>,
        hx: Double,
        hy: Double
    ): List<Coordinate>{
        val matrix = getIdentityMatrix()
        matrix[0][2] = hx
        matrix[1][2] = hy
        return transformation(vertex, matrix)
    }

    //angle should be in radian
    private fun rotateX(vertices: List<Coordinate>, angle: Double): List<Coordinate>{
        val matrix = getIdentityMatrix()
        matrix[1][1] = cos(angle); matrix[1][2] = -sin(angle)
        matrix[2][1] = sin(angle); matrix[2][2] = cos(angle)
        return transformation(vertices, matrix)
    }
    //angle should be in radian
    private fun rotateY(vertices: List<Coordinate>, angle: Double): List<Coordinate>{
        val matrix = getIdentityMatrix()
        matrix[0][0] = cos(angle); matrix[0][2] = sin(angle)
        matrix[2][0] = -sin(angle); matrix[2][2] = cos(angle)
        return transformation(vertices, matrix)
    }
    //angle should be in radian
    private fun rotateZ(vertices: List<Coordinate>, angle: Double): List<Coordinate>{
        val matrix = getIdentityMatrix()
        matrix[0][0] = cos(angle); matrix[0][1] = -sin(angle)
        matrix[1][0] = sin(angle); matrix[1][1] = cos(angle)
        return transformation(vertices, matrix)
    }

    private fun rotate(rotateX: Array<Array<Double>>, rotateY: Array<Array<Double>>, rotateZ: Array<Array<Double>>){

    }

    private fun getQuaternionMatrix(w: Double, x: Double, y: Double, z: Double): Array<Array<Double>>{
        val m = getIdentityMatrix()
        m[0][0] = w * w + x * x - y * y - z * z
        m[0][1] = 2 * x * y - 2 * w * z
        m[0][2] = 2 * x * z + 2 * w * y

        m[1][0] = 2 * x * y + 2 * w * z
        m[1][1] =  w * w - x * x + y * y - z * z
        m[1][2] = 2 * y * z - 2 * w * x

        m[2][0] = 2 * x * z - 2 * w * y
        m[2][1] = 2 * y * z + 2 * w * x
        m[2][2] = w * w - x * x - y * y + z * z

        return m
    }

    //Mp
    //need to be multiplied by 1/z * getPerspectiveMatrix to get the depth for camera
    private fun getProspectiveTransformationMatrix(near: Double): Array<Array<Double>>{
        val matrix = getIdentityMatrix()
        matrix[0][0] = near
        matrix[1][1] = near
        matrix[3][2] = -1.0
        return matrix
    }

    //Ms
    //sX = 2 / (right - left) and sY = 2/ (top - bottom)
    private fun getScaleTransformationMatrix(sX: Double, sY: Double): Array<Array<Double>>{
        val matrix = getIdentityMatrix()
        matrix[0][0] = sX
        matrix[1][1] = sY
        return matrix
    }

    //Mz
    // c1 = 2 * far * near / (far - near)
    // c2 = (far + near) / (far - near)
    private fun getDepthTransformationMatrix(c1: Double, c2: Double): Array<Array<Double>>{
        val matrix = getIdentityMatrix()
        matrix[2][2] = -c2
        matrix[2][3] = c1
        matrix[3][2] = -1.0
        return matrix
    }

    //Ms X Mp X Mz
    //aspectRatio = (right - left) / (top - bottom)
    private fun perspectiveTransformation(
        vertices: List<Coordinate>,
        aspectRation: Double,
        angle: Double,
        far: Double,
        near: Double
    ): List<Coordinate> {
        val matrix = getIdentityMatrix()
        matrix[0][0] = 1.0 / (aspectRation * tan(angle / 2))
        matrix[1][1] = 1.0 / tan(angle / 2)
        matrix[2][2] = -(far + near) / (far - near); matrix[2][3] = -(2 * far * near) / (far - near)
        matrix[3][2] = -1.0
        return transformation(vertices, matrix)
    }
}

//*********************************************
//* Homogeneous coordinate in 3D space
data class Coordinate(
    var x: Double,
    var y: Double,
    var z: Double,
    var w: Double
) {
    fun normalise() { //to keep it as a homogeneous coordinate -> divide the coordinate with w and set w=1
        if (w != 0.0) { //ensure that w!=0
            x /= w
            y /= w
            z /= w
            w = 1.0
        } else w = 1.0
    }
}