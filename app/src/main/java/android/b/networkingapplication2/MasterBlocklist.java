package android.b.networkingapplication2;

import java.util.UUID;

public class MasterBlocklist {

    private UUID mId;
    private String mBlockList;
    private String mDomain;
    private int mStatus;

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

    public String getBlockList() {
        return mBlockList;
    }

    public void setBlockList(String blockList) {
        mBlockList = blockList;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }
}
