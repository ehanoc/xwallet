package com.bytetobyte.xwallet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by bruno on 17.03.17.
 */
public class CustomCircleImageView extends CircleImageView {

    /**
     *
     * @param context
     */
    public CustomCircleImageView(Context context) {
        super(context);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public CustomCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CustomCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /**
     *
     */
    protected void init() {

    }
}
