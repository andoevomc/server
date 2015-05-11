package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;
import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.KickOutRequest;
import dreamgame.protocol.messages.KickOutResponse;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class KickOutJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(StartJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // request messsage
            KickOutRequest kickOut = (KickOutRequest) aDecodingObj;
            // parsing
            kickOut.mMatchId = jsonData.getLong("match_id");
            kickOut.uid = jsonData.getLong("uid");
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
            KickOutResponse kickOut = (KickOutResponse) aResponseMessage;
            encodingObj.put("code", kickOut.mCode);
            if (kickOut.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", kickOut.message);
            } else if (kickOut.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("matchId", kickOut.matchId);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
    
}
