package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.JoinPlayerRequest;
import dreamgame.protocol.messages.JoinPlayerResponse;
import org.slf4j.Logger;


import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONObject;

/**
 *
 * @author Dinhpv
 */
public class JoinPlayerJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(JoinPlayerJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            JoinPlayerRequest joinPlayer = (JoinPlayerRequest) aDecodingObj;
            joinPlayer.mMatchId = jsonData.getLong("match_id");
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            JoinPlayerResponse acceptJoin = (JoinPlayerResponse) aResponseMessage;
            encodingObj.put("code", acceptJoin.mCode);
            if (acceptJoin.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", acceptJoin.mErrorMsg);
            } else if (acceptJoin.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("uid", acceptJoin.mUid);
                encodingObj.put("money", acceptJoin.money);
                encodingObj.put("avatar", acceptJoin.avatarID);
                encodingObj.put("level", acceptJoin.level);
                encodingObj.put("username", acceptJoin.username);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
