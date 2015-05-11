package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class AcceptJoinRequest extends AbstractRequestMessage {
	
    public long mMatchId;
    public long uid;
    public String password;
    public boolean isAccept;
    public IRequestMessage createNew()
    {
        return new AcceptJoinRequest();
    }
}
