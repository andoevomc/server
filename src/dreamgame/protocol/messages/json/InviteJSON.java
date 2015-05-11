package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.InviteRequest;
import dreamgame.protocol.messages.InviteResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class InviteJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(InviteJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            InviteRequest invite = (InviteRequest) aDecodingObj;
            invite.roomID = jsonData.getLong("room_id");
            invite.destUid = jsonData.getLong("dest_uid");
            invite.sourceUid = jsonData.getInt("source_uid");
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
            InviteResponse invite = (InviteResponse) aResponseMessage;
            encodingObj.put("code", invite.mCode);
            if (invite.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", invite.mErrorMsg);
            } else if (invite.mCode == ResponseCode.SUCCESS) {
                
            	encodingObj.put("source_uid", invite.sourceID);
            	encodingObj.put("room_id", invite.roomID);
            	encodingObj.put("room_name", invite.roomName);
            	encodingObj.put("source_username", invite.sourceUserName);
            	encodingObj.put("minBet", invite.minBet);
                encodingObj.put("level", invite.level);
                encodingObj.put("zone", invite.currentZone);
                
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
