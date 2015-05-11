package dreamgame.protocol.messages.json;



//import com.migame.protocol.messages.KeepConnectionResponse;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

import org.json.JSONObject;
import org.slf4j.Logger;
import dreamgame.data.ResponseCode;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class KeepConnectionJSON implements IMessageProtocol
{
    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(StartJSON.class);
    
    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
            return true;
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException
    {
         try {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            encodingObj.put("code", 1);
            return encodingObj;
            
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }	
}
