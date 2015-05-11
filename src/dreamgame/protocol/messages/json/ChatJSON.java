package dreamgame.protocol.messages.json;
import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.ChatRequest;
import dreamgame.protocol.messages.ChatResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONObject;
import org.slf4j.Logger;

public class ChatJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ChatJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // plain obj
            ChatRequest chat = (ChatRequest) aDecodingObj;
            // decoding
            chat.mMessage = jsonData.getString("message");
            chat.mRoomId = jsonData.getLong("room_id");
            chat.type = jsonData.getInt("type");

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
            ChatResponse chat = (ChatResponse) aResponseMessage;
            encodingObj.put("code", chat.mCode);
            // System.out.println(" chat.mUsername : " +  chat.mUsername);
            if (chat.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", chat.mErrorMsg);
            } else if (chat.mCode == ResponseCode.SUCCESS) {
                //  encodingObj.put("username", chat.mUsername);
                encodingObj.put("message", chat.mMessage);
                encodingObj.put("username", chat.mUsername);
                encodingObj.put("room_id", chat.roomid);
                encodingObj.put("type", chat.type);

            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }

}
