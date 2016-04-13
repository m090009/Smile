package com.tohamy.smile.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.tohamy.smile.R;

/**
 * Created by tohamy on 4/13/16.
 */
public class TextGraphic extends GraphicOverlay.Graphic{
    private static final float TEXT_Y_OFFSET = 50.0f;
    private static final float TEXT_X_OFFSET = -50.0f;
    private static final float ID_TEXT_SIZE = 100.0f;
    private Paint textPaint;
    private Context context;
    private String textGraphicString = "Hello World!";
    int x, y;
    public TextGraphic(GraphicOverlay overlay) {
        super(overlay);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(ID_TEXT_SIZE);

        this.context = overlay.getContext();

        Typeface tf = Typeface.createFromAsset(overlay.getContext().getAssets()
                ,"fonts/KGBehindTheseHazelEyes.ttf");
        textPaint.setTypeface(tf);

        this.x = overlay.getWidth()/3;
        this.y = overlay.getHeight()/3;
    }

    public void changeTextForSmilePercentage(float smilePercentage){
        float smileProbability = smilePercentage * 100;
        if (smileProbability <= 30){
            this.textGraphicString = this.context.getString(R.string.encourage_comment_1);
        } else if (30 < smileProbability && smileProbability <= 60){
            this.textGraphicString = this.context.getString(R.string.tease_comment_1);
        } else if (60 < smileProbability) {
            this.textGraphicString = this.context.getString(R.string.praise_comment_1);
        }
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(this.textGraphicString, x + TEXT_X_OFFSET, y + TEXT_Y_OFFSET, textPaint);
    }
}
