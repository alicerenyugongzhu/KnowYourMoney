package com.alice.knowyourmoney.RecyclerViewAdapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceDecoration extends RecyclerView.ItemDecoration {
    int mSpace;
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;
        if(parent.getChildAdapterPosition(view) == 0){
            outRect.top = mSpace;
        }
    }
    public SpaceDecoration(int space){
        mSpace = space;
    }
}
