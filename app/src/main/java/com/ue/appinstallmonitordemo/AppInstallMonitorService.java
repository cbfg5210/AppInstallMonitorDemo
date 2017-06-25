package com.ue.appinstallmonitordemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AppInstallMonitorService extends Service {
    private static final String TAG = AppInstallMonitorService.class.getSimpleName();
    private static final String[] command = {"sh", "-c", "ps -P | grep packageinstaller"};

    private Disposable disposable;

    public AppInstallMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addDataScheme("package");
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        registerReceiver(appInstallCompleteReceiver, intentFilter);

        disposable = Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        boolean isInstallingApp = findPackageInstaller();
                        if (isInstallingApp) {
                            Log.e(TAG, "************is installing app*******");
                        } else {
                            Log.e(TAG, "package installer not found");
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
        unregisterReceiver(appInstallCompleteReceiver);
        super.onDestroy();
    }

    private boolean findPackageInstaller() {
        boolean isInstallingApp = false;
        Process localProcess = null;
        try {
            localProcess = Runtime.getRuntime().exec(command);
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            String str;
            do {
                str = localBufferedReader.readLine();
                if (str == null) {
                    break;
                }
            } while (!str.contains("packageinstaller"));
            if (!TextUtils.isEmpty(str)) {
                Log.e(TAG, "str=" + str);
                //str=u0_a44    1677  569   586076 42796 fg  ffffffff 00000000 S com.android.packageinstaller
                isInstallingApp = str.contains("fg");
            }
        } catch (IOException localIOException) {
            Log.e(TAG, "findPackageInstaller,error=" + localIOException.getMessage());
        } finally {
            if (localProcess != null) {
                localProcess.destroy();
            }
        }
        return isInstallingApp;
    }

    private BroadcastReceiver appInstallCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                Log.e(TAG, "*^_^* : app install completely");
            }
        }
    };
}
