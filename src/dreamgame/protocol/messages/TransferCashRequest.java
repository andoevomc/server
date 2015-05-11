package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class TransferCashRequest extends AbstractRequestMessage
{
    public long money;
    public long source_uid;
    public long desc_uid;
    public IRequestMessage createNew()
    {
        return new TransferCashRequest();
    }
}
