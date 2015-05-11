/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.NewRequest;
import dreamgame.protocol.messages.NewResponse;

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
public class NewJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(NewJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            NewRequest matchNew = (NewRequest) aDecodingObj;
            matchNew.mid = jsonData.getLong("mid");
            matchNew.uid = jsonData.getLong("uid");            
            matchNew.zise = jsonData.getInt("size");
            matchNew.roomName = jsonData.getString("roomname");

	    if (jsonData.has("roomtype")) {
		matchNew.roomType = jsonData.getInt("roomtype");
            }
	    
            if (jsonData.has("roomPosition")) {
                matchNew.roomPosition = jsonData.getInt("roomPosition");
            }

            try {
                matchNew.mRow = jsonData.getInt("row");
                matchNew.mCol = jsonData.getInt("col");
            } catch (Exception e) {
            }
	    
            try {
                matchNew.moneyBet = jsonData.getLong("money");
            } catch (Exception e) {
            }

            //phom
            if (jsonData.has("isAn")) {
                matchNew.isAn = jsonData.getBoolean("isAn");

                if (jsonData.has("isUkhan")) {
                    matchNew.isKhan = jsonData.getBoolean("isUkhan");
                } else {
                    matchNew.isKhan = false;
                }
                if (jsonData.has("isTaigui")) {
                    matchNew.isTai = jsonData.getBoolean("isTaigui");
                }else{
                    matchNew.isTai = false;
                }
                if (jsonData.has("testCode")) {
                    matchNew.testCode = jsonData.getInt("testCode");
                }
            } else {
                matchNew.isAn = false;
            }

            try {
                matchNew.zise = jsonData.getInt("size");
            } catch (Exception e1) {
                matchNew.zise = 4; // default
            }
            try {
                matchNew.password = jsonData.getString("password");
            } catch (Exception e) {
                matchNew.password = null;
            }
            //cờ tướng : chấp quân
            try {
                if (jsonData.has("available")) {
                    matchNew.available = jsonData.getInt("available");
                }
                if (jsonData.has("totalTime")) {
                    matchNew.totalTime = jsonData.getInt("totalTime");
                }

            } catch (Exception e1) {
                matchNew.available = 0; // default
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
            NewResponse matchNew = (NewResponse) aResponseMessage;
            encodingObj.put("code", matchNew.mCode);
            if (matchNew.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchNew.mErrorMsg);
            } else if (matchNew.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("match_id", matchNew.mMatchId);
                encodingObj.put("uid", matchNew.uid);
                encodingObj.put("minBet", matchNew.minBet);
                encodingObj.put("capacity", matchNew.capacity);
                encodingObj.put("ownerCash", matchNew.ownerCash);
                encodingObj.put("available", matchNew.available);
                encodingObj.put("totalTime", matchNew.totalTime);
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
