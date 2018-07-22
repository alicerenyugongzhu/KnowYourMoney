package com.alice.knowyourmoney;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alice.knowyourmoney.RecyclerViewAdapter.MyAdapter;
import com.alice.knowyourmoney.RecyclerViewAdapter.SpaceDecoration;
import com.alice.knowyourmoney.database.AccountComment;
import com.alice.knowyourmoney.database.DBSource;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountKeep extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyAdapter.MyItemClickListener, View.OnTouchListener{

    RecyclerView recordList = null;
    FloatingActionButton fab = null;
    LinearLayout newRecordLayout = null;
    EditText newReason  = null;
    EditText newPrice = null;
    Button recordDone = null;
    TextView newDate = null;


    DBSource myDb = null;
    MyAdapter myAdapter = null;
    List<AccountComment> records = null;
    ArrayList<AccountComment> weekRecords = null;
    GestureDetector mGestureDetector = null;

    //Parameter
    String recordDate;
    int year;
    int month;
    int day;
    List<Map<String, Object>> week_list;
    int insetPosition;

    private static final int FLING_MIN_DISTANCE = 50;   //Smallest instance
    private static final int FLING_MIN_VELOCITY = 0;  //Smallest speed

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_keep);

        //Handle Database
        try {
            DBInit();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Init Budget
        //Preference
        //TODO need confirm what the toolbar can help me
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init New Record update
        newRecordLayout = findViewById(R.id.record_add);
        newRecordLayout.setVisibility(View.GONE);
        newDate = findViewById(R.id.new_date);
        newDate.setVisibility(View.GONE);
        newReason = findViewById(R.id.new_reason);
        newReason.setVisibility(View.GONE);
        newPrice = findViewById(R.id.new_price);
        newPrice.setVisibility(View.GONE);
        recordDone = findViewById(R.id.entry_done);
        recordDone.setVisibility(View.GONE);
        recordDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNumberic(newPrice.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Price need to be number. Please enter it again", Toast.LENGTH_LONG).show();
                }
                MDHide(newDate);
                MDHide(newReason);
                MDHide(newPrice);
                MDHide(recordDone);
                MDHide(newRecordLayout);
                String date = recordDate;
                newDate.setText("");
                String reason = newReason.getText().toString();
                newReason.setText("");
                float price = Float.parseFloat(newPrice.getText().toString());
                newPrice.setText("");
                if(recordDone.getText().equals("Add Done")) {
                    try {
                        AccountComment newRecord = myDb.CreateAccount(date, reason, price);
                        Log.d("alice_debug", "Create one account record here.");
                        weekRecords.add(newRecord);
                        Log.d("alice_debug", "weekRecords is " + weekRecords);
                        myAdapter.setData(weekRecords);
                        myAdapter.notifyDataSetChanged();
                        //recordList.setAdapter(myAdapter);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        weekRecords.get(insetPosition).setDate(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    weekRecords.get(insetPosition).setReason(reason);
                    weekRecords.get(insetPosition).setPrice(price);
                    try {
                        myDb.UpdateAccount(weekRecords.get(insetPosition));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    myAdapter.notifyItemChanged(insetPosition);
                    myAdapter.setData(weekRecords);
                    myAdapter.notifyItemRangeChanged(insetPosition, weekRecords.size());

                }
                //myAdapter.DataReloadAll();
                //try {
                //    records = myDb.getAllAccount();
                //} catch (ParseException e) {
                //    e.printStackTrace();
                //}
                //AccountComment newRecord = new AccountComment();
            }


        });

        fab = findViewById(R.id.fab);
        //MDHide(fab);  //TODO hide the fab. Need confirm whether I need it in the future

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MDShow(newRecordLayout);
                MDShow(newReason);
                MDShow(newPrice);
                MDShow(recordDone);
                recordDone.setText("Add Done");
                new DatePickerDialog(AccountKeep.this, datePickerListener, year, (month - 1), day).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Init Calendar
        CalendarInit();

        mGestureDetector = new GestureDetector(this, onGestureListener);
        LinearLayout ll = findViewById(R.id.main1_layout);
        ll.setOnTouchListener(this);
        ll.setLongClickable(true);

        }

    private GestureDetector.OnGestureListener onGestureListener =

            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    Log.d("alice_debug", "Enter onFling event");
                    float x = e1.getX() - e2.getX();
                    float x2 = e2.getX() - e1.getX();
                    if (x > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                        //Left
                        Toast.makeText(AccountKeep.this, "Move left", Toast.LENGTH_SHORT).show();
                        return true;

                    } else if (x2 > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                        //Right
                        Toast.makeText(AccountKeep.this, "Move left", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            };



    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            recordDate = String.valueOf(year) + String.valueOf(month + 1) + String.valueOf(dayOfMonth);
            MDShow(newDate);
            newDate.setText(recordDate.toString());
        }
    };

    public static boolean isNumberic(String s) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(s);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CalendarInit() {
        //Get what's day today
        LocalDate cc = LocalDate.now();
        DayOfWeek dayOfWeek = cc.getDayOfWeek();
        year = cc.getYear();
        month = cc.getMonthValue();  //month start from 0??
        day = cc.getDayOfMonth();

        //Init View
        final TextView monthView = findViewById(R.id.month_view);
        monthView.setText(String.valueOf(month));
        monthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MDLinkMovement(view);
                //TODO Show the month picker
                monthView.setText(String.valueOf(month));
            }
        });

        //Init Grid View
        GridView gridView = findViewById(R.id.week_day);

        week_list = new ArrayList<Map<String, Object>>();
        week_list = GetWeekAndDay(day, dayOfWeek);
        String[] from = {"week", "day"};
        int[] to = {R.id.days_of_week, R.id.day};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, week_list, R.layout.grid_item, from, to);
        gridView.setAdapter(simpleAdapter);
        Log.d("alice_debug", "I am here after the gridView setting");

        //Init RecyclerView
        RecordShowInit();

    }
    //TODO this is a bug when open on Tuesday. Day of Week will show like 1 ,2, 3, 3, 3. Need fix
    @RequiresApi(api = Build.VERSION_CODES.O)
    List<Map<String, Object>> GetWeekAndDay(int day, DayOfWeek dayOfWeek) {
        List<Map<String, Object>> week_list;
        week_list = new ArrayList<Map<String, Object>>();
        String[] dayOfWeekString = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] loopDay = new int[7];
        loopDay[dayOfWeek.getValue() - 1] = day;
        Log.d("alice_debug", "dayOfWeek returns " + dayOfWeek.getValue());
        Log.d("alice_debug", "today is " + day);
        int dayTemp;
        int weekBefore = dayOfWeek.getValue() - 1;
        int number = 1;
        while (weekBefore-- > 0) {
            LocalDate ld = LocalDate.now().minusDays(number++);
            dayTemp = ld.getDayOfMonth();
            loopDay[weekBefore] = dayTemp;
        }

        int weekAfter = dayOfWeek.getValue();
        number = 1;
        while (weekAfter < 7) {
            LocalDate ld = LocalDate.now().plusDays(number++);
            dayTemp = ld.getDayOfMonth();
            loopDay[weekAfter++] = dayTemp;
        }

        for (int loop = 0; loop < 7; loop++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("week", dayOfWeekString[loop]);
            map.put("day", loopDay[loop]); //Need confirm how can I get the correct month Day
            week_list.add(map);
        }
        return week_list;
    }

    private void MDShow(View view) {
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

    private void MDHide(View view) {
        // previously visible view
        final View myView = view;

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

    private void MDLinkMovement(View view) {
        DisplayMetrics dm = getResources().getDisplayMetrics();


        Animation ani = new TranslateAnimation(0.0f, dm.widthPixels / 2, 0.0f, 0.0f);
        ani.setDuration(1000);
        ani.setRepeatCount(1);
        ani.setRepeatMode(1);
        TextView myView = (TextView) view;
        myView.setMovementMethod(LinkMovementMethod.getInstance());
        view.startAnimation(ani);
    }

    private void DBInit() throws ParseException {
        myDb = new DBSource(this);
        myDb.Open();
        records = new ArrayList<AccountComment>();
        records = myDb.getAllAccount();

    }

    private void RecordShowInit() {

        recordList = findViewById(R.id.detail_view);

        recordList.setHasFixedSize(true);
        Log.d("alice_debug", "number of Account list " + records.size());
        weekRecords = new ArrayList<AccountComment>();
        GetThisWeekList(records, weekRecords);
        myAdapter = new MyAdapter(weekRecords);
        myAdapter.SetItemClickListener(this);
        recordList.setAdapter(myAdapter);
        recordList.setLayoutManager(new LinearLayoutManager(this));
        recordList.setItemAnimator(new DefaultItemAnimator());
        recordList.addItemDecoration(new SpaceDecoration(30));

    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "item is clicked", Toast.LENGTH_LONG).show();
        //TODO this need update DBSource to add the update function
        MDShow(newRecordLayout);
        MDShow(newDate);
        MDShow(newReason);
        MDShow(newPrice);
        MDShow(recordDone);
        recordDate = weekRecords.get(position).getDate();
        newDate.setText(recordDate);
        newReason.setText(weekRecords.get(position).getReason());
        String sPrice = String.valueOf(weekRecords.get(position).getPrice());
        newPrice.setText(sPrice);
        recordDone.setText("Edit Done");
        //Buffer the position
        insetPosition = position;
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        //Toast.makeText(this, "long item is clicked", Toast.LENGTH_LONG).show();
        AccountComment item = weekRecords.get(position);
        myDb.DeleteAccount(item);
        weekRecords.remove(position);
        myAdapter.notifyItemRemoved(position);
        myAdapter.notifyItemRangeChanged(position, weekRecords.size());
        return false;
    }

    private void GetThisWeekList(List<AccountComment> source, ArrayList<AccountComment> dest) {
        int size = week_list.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> map = week_list.get(i);
            Integer day = (Integer) map.get("day");
            String dateCmp = String.valueOf(year) + String.valueOf(month) + day.toString();
            for (int j = 0; j < source.size(); j++) {
                AccountComment ac = source.get(j);
                if (dateCmp.equals(ac.getDate())) {
                    dest.add(ac);
                }
            }
        }

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

        if (id == R.id.record_sum) {
            // Jump to the record sum page
            Intent intent = new Intent(AccountKeep.this, AccountSum.class);
            startActivity(intent);

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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("alice_debug", "I am in onTouch Event");
        mGestureDetector.onTouchEvent(event);
        return false;
    }
}
