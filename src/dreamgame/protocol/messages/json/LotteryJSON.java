/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;
import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.LotteryRequest;
import dreamgame.protocol.messages.LotteryResponse;
import dreamgame.protocol.messages.PostCommentRequest;
import dreamgame.protocol.messages.PostCommentResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Dinhpv
 */
public class LotteryJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(
            SuggestJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj)
            throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            LotteryRequest lot = (LotteryRequest) aDecodingObj;
            lot.type = jsonData.getInt("type");
            if (jsonData.has("date"))
                lot.date=jsonData.getInt("date");
            
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
            LotteryResponse lot = (LotteryResponse) aResponseMessage;
            encodingObj.put("code", lot.mCode);
            
                        
            if (lot.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", lot.mErrorMsg);
                
            }else{
                encodingObj.put("lottery", lot.lotRes);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
