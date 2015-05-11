package dreamgame.protocol.messages;

import java.util.Hashtable;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetRoomMoneyResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public boolean getHelp=false;
    
    
    public Hashtable<Integer, Long>[] moneys;
    public Hashtable<Integer, Long>[] moneys_poker;
    
    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode, Hashtable<Integer, Long>[] m, Hashtable<Integer, Long>[] m_poker)
    {
    	mCode = aCode;
    	moneys = m;
	moneys_poker = m_poker;
    }

    public void setSuccess(int aCode, boolean gh)
    {
    	mCode = aCode;
    	getHelp=gh;
    }

    public IResponseMessage createNew()
    {
        return new GetRoomMoneyResponse();
    }
}
