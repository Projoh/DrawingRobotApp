package edu.usf.carrt.mohamed.drawingboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawPath: Path = Path()
    private var drawPaint: Paint = Paint()
    private var canvasPaint: Paint? = null
    private val paintColor = 0xFF660000
    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private var imageToDraw: ImageToDraw = ImageToDraw()
    private var currentSegment: Segment = Segment()
    private var converter: PointToRobotConverter? = null
    private var drawer: Drawer? = null

    init {
        setupDrawing()
    }

    fun getResult(): String {
        return converter!!.drawPicture(imageToDraw)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()


        this.viewTreeObserver.addOnGlobalLayoutListener {
            converter = PointToRobotConverter(this.height, this.width)

            var test = converter!!.convertToPolarBasedOffScreenSize(864.4f,-530f);
            var sti = "hi";
        }


    }

    public fun setupDrawing() {
        drawPaint.color = paintColor.toInt()
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 20f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
    }

    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
        canvas!!.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        var touchX = event!!.x
        var touchY = event.y
        fun endLine() {
//            drawPath.reset()
            imageToDraw.segments.add(currentSegment)
            currentSegment = Segment()
            currentSegment.color = drawer!!.getCurrentPen()
        }

        fun drawLine() {
            drawPath.lineTo(touchX, touchY)
            currentSegment.lines.add(converter!!.convertToPolarBasedOffScreenSize(touchX, touchY))
        }

        fun startLine() {
            drawPath.moveTo(touchX, touchY)
            currentSegment.lines.add(converter!!.convertToPolarBasedOffScreenSize(touchX, touchY))
        }



        when (event.action) {
            MotionEvent.ACTION_DOWN -> startLine()
            MotionEvent.ACTION_MOVE -> drawLine()
            MotionEvent.ACTION_UP -> endLine()
            else -> return false
        }

        invalidate()
        return true
    }

    fun resetDrawing() {
        drawPath.reset()
        canvasBitmap!!.eraseColor(Color.TRANSPARENT)
        drawCanvas!!.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        currentSegment = Segment()
        currentSegment.color = drawer!!.getCurrentPen()
        imageToDraw = ImageToDraw()


        invalidate()
    }

    fun linkDrawer(drawer: Drawer) {
        this.drawer = drawer
    }
}