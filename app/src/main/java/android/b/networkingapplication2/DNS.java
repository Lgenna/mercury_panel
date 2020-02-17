package android.b.networkingapplication2;

import java.util.UUID;

public class DNS {

    private UUID mId;
    private String mTitle;
    private String mTextbox;
    private boolean mStatus;

    public DNS() {
        this(UUID.randomUUID());
    }

    public DNS(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTextbox() {
        return mTextbox;
    }

    public void setTextbox(String textBox) {
        mTextbox = textBox;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }
}
