package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class AddFriendByNameRequest extends AbstractRequestMessage
{
	public String friendName;
    public IRequestMessage createNew()
    {
        return new AddFriendByNameRequest();
    }
}
