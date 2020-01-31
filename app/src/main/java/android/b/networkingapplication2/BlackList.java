package android.b.networkingapplication2;

import java.util.UUID;

public class BlackList {

    private UUID mId;
    private String mDomain;

    public BlackList() {
        this(UUID.randomUUID());
    }

    public BlackList(UUID id) {
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
