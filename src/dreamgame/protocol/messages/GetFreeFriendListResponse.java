package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.data.UserEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetFreeFriendListResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public Vector<UserEntity> mFrientList;

    public void setSuccess(int aCode, Vector<UserEntity> aFrientList) {
        mCode = aCode;
        mFrientList = aFrientList;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetFreeFriendListResponse();
    }
}
