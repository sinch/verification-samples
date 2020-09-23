package com.sinch.verificationsamplejava;

import android.app.Application;

import com.sinch.verification.core.auth.AppKeyAuthorizationMethod;
import com.sinch.verification.core.config.general.GlobalConfig;
import com.sinch.verification.core.config.general.SinchGlobalConfig;

public class VerificationJavaSampleApp extends Application {

    private GlobalConfig globalConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        globalConfig = SinchGlobalConfig.Builder.getInstance()
                .applicationContext(this)
                .authorizationMethod(new AppKeyAuthorizationMethod(BuildConfig.APP_KEY))
                .build();
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

}
