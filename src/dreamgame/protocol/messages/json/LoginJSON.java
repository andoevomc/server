package dreamgame.protocol.messages.json;

import dreamgame.data.AdvEntity;
import dreamgame.data.CuocEntity;
import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.LoginRequest;
import dreamgame.protocol.messages.LoginResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

public class LoginJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(LoginJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // plain obj
            LoginRequest login = (LoginRequest) aDecodingObj;
            // decoding

            login.mUsername = jsonData.getString("username");
            login.mPassword = jsonData.getString("password");

            if (jsonData.has("updateLoginMessage"))
            {
                login.updateLoginMessage=jsonData.getBoolean("updateLoginMessage");
            }
            
            if (jsonData.has("clientType"))
                login.clientType=jsonData.getString("clientType");
                
            if (jsonData.has("cp"))
                login.cp=jsonData.getString("cp");
            
            if (jsonData.has("downloadid"))
                login.downloadid=jsonData.getInt("downloadid");
            
            if (jsonData.has("flashVersion"))
                login.flashVersion=jsonData.getString("flashVersion");
            if (jsonData.has("mobileVersion"))
                login.mobileVersion=jsonData.getString("mobileVersion");
            
            if (jsonData.has("shutDown"))
                login.shutDown=jsonData.getBoolean("shutDown");
            if (jsonData.has("screen"))
                login.screen=jsonData.getString("screen");
            if (jsonData.has("device"))
                login.device=jsonData.getString("device");

            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            LoginResponse login = (LoginResponse) aResponseMessage;
            encodingObj.put("code", login.mCode);
            if (login.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", login.mErrorMsg);
                encodingObj.put("disconnect", login.disconnect);
                
                if(login.notActive){
                    encodingObj.put("isActive", false);
                    encodingObj.put("gameSMS", login.smsNumber);
                    encodingObj.put("contentSMS", login.smsContent);
                    encodingObj.put("messageSMS", login.smsMessage);
                    encodingObj.put("smsValue", login.smsActiveValue);
                }
                
            } else if (login.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("uid", login.mUid);
                encodingObj.put("money", login.money);
                encodingObj.put("avatar", login.avatarID);
                encodingObj.put("level", login.level);
                encodingObj.put("tuoc_vi", login.TuocVi);
                encodingObj.put("last_login", login.lastLogin);
//                encodingObj.put("version_id", login.lVersion.id);
//                encodingObj.put("version_link", login.lVersion.link);
//                encodingObj.put("version_desc", login.lVersion.desc);
                encodingObj.put("playsNumber", login.playNumber);
                encodingObj.put("moneyUpdateLevel", login.moneyUpdateLevel);
                
                encodingObj.put("gameSMS", login.smsNumber);
                encodingObj.put("contentSMS", login.smsContent);
                encodingObj.put("messageSMS", login.smsMessage);

                encodingObj.put("gameSMS2", login.smsNumber2);
                encodingObj.put("messageSMS2", login.smsMessage2);
                encodingObj.put("smsValue", login.smsValue);
                encodingObj.put("smsValue2", login.smsValue2);

                encodingObj.put("newAllowSms",DatabaseDriver.newAllowSms);
                encodingObj.put("allowSmsCharge",DatabaseDriver.allowSmsCharge);
                encodingObj.put("allowCardCharge",DatabaseDriver.allowCardCharge);
                
                encodingObj.put("allowIphoneCharge", DatabaseDriver.allowIphoneCharge);
                encodingObj.put("activeSystem",DatabaseDriver.activeSystem);
                
                
                if (login.newVer.length()>0)
                {
                    encodingObj.put("linkDown", login.linkDown);
                    encodingObj.put("newVer", login.newVer);
                }
                
                encodingObj.put("adminMessage", login.adminMessage);                
                encodingObj.put("eventAwardEnable", DatabaseDriver.eventAwardEnable);

                if (login.lastRoom>0)
                {
                    encodingObj.put("lastRoom", login.lastRoom);
                    encodingObj.put("lastRoomName", login.lastRoomName);
                    encodingObj.put("lastZone", login.zone_id);
                }             

                if (DatabaseDriver.noticeText.length()>0)
                {
                    encodingObj.put("noticeText",DatabaseDriver.noticeText);
                }
                
                if (login.jaOsCharge!=null){
                    encodingObj.put("osCharge",login.jaOsCharge);
                    
                    /*encodingObj.put("allowSmsCharge",false);
                    encodingObj.put("allowCardCharge",false);*/
                
                }

                if (login.chargeCards != null) {

                    JSONArray arr1 = new JSONArray();
                    for (int i = 0; i < login.chargeCards.length; i++) {
                        JSONObject jAdv = new JSONObject();
                        jAdv.put("id", login.chargeCards[i].cardId);
                        jAdv.put("name", login.chargeCards[i].cardName);
                        jAdv.put("numberInput", login.chargeCards[i].cardNumInput);
                        jAdv.put("cardMsg", login.chargeCards[i].cardMsg);
                        jAdv.put("len1", login.chargeCards[i].len1);
                        jAdv.put("len2", login.chargeCards[i].len2);
                        arr1.put(jAdv);
                    }
                    
                    encodingObj.put("ChargeCards", arr1);
                    
                }

                

                JSONArray arr = new JSONArray();
                for (AdvEntity adv : login.advs) {
                    JSONObject jAdv = new JSONObject();
                    jAdv.put("detail", adv.detail);
                    jAdv.put("link", adv.link);
                    arr.put(jAdv);
                }
                encodingObj.put("Adv", arr);

            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
