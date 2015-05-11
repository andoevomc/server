package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.TransferCashRequest;
import dreamgame.protocol.messages.TransferCashResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class TransferCashJSON implements IMessageProtocol
{

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(TransferCashJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException
    {
        try
        {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // cancel request message
            TransferCashRequest transfer = (TransferCashRequest) aDecodingObj;
            // decode
            try {
            	transfer.desc_uid = jsonData.getInt("desc_uid");
            	transfer.money = jsonData.getLong("money");
            	transfer.source_uid = jsonData.getLong("source_uid");            	
            	
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
            TransferCashResponse transfer = (TransferCashResponse) aResponseMessage;
            encodingObj.put("code", transfer.mCode);
            if (transfer.mCode == ResponseCode.FAILURE)
            {
                encodingObj.put("error_msg", transfer.errMessage);
               
            } else if (transfer.mCode == ResponseCode.SUCCESS)
            {
            	encodingObj.put("is_source", transfer.is_source);
            	encodingObj.put("desc_uid", transfer.desc_uid);
            	encodingObj.put("money", transfer.money);
            	encodingObj.put("source_uid", transfer.source_uid);
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
