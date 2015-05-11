package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class ChatRequest extends AbstractRequestMessage
{

    public long mRoomId;
    public String mMessage;

    public int type;

    public IRequestMessage createNew()
    {
        return new ChatRequest();
    }
}
