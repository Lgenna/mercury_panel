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

    public Firewall() {
        this(UUID.randomUUID());
    }

    public Firewall(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public String getProcessName() {
        return mProcessName;
    }

    public void setProcessName(String processName) {
        mProcessName = processName;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getApplication() {
        return mApplication;
    }

    public void setApplication(String application) {
        mApplication = application;
    }

    public Drawable getPicture() {
        return mPicture;
    }

    public void setPicture(Drawable picture) {
        mPicture = picture;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }
}
