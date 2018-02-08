package com.hitherejoe.vineyard.data.model;

import android.util.Log;


import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by mingyang.wu on 17/11/8.
 */

public class Category {

    public static int MOVIE=1;
    public static int SERIES=2;
    public static int ANIME=3;
    public static int VARIETY=4;
    public static int INDEX=10;


    public static class CategoryExtent{
        public String cate;
        public String type;
        public String area;
        public String star;
        public String year;
        public String state;
        public String language;
        public String version;

    }
    public String list_id;
    public String list_name;
    public String list_extend;



    public HashMap<String,String> getExtendValues(){

        Gson gson=new Gson();

        CategoryExtent categoryExtent=new Gson().fromJson(list_extend,CategoryExtent.class);

        String value=categoryExtent.cate;

        List<String> list= Arrays.asList(value.split("\r\n"));

        HashMap<String,String> categoryMap=new LinkedHashMap<>();
        for (String cateKV:list){
            String[] cateArray=cateKV.split("=");

            categoryMap.put(cateArray[0],cateArray[1]);

        }

        return categoryMap;


    }

//    public List<MenuItemInfo> getMenuItemList(){
//
//        JSONObject jsonObject= JSON.parseObject(list_extend);
//
//        String value=jsonObject.getString("cate");
//        Log.d(TAG,"catestring="+value);
//
//        List<MenuItemInfo> menuItemInfos=new CopyOnWriteArrayList<>();
//
//        if (value!=null) {
//
//            String[] infos = value.split("\r\n");
//
//            for (String info : infos) {
//
//                String[] items = info.split("=");
//
//                MenuItemInfo menuItemInfo = new MenuItemInfo();
//                menuItemInfo.setTitle(items[0]);
//                menuItemInfo.setUrl(items[1]);
//
//
//                menuItemInfos.add(menuItemInfo);
//            }
//        }
//        else{
//
//            List<String> types=getExtendValues("type");
//
//            for (String type: types){
//
//                MenuItemInfo menuItemInfo = new MenuItemInfo();
//                menuItemInfo.setTitle(type);
//                menuItemInfo.setUrl("-cid-"+list_id+"-type-"+type);
//
//                menuItemInfos.add(menuItemInfo);
//            }
//
//        }
//
//        return menuItemInfos;
//
//    }

}
