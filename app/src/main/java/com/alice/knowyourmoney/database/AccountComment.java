package com.alice.knowyourmoney.database;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by alice on 2016/6/19.
 */
public class AccountComment {
    private long id;
    private String date;
    private String reason;
    private float price;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String  getDate() {
        return date;
    }

    public void setDate(String date) throws ParseException {
        SimpleDateFormat format;
        format = new SimpleDateFormat("yyyy-MM-dd");
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
