package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class RestartRequest extends AbstractRequestMessage {

    public long mMatchId;
    public long money;
    public long uid;

    public IRequestMessage createNew() {
        return new RestartRequest();
    }
}
