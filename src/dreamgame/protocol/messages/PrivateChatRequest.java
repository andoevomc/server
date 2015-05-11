package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class PrivateChatRequest extends AbstractRequestMessage
{

    public long sourceUid;
    public long destUid;
    public String mMessage;


    public IRequestMessage createNew()
    {
        return new PrivateChatRequest();
    }
}
