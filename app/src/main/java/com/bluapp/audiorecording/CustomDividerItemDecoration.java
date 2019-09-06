package com.bluapp.audiorecording;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class CustomDividerItemDecoration extends RecyclerView.ItemDecoration{
    private Drawable drawableline;

    public CustomDividerItemDecoration(Context context){
        drawableline = ContextCompat.getDrawable(context, R.drawable.recyclerline);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state){
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + drawableline.getIntrinsicHeight();
            drawableline.setBounds(left, top, right, bottom);
            drawableline.draw(c);
        }
    }

}
