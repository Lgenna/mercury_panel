package android.b.networkingapplication2;

import java.util.UUID;

public class QueryLog {

    private UUID mId;
    private long mTime;
    private String mDomain;
    private String mStatus;

    QueryLog() {
        this(UUID.randomUUID());
    }

    private QueryLog(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
    }

    long getTime() {
        return mTime;
    }

    void setTime(long time) {
        mTime = time;
    }

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String domain) {
        mDomain = domain;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }
}
