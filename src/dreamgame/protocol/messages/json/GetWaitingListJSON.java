package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.GetWaitingListRequest;
import dreamgame.protocol.messages.GetWaitingListResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;

import dreamgame.config.DebugConfig;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.RoomEntity;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.data.SimplePlayer;

import dreamgame.data.SimpleTable;
import dreamgame.databaseDriven.DatabaseDriver;
//import dreamgame.oantuti.data.OTTPlayer;
//import dreamgame.oantuti.data.OantutiTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import phom.data.PhomPlayer;
import phom.data.PhomTable;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;
import dreamgame.gameserver.framework.session.AbstractSession;
import com.sun.midp.io.Base64;

public class GetWaitingListJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(GetWaitingListJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetWaitingListJSON - decode");
	}
	
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // request messsage
            GetWaitingListRequest getWaitingList = (GetWaitingListRequest) aDecodingObj;
            // parsing
            getWaitingList.mOffset = jsonData.getInt("offset");
            getWaitingList.mLength = jsonData.getInt("length");
            getWaitingList.level = jsonData.getInt("level");
            getWaitingList.minLevel = jsonData.getInt("minLevel");

            if (jsonData.has("compress")) {
                getWaitingList.compress = jsonData.getBoolean("compress");
            }
            if (jsonData.has("channelId")) {
                getWaitingList.channelId = jsonData.getInt("channelId");
            }
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetWaitingListJSON - encode");
	}
	
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            GetWaitingListResponse getWaitingList = (GetWaitingListResponse) aResponseMessage;
            encodingObj.put("code", getWaitingList.mCode);
            if (getWaitingList.mCode == ResponseCode.FAILURE) {
            } else if (getWaitingList.mCode == ResponseCode.SUCCESS) {

                if (getWaitingList.compress) {
                    encodingObj.put("com", true);
                    encodingObj.put("num", getWaitingList.totalRoom);
                    String data = "";
                    byte[] dest = new byte[0];
                    //matchId(long),pos(1 byte),cash(long)
                    if (getWaitingList.mWaitingRooms != null) {
                        for (RoomEntity roomEntity : getWaitingList.mWaitingRooms) {
                            dest = AbstractSession.appendByte(dest, AbstractSession.intToByteArray((int) roomEntity.mRoomId));
                            dest = AbstractSession.appendByte(dest, AbstractSession.intToByteArray((int) roomEntity.moneyBet));
                            dest = AbstractSession.appendByte(dest, AbstractSession.byteToByteArray(roomEntity.mPlayingSize));
                            dest = AbstractSession.appendByte(dest, AbstractSession.byteToByteArray(roomEntity.roomPosition));
                            SimpleTable table = (SimpleTable) roomEntity.mAttactmentData;
                            dest = AbstractSession.appendByte(dest, AbstractSession.byteToByteArray(table.maximumPlayer));
			    dest = AbstractSession.appendByte(dest, AbstractSession.booleanToByteArray(table.isPlaying));
			    dest = AbstractSession.appendByte(dest, AbstractSession.stringToByteArray(roomEntity.mRoomName));
                        }
                    }
                    System.out.println("dest : " + dest.length);
                    data = Base64.encode(dest, 0, dest.length);

                    encodingObj.put("data", data);

                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return encodingObj;
                }

                encodingObj.put("num_playing_room", getWaitingList.mNumPlayingRoom);
                encodingObj.put("total_room", getWaitingList.totalRoom);
                encodingObj.put("maxRoom", DatabaseDriver.maxRoom);
                // room dummy
                JSONArray arrRooms = new JSONArray();
                if (getWaitingList.mWaitingRooms != null) {
                    for (RoomEntity roomEntity : getWaitingList.mWaitingRooms) {
                        // with each playing room
                        JSONObject jRoom = new JSONObject();
                        jRoom.put("room_id", roomEntity.mRoomId);
                        jRoom.put("room_name", roomEntity.mRoomName);

                        jRoom.put("room_owner", roomEntity.mRoomOwnerName);
                        jRoom.put("channelId", roomEntity.channel);
                        jRoom.put("roomPosition", roomEntity.roomPosition);


                        if (roomEntity.mPassword != null) {
                            jRoom.put("isSecure", true);
                            jRoom.put("password", roomEntity.mPassword);
                        } else {
                            jRoom.put("isSecure", false);
                        }
			
			// fake room
			if (roomEntity.isFakeRoom) {
			    SimpleTable table = (SimpleTable) roomEntity.mAttactmentData;
			    SimplePlayer roomOwner = table.owner;
			    jRoom.put("capacity", table.getMaximumPlayer());
			    jRoom.put("minBet", table.getMinBet());
			    jRoom.put("level", roomOwner.level);
			    jRoom.put("avatar", roomOwner.avatarID);
			    jRoom.put("username", roomOwner.username);
			    jRoom.put("nameList", table.getPlayerName());
			    jRoom.put("isPlaying", table.isPlaying);
			    jRoom.put("startTime", System.currentTimeMillis() - table.startTime);
			    jRoom.put("playing_size", roomEntity.mPlayingSize);
			    arrRooms.put(jRoom);
			    continue;
			}
			
                        // normal room -> read attached object
                        switch (getWaitingList.zoneID) {
                            case ZoneID.BACAY: {
                                BacayTable table = (BacayTable) roomEntity.mAttactmentData;
                                if (table != null) {
                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    BacayPlayer roomOwner = table.getRoomOwner();
                                    jRoom.put("level", roomOwner.level);
                                    jRoom.put("avatar", roomOwner.avatarID);
                                    jRoom.put("username", roomOwner.username);

                                    if (!getWaitingList.isMobile) {
                                        jRoom.put("nameList", table.getPlayerName());
                                    }
                                }
                                break;
                            }
                            case ZoneID.OTT: {
				//deleted
                                break;
                            }
                            case ZoneID.GAME_CHAN: {
				//deleted
                                break;
                            }

                            case ZoneID.PHOM: {
                                PhomTable table = (PhomTable) roomEntity.mAttactmentData;
                                if (table != null) {

                                    roomEntity.mPlayingSize = table.getPlayings().size() + table.getWaitings().size();

                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    PhomPlayer roomOwner = (PhomPlayer) table.owner;
                                    jRoom.put("level", roomOwner.level);
                                    jRoom.put("avatar", roomOwner.avatarID);
                                    jRoom.put("username", roomOwner.username);
                                    jRoom.put("isPlaying", table.isPlaying);

                                    if (!getWaitingList.isMobile) {
                                        jRoom.put("startTime", System.currentTimeMillis() - table.startTime);
                                        jRoom.put("nameList", table.getPlayerName());
                                    }
                                }
                                break;
                            }
                            case ZoneID.COTUONG: {
                                CoTuongTable table = (CoTuongTable) roomEntity.mAttactmentData;
                                if (table != null) {
                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    CoTuongPlayer roomOwner = (CoTuongPlayer) table.owner;
                                    jRoom.put("level", roomOwner.level);
                                    jRoom.put("avatar", roomOwner.avatarID);
                                    jRoom.put("username", roomOwner.username);
                                    jRoom.put("available", table.available);
                                    jRoom.put("isPlaying", table.isPlaying);
                                    if (!getWaitingList.isMobile) {
                                        jRoom.put("nameList", table.getPlayerName());
                                    }
                                }
                                break;
                            }
                            case ZoneID.TIENLEN_MB:
                            case ZoneID.TIENLEN_DEMLA:
                            case ZoneID.TIENLEN: {
                                TienLenTable table = (TienLenTable) roomEntity.mAttactmentData;
                                if (table != null) {
                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    TienLenPlayer roomOwner = (TienLenPlayer) table.getOwner();
                                    jRoom.put("level", roomOwner.level);


                                    jRoom.put("isPlaying", table.isPlaying);

                                    if (!getWaitingList.isMobile) {
                                        jRoom.put("startTime", System.currentTimeMillis() - table.startTime);
                                        jRoom.put("nameList", table.getPlayerName());
                                        jRoom.put("username", roomOwner.username);
                                        jRoom.put("avatar", roomOwner.avatarID);
                                    }
                                }
                                break;
                            }
                            case ZoneID.BAUCUA: {
                                BauCuaTable table = (BauCuaTable) roomEntity.mAttactmentData;
                                if (table != null) {
                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    BauCuaPlayer roomOwner = (BauCuaPlayer) table.getOwner();
                                    jRoom.put("level", roomOwner.level);


                                    jRoom.put("isPlaying", table.isPlaying);

                                    if (!getWaitingList.isMobile) {
                                        jRoom.put("startTime", System.currentTimeMillis() - table.startTime);
                                        jRoom.put("nameList", table.getPlayerName());
                                        jRoom.put("username", roomOwner.username);
                                        jRoom.put("avatar", roomOwner.avatarID);
                                    }
                                }
                                break;
                            }
                            case ZoneID.POKER: {
                                PokerTable table = (PokerTable) roomEntity.mAttactmentData;
                                if (table != null) {
                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    PokerPlayer roomOwner = (PokerPlayer) table.getOwner();
                                    jRoom.put("level", roomOwner.level);
                                    jRoom.put("avatar", roomOwner.avatarID);
                                    jRoom.put("username", roomOwner.username);
                                    jRoom.put("isPlaying", table.isPlaying);
                                    jRoom.put("startTime", System.currentTimeMillis() - table.startTime);
                                    jRoom.put("nameList", table.getPlayerName());
                                }
                                break;
                            }

                            case ZoneID.XITO: {
                                XiToTable table = (XiToTable) roomEntity.mAttactmentData;
                                if (table != null) {
                                    jRoom.put("capacity", table.getMaximumPlayer());
                                    jRoom.put("minBet", table.getMinBet());
                                    PokerPlayer roomOwner = (PokerPlayer) table.getOwner();
                                    jRoom.put("level", roomOwner.level);
                                    jRoom.put("avatar", roomOwner.avatarID);
                                    jRoom.put("username", roomOwner.username);
                                    jRoom.put("isPlaying", table.isPlaying);
                                    jRoom.put("startTime", System.currentTimeMillis() - table.startTime);
                                    jRoom.put("nameList", table.getPlayerName());
                                }
                                break;
                            }
                                case ZoneID.MAUBINH: {
                                break;
                            }
                            default:
                                break;
                        }
                        if (getWaitingList.zoneID == ZoneID.BACAY) {
                            if (roomEntity.mPlayingSize > 4) {
                                roomEntity.mPlayingSize = 4;
                            }
                        }
                        jRoom.put("playing_size", roomEntity.mPlayingSize);


                        // then add to array of rooms

                        if (roomEntity.mPlayingSize > 0) {
                            arrRooms.put(jRoom);
                        }
                    } // end for each roomEntity
                }
                encodingObj.put("waiting_rooms", arrRooms);
            }
            // response encoded obj
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return null;
        }
    }
}
