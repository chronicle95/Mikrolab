package org.hiranoaiku.mikrolab.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Hikaru on 01.03.2016.
 */
public class LedStrip extends DataView {
    private float scalex, scaley;
    Paint ledLit, ledUnlit, ledBG;

    public LedStrip(Context context, AttributeSet attrs) {
        super(context, attrs);

        scalex = 1;
        scaley = scalex;

        ledLit = new Paint();
        ledLit.setColor(0xFFEA3030);
        ledLit.setFlags(Paint.ANTI_ALIAS_FLAG);

        ledUnlit = new Paint();
        ledUnlit.setColor(0xFF3E0202);
        ledUnlit.setFlags(Paint.ANTI_ALIAS_FLAG);

        ledBG = new Paint();
        ledBG.setColor(0xFF330000);
        ledBG.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(int i = 0; i < 8; i++)
            canvas.drawCircle((i*6+4)*scalex, 3*scaley, 2.5f*scalex, ledBG);

        canvas.drawCircle(4 * scalex, 3 * scaley, 2 * scalex, (data & 0x80) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(10 * scalex, 3 * scaley, 2 * scalex, (data & 0x40) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(16 * scalex, 3 * scaley, 2 * scalex, (data & 0x20) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(22 * scalex, 3 * scaley, 2 * scalex, (data & 0x10) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(28 * scalex, 3 * scaley, 2 * scalex, (data & 0x08) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(34 * scalex, 3 * scaley, 2 * scalex, (data & 0x04) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(40 * scalex, 3 * scaley, 2 * scalex, (data & 0x02) != 0 ? ledLit : ledUnlit);
        canvas.drawCircle(46 * scalex, 3 * scaley, 2 * scalex, (data & 0x01) != 0 ? ledLit : ledUnlit);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wm = View.MeasureSpec.getMode(widthMeasureSpec), w = View.MeasureSpec.getSize(widthMeasureSpec);
        int hm = View.MeasureSpec.getMode(heightMeasureSpec), h = View.MeasureSpec.getSize(heightMeasureSpec);

        scalex = w / 50.0f;
        scaley = h / 6.0f;

        ledLit.setStrokeWidth(Math.round(scalex));
        ledUnlit.setStrokeWidth(Math.round(scalex));

        setMeasuredDimension(
                View.MeasureSpec.makeMeasureSpec(w + getPaddingLeft() + getPaddingRight(), wm),
                View.MeasureSpec.makeMeasureSpec(h + getPaddingTop() + getPaddingBottom(), hm)
        );
    }
}
