package android.b.networkingapplication2;

import java.util.UUID;

public class BlackWhiteList {

    private UUID mId;
    private String mDomain;

    public BlackWhiteList() {
        this(UUID.randomUUID());
    }

    public BlackWhiteList(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String domain) {
        mDomain = domain;

    }

}
