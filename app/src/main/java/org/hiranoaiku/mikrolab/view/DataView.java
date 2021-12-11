package org.hiranoaiku.mikrolab.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Hikaru on 01.03.2016.
 */
public class DataView extends View {

    protected char data;

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        data = 0;
        setWillNotDraw(false);
    }

    public void setData(char data) {
        this.data = data;
        invalidate();
        requestLayout();
    }

    public char getData() {
        return this.data;
    }
}
