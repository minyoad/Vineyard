package com.hitherejoe.vineyard.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Record extends RealmObject {
    @PrimaryKey
    int record_id;
    int record_uid;
    int record_sid;
    int record_did;
    int record_did_sid;
    int record_did_pid;
    int record_type;
    int record_time;
}
