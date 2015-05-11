/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import java.text.SimpleDateFormat;
import java.util.Date;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.NewRequest;
import dreamgame.protocol.messages.NewResponse;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;


//import dreamgame.chan.data.ChanPlayer;
//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import dreamgame.config.GameRuleConfig;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.data.MessagesID;
import dreamgame.data.SimpleTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.protocol.messages.JoinRequest;
import dreamgame.xito.data.XiToTable;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.room.RoomEntity;
import dreamgame.gameserver.framework.session.AbstractSession;
import java.util.Vector;
import java.util.logging.Level;


import org.slf4j.Logger;

import phom.data.PhomPlayer;
import phom.data.PhomTable;

/**
 * @author binh_lethanh
 */
public class NewBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(NewBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "NewBusiness - handleMessage");
	}
        mLog.debug("[CREATE NEW ROOM]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        NewResponse resMatchNew = (NewResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        boolean joinroom = false;
        try {

            long status = DatabaseDriver.getUserGameStatus(aSession.getUID());
            if (status == 1) {
                resMatchNew.setFailure(ResponseCode.FAILURE,
                        "Bạn vẫn còn trong 1 bàn chơi, vui lòng chờ ván chơi kết thúc!");
                aSession.write(resMatchNew);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;

            }

            NewRequest rqMatchNew = (NewRequest) aReqMsg;
            UserEntity user = DatabaseDriver.getUserInfo(rqMatchNew.uid);
            resMatchNew.setCash(user.money);
//            long moneyBet = DatabaseDriver.getMoneyForRoom(rqMatchNew.roomType);
	    
	    long moneyBet;
	    
	    if (aSession.getCurrentZone() == ZoneID.POKER || aSession.getCurrentZone() == ZoneID.XITO)
		moneyBet = DatabaseDriver.getMoneyForRoomByMoneyAndChannelPoker(rqMatchNew.moneyBet, aSession.getChannel());
	    else
		moneyBet = DatabaseDriver.getMoneyForRoomByMoneyAndChannel(rqMatchNew.moneyBet, aSession.getChannel());
	    
	    // for get invite list based on current money match
	    aSession.setCurrentMoneyMatch(moneyBet);
	    
            String roomName = "";

            //DatabaseDriver.maxRoom = 2;
            System.out.println("rqMatchNew.roomPosition : " + rqMatchNew.roomPosition);


            int roomChannel = 0;

            System.out.println("aSession.getChannel() : " + aSession.getChannel());

	    Zone gameZone = aSession.findZone(aSession.getCurrentZone());
	    
            if (aSession.getChannel() > 0) {
                roomChannel = aSession.getChannel();
//                Zone gameZone = aSession.findZone(aSession.getCurrentZone());

		// nếu tạo bàn ở vị trí nào đó mà đã có bàn tồn tại rồi thì trả về lỗi
		RoomEntity re = gameZone.findRoomByPosition(roomChannel, rqMatchNew.roomPosition);
		if (re != null) {
		    resMatchNew.setFailure(ResponseCode.FAILURE,
                            "Đã có bàn ở vị trí này rồi, bạn chọn vị trí khác nhé!");
		    aSession.write(resMatchNew);
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
		}
				
		// trường hợp j2me tạo bàn ở vị trí số 1, khi ấn nút chấp nhận request gửi lên server
		// mà đã có người chơi iOS, android, j2me đã tạo bàn ở vị trí đó và bàn đó chưa start, vẫn còn chỗ trống, 
		// thì j2me sẽ tự động join vào bàn đó
//                Vector<RoomEntity> vv = gameZone.dumpWaiting(roomChannel);		
//                int sum = 0;
//                for (RoomEntity v : vv) {
//
//                    sum++;
//
//                    if (rqMatchNew.roomPosition > 0 && v.roomPosition == rqMatchNew.roomPosition) {
//
//                        JoinRequest rqJoin = (JoinRequest) msgFactory.getRequestMessage(MessagesID.MATCH_JOIN);
//                        rqJoin.mMatchId = v.mRoomId;
//                        rqJoin.uid = aSession.getUID();
//                        IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_JOIN);
//                        joinroom = true;
//                        try {
//                            business.handleMessage(aSession, rqJoin, aResPkg);
//                        } catch (ServerException ex) {
//                            java.util.logging.Logger.getLogger(AbstractSession.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        return 1;
//                    }
//                }
//
//                System.out.println("Sum : " + sum);

                System.out.println("DatabaseDriver.maxRoom : " + DatabaseDriver.maxRoom);
//                System.out.println("aSession.getChannel() : " + aSession.getChannel());

                if (gameZone.getTotalRoom(roomChannel) >= DatabaseDriver.maxRoom) {
                    resMatchNew.setFailure(ResponseCode.FAILURE,
                            "Phòng đã đầy không thể tạo thêm bàn nữa. Bạn hãy sang phòng khác!");
		    aSession.write(resMatchNew);
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }
            }

            if (aSession.getChannel() == 0) {
//                Zone gameZone = aSession.findZone(aSession.getCurrentZone());
                for (int i = 1; i <= 10; i++) {
                    if (gameZone.getTotalRoom(i) < DatabaseDriver.maxRoom) {
                        roomChannel = i;
                        break;
                    }
                }
                System.out.println("New mobile room channel : " + roomChannel);
            }
	    
	    // handle flash new table action, position == 0
	    if (rqMatchNew.roomPosition == 0) {
		rqMatchNew.roomPosition = gameZone.findAvailablePositionForRoom(roomChannel);
	    }

	    // rename table based on which device creating it
//            if (aSession.getMobile()) {
//                if (rqMatchNew.roomName.contains("p_")) {
//                    roomName = "m_" + rqMatchNew.roomName.substring(2);
//                } else {
//                    roomName = "m_" + rqMatchNew.roomName;
//                }
//            } else {
//                if (rqMatchNew.roomName.contains("p_")) {
//                    roomName = "f_" + rqMatchNew.roomName.substring(2);
//                } else {
//                    roomName = "f_" + rqMatchNew.roomName;
//                }
//            }	    
	    roomName = rqMatchNew.roomName;
	    if (roomName.equals("")) {
		if (user != null) {
		    roomName = "B. " + user.mUsername;
		}
	    }
		    
		    

//            System.out.println("roomChannel : " + roomChannel);

            if (user != null) {
                // Make the right Table compatible with zone
                switch (aSession.getCurrentZone()) {


                    case ZoneID.BACAY: {
                        if ((user.money < moneyBet * GameRuleConfig.MONEY_TIMES_BET_TO_CREATE)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo bàn này. Cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_CREATE + " lần tiền cược!");
                        } 
			else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);

                            System.out.println("rqMatchNew.roomName: " + zoneID);
                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);
                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.roomPosition = rqMatchNew.roomPosition;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);

                            newRoom.channel = roomChannel;


                            try {
                                // Create Player
                                BacayPlayer player = new BacayPlayer(
                                        rqMatchNew.uid);
                                player.setCash(user.money);
                                player.avatarID = user.avatarID;
                                player.level = user.level;
                                player.username = user.mUsername;
                                player.setCurrentMatchID(newRoom.getRoomId());
                                // create new table for this match
                                BacayTable newTable = new BacayTable(player,
                                        moneyBet, rqMatchNew.zise);
                                newTable.setLevel(rqMatchNew.roomType);

                                newTable.setMatchID(newRoom.getRoomId());
                                newTable.setOwnerSession(aSession);
//                                newRoom.setLevel(rqMatchNew.roomType);
                                // set attachment of this room
                                newRoom.setAttachmentData(newTable);
                                newTable.getMaximumPlayer();
                                resMatchNew.setSuccess(ResponseCode.SUCCESS,
                                        newRoom.getRoomId(), rqMatchNew.uid,
                                        moneyBet, newTable.getMaximumPlayer());
                                // Log Match

                                Date dateNow = new Date();
                                SimpleDateFormat dateformat = new SimpleDateFormat(
                                        "yyyyMMddHHmm");
                                StringBuilder nowStr = new StringBuilder(
                                        dateformat.format(dateNow));
                                long matchIDAuto = DatabaseDriver.logMatch(
                                        newTable.getRoomOwner().id, newTable.getMatchID(), newTable.getLevel(), "" + nowStr);
                                newTable.setMatchIDAuto(matchIDAuto);
                                mLog.debug("Creating room id:" + newRoom.getRoomId());
                            } 
			    catch (Exception ex1) {
                                zone.deleteRoom(newRoom);
                                resMatchNew.setFailure(ResponseCode.FAILURE, "Không tạo được trận này");
                                mLog.error("Process message " + aReqMsg.getID() + " error.", ex1);
                            }
                        }
                        break;
                    }
//                            case ZoneID.OTT: {
//                                OTTPlayer player = new OTTPlayer();
//                                player.setCash(user.money);
//                                player.avatarID = user.avatarID;
//                                player.level = user.level;
//                                player.username = user.mUsername;
//                                player.setCurrentMatchID(newRoom.getRoomId());
//
//                                OantutiTable newTable = new OantutiTable(player,
//                                        moneyBet, rqMatchNew.zise);
//                                newTable.setLevel(rqMatchNew.roomType);
//                                newTable.setMatchID(newRoom.getRoomId());
//                                newTable.setOwnerSession(aSession);
//                                newRoom.setLevel(rqMatchNew.roomType);
//                                // set attachment of this room
//                                newRoom.setAttachmentData(newTable);
//                                resMatchNew.setSuccess(ResponseCode.SUCCESS,
//                                        newRoom.getRoomId(), rqMatchNew.uid,
//                                        moneyBet, newTable.getMaximumPlayer());
//                                // TODO: log match
//                                break;
//                            }
//                            case ZoneID.CARO: {
//                                CaroTable newTable = new CaroTable(15, 15);
//                                newRoom.setAttachmentData(newTable);
//                                resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
//                                        rqMatchNew.uid, newTable.firstCashBet, 2);
//                                break;
//                            }

                    case ZoneID.POKER: {
                        if ((user.money < moneyBet * GameRuleConfig.MONEY_TIMES_BET_TO_CREATE)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo bàn này. Cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_CREATE + " lần tiền cược!");
                        } else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);
                            System.out.println("rqMatchNew.roomName: " + zone);
                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);
                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.channel = roomChannel;
                            newRoom.roomPosition = rqMatchNew.roomPosition;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);
                            PokerPlayer owner = new PokerPlayer(user.mUid);
                            owner.setCash(user.money);
                            owner.avatarID = user.avatarID;
                            owner.level = user.level;
                            owner.username = user.mUsername;
                            owner.currentSession = aSession;
                            PokerTable newTable = new PokerTable(owner, moneyBet, newRoom.getRoomId(), rqMatchNew.zise);
                            newTable.setOwnerSession(aSession);
                            newRoom.setAttachmentData(newTable);
//                            newTable.ownerRoom = newRoom;
//                            if (state == 3) {
//                                newTable.virtualRoom = true;
//                            }
                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
                                    rqMatchNew.uid, newTable.firstCashBet, rqMatchNew.zise);
                        }
                        break;
                    }
                    case ZoneID.XITO: {
                        if ((user.money < moneyBet * GameRuleConfig.MONEY_TIMES_BET_TO_CREATE)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo bàn này. Cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_CREATE + " lần tiền cược!");
                        } else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);
                            System.out.println("rqMatchNew.roomName: " + zone);
                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);

                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.channel = roomChannel;
                            newRoom.roomPosition = rqMatchNew.roomPosition;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);
                            PokerPlayer owner = new PokerPlayer(user.mUid);
                            owner.setCash(user.money);
                            owner.avatarID = user.avatarID;
                            owner.level = user.level;
                            owner.username = user.mUsername;
                            owner.currentSession = aSession;
                            XiToTable newTable = new XiToTable(owner, moneyBet, newRoom.getRoomId(), rqMatchNew.zise);
                            newTable.setOwnerSession(aSession);
                            newRoom.setAttachmentData(newTable);
//                            newTable.ownerRoom = newRoom;
                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
                                    rqMatchNew.uid, newTable.firstCashBet, rqMatchNew.zise);
                        }
                        break;
                    }
                    case ZoneID.COTUONG: {
                        if ((user.money < moneyBet)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo bàn này.");
                        } else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);

                            System.out.println("rqMatchNew.roomName: " + zone);
                            Room newRoom = zone.createRoom("cotuong", rqMatchNew.uid, roomChannel);
                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.roomPosition = rqMatchNew.roomPosition;
                            newRoom.channel = roomChannel;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);
                            CoTuongPlayer player = new CoTuongPlayer(rqMatchNew.uid);
                            player.cash = user.money;
                            player.avatarID = user.avatarID;
                            player.level = user.level;
                            player.username = user.mUsername;
                            player.currentMatchID = newRoom.getRoomId();
                            if (rqMatchNew.moneyBet > 0) {
                                moneyBet = rqMatchNew.moneyBet;
                            }
                            CoTuongTable newTabl = new CoTuongTable(player, moneyBet, rqMatchNew.available, rqMatchNew.totalTime * 60 * 1000);
                            newTabl.setOwnerSession(aSession);
                            newTabl.setMatchID(newRoom.getRoomId());
                            // System.out.println("Đây là trò cờ tướng này :D");
                            newRoom.setAttachmentData(newTabl);
                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
                                    rqMatchNew.uid, newTabl.firstCashBet, 2);
                            resMatchNew.available = newTabl.available;
                            resMatchNew.totalTime = newTabl.totalTime;
                        }
                        break;
                    }
                    case ZoneID.GAME_CHAN: {
//                        if ((user.money < moneyBet * GameRuleConfig.MONEY_TIMES_BET_TO_CREATE)) {
//                            resMatchNew.setFailure(ResponseCode.FAILURE,
//                                    "Bạn không đủ tiền để tạo bàn này. Cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_CREATE + " lần tiền cược!");
//                        } else {
//                            int zoneID = aSession.getCurrentZone();
//                            Zone zone = aSession.findZone(zoneID);
//
//                            aSession.leaveAllRoom(aResPkg);
//
//                            System.out.println("rqMatchNew.roomName: " + zone);
//                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);
//                            newRoom.join(aSession);
//                            newRoom.setZoneID(zoneID);
//                            newRoom.setPassword(rqMatchNew.password);
//                            newRoom.setName(roomName);
//                            newRoom.setPlayerSize(rqMatchNew.zise);
//                            newRoom.setOwnerName(user.mUsername);
//                            newRoom.minBet = moneyBet;
//                            newRoom.channel = roomChannel;
//                            newRoom.roomPosition = rqMatchNew.roomPosition;
//
//
//                            mLog.debug("RoomName:" + roomName);
//                            mLog.debug("Size:" + rqMatchNew.zise);
//                            ChanPlayer player = new ChanPlayer(user.mUid);
//                            player.money = user.money;
//                            player.avatarID = user.avatarID;
//                            player.level = user.level;
//                            player.username = user.mUsername;
//                            player.currentSession = aSession;
//
//                            if (rqMatchNew.moneyBet > 0) {
//                                moneyBet = rqMatchNew.moneyBet;
//                            }
//
//                            ChanTable newTable = new ChanTable(player, roomName, moneyBet);
//                            newTable.setOwnerSession(aSession);
//                            //newTable.ownerRoom = newRoom;
//
//                            //newRoom.setRoomId(matchIDAuto);
//                            newTable.setMatchID(newRoom.getRoomId());
//                            player.currentMatchID = newRoom.getRoomId();
//                            /*
//                             * DatabaseDriver.logUserMatch(player.id,
//                             * newRoom.getRoomId(), "ban la chu room",
//                             * table.firstCashBet, false, newRoom.getRoomId());
//                             */
//
//                            newTable.setMaximumPlayer(5);
//
//                            newRoom.setAttachmentData(newTable);
//                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
//                                    player.id, moneyBet, rqMatchNew.zise);
//                            resMatchNew.capacity = newTable.getMaximumPlayer();
//
//                            System.out.println("Maximum player : " + newTable.getMaximumPlayer());
//                        }
                        break;
                    }

                    case ZoneID.PHOM: {
                        if ((user.money < moneyBet * GameRuleConfig.MONEY_TIMES_BET_TO_CREATE)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo bàn này. Cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_CREATE + " lần tiền cược!");
                        } else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);

                            System.out.println("rqMatchNew.roomName: " + zone);
                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);
                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.channel = roomChannel;
                            newRoom.roomPosition = rqMatchNew.roomPosition;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);
                            PhomPlayer player = new PhomPlayer(user.mUid);
                            player.setCash(user.money);
                            player.avatarID = user.avatarID;
                            player.level = user.level;
                            player.username = user.mUsername;
                            player.currentSession = aSession;

                            if (rqMatchNew.moneyBet > 0) {
                                moneyBet = rqMatchNew.moneyBet;
                            }

                            PhomTable table = new PhomTable(player, roomName, moneyBet);
                            mLog.debug(" is an = "+rqMatchNew.isAn);
                            mLog.debug(" is tai = "+rqMatchNew.isTai);
                            mLog.debug(" is khan  = "+rqMatchNew.isKhan);
                            table.setAnCayMatTien(rqMatchNew.isAn);
                            table.setTai(rqMatchNew.isTai);
                            table.setUKhan(rqMatchNew.isKhan);
                            table.setOwnerSession(aSession);
                            table.testCode = rqMatchNew.testCode;
                            //newRoom.setRoomId(matchIDAuto);
                            table.setMatchID(newRoom.getRoomId());
                            player.currentMatchID = newRoom.getRoomId();
                            /*
                             * DatabaseDriver.logUserMatch(player.id,
                             * newRoom.getRoomId(), "ban la chu room",
                             * table.firstCashBet, false, newRoom.getRoomId());
                             */
                            table.setMaximumPlayer(rqMatchNew.zise);
                            newRoom.setAttachmentData(table);
                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
                                    player.id, moneyBet, rqMatchNew.zise);
                        }
                        break;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        if ((user.money < moneyBet * GameRuleConfig.MONEY_TIMES_BET_TO_CREATE)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo bàn này. Cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_CREATE + " lần tiền cược!");
                        } else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);
                            System.out.println("rqMatchNew.roomName: " + zone);
                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);

                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.channel = roomChannel;
                            newRoom.roomPosition = rqMatchNew.roomPosition;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);
                            TienLenPlayer owner = new TienLenPlayer(user.mUid);
                            owner.setCash(user.money);
                            owner.avatarID = user.avatarID;
                            owner.level = user.level;
                            owner.username = user.mUsername;
                            owner.currentSession = aSession;
                            TienLenTable newTable = new TienLenTable(owner, moneyBet, newRoom.getRoomId(), rqMatchNew.zise);
                            if (aSession.getCurrentZone() == ZoneID.TIENLEN_MB) {
                                newTable.setTLMB(true);
                            } else {
                                newTable.setDemLa(true);
                            }
                            newTable.setOwnerSession(aSession);
                            newRoom.setAttachmentData(newTable);
                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
                                    rqMatchNew.uid, newTable.firstCashBet, rqMatchNew.zise);
                        }
                        break;
                    }
                    case ZoneID.BAUCUA: {
                        BauCuaPlayer owner = new BauCuaPlayer(user.mUid);
                        owner.setCash(user.money);
//                        System.out.println("owner cash:" + owner.cash);
                        if (!owner.canOwner(rqMatchNew.zise, moneyBet, 10)) {
                            resMatchNew.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để tạo trận này.");
                        } else {
                            int zoneID = aSession.getCurrentZone();
                            Zone zone = aSession.findZone(zoneID);

                            aSession.leaveAllRoom(aResPkg);
                            System.out.println("rqMatchNew.roomName: " + zone);
                            Room newRoom = zone.createRoom(roomName, rqMatchNew.uid, roomChannel);

                            newRoom.join(aSession);
                            newRoom.setZoneID(zoneID);
                            newRoom.setPassword(rqMatchNew.password);
                            newRoom.setName(roomName);
                            newRoom.setPlayerSize(rqMatchNew.zise);
                            newRoom.setOwnerName(user.mUsername);
                            newRoom.minBet = moneyBet;
                            newRoom.channel = roomChannel;
                            newRoom.roomPosition = rqMatchNew.roomPosition;

                            mLog.debug("RoomName:" + roomName);
                            mLog.debug("Size:" + rqMatchNew.zise);
//                            BauCuaPlayer owner = new BauCuaPlayer(user.mUid);
//                            owner.setCash(user.money);
                            owner.avatarID = user.avatarID;
                            owner.level = user.level;
                            owner.username = user.mUsername;
                            owner.currentSession = aSession;
                            BauCuaTable newTable = new BauCuaTable(owner, moneyBet, newRoom.getRoomId(), rqMatchNew.zise);
                            newTable.setOwnerSession(aSession);
                            newRoom.setAttachmentData(newTable);
                            resMatchNew.setSuccess(ResponseCode.SUCCESS, newRoom.getRoomId(),
                                    rqMatchNew.uid, newTable.firstCashBet, rqMatchNew.zise);
                        }
                        break;
                    }
                    case ZoneID.MAUBINH: {
                        break;

                    }
                    // TODO: Add more here
                    default:
                        break;
                }
//                }
            } else {
                resMatchNew.setFailure(ResponseCode.FAILURE,
                        "Bạn không tồn tại trong cơ sở dữ liệu.");
            }
        } catch (Throwable t) {
            resMatchNew.setFailure(ResponseCode.FAILURE, "Hệ thống đang bảo trì xin bạn vui lòng quay lại sau ít phút nữa! ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resMatchNew != null)) {
                if (!joinroom) {
                    aResPkg.addMessage(resMatchNew);
                }
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;

    }
}
