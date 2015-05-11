package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class OutResponse extends AbstractResponseMessage {

    public long mUid;
    public String message;
    public String username;
    public long newRoomOwner = 0;
    public int out;

    public void setSuccess(int aCode, long aUid, String m, String u, int o) {
        mCode = aCode;
        mUid = aUid;
        message = m;
        username = u;
        out = o;
    }

    public void setNewRoomOwner(long newOwner) {
        newRoomOwner = newOwner;
    }

    public IResponseMessage createNew() {
        return new OutResponse();
    }
}
