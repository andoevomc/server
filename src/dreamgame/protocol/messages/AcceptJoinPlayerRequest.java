

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author Dinhpv
 */
public class AcceptJoinPlayerRequest extends AbstractRequestMessage {

    public long mMatchId;
    public long uid;
    public boolean isAccept;
    public IRequestMessage createNew()
    {
        return new AcceptJoinPlayerRequest();
    }
}
