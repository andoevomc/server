package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class EnterZoneResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public int channelId;
    public int maxRoom=0;
    public int zoneId=0;
    
    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode) {
        mCode = aCode;
    }

    public IResponseMessage createNew() {
        return new EnterZoneResponse();
    }
}
