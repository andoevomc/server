package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetWaitingListRequest extends AbstractRequestMessage
{

    public boolean compress=false;
    public int mOffset;
    public int mLength;
    public int level;
    public int minLevel;
    public int channelId=0;
    public IRequestMessage createNew()
    {
        return new GetWaitingListRequest();
    }

}
