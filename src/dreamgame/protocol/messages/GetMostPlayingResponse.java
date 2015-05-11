package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.data.UserEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetMostPlayingResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public Vector<UserEntity> mMostPlayingist;

    public void setSuccess(int aCode, Vector<UserEntity> aMostPlayingList) {
        mCode = aCode;
        mMostPlayingist = aMostPlayingList;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetMostPlayingResponse();
    }
}
