package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.data.UserEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetBestPlayerResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public Vector<UserEntity> mBestPlayerList;

    public void setSuccess(int aCode, Vector<UserEntity> aBestList) {
        mCode = aCode;
        mBestPlayerList = aBestList;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetBestPlayerResponse();
    }
}
