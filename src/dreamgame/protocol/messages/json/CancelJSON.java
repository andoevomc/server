/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.CancelRequest;
import dreamgame.protocol.messages.CancelResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.Zone;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author binh_lethanh
 */
public class CancelJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(CancelJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            CancelRequest matchCancel = (CancelRequest) aDecodingObj;
            matchCancel.mMatchId = jsonData.getLong("match_id");
            matchCancel.uid = jsonData.getLong("uid");
            try {
                matchCancel.isLogout = jsonData.getBoolean("is_logout");
            } catch (Exception e) {
                matchCancel.isLogout = false;
            }
            try {
                matchCancel.isOutOfGame = jsonData.getBoolean("is_out_game");
            } catch (Exception e) {
                matchCancel.isOutOfGame = false;
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
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            CancelResponse matchCancel = (CancelResponse) aResponseMessage;
            encodingObj.put("code", matchCancel.mCode);
            if (matchCancel.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchCancel.mErrorMsg);
                encodingObj.put("is_game_playing", matchCancel.isGamePlaying);
                encodingObj.put("uid", matchCancel.uid);
            } else if (matchCancel.mCode == ResponseCode.SUCCESS) {

                encodingObj.put("uid", matchCancel.uid);
                encodingObj.put("is_user_playing", matchCancel.isUserPlaying);
                encodingObj.put("newOwner", matchCancel.newOwner);
                //Thomc
                System.out.println(matchCancel.zone_id+" : "+ZoneID.BACAY);
                
                if (matchCancel.zone_id == ZoneID.BACAY) {
                    encodingObj.put("money", matchCancel.money);
                }
                
                if (matchCancel.zone_id == ZoneID.COTUONG) {
                    System.out.println("Cancel match chạy vào zone cờ tướng! " + matchCancel.ownerMoney);
                    if (matchCancel.ownerMoney >= 0) {
                        encodingObj.put("ownerMoney", matchCancel.ownerMoney);
                    }
                    if (matchCancel.playerMoney >= 0) {
                        encodingObj.put("playerMoney", matchCancel.playerMoney);
                    }

                    if (matchCancel.money >= 0) {
                        encodingObj.put("money", matchCancel.money);
                    }
                } else if (matchCancel.zone_id == ZoneID.TIENLEN || matchCancel.zone_id == ZoneID.TIENLEN_DEMLA || matchCancel.zone_id == ZoneID.TIENLEN_MB
                        || matchCancel.zone_id == ZoneID.GAME_CHAN) {
                    System.out.println("Cancel match chạy vào zone tiến lên!");
                    if (matchCancel.next_id != -1) {
                        encodingObj.put("next_id", matchCancel.next_id);
                        encodingObj.put("isNewRound", matchCancel.isNewRound);
                    }
                    System.out.println(matchCancel.money + "");
                    if (matchCancel.money >= 0) {
                        encodingObj.put("money", matchCancel.money);
                    }
                    encodingObj.put("sttToi", matchCancel.stt);
                }
                try {
                    encodingObj.put("message", matchCancel.message);
                } catch (Exception e) {
                }
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
