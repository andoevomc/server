package dreamgame.protocol.messages.json;

import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetRoomMoneyRequest;
import dreamgame.protocol.messages.GetRoomMoneyResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetRoomMoneyJSON implements IMessageProtocol
{

    private final Logger mLog = 
    	LoggerContext.getLoggerFactory().getLogger(GetRoomMoneyJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try
        {
            
            JSONObject jsonData = (JSONObject) aEncodedObj;
            if (jsonData.has("getHelp"))
            {
                GetRoomMoneyRequest get = (GetRoomMoneyRequest) aDecodingObj;
                get.getHelp=jsonData.getBoolean("getHelp");
            }
            
            return true;
        } catch (Throwable t)
        {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException
    {
        try
        {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            GetRoomMoneyResponse getRoomMoney = (GetRoomMoneyResponse) aResponseMessage;
            encodingObj.put("code", getRoomMoney.mCode);
            if (getRoomMoney.mCode == ResponseCode.FAILURE)
            {
            	encodingObj.put("error_msg", getRoomMoney.mErrorMsg);
            } else if (getRoomMoney.mCode == ResponseCode.SUCCESS)
            {                
		JSONObject arrRooms = new JSONObject();
		JSONObject arrRooms_poker = new JSONObject();
                if (getRoomMoney.getHelp)
                {
                    encodingObj.put("getHelp", true);
                    encodingObj.put("helpContent", DatabaseDriver.helpContent);
                }
		else if (getRoomMoney.moneys != null && getRoomMoney.moneys_poker != null)
                {
		    JSONArray arrChannel;
		    for (int i = 0; i < getRoomMoney.moneys.length; i++) {
			arrChannel = new JSONArray();
			Enumeration<Integer> keys = getRoomMoney.moneys[i].keys();
			while(keys.hasMoreElements()){
			    int k = keys.nextElement();
			    long v = getRoomMoney.moneys[i].get(k);
//			    JSONObject jRoom = new JSONObject();
//			    jRoom.put("room_type", k);
//			    jRoom.put("money", v);			    
//			    arrChannel.put(jRoom);
			    arrChannel.put(v);
			}
			arrRooms.put(String.valueOf(i), arrChannel);
		    }
		    
		    for (int i = 0; i < getRoomMoney.moneys_poker.length; i++) {
			arrChannel = new JSONArray();
			Enumeration<Integer> keys = getRoomMoney.moneys_poker[i].keys();
			while(keys.hasMoreElements()){
			    int k = keys.nextElement();
			    long v = getRoomMoney.moneys_poker[i].get(k);
//			    JSONObject jRoom = new JSONObject();
//			    jRoom.put("room_type", k);
//			    jRoom.put("money", v);
//			    arrChannel.put(jRoom);
			    arrChannel.put(v);
			}
			arrRooms_poker.put(String.valueOf(i), arrChannel);
		    }
                }
                encodingObj.put("room_money_list", arrRooms);
		encodingObj.put("room_poker_money_list", arrRooms_poker);
            }
            return encodingObj;
        } catch (Throwable t)
        {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
