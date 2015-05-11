package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class AnPhomRequest extends AbstractRequestMessage {
	public long matchID;
    public int cardValue;
    
    public IRequestMessage createNew()
    {
        return new AnPhomRequest();
    }
}
