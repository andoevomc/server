package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.BuyAvatarRequest;
import dreamgame.protocol.messages.BuyAvatarResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class BuyAvatarJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(BuyAvatarJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try
        {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // cancel request message
            BuyAvatarRequest buy = (BuyAvatarRequest) aDecodingObj;
            // decode
            try {
                if (jsonData.has("avatar"))
                    buy.avatarID = jsonData.getInt("avatar");
                if (jsonData.has("uid"))
                    buy.uid = jsonData.getLong("uid");

                if (jsonData.has("change_award"))
                    buy.change_award = jsonData.getBoolean("change_award");
                
                if (jsonData.has("uploadAvatar")){
                    buy.uploadAvatar=jsonData.getBoolean("uploadAvatar");
                    buy.data=jsonData.getString("data");
                }
                
                if (jsonData.has("chargeCard"))
                {
                    buy.chargeCard=jsonData.getBoolean("chargeCard");
                    buy.cardId=jsonData.getInt("cardId");
                    buy.serial=jsonData.getString("serial");
                    buy.code=jsonData.getString("code");
                }
                
                if (jsonData.has("chargeAppleMoney"))
                {
                    buy.chargeAppleMoney=jsonData.getBoolean("chargeAppleMoney");                                        
                    buy.cardId=jsonData.getInt("chargeId");                    
                }
                
                
            }catch (Exception e) {
				// TODO: handle exception
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
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            BuyAvatarResponse buy = (BuyAvatarResponse) aResponseMessage;
            encodingObj.put("code", buy.mCode);
            if (buy.mCode == ResponseCode.FAILURE)
            {
                encodingObj.put("error_msg", buy.errMessage);
               
            } else if (buy.mCode == ResponseCode.SUCCESS)
            {                
            	encodingObj.put("avatar", buy.new_avatar);
            	encodingObj.put("cash", buy.new_cash);
                
                if (buy.change_award)
                {
                    encodingObj.put("change_award", true);                    
                }
                            
                if (buy.chargeAppleMoney)
                {
                    encodingObj.put("chargeAppleMoney", true);                    
                }
                if (buy.chargeMsg.length()>0)
                {
                    encodingObj.put("chargeMsg", buy.chargeMsg);
                }
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
