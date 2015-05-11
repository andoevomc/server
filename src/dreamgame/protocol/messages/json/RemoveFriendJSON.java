package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.RemoveFriendRequest;
import dreamgame.protocol.messages.RemoveFriendResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class RemoveFriendJSON implements IMessageProtocol {

    private final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(RemoveFriendJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            RemoveFriendRequest removeFriend = (RemoveFriendRequest) aDecodingObj;
            removeFriend.currID = jsonData.getLong("uid");
            removeFriend.friendID = jsonData.getLong("friend_uid");
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            RemoveFriendResponse removeFriend = (RemoveFriendResponse) aResponseMessage;
            encodingObj.put("code", removeFriend.mCode);
            if (removeFriend.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", removeFriend.mErrorMsg);
            } else if (removeFriend.mCode == ResponseCode.SUCCESS) {
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
