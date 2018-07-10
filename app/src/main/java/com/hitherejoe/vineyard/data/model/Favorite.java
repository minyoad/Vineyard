package com.hitherejoe.vineyard.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Favorite extends RealmObject {
    @PrimaryKey
    String favId;

    long vodId;
    long userId;

    long addTime;

    public String getFavId() {
        return favId;
    }

    public void setFavId(String favId) {
        this.favId = favId;
    }

    public long getVodId() {
        return vodId;
    }

    public void setVodId(long vodId) {
        this.vodId = vodId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }
}
