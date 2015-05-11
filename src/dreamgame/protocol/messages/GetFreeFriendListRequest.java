package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetFreeFriendListRequest extends AbstractRequestMessage
{

    
    public int level;
    public IRequestMessage createNew()
    {
        return new GetFreeFriendListRequest();
    }

}
