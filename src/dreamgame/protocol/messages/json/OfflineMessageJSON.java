/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.MessageOfflineEntity;
import dreamgame.protocol.messages.OfflineMessageResponse;
import dreamgame.data.ResponseCode;
//import bacaymessage.BacayGetFrientListRequest;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
//import com.migame.gameserver.framework.room.RoomEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author Dinhpv
 */
public class OfflineMessageJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(OfflineMessageJSON.class);

    public boolean decode(Object paramObject, IRequestMessage paramIRequestMessage) throws ServerException {
        try {
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + paramIRequestMessage.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {            
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            OfflineMessageResponse getFrientList = (OfflineMessageResponse) aResponseMessage;
            encodingObj.put("code", getFrientList.mCode);
            if (getFrientList.mCode == ResponseCode.FAILURE) {
            } else if (getFrientList.mCode == ResponseCode.SUCCESS) {
                JSONArray arrRooms = new JSONArray();
                if (getFrientList.mPostList != null) {
                    for (MessageOfflineEntity userEntity : getFrientList.mPostList) {
                        // with each playing room
                        JSONObject jRoom = new JSONObject();
                        // attached object
                        jRoom.put("username", userEntity.sendName);
                        jRoom.put("uid", userEntity.sendID);
                        jRoom.put("message", userEntity.mess);
                        jRoom.put("date", userEntity.datetime);
                        arrRooms.put(jRoom);
                    }
                }
                encodingObj.put("OfflineMessage_List", arrRooms);
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
