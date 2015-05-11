package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class BuyLevelRequest extends AbstractRequestMessage
{
    public int level;
    public long uid;
    public IRequestMessage createNew()
    {
        return new BuyLevelRequest();
    }
}
