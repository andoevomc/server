package dreamgame.protocol.messages;

import dreamgame.data.PostEntity;
import java.util.Vector;


import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetPostListResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public Vector<PostEntity> mPostList;

    public void setSuccess(int aCode, Vector<PostEntity> aPostList) {
        mCode = aCode;
        mPostList = aPostList;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetPostListResponse();
    }
}
