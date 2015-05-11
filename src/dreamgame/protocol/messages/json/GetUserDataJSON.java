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
import dreamgame.protocol.messages.GetUserDataRequest;
import dreamgame.protocol.messages.GetUserDataResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author thohd
 */
public class GetUserDataJSON implements IMessageProtocol {

    private final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetUserDataJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            GetUserDataRequest rq = (GetUserDataRequest) aDecodingObj;

            if (jsonData.has("getImage")) {
                rq.getImage = jsonData.getInt("getImage");
                rq.uid = jsonData.getInt("uid");
            }
            if (jsonData.has("getUserAvatar")) {
                rq.getUserAvatar = jsonData.getInt("getUserAvatar");
            }

            if (jsonData.has("chargeAppleMoney"))
                {
                    rq.chargeAppleMoney=jsonData.getBoolean("chargeAppleMoney");                                        
                    rq.cardId=jsonData.getInt("chargeId");                    
                }
            
            if (jsonData.has("chargeCard")) {
                rq.chargeCard = jsonData.getBoolean("chargeCard");
                rq.cardId = jsonData.getInt("cardId");
                rq.serial = jsonData.getString("serial");
                rq.code = jsonData.getString("code");
                if(jsonData.has("cp")){
                    rq.cp = jsonData.getString("cp");
                }
            }

            if (jsonData.has("start")) {
                rq.start = jsonData.getInt("start");
            }
            if (jsonData.has("uid")) {
                rq.uid = jsonData.getInt("uid");
            }
            if (jsonData.has("length")) {
                rq.length = jsonData.getInt("length");
            }
            if (jsonData.has("getChargeHistory")) {
                rq.getChargeHistory = jsonData.getBoolean("getChargeHistory");
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
            GetUserDataResponse res = (GetUserDataResponse) aResponseMessage;
            encodingObj.put("code", res.mCode);

            if (res.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", res.mErrorMsg);
            } else if (res.mCode == ResponseCode.SUCCESS) {
                JSONArray arrRooms = new JSONArray();

                if (res.chargeAppleMoney)
                {
                    encodingObj.put("chargeAppleMoney", true);          
                    encodingObj.put("cash", res.new_cash);
                }
                
                if (res.chargeMsg.length() > 0) {
                    encodingObj.put("cash", res.newMoney);
                    encodingObj.put("chargeMsg", res.chargeMsg);
                } else if (res.getHistroy) {
                    encodingObj.put("getHistory", true);
                    encodingObj.put("newMoney", res.newMoney);

                    if (res.mChargeHistory != null) {
                        for (ChargeHistoryEntity avaEntity : res.mChargeHistory) {
                            JSONObject jRoom = new JSONObject();

                            jRoom.put("phone", avaEntity.phone);
                            jRoom.put("time", avaEntity.time);
                            jRoom.put("money", avaEntity.money);
                            arrRooms.put(jRoom);

                        }
                    }
                    encodingObj.put("history", arrRooms);

                } else if (res.getImage && res.imageData.length() > 0) {
                    encodingObj.put("getImage", true);
                    encodingObj.put("imageData", res.imageData);
                    encodingObj.put("uid", res.uid);
                }
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
