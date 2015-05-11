package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import java.util.ArrayList;
import phom.data.PhomPlayer;

public class ReadyResponse extends AbstractResponseMessage {
	public String mErrorMsg;
        public long mUid;
        public ArrayList<PhomPlayer> mWaitingPlayerPhom;
        public ArrayList<PhomPlayer> mPlayerPhom;
        
    public void setSuccess(int aCode, long aUid)
    {
        mCode = aCode;
        mUid = aUid;
    }


    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new ReadyResponse();
    }
}
