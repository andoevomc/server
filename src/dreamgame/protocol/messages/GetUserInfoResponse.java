package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetUserInfoResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public long mUid;
    public String mUsername;
    public int mAge;
    public boolean mIsMale;
    public int level;

    public long money;
    public int playsNumber;
    public int AvatarID;
    public boolean isFriend;
    public long award=0;
    
    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode, long aUid, String aUsername, 
    		int aAge, boolean aIsMale, long m, int pN, int aID, boolean is, int l)
    {
        mCode = aCode;
        mUid = aUid;
        mUsername = aUsername;
        mAge = aAge;
        mIsMale = aIsMale;
        money = m;
        playsNumber = pN;
        AvatarID = aID;
        isFriend = is;
        level = l;
    }

    public IResponseMessage createNew()
    {
        return new GetUserInfoResponse();
    }

}
