package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class BuyAvatarResponse extends AbstractResponseMessage
{

	public String errMessage;
	public long new_cash;
	public int new_avatar;

        public boolean chargeMoney=false;
        public boolean chargeAppleMoney=false;
        public boolean change_award=false;
	
	public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        errMessage = aErrorMsg;
    }
    public void setSuccess(int aCode, long money, int ava)
    {
        mCode = aCode;
        new_cash = money;
        new_avatar = ava;
    }

    public String chargeMsg="";
    
    public void setSuccess(int aCode,String msg,long money)
    {
        mCode = aCode;
        chargeMsg = msg;
        new_cash = money;

    }

    public IResponseMessage createNew()
    {
        return new BuyAvatarResponse();
    }
}
