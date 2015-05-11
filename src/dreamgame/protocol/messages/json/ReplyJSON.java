/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.ReplyRequest;
import dreamgame.protocol.messages.ReplyResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author binh_lethanh
 */
public class ReplyJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ReplyJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            ReplyRequest matchReply = (ReplyRequest) aDecodingObj;
            matchReply.mMatchId = jsonData.getLong("match_id");
            matchReply.mIsAccept = jsonData.getBoolean("is_accept");
            matchReply.buddy_uid = jsonData.getLong("buddy_uid");
            matchReply.uid = jsonData.getLong("uid");
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
            ReplyResponse matchReply = (ReplyResponse) aResponseMessage;
            encodingObj.put("code", matchReply.mCode);
            if (matchReply.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchReply.mErrorMsg);
            } else if (matchReply.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("is_accept", matchReply.mIsAccept);
                if (matchReply.mIsAccept) {
                    encodingObj.put("source_uid", matchReply.source_uid);
                    encodingObj.put("name", matchReply.username);
                }
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
