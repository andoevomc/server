package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.AnPhomRequest;
import dreamgame.protocol.messages.AnPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;


public class AnPhomJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AnPhomJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try {
        	// request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // cancel request message
            AnPhomRequest an = (AnPhomRequest) aDecodingObj;
            
            an.matchID = jsonData.getLong("match_id");
            
            if (jsonData.has("cardValue")){
                an.cardValue=jsonData.getInt("cardValue");
            }
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
            AnPhomResponse an = (AnPhomResponse) aResponseMessage;
            encodingObj.put("code", an.mCode);
            if(an.mCode == ResponseCode.SUCCESS){
                
                encodingObj.put("money", an.money);
                encodingObj.put("uid", an.uid);
                encodingObj.put("p_uid", an.p_uid);
                encodingObj.put("prePlayer", an.prePlayer);
                
                encodingObj.put("phom", an.phom);
                encodingObj.put("u", an.u);
                encodingObj.put("haBaiFlag", an.haBaiFlag);
                
                encodingObj.put("cardValue", an.cardValue);
                
                encodingObj.put("swap1", an.swap1);
                encodingObj.put("swap2", an.swap2);
            }else {
            	encodingObj.put("error", an.message);
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
