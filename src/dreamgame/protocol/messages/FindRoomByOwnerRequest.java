package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class FindRoomByOwnerRequest extends AbstractRequestMessage
{
    public String roomOwner;
    public IRequestMessage createNew()
    {
        return new FindRoomByOwnerRequest();
    }
}
