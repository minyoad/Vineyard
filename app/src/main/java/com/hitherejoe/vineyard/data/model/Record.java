package com.hitherejoe.vineyard.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Record extends RealmObject {
    @PrimaryKey
    String record_id;

    int record_uid;//userid

    int record_sid;

    int record_did; //vodid
    int record_did_sid; //vod-source
    int record_did_pid; //vod-sourde-program
    int record_pos; //viewed position

    int record_type;

    long record_time;

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    public int getRecord_uid() {
        return record_uid;
    }

    public void setRecord_uid(int record_uid) {
        this.record_uid = record_uid;
    }

    public int getRecord_sid() {
        return record_sid;
    }

    public void setRecord_sid(int record_sid) {
        this.record_sid = record_sid;
    }

    public int getRecord_did() {
        return record_did;
    }

    public void setRecord_did(int record_did) {
        this.record_did = record_did;
    }

    public int getRecord_did_sid() {
        return record_did_sid;
    }

    public void setRecord_did_sid(int record_did_sid) {
        this.record_did_sid = record_did_sid;
    }

    public int getRecord_did_pid() {
        return record_did_pid;
    }

    public void setRecord_did_pid(int record_did_pid) {
        this.record_did_pid = record_did_pid;
    }

    public int getRecord_type() {
        return record_type;
    }

    public void setRecord_type(int record_type) {
        this.record_type = record_type;
    }

    public long getRecord_time() {
        return record_time;
    }

    public void setRecord_time(long record_time) {
        this.record_time = record_time;
    }

    public int getRecord_pos() {
        return record_pos;
    }

    public void setRecord_pos(int record_pos) {
        this.record_pos = record_pos;
    }

}
