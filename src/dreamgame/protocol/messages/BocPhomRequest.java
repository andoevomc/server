package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class BocPhomRequest extends AbstractRequestMessage {
	public long matchID;
        public long uid=-1;
        
    public IRequestMessage createNew()
    {
        return new BocPhomRequest();
    }
}
