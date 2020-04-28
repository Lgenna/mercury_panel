package android.b.networkingapplication2;

import java.util.UUID;

public class BlockList {

    private UUID mId;
    private String mDomain;

    BlockList() {
        this(UUID.randomUUID());
    }

    private BlockList(UUID id) {
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
