package com.hitherejoe.vineyard;

import android.app.Application;
import android.content.Context;

import com.hitherejoe.vineyard.injection.component.ApplicationComponent;
import com.hitherejoe.vineyard.injection.component.DaggerApplicationComponent;
import com.hitherejoe.vineyard.injection.module.ApplicationModule;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class VineyardApplication extends Application {

    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("vineyard.realm").build();
        Realm.setDefaultConfiguration(config);
    }

    public static VineyardApplication get(Context context) {
        return (VineyardApplication) context.getApplicationContext();
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mApplicationComponent;
    }

}
