/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ChargeHistoryEntity;
import dreamgame.data.ResponseCode;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.protocol.messages.VerifyReceiptIphoneRequest;
import dreamgame.protocol.messages.VerifyReceiptIphoneResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author thohd
 */
public class VerifyReceiptIphoneJSON implements IMessageProtocol {

    private final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(VerifyReceiptIphoneJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            VerifyReceiptIphoneRequest rq = (VerifyReceiptIphoneRequest) aDecodingObj;

            if (jsonData.has("receipt")) {
                rq.receipt = jsonData.getString("receipt");
            }

            if (jsonData.has("isSandbox")) {
                rq.isSandbox = jsonData.getBoolean("isSandbox");
            }
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
            VerifyReceiptIphoneResponse res = (VerifyReceiptIphoneResponse) aResponseMessage;
            encodingObj.put("code", res.mCode);

            if (res.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", res.mErrorMsg);
            } 
            else if (res.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("cash", res.cash);
                encodingObj.put("chargeID", res.chargeID);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
