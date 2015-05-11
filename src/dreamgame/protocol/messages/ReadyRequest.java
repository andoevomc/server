package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class ReadyRequest extends AbstractRequestMessage{
	public long matchID;
	public long uid;
    public IRequestMessage createNew()
    {
        return new ReadyRequest();
    }
}
