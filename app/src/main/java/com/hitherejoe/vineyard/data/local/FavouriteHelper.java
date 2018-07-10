package com.hitherejoe.vineyard.data.local;

import com.hitherejoe.vineyard.data.model.Favorite;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class FavouriteHelper {

    public static void addFavourite(long vodId){

        if(!isFavorite(vodId)){
            Realm mRealm=Realm.getDefaultInstance();
            mRealm.beginTransaction();
            Favorite favorite=mRealm.createObject(Favorite.class,UUID.randomUUID().toString());
            favorite.setVodId(vodId);
            favorite.setUserId(0);
            favorite.setAddTime((new Date()).getTime());

            mRealm.commitTransaction();
        }
    }

    public static void delFaourite(long vodId){

        Realm mRealm=Realm.getDefaultInstance();

        final RealmResults<Favorite> results=mRealm.where(Favorite.class)
                .equalTo("vodId",vodId)
                .and()
                .equalTo("userId",0).findAll();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });

    }

    public static boolean isFavorite(long vodId){

        Realm mRealm=Realm.getDefaultInstance();
        long count=mRealm.where(Favorite.class)
                .equalTo("vodId",vodId)
                .and()
                .equalTo("userId",0).count();

        return count>0;
    }

    public static LinkedList getFavoriteList(){

        LinkedList favlist=new LinkedList();

        Realm mRealm=Realm.getDefaultInstance();

        final RealmResults<Favorite> results=mRealm.where(Favorite.class)
                .equalTo("userId",0)
                .findAll();

        for (Favorite fav : results) {
            favlist.add(fav.getVodId());

        }

        return favlist;

    }


}
