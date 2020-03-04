package android.b.networkingapplication2;

import java.util.UUID;

public class MasterBlocklist {

    private UUID mId;
    private String mDomain;

    public MasterBlocklist() {
        this(UUID.randomUUID());
    }

    public MasterBlocklist(UUID id) {
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
