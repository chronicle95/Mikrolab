package org.hiranoaiku.mikrolab.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by Hikaru on 02.03.2016.
 */
public class Switch extends DataView {

    private float scalex, scaley, textPos;
    private Paint paintBorder, paintToggle, paintBG, paintText;
    private String textTop, textBottom;

    public Switch(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintBorder = new Paint();
        paintBorder.setColor(0xFF003300);
        paintBorder.setStyle(Paint.Style.FILL);

        paintToggle = new Paint();
        paintToggle.setColor(0xFFEEEEEE);
        paintToggle.setStyle(Paint.Style.FILL);
        paintToggle.setStrokeWidth(0);

        paintBG = new Paint();
        paintBG.setColor(0xFFAAAAAA);
        paintBG.setStyle(Paint.Style.FILL);
        paintBG.setStrokeWidth(0);

        paintText = new Paint();
        paintText.setColor(0xFF202020);
        paintText.setTextSize(20);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setFlags(Paint.ANTI_ALIAS_FLAG);

        textTop = "1";
        textBottom = "0";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0 * scalex, 0 * scaley, 7 * scalex, 11 * scaley, paintBorder);
        canvas.drawRect(1 * scalex, 1 * scaley, 6 * scalex, 10 * scaley, paintBG);

        canvas.drawText(textBottom, textPos, 9 * scaley, paintText);
        canvas.drawText(textTop, textPos, 4 * scaley, paintText);

        canvas.drawRect(1.5f * scalex, 1.5f * scaley + 3.5f * scaley * dataIndex(), 5.5f * scalex, 6 * scaley + 3.5f * scaley * dataIndex(), paintToggle);
        canvas.drawLine(2 * scalex, 3.75f * scaley + 3.5f * scaley * dataIndex(), 5 * scalex, 3.75f * scaley + 3.5f * scaley * dataIndex(), paintBG);
    }

    public void setLabels(String top, String bottom) {
        textTop = top;
        textBottom = bottom;
    }

    public void setToggleColor(int color) {
        paintToggle.setColor(color);
    }

    public int dataIndex() {
        return (this.data != 0) ? 1 : 0;
    }

    public void toggle() {
        this.setData((char) (1 - this.dataIndex()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wm = MeasureSpec.getMode(widthMeasureSpec), w = MeasureSpec.getSize(widthMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec), h = MeasureSpec.getSize(heightMeasureSpec);

        scalex = w / 7.0f;
        scaley = h / 12.0f;

        paintBG.setStrokeWidth(scaley/2.5f);
        paintText.setTextSize(h/4);
        textPos = (w-paintText.measureText(textTop)) / 2;

        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(w + getPaddingLeft() + getPaddingRight(), wm),
                MeasureSpec.makeMeasureSpec(h + getPaddingTop() + getPaddingBottom(), hm)
        );
    }

}
