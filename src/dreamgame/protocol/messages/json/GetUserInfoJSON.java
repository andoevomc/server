package dreamgame.protocol.messages.json;


import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.GetUserInfoRequest;
import dreamgame.protocol.messages.GetUserInfoResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONObject;
import org.slf4j.Logger;

public class GetUserInfoJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(GetUserInfoJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try
        {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            GetUserInfoRequest getUserInfo = (GetUserInfoRequest) aDecodingObj;
            getUserInfo.mUid = jsonData.getLong("uid");
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
            GetUserInfoResponse getUserInfo = (GetUserInfoResponse) aResponseMessage;
            encodingObj.put("code", getUserInfo.mCode);
            if (getUserInfo.mCode == ResponseCode.FAILURE)
            {
                encodingObj.put("error_msg", getUserInfo.mErrorMsg);
            } else if (getUserInfo.mCode == ResponseCode.SUCCESS)
            {
                encodingObj.put("uid", getUserInfo.mUid);
                encodingObj.put("username", getUserInfo.mUsername);
                encodingObj.put("age", getUserInfo.mAge);
                encodingObj.put("is_male", getUserInfo.mIsMale);
                encodingObj.put("avatar", getUserInfo.AvatarID);
                encodingObj.put("money", getUserInfo.money);
                encodingObj.put("level", getUserInfo.level);
                encodingObj.put("playsNumber", getUserInfo.playsNumber);
                encodingObj.put("is_friend", getUserInfo.isFriend);
                encodingObj.put("award", getUserInfo.award);
            }
            return encodingObj;
        } catch (Throwable t)
        {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }

}
