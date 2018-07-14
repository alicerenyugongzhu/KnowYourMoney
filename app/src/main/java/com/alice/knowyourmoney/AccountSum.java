package com.alice.knowyourmoney;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.alice.knowyourmoney.database.AccountComment;
import com.alice.knowyourmoney.database.DBSource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alice on 2016/9/27.
 */

public class AccountSum extends Activity {

    //Global Parameters
    Map pie = null;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SumCalculation();
        PanView panView = new PanView(this);
        setContentView(panView);

    }

    private void SumCalculation() {
        DBSource myDb = new DBSource(this);
        myDb.Open();
        List<AccountComment> records = new ArrayList<AccountComment>();
        pie = new HashMap();
        try {
            records = myDb.getAllAccount();
            for (AccountComment i : records) {
                pie.put(i.getReason(), (int)pie.get(i.getReason()) + i.getPrice());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public class PanView extends View{
        public PanView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas){
            //Get mid point
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            float midWidth = width/2;
            float midHeight = height/2;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            canvas.drawCircle(midWidth, midHeight, 30,paint);
        }
    }
}
