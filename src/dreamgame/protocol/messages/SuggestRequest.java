package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class SuggestRequest extends AbstractRequestMessage {
    
    public long uid;
    public String note;
    public IRequestMessage createNew()
    {
        return new SuggestRequest();
    }
}
