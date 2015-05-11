package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.StartedResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class StartedJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(StartedJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            StartedResponse matchStarted = (StartedResponse) aResponseMessage;
            encodingObj.put("code", matchStarted.mCode);
            if (matchStarted.mCode == ResponseCode.FAILURE) {
                //encodingObj.put("Error", matchStarted.mCode);
            } else if (matchStarted.mCode == ResponseCode.SUCCESS) {
                try {
                    encodingObj.put("uid", matchStarted.starterID);
                } catch (Exception e) {
                }
                try {
                    encodingObj.put("isFinalFight", matchStarted.isFinalFight);
                } catch (Exception e) {
                }

            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
