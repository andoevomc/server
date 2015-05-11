package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.WaitingAcceptResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class WaitingAcceptJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(WaitingAcceptJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            WaitingAcceptResponse waiting = (WaitingAcceptResponse) aResponseMessage;
            encodingObj.put("code", waiting.mCode);
            if (waiting.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", waiting.mErrorMsg);
            } else if (waiting.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("uid", waiting.mUid);
                encodingObj.put("money", waiting.money);
                encodingObj.put("avatar", waiting.avatarID);
                encodingObj.put("level", waiting.level);
                encodingObj.put("username", waiting.username);

            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
