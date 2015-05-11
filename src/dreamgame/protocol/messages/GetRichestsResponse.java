package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.data.UserEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetRichestsResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public Vector<UserEntity> mRichestsList;

    public void setSuccess(int aCode, Vector<UserEntity> aRichestList) {
        mCode = aCode;
        mRichestsList = aRichestList;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetRichestsResponse();
    }
}
