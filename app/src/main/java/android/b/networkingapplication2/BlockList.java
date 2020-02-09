package android.b.networkingapplication2;

import java.util.UUID;

public class BlockList {

    private UUID mId;
    private String mDomain;
//    private Boolean mStatus;

    public BlockList() {
        this(UUID.randomUUID());
    }

    public BlockList(UUID id) {
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

//    public Boolean getStatus() {
//        return mStatus;
//    }

//    public void setStatus(Boolean status) {
//        mStatus = status;
//    }
}