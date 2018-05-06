package com.hitherejoe.vineyard.data.model;

import io.realm.RealmObject;

public class Player extends RealmObject {
    int player_id;
    String player_name_zh;
    String player_name_en;
    String player_info;
    String player_jiexi;
    int player_status;
    int player_order;
    int player_copyright;

}
