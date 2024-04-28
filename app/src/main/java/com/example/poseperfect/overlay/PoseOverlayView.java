package com.example.poseperfect.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private boolean isUsingFrontCamera = false;
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
    private float calculateAspectRatioFit(float sourceWidth, float sourceHeight, float destWidth,
                                          float destHeight) {
        float ratio = Math.min(destWidth / sourceWidth, destHeight / sourceHeight);
        return ratio;
    }
    public void setUsingFrontCamera(boolean isUsingFrontCamera) {
        this.isUsingFrontCamera = isUsingFrontCamera;
        invalidate();  // Redraw the view with the new setting
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null) return;

        // bounding box of the pose
        RectF poseBounds = calculatePoseBoundingBox(pose);
        float poseWidth = poseBounds.width();
        float poseHeight = poseBounds.height();

        // aspect ratio fit based on the bounding box of the pose
        float ratio = calculateAspectRatioFit(poseWidth, poseHeight, getWidth(), getHeight());

        // offset to center the pose in the view
        float offsetX = (getWidth() - (poseWidth * ratio)) / 2 - (poseBounds.left * ratio);
        float offsetY = (getHeight() - (poseHeight * ratio)) / 2 - (poseBounds.top * ratio);

        // If using the front camera, flip the canvas horizontally
        if (isUsingFrontCamera) {
            canvas.scale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
        }

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (!landmarks.isEmpty()) {
            drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, ratio,
                    offsetX, offsetY);
            drawLine(canvas, pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, ratio,
                    offsetX, offsetY);
            drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, ratio,
                    offsetX, offsetY);
            drawLine(canvas, pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, ratio,
                    offsetX, offsetY);

            // Left arm
            drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, ratio,
                    offsetX, offsetY);
            drawLine(canvas, pose, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, ratio,
                    offsetX, offsetY);

            // Right arm
            drawLine(canvas, pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, ratio,
                    offsetX, offsetY);
            drawLine(canvas, pose, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, ratio,
                    offsetX, offsetY);

            // Body To Legs
            drawLine(canvas, pose, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, ratio, offsetX,
                    offsetY);
            drawLine(canvas, pose, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, ratio, offsetX,
                    offsetY);
            drawLine(canvas, pose, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, ratio, offsetX,
                    offsetY);
            drawLine(canvas, pose, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, ratio, offsetX,
                    offsetY);
        }
    }

    private RectF calculatePoseBoundingBox(Pose pose) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        for (PoseLandmark landmark : pose.getAllPoseLandmarks()) {
            minX = Math.min(minX, landmark.getPosition().x);
            minY = Math.min(minY, landmark.getPosition().y);
            maxX = Math.max(maxX, landmark.getPosition().x);
            maxY = Math.max(maxY, landmark.getPosition().y);
        }

        return new RectF(minX, minY, maxX, maxY);
    }
    private void drawLine(Canvas canvas, Pose pose, int startLandmarkId, int endLandmarkId,
                          float scale, float offsetX, float offsetY) {
        PoseLandmark startLandmark = pose.getPoseLandmark(startLandmarkId);
        PoseLandmark endLandmark = pose.getPoseLandmark(endLandmarkId);

        if (startLandmark != null && endLandmark != null) {
            float startX = (startLandmark.getPosition().x * scale) + offsetX;
            float startY = (startLandmark.getPosition().y * scale) + offsetY;
            float endX = (endLandmark.getPosition().x * scale) + offsetX;
            float endY = (endLandmark.getPosition().y * scale) + offsetY;

            canvas.drawLine(startX, startY, endX, endY, linePaint);
        }
    }

}
