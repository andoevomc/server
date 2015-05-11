package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Dinhpv
 */
public class JoinPlayerResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public long mUid;
    public long money;
    public int avatarID;
    public int level;
    public String username;

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode, long aUid, long mn, int avatar, int lev, String username_) {
        mCode = aCode;
        mUid = aUid;
        money = mn;
        avatarID = avatar;
        level = lev;
        username = username_;
    }

    public IResponseMessage createNew() {
        return new JoinPlayerResponse();
    }
}
