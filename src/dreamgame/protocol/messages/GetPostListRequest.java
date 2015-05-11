package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetPostListRequest extends AbstractRequestMessage {

    public int start = 0;
    public int length = 15;

    public IRequestMessage createNew() {
        return new GetPostListRequest();
    }
}
