package com.example.poseperfect;

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
    private Paint paint;
    private int imageWidth;
    private int imageHeight;

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void updatePose(Pose pose, int imageWidth, int imageHeight) {
        this.pose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose != null) {
            float widthScale = (float) getWidth() / imageWidth;
            float heightScale = (float) getHeight() / imageHeight;
            List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
            for (PoseLandmark landmark : landmarks) {
                float x = landmark.getPosition().x * widthScale;
                float y = landmark.getPosition().y * heightScale;
                canvas.drawCircle(x, y, 10, paint);
            }
            drawLineBetween(canvas, pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER), pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER));
            drawLineBetween(canvas, pose.getPoseLandmark(PoseLandmark.LEFT_HIP), pose.getPoseLandmark(PoseLandmark.RIGHT_HIP));
            drawLineBetween(canvas, pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER), pose.getPoseLandmark(PoseLandmark.LEFT_HIP));
            drawLineBetween(canvas, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER), pose.getPoseLandmark(PoseLandmark.RIGHT_HIP));
        }
    }
    private void drawLineBetween(Canvas canvas, PoseLandmark startLandmark, PoseLandmark endLandmark) {
        if (startLandmark != null && endLandmark != null) {
            canvas.drawLine(startLandmark.getPosition().x, startLandmark.getPosition().y,
                    endLandmark.getPosition().x, endLandmark.getPosition().y, paint);
        }
    }
}
