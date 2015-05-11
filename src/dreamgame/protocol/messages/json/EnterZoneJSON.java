package dreamgame.protocol.messages.json;

import dreamgame.data.CuocEntity;
import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.EnterZoneRequest;
import dreamgame.protocol.messages.EnterZoneResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONArray;

public class EnterZoneJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(EnterZoneJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            EnterZoneRequest enterZone = (EnterZoneRequest) aDecodingObj;
            enterZone.zoneID = jsonData.getInt("zone");

            if (jsonData.has("zoneLevel"))
            {
                enterZone.zoneLevel = jsonData.getInt("zoneLevel");
                enterZone.channelId = jsonData.getInt("channelId");
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
            EnterZoneResponse enterZone = (EnterZoneResponse) aResponseMessage;
            encodingObj.put("code", enterZone.mCode);
            if (enterZone.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", enterZone.mErrorMsg);
            } else if (enterZone.mCode == ResponseCode.SUCCESS) {

                if (enterZone.zoneId==ZoneID.GAME_CHAN){
                    JSONArray cc = new JSONArray();
                    for (CuocEntity ce : DatabaseDriver.cuocList) {
                        JSONObject jAdv = new JSONObject();
                        jAdv.put("id", ce.id);
                        jAdv.put("name", ce.name);
                        jAdv.put("point", ce.point);
                        jAdv.put("dich", ce.dich);
                        cc.put(jAdv);
                    }
                    encodingObj.put("CuocList", cc);
                }
                if (enterZone.channelId > 0)
                {                                        
                    encodingObj.put("channelId", enterZone.channelId);                    
                    encodingObj.put("maxRoom", DatabaseDriver.maxRoom );                    
                }else
                {
                    encodingObj.put("maxChannel", DatabaseDriver.maxChannel );
                }

            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
