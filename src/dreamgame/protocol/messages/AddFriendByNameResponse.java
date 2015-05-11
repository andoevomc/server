package dreamgame.protocol.messages;

import dreamgame.data.UserEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class AddFriendByNameResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public UserEntity user;
    public void setSuccess(int aCode, UserEntity u)
    {
        mCode = aCode;
        user = u;
    }

    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new AddFriendByNameResponse();
    }
}
