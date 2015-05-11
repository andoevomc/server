package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class RegisterResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public long mUid;
    public long money;
    public int avatarID;
    public int level;

    public String smsValue="100";
    public String smsNumber="8700";
    public String smsContent="VUI";
    public String adminMessage="";
    public String smsMessage="Bạn đã nạp tiền thành công. Tài khoản của bạn sẽ được công thêm 15k tiền.";
    
    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode, long aUid, long mn, int avatar, int lev)
    {
        mCode = aCode;
        mUid = aUid;
        money = mn;
        avatarID = avatar;
        level = lev;
        
    }

    public IResponseMessage createNew()
    {
        return new RegisterResponse();
    }
}
