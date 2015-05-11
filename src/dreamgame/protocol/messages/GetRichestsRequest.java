package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetRichestsRequest extends AbstractRequestMessage
{

    public IRequestMessage createNew()
    {
        return new GetRichestsRequest();
    }
}
