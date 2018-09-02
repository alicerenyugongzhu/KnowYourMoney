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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SumCalculation();
        StatisticalView msView = new StatisticalView(this);
        setContentView(msView);

    }

    private void SumCalculation() {
        DBSource myDb = new DBSource(this);
        myDb.Open();
        List<AccountComment> records = new ArrayList<AccountComment>();
        try {
            records = myDb.getAllAccount();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public class StatisticalView extends View{
        public StatisticalView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas){
            //Get mid point
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            String text = "TO BE DEVELOPED";
            canvas.drawText(text, width/2, height/2, paint);
        }
    }
}
