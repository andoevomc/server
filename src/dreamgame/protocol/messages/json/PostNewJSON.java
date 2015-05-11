/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;
import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.PostNewRequest;
import dreamgame.protocol.messages.PostNewResponse;
import dreamgame.protocol.messages.SuggestRequest;
import dreamgame.protocol.messages.SuggestResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Dinhpv
 */
public class PostNewJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(
            SuggestJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj)
            throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            PostNewRequest matchStart = (PostNewRequest) aDecodingObj;
            matchStart.name = jsonData.getString("name");
            matchStart.note = jsonData.getString("post");
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage)
            throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            PostNewResponse suggest = (PostNewResponse) aResponseMessage;
            encodingObj.put("code", suggest.mCode);
            if (suggest.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", suggest.mErrorMsg);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
