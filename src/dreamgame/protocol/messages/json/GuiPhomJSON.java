package dreamgame.protocol.messages.json;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.GuiPhomRequest;
import dreamgame.protocol.messages.GuiPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;


public class GuiPhomJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(GuiPhomJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try {
        	// request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // cancel request message
            GuiPhomRequest gui = (GuiPhomRequest) aDecodingObj;
            
            gui.matchID = jsonData.getLong("match_id");
            gui.dUID = jsonData.getLong("d_uid");
            gui.phomID = jsonData.getInt("phom");
            gui.cards = jsonData.getString("cards");
//            JSONArray cardsJSON = jsonData.getJSONArray("cards");
//            for(int i = 0; i < cardsJSON.length(); i++){
//            	JSONObject c = cardsJSON.getJSONObject(i);
//            	gui.cards.add(c.getInt("card"));
//
//            }
        	return true;
        }catch (Exception e) {
			return false;
		}
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException
    {
        try
        {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            GuiPhomResponse gui = (GuiPhomResponse) aResponseMessage;
            encodingObj.put("code", gui.mCode);
            if(gui.mCode == ResponseCode.SUCCESS){
            	encodingObj.put("d_uid", gui.dUID);
            	encodingObj.put("s_uid", gui.sUID);
            	encodingObj.put("phomID", gui.phomID);
                
                encodingObj.put("u", gui.u);
                encodingObj.put("phom", gui.phom);
            	
//            	JSONArray cardsJSON = new JSONArray();
//        		for(int c : gui.cards){
//        			JSONObject obj = new JSONObject();
//        			obj.put("card", c);
//        			cardsJSON.put(obj);
//        		}
        		encodingObj.put("cards", gui.cards);
            }else {
            	encodingObj.put("error", gui.message);
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t)
        {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
