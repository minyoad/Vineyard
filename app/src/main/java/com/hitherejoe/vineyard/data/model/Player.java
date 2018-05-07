package com.hitherejoe.vineyard.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Player extends RealmObject {
    @PrimaryKey
    int player_id;

    String player_name_zh;
    String player_name_en;
    String player_info;
    String player_jiexi;
    int player_status;
    int player_order;
    int player_copyright;

    public int getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public String getPlayer_name_zh() {
        return player_name_zh;
    }

    public void setPlayer_name_zh(String player_name_zh) {
        this.player_name_zh = player_name_zh;
    }

    public String getPlayer_name_en() {
        return player_name_en;
    }

    public void setPlayer_name_en(String player_name_en) {
        this.player_name_en = player_name_en;
    }

    public String getPlayer_info() {
        return player_info;
    }

    public void setPlayer_info(String player_info) {
        this.player_info = player_info;
    }

    public String getPlayer_jiexi() {
        return player_jiexi;
    }

    public void setPlayer_jiexi(String player_jiexi) {
        this.player_jiexi = player_jiexi;
    }

    public int getPlayer_status() {
        return player_status;
    }

    public void setPlayer_status(int player_status) {
        this.player_status = player_status;
    }

    public int getPlayer_order() {
        return player_order;
    }

    public void setPlayer_order(int player_order) {
        this.player_order = player_order;
    }

    public int getPlayer_copyright() {
        return player_copyright;
    }

    public void setPlayer_copyright(int player_copyright) {
        this.player_copyright = player_copyright;
    }



}
