package com.alice.knowyourmoney;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
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
import android.view.inputmethod.InputMethodManager;
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

public class AccountKeep extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Button myBT = null;
    RecyclerView recordList = null;
    FloatingActionButton fab = null;
    DBSource myDb = null;
    MyAdapter myAdapter = null;
    List<AccountComment> records = null;

    ArrayList<String> reasonList = null;
    ArrayList<String> reasonMorning = null;
    ArrayList<String> reasonNoon = null;
    ArrayList<String> reasonEve = null;

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
                //MDHide(myET);
                //MDHide(myTV);
                //MDHide(myBT);
                MDShow(fab);
                MDShow(recordList);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                //Recycler View
                if(!price.equals("")) {
                    //Get System time
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss ");
                    //Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String time = formatter.format(System.currentTimeMillis());
                    String date = df.format(System.currentTimeMillis());
                    date = date + " " + time;
                    Log.d("alice_debug", "the date that I final will insert is " + date);
                    myDb.Open();
                    try {
                        AccountComment ac = myDb.CreateAccount(date, GetSpendReason(), Integer.parseInt(price));
                        Log.d("alice_debug", "I am insert an account item into DB");
                        Log.d("alice_debug", "id is " + ac.getId());
                        Log.d("alice_debug", "date is " + ac.getDate());
                        Log.d("alice_debug", "reason is " + ac.getReason());
                        //myAdapter.notifyItemInserted((int) ac.getId() - 1);
                        //records.clear();
                        records.add(ac);
                        Log.d("alice_debug", "records refresh " + records.size());
                        //myAdapter.notifyItemChanged((int)ac.getId());
                        myAdapter.DataReload(records);
                        //myAdapter.DataReload(ac);
                        recordList.setAdapter(myAdapter);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    myET.setText("");
                }
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText myET = (EditText)findViewById(R.id.price);
                TextView myTV = (TextView)findViewById(R.id.price_hint);

                // previously invisible view
                MDShow(myET);
                MDShow(myTV);
                MDShow(myBT);
                //myET.setVisibility(View.VISIBLE);
                //myTV.setVisibility(View.VISIBLE);
                //myBT.setVisibility(View.VISIBLE);
                //fab.setVisibility(View.INVISIBLE);
                MDHide(fab);
                MDHide(recordList);
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

    private void MDShow(View view){
        View myView = view;

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
    }

    private void MDHide(View view){
        // previously visible view
        final View myView = view ;

// get the center for the clipping circle
        int cx = (myView.getLeft() + myView.getRight()) / 2;
        int cy = (myView.getTop() + myView.getBottom()) / 2;

// get the initial radius for the clipping circle
        int initialRadius = myView.getWidth();

// create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

// make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

// start the animation
        anim.start();
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

        reasonMorning = new ArrayList<String>();
        reasonMorning.add(getString(R.string.breakfast));

        reasonNoon = new ArrayList<String>();
        reasonNoon.add(getString(R.string.lunch));

        reasonEve = new ArrayList<String>();
        reasonEve.add(getString(R.string.supper));

    }

    private String GetSpendReason() {
        SimpleDateFormat formatter = new SimpleDateFormat ("HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        String reason = null;
        //guess reason according to time
        //Proper reason
        Log.d("alice_debug", "the time is " + str);
        //if t is morning from 5:00 - 11:00 breakfast/drink
        String [] hour = str.split(":");
        if(Integer.parseInt(hour[0]) < 11) //Morning
            reason= reasonMorning.get(0);
        //if t is noon from 11:00 - 14:00 lunch/drink/supermarket/Commodity
        else if(Integer.parseInt(hour[0]) < 14) //Noon
            reason = reasonNoon.get(0);
        else
            reason = reasonEve.get(0);
        return reason;
    }

    private void DBInit() throws ParseException {
        myDb = new DBSource(this);
        myDb.Open();

        records = new ArrayList<AccountComment>();
        records = myDb.getAllAccount();
        recordList = (RecyclerView)findViewById(R.id.record_list);
        recordList.setLayoutManager(new LinearLayoutManager(this));
       // recordList.setAdapter(new MyAdapter(myDb.getAllAccount()));
        Log.d("alice_debug", "number of Account list " + records.size());
        myAdapter = new MyAdapter(records);
        recordList.setAdapter(myAdapter);
        recordList.setItemAnimator(new DefaultItemAnimator());
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

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        //TODO: need add 3 item : Date Reason Price
        List<AccountComment> ac = new ArrayList<AccountComment>();
        public MyAdapter(List<AccountComment> ac){
            this.ac = ac;
        }

        public void DataReload(List<AccountComment> ac){
            this.ac.clear();
            this.ac.addAll(ac);
            notifyDataSetChanged();
        }

        public void DataReload(AccountComment acItem){
            this.ac.add(acItem);
            notifyItemInserted(this.ac.size());
            notifyItemInserted(this.ac.size());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_account, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Log.d("alice_debug", "position is " + position);
            Log.d("alice_debug", "Date is this position is " + ac.get(position).getDate());
            holder.rdDate.setText(ac.get(position).getDate());
            holder.rdReason.setText(ac.get(position).getReason());
            holder.rdPrice.setText(Float.toString(ac.get(position).getPrice()));
        }

        @Override
        public int getItemCount() {
            return ac.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView rdDate;
            TextView rdReason;
            TextView rdPrice;
            public ViewHolder(View view){
                super(view);
                rdDate = (TextView)view.findViewById(R.id.recordDate);
                rdReason = (TextView)view.findViewById(R.id.recordReason);
                rdPrice = (TextView)view.findViewById(R.id.recordPrice);

            }
        }
    }
}
