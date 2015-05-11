package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.RegisterRequest;
import dreamgame.protocol.messages.RegisterResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class RegisterJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(RegisterJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try
        {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            RegisterRequest register = (RegisterRequest) aDecodingObj;
            
            register.mUsername = jsonData.getString("username");
            register.mPassword = jsonData.getString("password");
            register.isMale = jsonData.getBoolean("male");
            register.mAge =  jsonData.getInt("age");
            register.mail = jsonData.getString("mail");
            register.phone =  jsonData.getString("phone");
            
            if (jsonData.has("clientType"))
                register.clientType=jsonData.getString("clientType");
            
            if (jsonData.has("cp"))
                register.cp =  jsonData.getString("cp");
            if (jsonData.has("downloadid"))
                register.downloadid =  jsonData.getInt("downloadid");
            if (jsonData.has("mobileVersion"))
                register.mobileVersion=jsonData.getString("mobileVersion");

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
            System.out.println("mid " + aResponseMessage.getID());
            encodingObj.put("mid", aResponseMessage.getID());
            RegisterResponse register = (RegisterResponse) aResponseMessage;
            encodingObj.put("code", register.mCode);
            if (register.mCode == ResponseCode.FAILURE)
            {
                encodingObj.put("error_msg", register.mErrorMsg);
            }
            else if (register.mCode == ResponseCode.SUCCESS)
            {
                
                encodingObj.put("uid", register.mUid);
                encodingObj.put("money", register.money);
                encodingObj.put("avatar", register.avatarID);
                encodingObj.put("level", register.level);

                encodingObj.put("gameSMS", register.smsNumber);
                encodingObj.put("contentSMS", register.smsContent);
                encodingObj.put("messageSMS", register.smsMessage);                
                encodingObj.put("smsValue", register.smsValue);

                encodingObj.put("activeSystem",DatabaseDriver.activeSystem);
                
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
