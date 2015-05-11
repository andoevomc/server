package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class TransferCashResponse extends AbstractResponseMessage
{

	public String errMessage;
	public long money;
	public long source_uid;
	public long desc_uid;
	public boolean is_source;
	public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        errMessage = aErrorMsg;
    }
    public void setSuccess(int aCode, long s, long d, long m, boolean i)
    {
        mCode = aCode;
        money = m;
        source_uid = s;
        desc_uid = d;
        is_source = i;
    }

    public IResponseMessage createNew()
    {
        return new TransferCashResponse();
    }
}
