package com.mykong;

import java.sql.Date;

public class RespUser {
    public  String status;
    public  String joinDate;
    public  int id;
    public String error;




    public  String getStatus() {
        return status;
    }

    public void setStatus(String name) {
        status = name;
    }

    public void setJoinDate(Date date){ joinDate = "" + date; }

    public void setError(String error) {this.error = error; }



    public String getJoinDate(){ return joinDate; }
    public void setId(int id){ this.id = id; }

    public int getId() {
        return id;
    }


}
