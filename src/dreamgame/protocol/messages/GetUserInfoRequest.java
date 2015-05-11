package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetUserInfoRequest extends AbstractRequestMessage
{

    public long mUid;

    public IRequestMessage createNew()
    {
        return new GetUserInfoRequest();
    }

}
