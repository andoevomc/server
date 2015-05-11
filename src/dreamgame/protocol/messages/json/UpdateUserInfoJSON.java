/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.UpdateUserInfoRequest;
import dreamgame.protocol.messages.UpdateUserInfoResponse;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author Dinhpv
 */
public class UpdateUserInfoJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(UpdateUserInfoJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            UpdateUserInfoRequest update = (UpdateUserInfoRequest) aDecodingObj;
            try {
                update.email = jsonData.getString("email");
            }catch (Exception e2){
                update.email = "";
            }

            update.newPassword = jsonData.getString("new_password");
            update.oldPassword = jsonData.getString("old_password");
            try{
                update.number = jsonData.getString("PhoneNumber");
            }catch(Exception ex){
                update.number = "";
            }
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            UpdateUserInfoResponse update = (UpdateUserInfoResponse) aResponseMessage;
            encodingObj.put("code", update.mCode);
            if (update.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", update.mErrorMsg);
            } else if (update.mCode == ResponseCode.SUCCESS) {
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
