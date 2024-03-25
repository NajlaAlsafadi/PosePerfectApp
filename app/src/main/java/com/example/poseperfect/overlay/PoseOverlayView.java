package com.example.poseperfect.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class PoseOverlayView extends View {
    private Pose pose;
    private Paint linePaint;
    private int imageWidth;
    private int imageHeight;

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    public void updatePose(Pose pose, int imageWidth, int imageHeight) {
        this.pose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }
    private float calculateAspectRatioFit(float sourceWidth, float sourceHeight, float destWidth, float destHeight) {
        float ratio = Math.min(destWidth / sourceWidth, destHeight / sourceHeight);
        return ratio;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null) return;

        float sourceWidth = imageWidth;
        float sourceHeight = imageHeight;

        float destWidth = getWidth();
        float destHeight = getHeight();

        float ratio = calculateAspectRatioFit(sourceWidth, sourceHeight, destWidth, destHeight);

        float widthScale = ratio;
        float heightScale = ratio;


        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (!landmarks.isEmpty()) {

            drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, widthScale, heightScale);

            // Left arm
            drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, widthScale, heightScale);

            // Right arm
            drawLine(canvas, pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, widthScale, heightScale);

            // Body to legs
            drawLine(canvas, pose, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, widthScale, heightScale);
            drawLine(canvas, pose, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, widthScale, heightScale);
        }
    }

    private void drawLine(Canvas canvas, Pose pose, int startLandmarkId, int endLandmarkId, float widthScale, float heightScale) {
        PoseLandmark startLandmark = pose.getPoseLandmark(startLandmarkId);
        PoseLandmark endLandmark = pose.getPoseLandmark(endLandmarkId);

        if (startLandmark != null && endLandmark != null) {
            float startX = startLandmark.getPosition().x * widthScale;
            float startY = startLandmark.getPosition().y * heightScale;
            float endX = endLandmark.getPosition().x * widthScale;
            float endY = endLandmark.getPosition().y * heightScale;

            canvas.drawLine(startX, startY, endX, endY, linePaint);
        }
    }
}
