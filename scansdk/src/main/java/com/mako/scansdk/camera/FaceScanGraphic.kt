package com.mako.scansdk.camera

import android.graphics.*
import com.google.mlkit.vision.face.Face

/**
 * Class draws face contour and check the face position
 * @param overlay {@link GraphicOverlay}
 * @param face {@link Face} data recognized by ML
 * @param imageRect {@link Rect}
 * @param facePositioningListener {@link FacePositionListener} face position listener
 */
internal class FaceScanGraphic(
    private val overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private val facePositioningListener: FacePositionListener?
) : GraphicOverlay.Graphic(overlay) {

    private var paint: Paint = Paint()
    private var contourPaint: Paint = Paint()
    private val messageBackground = Paint()
    private val messagePaint = Paint()

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        paint.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)

        contourPaint.color = Color.CYAN
        contourPaint.style = Paint.Style.STROKE
        contourPaint.strokeWidth = BOX_STROKE_WIDTH

        messageBackground.color = Color.BLACK

        messagePaint.apply {
            color = Color.WHITE
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun draw(canvas: Canvas?) {
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
        drawContour(canvas)

        canvas?.let {
            val height = it.height / 3
            val width = it.width / 2
            val ovalRect = RectF(
                width - 350f,
                height - 500f,
                width + 350f,
                height + 500f
            )
            it.drawOval(ovalRect, paint)

            drawMessage(rect, ovalRect, canvas)
        }
    }

    private fun drawContour(canvas: Canvas?){
        val contours = face.allContours
        val path = Path()
        contours.forEach {
            it.points.forEachIndexed { index, pointF ->
                if (index == 0) {
                    path.moveTo(
                        translateX(pointF.x),
                        translateY(pointF.y)
                    )
                }
                path.lineTo(
                    translateX(pointF.x),
                    translateY(pointF.y)
                )
            }
            canvas?.drawPath(path, contourPaint)
        }
    }

    private fun drawMessage(
        rect: RectF,
        ovalRect: RectF,
        canvas: Canvas
    ) {
        val isFaceInTheOval = rect.left > ovalRect.left &&
                rect.top > ovalRect.top &&
                rect.bottom < ovalRect.bottom &&
                rect.right < ovalRect.right

        facePositioningListener?.isPositionCorrect(
            isFaceInTheOval
        )

        val text = if (isFaceInTheOval) {
            messagePaint.color = Color.GREEN
            "The face position is correct"
        } else {
            messagePaint.color = Color.parseColor("#FF8000")
            "Please position your face in the oval"
        }

        val textRect = Rect()
        messagePaint.getTextBounds(text, 0, text.length, textRect)

        val textYPosition = ovalRect.bottom + 100
        canvas.drawText(
            text, canvas.width / 2f - textRect.width() / 2, textYPosition, messagePaint
        )
        messagePaint.setShadowLayer(20f, 3f, 3f, Color.BLACK);
        canvas.drawText(
            text, canvas.width / 2f - textRect.width() / 2, textYPosition, messagePaint
        )
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}