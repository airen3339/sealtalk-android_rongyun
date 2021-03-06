package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cn.rongcloud.im.model.ChatRoomResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.utils.language.LangUtils;


public class AppViewModel extends AndroidViewModel {
    private final AppTask appTask;
    private String sealTalkVersionName;
    private SingleSourceMapLiveData<Resource<VersionInfo>, Resource<VersionInfo.AndroidVersion>> hasNew;
    private MutableLiveData<String> sdkVersion = new MutableLiveData<>();
    private MutableLiveData<String> sealTalkVersion = new MutableLiveData<>();
    private SingleSourceLiveData<Resource<List<ChatRoomResult>>> chatRoomResultList = new SingleSourceLiveData<>();
    private MutableLiveData<LangUtils.RCLocale> languageLocal = new MutableLiveData<>();
    private MutableLiveData<Boolean> debugMode = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        appTask = new AppTask(application);
        sealTalkVersionName = getSealTalkVersion(application);

        hasNew = new SingleSourceMapLiveData<>(new Function<Resource<VersionInfo>, Resource<VersionInfo.AndroidVersion>>() {
            @Override
            public Resource<VersionInfo.AndroidVersion> apply(Resource<VersionInfo> input) {
                if (input.data != null) {
                    SLog.d("ss_version", "input == " + input);
                    boolean hasNew = false;
                    String newVersion = input.data.getAndroidVersion().getVersion();
                    if (sealTalkVersionName != null) {
                        if (hasNewVersion(sealTalkVersionName, newVersion)) {
                            return new Resource<VersionInfo.AndroidVersion>(input.status, input.data.getAndroidVersion(), input.code);
                        }
                    }

                }
                return new Resource<VersionInfo.AndroidVersion>(input.status, null, input.code);
            }
        });

        sdkVersion.setValue(getSdkVersion());
        sealTalkVersion.setValue(sealTalkVersionName);
        checkVersion();
        requestChatRoomList();
        // ??????
        languageLocal.setValue(appTask.getLanguageLocal());

        debugMode.setValue(appTask.isDebugMode());
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public LiveData<Resource<VersionInfo.AndroidVersion>> getHasNewVersion() {
        return hasNew;
    }

    /**
     * ??????sdk ??????
     *
     * @return
     */
    public LiveData<String> getSDKVersion() {
        return sdkVersion;
    }

    /**
     * sealtalk ??????
     *
     * @return
     */
    public LiveData<String> getSealTalkVersion() {
        return sealTalkVersion;
    }

    /**
     * ????????????
     */
    private void checkVersion() {
        hasNew.setSource(appTask.getNewVersion());
    }

    /**
     * ?????????????????????
     */
    public void requestChatRoomList() {
        chatRoomResultList.setSource(appTask.getDiscoveryChatRoom());
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public LiveData<Resource<List<ChatRoomResult>>> getChatRoomList() {
        return chatRoomResultList;
    }

    /**
     * ??????SDK??????
     */
    private String getSdkVersion() {
        return appTask.getSDKVersion();
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public LiveData<LangUtils.RCLocale> getLanguageLocal() {
        return languageLocal;
    }

    /**
     * ?????? SealTalk ??????
     *
     * @param application
     * @return
     */
    private String getSealTalkVersion(Context application) {
        try {
            PackageInfo packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ????????????
     *
     * @param currentVersion
     * @param newVersion
     * @return
     */
    private boolean hasNewVersion(String currentVersion, String newVersion) {
        String[] currentVersionArray = currentVersion.split("\\.");
        String[] newVersionArray = newVersion.split("\\.");
        if (currentVersionArray.length > 0 && newVersionArray.length > 0) {
            for (int i = 0; i < newVersionArray.length; i++) {
                if (i > currentVersionArray.length - 1) {
                    break;
                }
                if (Integer.parseInt(newVersionArray[i]) > Integer.parseInt(currentVersionArray[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ????????????
     *
     * @param selectedLocale
     */
    public void changeLanguage(LangUtils.RCLocale selectedLocale) {
        if (appTask.changeLanguage(selectedLocale)) {
            languageLocal.postValue(appTask.getLanguageLocal());
        }
    }

    public LiveData<Boolean> getDebugMode() {
        return debugMode;
    }
}
