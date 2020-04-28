package android.b.networkingapplication2;

import android.graphics.drawable.Drawable;

import java.util.UUID;

public class Firewall {

    private UUID mId;
    private String mProcessName;
    private String mApplication;
    private String mUid;
    private boolean mStatus;
    private Drawable mPicture;

    Firewall() {
        this(UUID.randomUUID());
    }

    private Firewall(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
    }

    String getProcessName() {
        return mProcessName;
    }

    void setProcessName(String processName) {
        mProcessName = processName;
    }

    String getUid() {
        return mUid;
    }

    void setUid(String uid) {
        mUid = uid;
    }

    public String getApplication() {
        return mApplication;
    }

    public void setApplication(String application) {
        mApplication = application;
    }

    Drawable getPicture() {
        return mPicture;
    }

    void setPicture(Drawable picture) {
        mPicture = picture;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }
}
