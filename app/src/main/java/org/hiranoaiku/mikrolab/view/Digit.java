package org.hiranoaiku.mikrolab.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by Hikaru on 29.02.2016.
 */
public class Digit extends DataView {

    private float scalex, scaley;
    Paint segmentLit, segmentDark;

    public Digit(Context context, AttributeSet attrs) {
        super(context, attrs);

        scalex = 8;
        scaley = scalex;
        segmentLit = new Paint();
        segmentLit.setStrokeWidth(Math.round(2 * scalex) + 1);
        segmentLit.setColor(0xFFEA3050);
        segmentLit.setStrokeCap(Paint.Cap.ROUND);
        segmentLit.setFlags(Paint.ANTI_ALIAS_FLAG);
        segmentDark = new Paint();
        segmentDark.setStrokeWidth(Math.round(2 * scalex));
        segmentDark.setColor(0xFF3E0202);
        segmentDark.setStrokeCap(Paint.Cap.ROUND);
        segmentDark.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(6 * scalex, 1 * scaley, 13 * scalex, 1 * scaley, ((data&0x01)!=0) ? segmentLit : segmentDark); // a
        canvas.drawLine(15 * scalex, 2 * scaley, 14 * scalex, 5 * scaley, ((data&0x02)!=0) ? segmentLit : segmentDark); // b
        canvas.drawLine(13 * scalex, 7 * scaley, 12 * scalex, 10 * scaley, ((data&0x04)!=0) ? segmentLit : segmentDark); // c
        canvas.drawLine(2 * scalex, 11 * scaley, 10 * scalex, 11 * scaley, ((data&0x08)!=0) ? segmentLit : segmentDark); // d
        canvas.drawLine(2 * scalex, 7 * scaley, 1 * scalex, 10 * scaley, ((data&0x10)!=0) ? segmentLit : segmentDark); // e
        canvas.drawLine(4 * scalex, 2 * scaley, 3 * scalex, 5 * scaley, ((data&0x20)!=0) ? segmentLit : segmentDark); // f
        canvas.drawLine(4 * scalex, 6 * scaley, 12 * scalex, 6 * scaley, ((data&0x40)!=0) ? segmentLit : segmentDark); // g
        canvas.drawLine(14 * scalex, 12 * scaley, 14 * scalex, 13 * scaley, ((data&0x80)!=0) ? segmentLit : segmentDark); // h
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wm = MeasureSpec.getMode(widthMeasureSpec), w = MeasureSpec.getSize(widthMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec), h = MeasureSpec.getSize(heightMeasureSpec);

        scalex = w / 24.0f;
        scaley = h / 13.0f;

        segmentLit.setStrokeWidth(Math.round(scalex * 2.5f));
        segmentDark.setStrokeWidth(Math.round(scalex * 2.5f));

        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(w + getPaddingLeft() + getPaddingRight(), wm),
                MeasureSpec.makeMeasureSpec(h + getPaddingTop() + getPaddingBottom(), hm)
        );
    }
}
