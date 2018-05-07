package com.hitherejoe.vineyard.data.local;

import android.content.Context;

import com.hitherejoe.vineyard.data.model.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RecordHelper {

    private Context mContext;
    public RecordHelper(Context context) {
        mContext=context;
    }


    public void update(int did,int sid,int pid,int uid){

        Realm realm=Realm.getDefaultInstance();

        Record record=realm.where(Record.class)
                .equalTo("record_did",did)
                .and()
                .equalTo("record_did_sid",sid)
                .and()
                .equalTo("record_did_pid",pid)
                .and()
                .equalTo("record_uid",uid)
                .findFirst();

        realm.beginTransaction();

        if (record==null){
            record=realm.createObject(Record.class);
            record.setRecord_did(did);
            record.setRecord_did_sid(sid);
            record.setRecord_did_pid(pid);
            record.setRecord_uid(uid);
            record.setRecord_type(1);
            record.setRecord_sid(1);
        }

        record.setRecord_time((new Date()).getTime());


        realm.commitTransaction();

    }

    public List<Integer> visitedPids(int did){
        Realm realm=Realm.getDefaultInstance();

        RealmResults<Record> records=realm.where(Record.class).equalTo("record_did",did).findAll();

        List<Integer> pidList=new ArrayList<>();

        for (Record record:records){
            pidList.add(record.getRecord_did_pid());
        }

        return pidList;
    }


}
