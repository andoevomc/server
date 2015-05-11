package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.BocPhomRequest;
import dreamgame.protocol.messages.BocPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class BocPhomJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(BocPhomJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // cancel request message
            BocPhomRequest boc = (BocPhomRequest) aDecodingObj;

            boc.matchID = jsonData.getLong("match_id");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            BocPhomResponse boc = (BocPhomResponse) aResponseMessage;
            encodingObj.put("code", boc.mCode);
            encodingObj.put("uid", boc.uid);
            if (boc.mCode == ResponseCode.SUCCESS) {
                if (boc.card != -1) {
                    encodingObj.put("card", boc.card);

                    encodingObj.put("phom", boc.phom);
                    encodingObj.put("u", boc.u);
                    encodingObj.put("haBaiFlag", boc.haBaiFlag);

                } else {
                    encodingObj.put("card", 0);
                }
            } else {
                encodingObj.put("error", boc.message);
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
