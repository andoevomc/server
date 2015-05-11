/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.TimeOutRequest;
import dreamgame.protocol.messages.TimeOutResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Admin
 */
public class TimeOutJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(TimeOutJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj)
            throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            TimeOutRequest timeOut = (TimeOutRequest) aDecodingObj;
            timeOut.player_friend_id = jsonData.getLong("player_friend_id");
            timeOut.mMatchId = jsonData.getLong("match_id");
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage)
            throws ServerException {
        try {
            TimeOutResponse resTimeOut = (TimeOutResponse) aResponseMessage;
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            encodingObj.put("code", resTimeOut.mCode);
            encodingObj.put("player_friend_id", resTimeOut.player_friend_id);
            encodingObj.put("timeout_player_name", resTimeOut.timeout_player_name);
            if (resTimeOut.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", resTimeOut.errMgs);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
