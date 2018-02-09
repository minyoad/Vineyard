/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hitherejoe.vineyard.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.hitherejoe.vineyard.data.remote.VineyardService;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *  Modified from AOSP sample source code, by corochann on 2/7/2015.
 *  Movie class represents video entity with title, description, image thumbs and video url.
 */
public class Movie implements Comparable<Movie>, Parcelable{

    private static final String TAG = Movie.class.getSimpleName();

    public static class PlayUrlInfo implements Serializable {
        public String title;
        public String url;
    }

    private List<String> mPlaySrcList;

    private HashMap<String,List<PlayUrlInfo>> mPlayUrlMap;

    public List<String> getmPlaySrcList() {
        return mPlaySrcList;
    }

    public HashMap<String, List<PlayUrlInfo>> getmPlayUrlMap() {
        return mPlayUrlMap;
    }


//    public String vod_id;
    public String vod_cid;
//    public String vod_name;
//    public String vod_title;
    public String vod_type;
    public String vod_keywords;
    public String vod_actor;
//    public String vod_director;
//    public String vod_content;
//    public String vod_pic;
    public String vod_area;
    public String vod_language;
    public String vod_year;
    public String vod_addtime;
    public String vod_filmtime;
    public String vod_server;
    @SerializedName("vod_play")
    public String vod_play;
//    public String vod_url;
    public String vod_inputer;
    public String vod_reurl;
    public String vod_length;
    public String vod_weekday;
    public String vod_copyright;
    public String vod_state;
    public String vod_version;
    public String vod_tv;
    public String vod_total;
    public String vod_continu;
    public String vod_status;
    public String vod_stars;
    public String vod_hits;
    public String vod_isend;
    public String vod_douban_id;
    public String vod_series;
//    public String list_name;

    @SerializedName("vod_id")
    private long id;
    @SerializedName("vod_name")
    private String title;
    @SerializedName("vod_director")
    private String studio;
    @SerializedName("vod_content")
    private String description;
    private String bgImageUrl;
    @SerializedName("vod_pic")
    private String cardImageUrl;
    @SerializedName("vod_url")
    private String videoUrl;
    @SerializedName("list_name")
    private String category;


    public String currentSource;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackgroundImageUrl() {
        return getCardImageUrl();
    }

    public void setBackgroundImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        String pic=cardImageUrl;
        if (!cardImageUrl.startsWith("http")){
            pic= VineyardService.ENDPOINT+cardImageUrl;
        };

        return pic;
    }

    public void setCardImageUrl(String cardImageUrl) {

        this.cardImageUrl = cardImageUrl;
    }

    public Movie() {
    }

    public URI getCardImageURI() {
        try {
            return new URI(getCardImageUrl());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public String getVideoUrl() {

        if (videoUrl!=null && mPlayUrlMap==null){
            parsePlayUrls();
        }

        return getVideoUrlInfo(currentSource,0).url;

    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
        parsePlayUrls();
    }

    public String getCategory() { return category; }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", studio='" + studio + '\'' +
                ", description='" + description + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Movie movie) {
        return (int)(movie.id-this.id);
    }
    /*** auto generated codes by Parcelable plugin end ***/


    private void parsePlayUrls(){

        if(vod_play==null)
            return;

        String[] playNameList=vod_play.split("\\$\\$\\$");
        mPlaySrcList =Arrays.asList(playNameList);

        String[] urlList=videoUrl.split("\\$\\$\\$");
        mPlayUrlMap =new HashMap();


        for (int i = 0; i< mPlaySrcList.size(); i++){
            String name= mPlaySrcList.get(i);

            String urlStr=urlList[i];

            String[] NameUrls=urlStr.split("\\r");


            ArrayList<PlayUrlInfo> urlLists=new ArrayList<>();
            for(String nameurl:NameUrls){

                String[] infos= nameurl.split("\\$");

                PlayUrlInfo playUrlInfo=new PlayUrlInfo();

                if(infos.length>1) {
                    playUrlInfo.title = infos[0];
                    playUrlInfo.url = infos[1];
                }
                else{
                    playUrlInfo.title = name;
                    playUrlInfo.url = infos[0];
                }

                urlLists.add(playUrlInfo);

            }

            mPlayUrlMap.put(name,urlLists);

        }

        currentSource=mPlaySrcList.get(0);

    }

    public String getProxyUrlByPlayer(String playerName){
        String proxyurl="";
        switch (playerName){
            case "kkyun":
            case "kuyun":

                break;

            default:
                proxyurl="http://jx.vgoodapi.com/jx.php?url=";
                break;
        }
        return  proxyurl;

    }

    public String getRelatedWord(){
        String word=getTitle();

        return word;
    }


    public PlayUrlInfo getVideoUrlInfo(String srcName,int index){

        if (mPlaySrcList==null || mPlayUrlMap==null){
            parsePlayUrls();
        }


        return mPlayUrlMap.get(srcName).get(index);


//        List<String> nameList= getPlaySourceList();
//
//        final String[] PLAYERS = {"qq","youku","iqiyi","ku6"};
//
//        PlayUrlInfo playUrlInfo= getPlayUrl(nameList.get(0),0);
//
//        for (String name:nameList) {
//
//            if ( Arrays.asList(PLAYERS).contains(name) ) {
//                // Do some stuff.
//                playUrlInfo=getPlayUrl(name,0);
//
//                break;
//            }
//
//        }
//
//        return playUrlInfo;

    }


    public PlayUrlInfo getPlayUrl(String playName,int index){

        if (mPlaySrcList!=null && mPlaySrcList.contains(playName)){
            List<PlayUrlInfo> urls= mPlayUrlMap.get(playName);

            if(urls!=null && index<urls.size()){
                return urls.get(index);
            }
        }


        return  null;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mPlaySrcList);
        dest.writeSerializable(this.mPlayUrlMap);
        dest.writeString(this.vod_cid);
        dest.writeString(this.vod_type);
        dest.writeString(this.vod_keywords);
        dest.writeString(this.vod_actor);
        dest.writeString(this.vod_area);
        dest.writeString(this.vod_language);
        dest.writeString(this.vod_year);
        dest.writeString(this.vod_addtime);
        dest.writeString(this.vod_filmtime);
        dest.writeString(this.vod_server);
        dest.writeString(this.vod_play);
        dest.writeString(this.vod_inputer);
        dest.writeString(this.vod_reurl);
        dest.writeString(this.vod_length);
        dest.writeString(this.vod_weekday);
        dest.writeString(this.vod_copyright);
        dest.writeString(this.vod_state);
        dest.writeString(this.vod_version);
        dest.writeString(this.vod_tv);
        dest.writeString(this.vod_total);
        dest.writeString(this.vod_continu);
        dest.writeString(this.vod_status);
        dest.writeString(this.vod_stars);
        dest.writeString(this.vod_hits);
        dest.writeString(this.vod_isend);
        dest.writeString(this.vod_douban_id);
        dest.writeString(this.vod_series);
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.studio);
        dest.writeString(this.description);
        dest.writeString(this.bgImageUrl);
        dest.writeString(this.cardImageUrl);
        dest.writeString(this.videoUrl);
        dest.writeString(this.category);
        dest.writeString(this.currentSource);
    }

    protected Movie(Parcel in) {
        this.mPlaySrcList = in.createStringArrayList();
        this.mPlayUrlMap = (HashMap<String, List<PlayUrlInfo>>) in.readSerializable();
        this.vod_cid = in.readString();
        this.vod_type = in.readString();
        this.vod_keywords = in.readString();
        this.vod_actor = in.readString();
        this.vod_area = in.readString();
        this.vod_language = in.readString();
        this.vod_year = in.readString();
        this.vod_addtime = in.readString();
        this.vod_filmtime = in.readString();
        this.vod_server = in.readString();
        this.vod_play = in.readString();
        this.vod_inputer = in.readString();
        this.vod_reurl = in.readString();
        this.vod_length = in.readString();
        this.vod_weekday = in.readString();
        this.vod_copyright = in.readString();
        this.vod_state = in.readString();
        this.vod_version = in.readString();
        this.vod_tv = in.readString();
        this.vod_total = in.readString();
        this.vod_continu = in.readString();
        this.vod_status = in.readString();
        this.vod_stars = in.readString();
        this.vod_hits = in.readString();
        this.vod_isend = in.readString();
        this.vod_douban_id = in.readString();
        this.vod_series = in.readString();
        this.id = in.readLong();
        this.title = in.readString();
        this.studio = in.readString();
        this.description = in.readString();
        this.bgImageUrl = in.readString();
        this.cardImageUrl = in.readString();
        this.videoUrl = in.readString();
        this.category = in.readString();
        this.currentSource = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}