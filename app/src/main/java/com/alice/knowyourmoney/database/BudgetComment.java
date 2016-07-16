package com.alice.knowyourmoney.database;

/**
 * Created by alice on 2016/6/19.
 */
public class BudgetComment {
        private long id;
    private int number;
        private String name;
        private int due;

        public long getId(){
            return id;
        }

        public void setId(long id){
            this.id = id;
        }

        public String getName(){
            return name;
        }

        public void setName(String name){
            this.name = name;
        }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getDue() {
        return due;
    }

    public void setDue(int due) {
        this.due = due;
    }

        @Override
        public String toString(){
            return name;
        }
}
