package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class KickOutRequest extends AbstractRequestMessage {

    public long mMatchId;
    public long uid;
    public boolean isAutoKickOut = false;
    public long newOwner = 0;

    public IRequestMessage createNew() {
        return new KickOutRequest();
    }
}
