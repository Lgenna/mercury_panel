package android.b.networkingapplication2;

import java.util.UUID;

public class AppInfo {

    private UUID mId;
    private String mAppName;
    private String mProcessName;
    private Boolean mAppStatus;

    public AppInfo() {
    }

    public AppInfo(UUID id) {
        mId = id;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getProcessName() {
        return mProcessName;
    }

    public void setProcessName(String processName) {
        mProcessName = processName;
    }

    public Boolean getStatus() {
        return mAppStatus;
    }

    public void setStatus(Boolean appStatus) {
        mAppStatus = appStatus;
    }
}
