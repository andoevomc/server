package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class RemoveFriendRequest extends AbstractRequestMessage
{
	public long currID;
	public long friendID;
    public IRequestMessage createNew()
    {
        return new RemoveFriendRequest();
    }
}
