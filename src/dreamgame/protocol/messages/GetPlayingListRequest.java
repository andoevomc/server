package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetPlayingListRequest extends AbstractRequestMessage
{

    public int mOffset;
    public int mLength;

    public IRequestMessage createNew()
    {
        return new GetPlayingListRequest();
    }

}
