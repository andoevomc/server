package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class AddFriendRequest extends AbstractRequestMessage
{
	public long currID;
	public long friendID;
    public IRequestMessage createNew()
    {
        return new AddFriendRequest();
    }
}
