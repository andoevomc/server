package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetRoomMoneyRequest extends AbstractRequestMessage
{
    public boolean getHelp=false;
    public IRequestMessage createNew()
    {
        return new GetRoomMoneyRequest();
    }
}
