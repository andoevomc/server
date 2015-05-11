package dreamgame.protocol.messages.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.AvatarEntity;
import dreamgame.data.ChargeHistoryEntity;
import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetAvatarListRequest;
import dreamgame.protocol.messages.GetAvatarListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetAvatarListJSON implements IMessageProtocol {

    private final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetAvatarListJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            GetAvatarListRequest rq = (GetAvatarListRequest) aDecodingObj;
            try {
                if (jsonData.has("getChargeHistory")) {
                    rq.getChargeHistory = jsonData.getBoolean("getChargeHistory");
                }
                if (jsonData.has("getGameInfo")) {
                    rq.getGameInfo = jsonData.getBoolean("getGameInfo");
                }

                if (jsonData.has("turnOffServer")) {
                    rq.turnOffServer = jsonData.getBoolean("turnOffServer");
                }

                if (jsonData.has("getImage")) {
                    rq.getImage = jsonData.getBoolean("getImage");
                }

                if (jsonData.has("updateConfig")) {
                    rq.updateConfig = jsonData.getBoolean("updateConfig");
                }
                if (jsonData.has("notification")) {
                    rq.notification = jsonData.getBoolean("notification");
                }

                if (jsonData.has("start")) {
                    rq.start = jsonData.getInt("start");
                }
                if (jsonData.has("uid")) {
                    rq.uid = jsonData.getInt("uid");
                }
                if (jsonData.has("length")) {
                    rq.length = jsonData.getInt("length");
                }

            } catch (Exception e) {
            }
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);


            // request messsage



            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            encodingObj.put("mid", aResponseMessage.getID());
            GetAvatarListResponse getAvatarList = (GetAvatarListResponse) aResponseMessage;
            encodingObj.put("code", getAvatarList.mCode);
            if (getAvatarList.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", getAvatarList.mErrorMsg);
            } else if (getAvatarList.mCode == ResponseCode.SUCCESS) {
                JSONArray arrRooms = new JSONArray();

                if (getAvatarList.getImage) {
                    encodingObj.put("imageData", getAvatarList.imageData);
                } else if (getAvatarList.getGameInfo) {
                    encodingObj.put("getGameInfo", true);
                    //runtime.totalMemory () - runtime.freeMemory ();
                    encodingObj.put("totalMemory", Runtime.getRuntime().totalMemory());
                    encodingObj.put("freeMemory", Runtime.getRuntime().freeMemory());
                    encodingObj.put("ServerRunTime", System.currentTimeMillis() - DatabaseDriver.startServerTime);

                    encodingObj.put("totalFlashUser", getAvatarList.totalFlashUser);
                    encodingObj.put("totalMobileUser", getAvatarList.totalMobileUser);
                    encodingObj.put("totalPhom", getAvatarList.totalPhom);
                    encodingObj.put("totalRoom", getAvatarList.totalRoom);
                    encodingObj.put("totalTienLen", getAvatarList.totalTienLen);

                } else if (getAvatarList.notification) {
                    encodingObj.put("notification", true);
                    encodingObj.put("isFriendNotice", getAvatarList.isFriendNotice);
                    encodingObj.put("msg", getAvatarList.msgNotification);
                } else if (getAvatarList.getHistroy) {
                    encodingObj.put("getHistory", true);
                    encodingObj.put("newMoney", getAvatarList.newMoney);

                    if (getAvatarList.mChargeHistory != null) {
                        for (ChargeHistoryEntity avaEntity : getAvatarList.mChargeHistory) {
                            JSONObject jRoom = new JSONObject();

                            jRoom.put("phone", avaEntity.phone);
                            jRoom.put("time", avaEntity.time);
                            jRoom.put("money", avaEntity.money);
                            arrRooms.put(jRoom);

                        }
                    }
                    encodingObj.put("history", arrRooms);

                } else {
                    if (getAvatarList.mAvatarList != null) {
                        for (AvatarEntity avaEntity : getAvatarList.mAvatarList) {
                            JSONObject jRoom = new JSONObject();
                            jRoom.put("id", avaEntity.id);
                            jRoom.put("desciption", avaEntity.description);
                            jRoom.put("money", avaEntity.money);
                            arrRooms.put(jRoom);
                        }
                    }
                    encodingObj.put("avatar_list", arrRooms);
                }
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
