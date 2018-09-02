package com.alice.knowyourmoney;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alice.knowyourmoney.RecyclerViewAdapter.MyAdapter;
import com.alice.knowyourmoney.RecyclerViewAdapter.SpaceDecoration;
import com.alice.knowyourmoney.database.AccountComment;
import com.alice.knowyourmoney.database.DBSource;
import com.alice.knowyourmoney.dateUtils.dateUtils;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;

import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountKeep extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                     MyAdapter.MyItemClickListener, View.OnTouchListener{

    private RecyclerView recordList = null;
    private FloatingActionButton fab = null;
    private LinearLayout newRecordLayout = null;
    private EditText newReason  = null;
    private EditText newPrice = null;
    private Button recordDone = null;
    private TextView newDate = null;
    private TextView monthView = null;
    private GridView gridView = null;
    private SimpleAdapter simpleAdapter = null;
    private CoordinatorLayout container = null;
    private LinearLayout prefLayout = null;
    private TextView prefInfo = null;
    private EditText prefValue = null;
    private Button prefDone = null;
    private dateUtils myDateUtils = null;


    private DBSource myDb = null;
    private MyAdapter myAdapter = null;
    private List<AccountComment> records = null;
    private ArrayList<AccountComment> weekRecords = null;  //Current week's spend records
    private GestureDetector mGestureDetector = null;

    //Parameter
    private String recordDate;
    private int year;
    private int month;
    private int day;
    private List<Map<String, Object>> week_list; //The current weeks 7 days info
    private int insetPosition;
    private LocalDate cc;

    //Sharedpreference
    private static final String PREF_LIB = "TABLE_BUDGET";
    private float budgetDefault = 1500;
    private float budget;
    private float budgetLeft = 0;
    private String firstDay = null;
    private float init = 0;

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
        //GestureInit
        GestureInit();

        //Init Calendar
        CalendarInit();

        //Budget init
        BudgetInit();

        //Init Budget
        //Preference
        ToolBarBehave();

        //Init New Record update
        NewRecordBehave();

        //Click fab when add new record
        AddNewBehave();

        //SnackBar to show the sum
        container = findViewById(R.id.container);
        SnackbarShow();

    }

    private void SnackbarShow() {
        float sum = myDateUtils.SumForThisWeek(weekRecords);
        String snack = "Weekly Budget: " + budget +
                " Sum: " + String.valueOf(sum)  +
                " Week Left: " + (budget - sum) +
                " Left: " + budgetLeft ;  //This is the first time

        Snackbar.make(container, snack, Snackbar.LENGTH_INDEFINITE).setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        }).show();
    }

    private void GestureInit() {
        mGestureDetector = new GestureDetector(this, onGestureListener);
        RelativeLayout ll = findViewById(R.id.main_layout);
        ll.setOnTouchListener(this);
        ll.setLongClickable(true);
    }

    private void AddNewBehave() {
        fab = findViewById(R.id.fab);
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
    }

    private void NewRecordBehave() {
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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(newReason.getText().toString().equals(null) || newPrice.getText().toString().equals(null)){
                    Toast.makeText(getApplicationContext(), "Price need to be number. Please enter it again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isNumberic(newPrice.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Price need to be number. Please enter it again", Toast.LENGTH_LONG).show();
                    return;
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
                        //Log.d("alice_debug", "Create one account record here.");
                        weekRecords.add(newRecord);
                        //Log.d("alice_debug", "weekRecords is " + weekRecords);
                        myAdapter.setData(weekRecords);
                        myAdapter.notifyDataSetChanged();
                        budgetLeft = GetNewBudgetLeft();
                        SnackbarShow();
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
                    budgetLeft = GetNewBudgetLeft();
                    SnackbarShow();
                }
            }


        });
    }

    private void ToolBarBehave() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Pref setting
        prefLayout = findViewById(R.id.pref_setting);
        prefInfo = findViewById(R.id.pref_info);
        prefValue = findViewById(R.id.pref_value);
        prefDone = findViewById(R.id.pref_done);
        prefLayout.setVisibility(View.GONE);
        prefInfo.setVisibility(View.GONE);
        prefValue.setVisibility(View.GONE);
        prefDone.setVisibility(View.GONE);
        prefDone.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (prefInfo.getText().toString() == "New Budget"){
                    SharedPreferences budgetTable = getSharedPreferences(PREF_LIB, MODE_PRIVATE);
                    SharedPreferences.Editor editor = budgetTable.edit();
                    budget = Float.parseFloat(prefValue.getText().toString());
                    editor.putFloat("BUDGET", budget);
                    editor.commit();
                    MDHide(prefDone);
                    MDHide(prefInfo);
                    MDHide(prefLayout);
                    MDHide(prefValue);
                    budgetLeft = GetNewBudgetLeft();
                    SnackbarShow();
                } else if (prefInfo.getText().toString() == "New Init") {
                    SharedPreferences budgetTable = getSharedPreferences(PREF_LIB, MODE_PRIVATE);
                    SharedPreferences.Editor editor = budgetTable.edit();
                    init = Float.parseFloat(prefValue.getText().toString());
                    editor.putFloat("INIT", init);
                    editor.commit();
                    MDHide(prefDone);
                    MDHide(prefInfo);
                    MDHide(prefLayout);
                    MDHide(prefValue);
                    budgetLeft = GetNewBudgetLeft();
                    SnackbarShow();
                } else if(prefInfo.getText().toString() == "New Start Date"){
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void BudgetInit() {
        SharedPreferences budgetTable = getSharedPreferences(PREF_LIB, MODE_PRIVATE);
        budget = budgetTable.getFloat("BUDGET", -1);
        if(budget == -1) {
            SharedPreferences.Editor editor = budgetTable.edit();
            editor.putFloat("BUDGET", budgetDefault);
            editor.commit();
            budget = budgetDefault;
        }
        budgetLeft = budgetTable.getFloat("BUDGET_LEFT", 0);
        //Log.d("alice_debug", "Budget Left before enter this time is " + budgetLeft);
        firstDay = budgetTable.getString("FIRST_DAY", null);
        if(firstDay == null){
             firstDay = String.valueOf(year) + ((month > 9) ? "" : "0") + String.valueOf(month) + ((day > 9) ? "" : "0") + String.valueOf(day);
             //Log.d("alice_debug","I want add this to firstWeek: " + firstDay);
            SharedPreferences.Editor editor = budgetTable.edit();
            editor.putString("FIRST_DAY", firstDay);
            editor.commit();
        }
        init = budgetTable.getFloat("INIT", 0);
    }

    private GestureDetector.OnGestureListener onGestureListener =

            new GestureDetector.SimpleOnGestureListener() {
                @SuppressLint("NewApi")
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    //Log.d("alice_debug", "Enter onFling event");
                    float x = e1.getX() - e2.getX();
                    float x2 = e2.getX() - e1.getX();
                    if (x > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                        //Left: last week
                        //Toast.makeText(AccountKeep.this, "Move left", Toast.LENGTH_SHORT).show();
                        cc = cc.plusDays(7);
                        year = cc.getYear();
                        month = cc.getMonthValue();
                        day = cc.getDayOfMonth();
                        DayOfWeek dayOfWeek = cc.getDayOfWeek();
                        monthView.setText(String.valueOf(month));
                        week_list.clear();
                        week_list.addAll(myDateUtils.GetWeekAndDay(cc, dayOfWeek));
                        simpleAdapter.notifyDataSetChanged();
                        weekRecords.clear();
                        try {
                            records = myDb.getAllAccount();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        GetThisWeekList(records, weekRecords);
                        myAdapter.setData(weekRecords);
                        myAdapter.notifyDataSetChanged();
                        budgetDefault = GetNewBudgetLeft();
                        SnackbarShow();

                    } else if (x2 > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                        //Right next week
                        //Toast.makeText(AccountKeep.this, "Move left", Toast.LENGTH_SHORT).show();
                        cc = cc.minusDays(7);
                        year = cc.getYear();
                        month = cc.getMonthValue();
                        day = cc.getDayOfMonth();
                        DayOfWeek dayOfWeek = cc.getDayOfWeek();
                        monthView.setText(String.valueOf(month));
                        week_list.clear();
                        week_list.addAll(myDateUtils.GetWeekAndDay(cc, dayOfWeek));
                        simpleAdapter.notifyDataSetChanged();
                        weekRecords.clear();
                        try {
                            records = myDb.getAllAccount();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        GetThisWeekList(records, weekRecords);
                        myAdapter.setData(weekRecords);
                        myAdapter.notifyDataSetChanged();
                        //myAdapter.notifyItemRangeChanged(0, weekRecords.size());
                        budgetDefault = GetNewBudgetLeft();
                        SnackbarShow();

                    }
                    return false;
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private float GetNewBudgetLeft() {
        float bl = myDb.GetLeft(firstDay);
        int lyear = Integer.valueOf(firstDay)/10000;
        int lmonth = (Integer.valueOf(firstDay) - lyear*10000)/100;
        int lday = Integer.valueOf(firstDay) - lyear * 10000 - lmonth * 100;
        LocalDate start = LocalDate.of(lyear, lmonth, lday);
        LocalDate rightNow = LocalDate.now();
        Period period = Period.between(start, rightNow);
        int weeks = period.getDays()/7 + 1;
        //Log.d("alice_debug", "Budget Left (bl) is" + bl);
        bl = init + budget * weeks - bl; //TODO calculate the number of Budget ;
        //Log.d("alice_debug", "init is " + init);
        //Log.d("alice_debug", "weeks is " + weeks);
        Log.d("alice_debug", "between days is  " + period.getDays() + " year is " + lyear +
            " month is " + lmonth + " day is " + lday);
        Log.d("alice_debug", "Budget Left (bl) is" + bl);

        SharedPreferences budgetTable = getSharedPreferences(PREF_LIB, MODE_PRIVATE);
        SharedPreferences.Editor editor = budgetTable.edit();
        editor.putFloat("BUDGET_LEFT", bl);
        editor.commit();
        return bl;
    }


    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            recordDate = String.valueOf(year) + ((month > 8) ? "" : "0") +
                    String.valueOf(month + 1) + ((dayOfMonth > 9) ? "" : "0") +
                    String.valueOf(dayOfMonth);
            MDShow(newDate);
            newDate.setText(recordDate.toString());
        }
    };

    private DatePickerDialog.OnDateSetListener StartDateListener = new DatePickerDialog.OnDateSetListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            firstDay = String.valueOf(year) + ((month > 8) ? "" : "0") +
                    String.valueOf(month + 1) + ((dayOfMonth > 9) ? "" : "0") +
                    String.valueOf(dayOfMonth);
            //Log.d("alice_debug", "update firstDay here to " + firstDay);
            SharedPreferences budgetTable = getSharedPreferences(PREF_LIB, MODE_PRIVATE);
            SharedPreferences.Editor editor = budgetTable.edit();
            //init = Float.parseFloat(prefValue.getText().toString());
            editor.putString("FIRST_DAY", firstDay);
            editor.commit();
            budgetLeft = GetNewBudgetLeft();
            SnackbarShow();
            //MDShow(newDate);
        }
    };

    public static boolean isNumberic(String s) {
        Pattern pattern = Pattern.compile("[0-9\\.]*");
        Matcher isNum = pattern.matcher(s);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CalendarInit() {
        //Init dateUtils
        myDateUtils = new dateUtils();
        //Get what's day today
        cc = LocalDate.now();
        DayOfWeek dayOfWeek = cc.getDayOfWeek();
        year = cc.getYear();
        month = cc.getMonthValue();
        day = cc.getDayOfMonth();

        //Init View
        monthView = findViewById(R.id.month_view);
        monthView.setText(String.valueOf(month));
        monthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MDLinkMovement(view);
                monthView.setText(String.valueOf(month));
            }
        });

        //Init Grid View
        gridView = findViewById(R.id.week_day);

        week_list = new ArrayList<Map<String, Object>>();
        week_list = myDateUtils.GetWeekAndDay(cc, dayOfWeek);
        String[] from = {"week", "day"};
        int[] to = {R.id.days_of_week, R.id.day};
        simpleAdapter = new SimpleAdapter(this, week_list, R.layout.grid_item, from, to);
        gridView.setAdapter(simpleAdapter);
        Log.d("alice_debug", "I am here after the gridView setting");
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        //Init RecyclerView
        RecordShowInit();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onItemLongClick(View view, int position) {
        //Toast.makeText(this, "long item is clicked", Toast.LENGTH_LONG).show();
        AccountComment item = weekRecords.get(position);
        myDb.DeleteAccount(item);
        weekRecords.remove(position);
        myAdapter.notifyItemRemoved(position);
        myAdapter.notifyItemRangeChanged(position, weekRecords.size());
        budgetLeft = GetNewBudgetLeft();
        SnackbarShow();
        return false;
    }

    private void GetThisWeekList(List<AccountComment> source, ArrayList<AccountComment> dest) {
        int size = week_list.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> map = week_list.get(i);
            Integer day = (Integer) map.get("day");
            String dateCmp = String.valueOf(year) + ((month > 9) ? "" : "0") +
                    String.valueOf(month) + ((day > 9) ? "" : "0") + day.toString();
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
            if( recordDone.getVisibility() == View.VISIBLE) {
                MDHide(newDate);
                MDHide(newReason);
                MDHide(newPrice);
                MDHide(recordDone);
                MDHide(newRecordLayout);
                newDate.setText("");
                newReason.setText("");
            } else {
                super.onBackPressed();
                myDb.Close();
            }
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
        if (id == R.id.start_day_setting) {
            new DatePickerDialog(AccountKeep.this, StartDateListener, year, (month - 1), day).show();
            return true;
        } else if (id == R.id.budget_setting) {
            MDShow(prefLayout);
            MDShow(prefValue);
            MDShow(prefInfo);
            MDShow(prefDone);
            prefInfo.setText("New Budget");
            prefValue.setText(String.valueOf(budget));
            return true;
        } else if(id == R.id.init_setting) {
            MDShow(prefLayout);
            MDShow(prefValue);
            MDShow(prefInfo);
            MDShow(prefDone);
            prefInfo.setText("New Init");
            prefValue.setText(String.valueOf(init));
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

        } else if (id == R.id.nav_talk) {
            Intent intent = new Intent(AccountKeep.this, TalkInput.class);
            startActivity(intent);

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
