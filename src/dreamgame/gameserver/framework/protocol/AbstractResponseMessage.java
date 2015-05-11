package dreamgame.gameserver.framework.protocol;

public abstract class AbstractResponseMessage
        implements IResponseMessage {

    private int mMsgId;
    public int mCode;
    public long matchId;
    //Thomc
    public String msgNotification = "";
    public String msgAdmin = "";
    public boolean disconnect = false;
    public long uid;
    void setID(int aMsgId) {
        this.mMsgId = aMsgId;
    }

    public int getID() {
        return this.mMsgId;
    }

    protected AbstractResponseMessage clone() {
        AbstractResponseMessage resMsg = (AbstractResponseMessage) createNew();
        resMsg.setID(this.mMsgId);
        return resMsg;
    }
}
