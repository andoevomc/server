/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.AcceptJoinPlayerRequest;
import dreamgame.protocol.messages.AcceptJoinPlayerResponse;
import dreamgame.protocol.messages.AcceptJoinRequest;
import dreamgame.protocol.messages.AcceptJoinResponse;
import dreamgame.protocol.messages.PeaceAcceptRequest;
import dreamgame.protocol.messages.PeaceAcceptResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Dinhpv
 */
public class PeaceAcceptJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(PeaceAcceptJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            PeaceAcceptRequest acceptJoin = (PeaceAcceptRequest) aDecodingObj;
            acceptJoin.uid = jsonData.getLong("uid");
            acceptJoin.isAccept = jsonData.getBoolean("isAccept");
            acceptJoin.mMatchId = jsonData.getLong("match_id");
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
            PeaceAcceptResponse acceptJoin = (PeaceAcceptResponse) aResponseMessage;
            encodingObj.put("code", acceptJoin.mCode);
            if (acceptJoin.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", acceptJoin.message);
            } else if (acceptJoin.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("ownerMoney", acceptJoin.ownerMoney);
                encodingObj.put("playerMoney", acceptJoin.playerMoney);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
