package com.hitherejoe.vineyard.data.local;

import android.content.Context;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Player;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.injection.ApplicationContext;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.Realm;
import retrofit.Call;
import retrofit.Response;
import timber.log.Timber;

@Singleton
public class PlayerHelper {

    private Context mContext;

    @Inject
    public PlayerHelper(@ApplicationContext Context context) {
        mContext=context;

//        loadPlayerData();
    }

    public String getPlayerName(String player){

        Realm realm=Realm.getDefaultInstance();

        final Player player1=realm.where(Player.class)
                .equalTo("player_name_en",player)
                .findFirst();

        if (player1!=null){
            return player1.getPlayer_name_zh();
        }

        return player;
    }

    public void loadPlayerData(){

        DataManager mDataManager = VineyardApplication.get(mContext).getComponent().dataManager();

        Call<VineyardService.PlayerResponse> playerResponseCall = mDataManager.getPlayerList();
        try {
            Response<VineyardService.PlayerResponse> response = playerResponseCall.execute();
            VineyardService.PlayerResponse playerResponse = response.body();

            Realm realm=Realm.getDefaultInstance();

            realm.beginTransaction();

            for (Player player:playerResponse.data) {

                realm.copyToRealm(player);

//                Player player1=realm.copyFromRealm(player);

            }

            realm.commitTransaction();


        } catch (IOException e) {
            Timber.e("There was an error retrieving the posts", e);
        }

    }
}
