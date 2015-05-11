package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.protocol.messages.AllReadyResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class AllReadyJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AllReadyJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException
    {
        try
        {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            AllReadyResponse matchAllReady = (AllReadyResponse) aResponseMessage;
            encodingObj.put("code", matchAllReady.mCode);
            // response encoded obj
            return encodingObj;
        } catch (Throwable t)
        {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
