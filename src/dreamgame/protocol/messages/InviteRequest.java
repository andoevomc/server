package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class InviteRequest extends AbstractRequestMessage
{

    public long roomID;
    public long destUid;
    public long sourceUid;


    public IRequestMessage createNew()
    {
        return new InviteRequest();
    }
}
