package com.hitherejoe.vineyard.data.local;

import android.content.Context;

import com.hitherejoe.vineyard.data.model.Record;
import com.hitherejoe.vineyard.injection.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.Realm;
import io.realm.RealmResults;

@Singleton
public class RecordHelper {

    private Context mContext;

    private Record currentRecord;

    private String currentUUID;

    @Inject
    public RecordHelper(@ApplicationContext  Context context) {
        mContext=context;
    }


    public int getCurrentPosition() {
        Realm  mRealm=Realm.getDefaultInstance();

        Record record = mRealm.where(Record.class).equalTo("record_id", this.currentUUID).findFirst();
        return  record!=null?record.getRecord_pos():0;
    }

    public void updatePosition(int pos){

//        if(this.currentRecord!=null){
//            currentRecord.setRecord_pos(pos);
//        }

        Realm  mRealm=Realm.getDefaultInstance();

        Record record = mRealm.where(Record.class).equalTo("record_id", this.currentUUID).findFirst();
        mRealm.beginTransaction();
        record.setRecord_pos(pos);
        mRealm.commitTransaction();


    }

    public void setCurrentPlaying(int did,int sid,int pid){

        Realm realm=Realm.getDefaultInstance();

        Record record=realm.where(Record.class)
                .equalTo("record_did",did)
                .and()
                .equalTo("record_did_sid",sid)
                .and()
                .equalTo("record_did_pid",pid)
                .and()
                .equalTo("record_uid",0)
                .findFirst();

        realm.beginTransaction();

        if (record==null){
            record=realm.createObject(Record.class, UUID.randomUUID().toString());
            record.setRecord_did(did);
            record.setRecord_did_sid(sid);
            record.setRecord_did_pid(pid);
            record.setRecord_uid(0);
            record.setRecord_type(1);
            record.setRecord_sid(1);
            record.setRecord_pos(0);
        }

        record.setRecord_time((new Date()).getTime());

//        this.currentRecord=record;

        this.currentUUID=record.getRecord_id();

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
