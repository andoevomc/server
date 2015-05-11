package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.protocol.messages.AddFriendByNameRequest;
import dreamgame.protocol.messages.AddFriendByNameResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class AddFriendByNameJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AddFriendByNameJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try
        {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            AddFriendByNameRequest addF = (AddFriendByNameRequest) aDecodingObj;
            addF.friendName = jsonData.getString("friend_name");
            return true;
        } catch (Throwable t)
        {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException
    {
        try
        {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            AddFriendByNameResponse addF = (AddFriendByNameResponse) aResponseMessage;
            encodingObj.put("code", addF.mCode);
            if (addF.mCode == ResponseCode.FAILURE)
            {
                encodingObj.put("error_msg", addF.mErrorMsg);
            }
            else if (addF.mCode == ResponseCode.SUCCESS)
            {
            	UserEntity user = addF.user;
                encodingObj.put("uid", user.mUid);
                encodingObj.put("money", user.money);
                encodingObj.put("avatar", user.avatarID);
                encodingObj.put("level", user.level);
                encodingObj.put("last_login", user.lastLogin);
                encodingObj.put("playsNumber", user.playsNumber);
                
            }
            return encodingObj;
        } catch (Throwable t)
        {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
