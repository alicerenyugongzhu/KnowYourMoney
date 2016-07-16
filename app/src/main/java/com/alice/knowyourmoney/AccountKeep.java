package com.alice.knowyourmoney;

import android.animation.Animator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alice.knowyourmoney.database.AccountComment;
import com.alice.knowyourmoney.database.DBSource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO I need database
public class AccountKeep extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Button myBT = null;
    RecyclerView recordList = null;
    FloatingActionButton fab = null;
    DBSource myDb = null;

    ArrayList<String> reasonList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_keep);

        //ReasonList Init
        ReasonListInit();
        
        //Handle Database
        try {
            DBInit();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);

        myBT = (Button)findViewById(R.id.record);
        myBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText myET = (EditText)findViewById(R.id.price);
                TextView myTV = (TextView)findViewById(R.id.price_hint);
                String price = myET.getText().toString();
                myET.setVisibility(View.INVISIBLE);
                myTV.setVisibility(View.INVISIBLE);
                myBT.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
                //Recycler View
                //Get System time
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String date = df.format(System.currentTimeMillis());
                myDb.Open();
                try {
                    myDb.CreateAccount(date, GetSpendReason(), Integer.parseInt(price));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText myET = (EditText)findViewById(R.id.price);
                TextView myTV = (TextView)findViewById(R.id.price_hint);

                // previously invisible view
                View myView = findViewById(R.id.price_hint);

                // get the center for the clipping circle
                int cx = (myView.getLeft() + myView.getRight()) / 2;
                int cy = (myView.getTop() + myView.getBottom()) / 2;

                // get the final radius for the clipping circle
                int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

                // create the animator for this view (the start radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

// make the view visible and start the animation
                myView.setVisibility(View.VISIBLE);
                anim.start();
                myET.setVisibility(View.VISIBLE);
                //myTV.setVisibility(View.VISIBLE);
                myBT.setVisibility(View.VISIBLE);
                fab.setVisibility(View.INVISIBLE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void ReasonListInit() {
        reasonList = new ArrayList<String>();
        reasonList.add(getString(R.string.breakfast));
        reasonList.add(getString(R.string.lunch));
        reasonList.add(getString(R.string.dinner));
        reasonList.add(getString(R.string.supper));
        reasonList.add(getString(R.string.commodity));
        reasonList.add(getString(R.string.clothes));
        reasonList.add(getString(R.string.entertainment));
        reasonList.add(getString(R.string.shoes));

    }

    private String GetSpendReason() {
        SimpleDateFormat formatter = new SimpleDateFormat ("HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        //guess reason according to time
        //Proper reason
        Log.d("alice_debug", "the time is " + str);
        //if t is morning from 5:00 - 11:00 breakfast/drink
        //if t is noon from 11:00 - 14:00 lunch/drink/supermarket/Commodity
        return null;
    }

    private void DBInit() throws ParseException {
        myDb = new DBSource(this);
        myDb.Open();

        List<AccountComment> records = myDb.getAllAccount();
        recordList = (RecyclerView)findViewById(R.id.record_list);
        recordList.setLayoutManager(new LinearLayoutManager(this));
        recordList.setAdapter(new MyAdapter(myDb.getAllAccount()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account_keep, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        //TODO: need add 3 item : Date Reason Price
        List<AccountComment> ac = null;
        public MyAdapter(List<AccountComment> ac){
            this.ac = ac;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_account, parent, false);
            MyViewHolder viewHolder = new MyViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            holder.rdDate.setText(df.format(ac.get(position).getDate()));
            holder.rdReason.setText(ac.get(position).getReason());
            holder.rdPrice.setText(Float.toString(ac.get(position).getPrice()));
        }

        @Override
        public int getItemCount() {
            return ac.size();
        }

        class MyViewHolder extends ViewHolder {
            TextView rdDate;
            TextView rdReason;
            TextView rdPrice;
            public MyViewHolder(View view){
                super(view);
                rdDate = (TextView)findViewById(R.id.recordDate);
                rdReason = (TextView)findViewById(R.id.recordReason);
                rdPrice = (TextView)findViewById(R.id.recordPrice);

            }
        }
    }
}
