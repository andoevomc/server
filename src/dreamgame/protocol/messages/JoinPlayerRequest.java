package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author Dinhpv
 */
public class JoinPlayerRequest extends AbstractRequestMessage {

    public long mMatchId;

    public IRequestMessage createNew() {
        return new JoinPlayerRequest();
    }
}
