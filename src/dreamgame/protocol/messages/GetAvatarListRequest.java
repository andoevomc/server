package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GetAvatarListRequest extends AbstractRequestMessage {

    public boolean getChargeHistory = false;
    public boolean getGameInfo = false;
    public boolean turnOffServer = false;
    public boolean updateConfig = false;
    public boolean getImage = false;
    public boolean notification = false;
    public int start = 0;
    public int length = 6;
    public long uid = 0;

    public IRequestMessage createNew() {
        return new GetAvatarListRequest();
    }
}
