package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.OutResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class OutJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(OutJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            OutResponse matchOut = (OutResponse) aResponseMessage;
            encodingObj.put("code", matchOut.mCode);
            if (matchOut.mCode == ResponseCode.FAILURE) {
            } else if (matchOut.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("uid", matchOut.mUid);
                encodingObj.put("username", matchOut.username);
                encodingObj.put("message", matchOut.message);
                encodingObj.put("out_room", matchOut.out);
                encodingObj.put("matchId", matchOut.matchId);
                
                if (matchOut.newRoomOwner > 0) {
                    encodingObj.put("newOwner", matchOut.newRoomOwner);
                }

            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
