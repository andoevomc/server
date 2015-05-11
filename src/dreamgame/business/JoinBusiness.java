/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import java.util.Random;
import java.util.Vector;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.JoinRequest;
import dreamgame.protocol.messages.JoinResponse;
import dreamgame.protocol.messages.JoinedResponse;
import dreamgame.protocol.messages.WaitingAcceptResponse;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.bacay.data.BacayTable;
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
import dreamgame.gameserver.framework.room.RoomStatus;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.data.SimpleTable;

//import dreamgame.oantuti.data.OTTPlayer;
//import dreamgame.oantuti.data.OantutiTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.tienlen.data.Utils;
import dreamgame.xito.data.XiToTable;
import dreamgame.gameserver.framework.room.RoomEntity;
import java.util.ArrayList;

import org.slf4j.Logger;

import phom.data.PhomPlayer;
import phom.data.PhomTable;

/**
 *
 * @author binh_lethanh
 */
public class JoinBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(JoinBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) throws ServerException {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "JoinBusiness - handleMessage");
	}
	
	mLog.debug("[JOIN ROOM]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        WaitingAcceptResponse resWaitingAcceptJoin = (WaitingAcceptResponse) msgFactory.getResponseMessage(MessagesID.WAITING_ACCEPT);
        try {
//            Vector<Room> joinedRooms = aSession.getJoinedRooms();
            if (true) {

                long status = DatabaseDriver.getUserGameStatus(aSession.getUID());
                if (status == 1) {
                    JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                            "Bạn vẫn còn trong 1 bàn chơi, vui lòng chờ ván chơi kết thúc!");
                    aSession.write(resMatchJoin);
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;

                }

                // other room
                // request message and values
                JoinRequest rqMatchJoin = (JoinRequest) aReqMsg;

//                int zone = 0;
                if (rqMatchJoin.zone_id >= 0) {
                    aSession.setCurrentZone(rqMatchJoin.zone_id);
                }
                Zone gameZone = aSession.findZone(aSession.getCurrentZone());

                UserEntity newUser = DatabaseDriver.getUserInfo(rqMatchJoin.uid);
                mLog.debug("CURRENT ZONE = " + gameZone.getZoneName());
                
		// <editor-fold desc="if (rqMatchJoin.quickplay) ">
                if (rqMatchJoin.quickplay) {
                    long maxId = 0;

                    switch (aSession.getCurrentZone()) {
                        case ZoneID.TIENLEN_MB:
                        case ZoneID.TIENLEN_DEMLA:
                        case ZoneID.TIENLEN:
                        case ZoneID.PHOM:
                        case ZoneID.XITO:
                        case ZoneID.POKER:
                        case ZoneID.BACAY:
                        case ZoneID.GAME_CHAN: 
                        case ZoneID.MAUBINH:{
//                            mLog.debug("vo vo");
                            int maxAvail = 100;
//                            long maxTime = System.currentTimeMillis();

                            Vector<RoomEntity> vv = gameZone.dumpWaiting();
                            for (int i = 0; i < vv.size(); i++) {
//                                if (vv.get(i).getJoinMoney() <= newUser.money) {
				if ( GameRuleConfig.getRequiredMoneyToJoin( aSession.getCurrentZone(), vv.get(i).moneyBet) <= newUser.money) {
                                    SimpleTable table = (SimpleTable) vv.get(i).mAttactmentData;
                                    int avail = vv.get(i).mCapacity - vv.get(i).mPlayingSize;

                                    if (!table.roomIsFull()) {
                                        if (!table.isPlaying) {
                                            if (avail > 0 && avail < maxAvail) {
//                                                if ((System.currentTimeMillis() - table.startTime) < maxTime) {
//                                                    maxTime = (System.currentTimeMillis() - table.startTime);

                                                    maxAvail = avail;
                                                    maxId = vv.get(i).mRoomId;
//                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (maxId > 0) {
                        rqMatchJoin.mMatchId = maxId;
                    }
                    if (maxId == 0) {
                        JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                        resMatchJoin.setFailure(ResponseCode.FAILURE,
                                "Không tìm được bàn còn trống. Bạn hãy tạo bàn mới, hoặc vào xem 1 bàn đang chơi!");
                        aSession.write(resMatchJoin);
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }
                }
		// </editor-fold>
		
                // get the current room to notify to the opponent
                Room room = gameZone.findRoom(rqMatchJoin.mMatchId);

                if (room != null) {
                    mLog.debug("[JOIN ROOM] : match_id = " + rqMatchJoin.mMatchId);
		    
		    if (room.isFakeRoom) {
			mLog.debug("fake room, denied");
			resWaitingAcceptJoin.setFailure(ResponseCode.FAILURE, "Bàn đã đầy, xin vui lòng chọn bàn khác nhé!");
			aResPkg.addMessage(resWaitingAcceptJoin);
			return 1;
		    }
		    
                    // Get user information
                    //UserEntity newUser = DatabaseDriver.getUserInfo(rqMatchJoin.uid);
		    
		    // trungnm -> them vao de biet duoc nguoi choi dang o ban voi so tien bao nhieu
		    aSession.setCurrentMoneyMatch(room.minBet);

                    switch (aSession.getCurrentZone()) {
			
			// <editor-fold desc="case ZoneID.BACAY">
                        case ZoneID.BACAY: {
                            BacayTable currentTable = (BacayTable) room.getAttactmentData();
                            long ownerID = currentTable.getRoomOwner().id;
                            ISession ownerSession = aSession.getManager().findSession(ownerID);
                            if (newUser != null) {
                                long uid = newUser.mUid;
                                long cash = newUser.money;
                                int avatar = newUser.avatarID;
                                int level = newUser.level;
                                String username = newUser.mUsername;

                                if (currentTable.isPlaying) {
                                    resWaitingAcceptJoin.setFailure(
                                            ResponseCode.FAILURE,
                                            "Bàn chơi đã bắt đầu bạn không thể tham gia!");

                                    aResPkg.addMessage(resWaitingAcceptJoin);
                                } 
				else if (cash >= currentTable.getJoinMoney()) {
                                    System.out.println("Join info : " + currentTable.getPlayers().size() + " : " + currentTable.getMaximumPlayer());
                                    if ((currentTable.getPlayers().size() + 1) >= currentTable.getMaximumPlayer()) {
                                        resWaitingAcceptJoin.setFailure(
                                                ResponseCode.FAILURE,
                                                "Chủ bàn không muốn thêm người!");
                                        aResPkg.addMessage(resWaitingAcceptJoin);

                                    } 
				    else {
					//TODO: check level để tham gia bàn ở đây
//                                        if (level >= currentTable.getLevel()) {
                                            room.addWaitingSessionByID(aSession);
                                            ReplyBusiness rb = new ReplyBusiness();
                                            rb.make(aSession, rqMatchJoin.mMatchId, uid, ownerID);
                                            /*
                                             * resWaitingAcceptJoin.setSuccess(
                                             * ResponseCode.SUCCESS, uid, cash,
                                             * avatar, level, username);
                                             *
                                             * if (!ownerSession.realDead()) {
                                             * ownerSession.write(resWaitingAcceptJoin);
                                             * }
                                             */


//                                        } 
//					else { // not enough level
//                                            resWaitingAcceptJoin.setFailure(
//                                                    ResponseCode.FAILURE,
//                                                    "Bạn chưa đủ kinh nghiệm để tham gia bàn này!");
//                                            aResPkg.addMessage(resWaitingAcceptJoin);
//                                        }
                                    }
                                } else { // send back only player
                                    resWaitingAcceptJoin.setFailure(ResponseCode.FAILURE,
                                            currentTable.getJoinMoneyErrorMessage());
                                    aResPkg.addMessage(resWaitingAcceptJoin);
                                }
                            } else {
                                resWaitingAcceptJoin.setFailure(
                                        ResponseCode.FAILURE,
                                        "Không tìm thấy bạn trong cơ sở dữ liệu!");
                                aResPkg.addMessage(resWaitingAcceptJoin);
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.OTT">
                        case ZoneID.OTT: {
                            //deleted
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.CARO">
                        case ZoneID.CARO: {
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.COTUONG">
                        case ZoneID.COTUONG: {
//                            mLog.debug("Vao case co tuong");
                            JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                            if (newUser != null) {
                                long uid = newUser.mUid;
                                resMatchJoin.uid = uid;
//                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                CoTuongTable currentTable = (CoTuongTable) room.getAttactmentData();
                                // Check money of player
                                //Tho
//                                if (currentTable.isFull) {
//                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                            "Phòng đã đầy!");
//                                    aSession.write(resMatchJoin);
//                                    return 1;
//                                }
                                //end
                                // in cases
//                                CoTuongPlayer newPlayer = new CoTuongPlayer(uid);
//                                newPlayer.setAvatarID(newUser.avatarID);
//                                newPlayer.setLevel(newUser.level);
//                                newPlayer.setCash(newUser.money);
//                                newPlayer.setUsername(newUser.mUsername);
//                                newPlayer.moneyForBet = currentTable.getMinBet();
//                                newPlayer.setCurrentMatchID(rqMatchJoin.mMatchId);
//                                currentTable.addPlayer(newPlayer);
//                                // join's values
//                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
//                                        room.getName(),
//                                        currentTable.getMinBet(), aSession.getCurrentZone());
//                                resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
//                                resMatchJoin.setCurrentPlayersCoTuong(
//                                        currentTable.getPlayers(),
//                                        currentTable.getOwner());

//                                    Tho: Start
                                //send owner and player to observers when match started!
                                if (currentTable.isFull) {
                                    CoTuongPlayer newPlayer = new CoTuongPlayer(uid);
                                    currentTable.player_list.add(newPlayer);
                                    int joinedCode = room.joinRoom(aSession);
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            currentTable.getMinBet(), aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    currentTable.removeAllPlayer();
                                    currentTable.addPlayer(currentTable.player);
                                    resMatchJoin.setCurrentPlayersCoTuong(
                                            currentTable.getPlayers(),
                                            currentTable.getOwner());
                                    resMatchJoin.setIsJoinAfterPlaying(true);
                                    resMatchJoin.currentID = currentTable.getCurrPlayID();
                                    resMatchJoin.setChessBoard(currentTable.getBoard());
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                int joinedCode = room.joinRoom(aSession);
                                if (joinedCode == 1) {
                                    CoTuongPlayer newPlayer = new CoTuongPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.getMinBet();
                                    newPlayer.setCurrentMatchID(rqMatchJoin.mMatchId);
                                    currentTable.removeAllPlayer();
                                    currentTable.addPlayer(newPlayer);
                                    // join's values
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            currentTable.getMinBet(), aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    resMatchJoin.setCurrentPlayersCoTuong(
                                            currentTable.getPlayers(),
                                            currentTable.getOwner());
                                    currentTable.player_list.add(newPlayer);
                                    resMatchJoin.setPlayerList(currentTable.getPlayersList());
                                    currentTable.mIsEnd = false;
                                    resMatchJoin.available = currentTable.available;
                                    resMatchJoin.totalTime = currentTable.totalTime;


                                    //for android
                                    JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                            uid, newUser.mUsername,
                                            newUser.level,
                                            newUser.avatarID,
                                            newUser.money,
                                            ZoneID.TIENLEN);
                                    ArrayList<ISession> list = room.getListSession();
                                     System.out.println("list session: !" +list.size());
                                    for (ISession i : list) {
                                        if (i.isAndroid() || i.isIphone()) {
                                            System.out.println("sent Android!" + i);
                                            i.write(broadcastMsg);
                                        }
                                    }

                                    // broadcast's values
//                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
//                                            uid, newUser.mUsername,
//                                            newUser.level,
//                                            newUser.avatarID,
//                                            newUser.money, ZoneID.OTT);

                                    // join's values
//                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
//                                            room.getName(),
//                                            currentTable.getMinBet(), aSession.getCurrentZone());
//                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
//                                    resMatchJoin.setCurrentPlayersCoTuong(
//                                            currentTable.getPlayers(),
//                                            currentTable.getOwner());
                                    // send broadcast msg to friends
//                                    room.broadcastMessage(broadcastMsg, aSession, true);
                                } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                                }

                                //Tho: End
                                room.broadcastMessage(resMatchJoin, aSession, true);
//                                aSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.GAME_CHAN">
                        case ZoneID.GAME_CHAN: {
//                            if (newUser != null) {
//                                JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
//                                long moneyOfPlayer = newUser.money;
//                                long uid = newUser.mUid;
//                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
//                                ChanTable table = (ChanTable) room.getAttactmentData();
//                                // Check money of player
//                                if (moneyOfPlayer < table.getJoinMoney()) {
//                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                            table.getJoinMoneyErrorMessage());
//                                    aSession.write(resMatchJoin);
//                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                                    return 1;
//                                }
//
//                                if (table.getPlayings().size() + table.getWaitings().size() >= table.getMaximumPlayer()) {
//                                    System.out.println("table.getMaximumPlayer() : " + table.getMaximumPlayer());
//                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                            "Bàn chơi này đã đủ người, bạn vui lòng chọn bàn khác!");
//                                    aSession.write(resMatchJoin);
//                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                                    return 1;
//                                }
//                                if (table.containPlayer(uid)) {
//
//                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                            "Bạn đã thoát khỏi bàn chơi, vui lòng chờ hết ván!");
//                                    aSession.leftRoom(rqMatchJoin.mMatchId);
//                                    aSession.write(resMatchJoin);
//                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                                    return 1;
//
//                                }
//                                int joinedCode = room.join(aSession);
//
//                                // in cases
//                                if (joinedCode == RoomStatus.JOIN_OBSERVER || joinedCode == RoomStatus.JOIN_PLAYER) {
//                                    boolean isResume = false;
//                                    String cards = "";
//                                    if (true) {
//                                        ChanPlayer newPlayer = new ChanPlayer(uid);
//                                        newPlayer.avatarID = (newUser.avatarID);
//                                        newPlayer.level = (newUser.level);
//                                        newPlayer.money = (newUser.money);
//                                        newPlayer.username = (newUser.mUsername);
//                                        newPlayer.moneyForBet = table.firstCashBet;
//                                        newPlayer.currentMatchID = rqMatchJoin.mMatchId;
//                                        newPlayer.currentSession = aSession;
//
//                                        table.join(newPlayer);
//                                        if (table.isPlaying) {
//                                            resMatchJoin.isObserve = true;
//                                        }
//                                        /*
//                                         * DatabaseDriver.logUserMatch(newPlayer.id,
//                                         * rqMatchJoin.mMatchId, "ban la nguoi
//                                         * choi", table.firstCashBet, false,
//                                         * rqMatchJoin.mMatchId);
//                                         */
//
//                                    }
//
//                                    // broadcast's values
//                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
//                                            uid, newUser.mUsername,
//                                            newUser.level,
//                                            newUser.avatarID,
//                                            newUser.money,
//                                            ZoneID.GAME_CHAN);
//
//
//                                    //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);
//
//                                    // join's values
//                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
//                                            room.getName(),
//                                            table.firstCashBet, aSession.getCurrentZone());
//                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
//
//                                    resMatchJoin.setCurrentPlayersChan(
//                                            table.getPlayings(), table.getWaitings(),
//                                            table.owner);
//
//                                    resMatchJoin.setCapacity(table.getMaximumPlayer());
//
//
//                                    // send broadcast msg to friends
//                                    room.broadcastMessage(broadcastMsg, aSession, true);
//
//                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
//                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                            "Bàn chơi này đã đủ người, bạn vui lòng chọn bàn khác!");
//                                }
//                                // Feedback to Player
//                                aSession.write(resMatchJoin);
//                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.PHOM">
                        case ZoneID.PHOM: {
                            // Feedback to Player
                            JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                            if (newUser != null) {
                                long moneyOfPlayer = newUser.money;
                                long uid = newUser.mUid;
                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                PhomTable table = (PhomTable) room.getAttactmentData();
                                // Check money of player
                                if (moneyOfPlayer < table.getJoinMoney()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            table.getJoinMoneyErrorMessage());
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }

                                if (table.getPlayings().size() + table.getWaitings().size() >= table.getMaximumPlayer()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đầy rồi, bạn thông cảm chờ nhé!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                if (table.containPlayer(uid)) {

                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã thóat khỏi bàn. Nên không thể quay lại!");
                                    aSession.leftRoom(rqMatchJoin.mMatchId);
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;

                                    /*
                                     * if (table.isPlaying) { isResume = true; }
                                     *
                                     * PhomPlayer newPlayer =
                                     * table.findPlayer(uid); cards =
                                     * newPlayer.allPokersToString();
                                     * newPlayer.isAutoPlay = false;
                                     */

                                }
                                int joinedCode = room.join(aSession);

                                // in cases
                                if (joinedCode == RoomStatus.JOIN_OBSERVER || joinedCode == RoomStatus.JOIN_PLAYER) {
                                    boolean isResume = false;
                                    String cards = "";
                                    if (true) {
                                        PhomPlayer newPlayer = new PhomPlayer(uid);
                                        newPlayer.setAvatarID(newUser.avatarID);
                                        newPlayer.setLevel(newUser.level);
                                        newPlayer.setCash(newUser.money);
                                        newPlayer.setUsername(newUser.mUsername);
                                        newPlayer.moneyForBet = table.firstCashBet;
                                        newPlayer.currentMatchID = rqMatchJoin.mMatchId;
                                        newPlayer.currentSession = aSession;

                                        table.join(newPlayer);
                                        if (table.isPlaying) {
                                            resMatchJoin.isObserve = true;
                                        }
                                        /*
                                         * DatabaseDriver.logUserMatch(newPlayer.id,
                                         * rqMatchJoin.mMatchId, "ban la nguoi
                                         * choi", table.firstCashBet, false,
                                         * rqMatchJoin.mMatchId);
                                         */

                                    }

                                    // broadcast's values
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                            uid, newUser.mUsername,
                                            newUser.level,
                                            newUser.avatarID,
                                            newUser.money,
                                            ZoneID.PHOM);


                                    //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);

                                    // join's values
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            table.firstCashBet, aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    resMatchJoin.setCurrentPlayersPhom(
                                            table.getPlayings(), table.getWaitings(),
                                            table.owner);

                                    resMatchJoin.setPhomInfo(table.anCayMatTien, table.taiGuiUDen, table.isPlaying,
                                            isResume, table.currentPlayer.id, cards, table.restCards.size());
                                    resMatchJoin.setCapacity(table.getMaximumPlayer());


                                    // send broadcast msg to friends
                                    room.broadcastMessage(broadcastMsg, aSession, true);

                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                                }
                                // Feedback to Player
                                aSession.write(resMatchJoin);
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.TIENLEN_MB, TIENLEN_DEMLA, TIENLEN">
                        case ZoneID.TIENLEN_MB:
                        case ZoneID.TIENLEN_DEMLA:
                        case ZoneID.TIENLEN: {
                            JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                            if (newUser != null) {
                                long moneyOfPlayer = newUser.money;
                                long uid = newUser.mUid;
                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                TienLenTable table = (TienLenTable) room.getAttactmentData();
                                // Check money of player
                                if (moneyOfPlayer < table.getJoinMoney()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            table.getJoinMoneyErrorMessage());
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                if ((table.getPlayings().size() + table.getWaitings().size()) >= table.getMaximumPlayer()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đầy rồi, bạn thông cảm chờ nhé!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Bấm vào bàn n lần!!!
                                if (table.containPlayer(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn vẫn đang còn trong bàn, vui lòng chờ hết ván nhé!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Nếu bị đuổi quá 2 lần không cho  vào
                                if (table.isblk(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã bị chủ phòng đuổi quá 2 lần nên không vào lại được!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                int joinedCode = room.join(aSession);
                                // in cases
                                if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                    TienLenPlayer newPlayer = new TienLenPlayer(uid);

                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = table.firstCashBet;
                                    newPlayer.currentMatchID = rqMatchJoin.mMatchId;
                                    newPlayer.currentSession = aSession;
                                    table.join(newPlayer);
                                    if (table.isPlaying) {
                                        resMatchJoin.isObserve = true;
                                        resMatchJoin.cards = Utils.bytesToString(table.lastCards);
                                        resMatchJoin.turn = table.getCurrentTurnID();
                                    }
//                                    DatabaseDriver.logUserMatch(newPlayer.id, rqMatchJoin.mMatchId,
//                                            "ban la nguoi choi", table.firstCashBet, false, rqMatchJoin.mMatchId);
//
//                                    // broadcast's values
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                            uid, newUser.mUsername,
                                            newUser.level,
                                            newUser.avatarID,
                                            newUser.money,
                                            ZoneID.TIENLEN);
                                    // join's values
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            table.firstCashBet, aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    resMatchJoin.setCurrentTienLenPlayers(table.getPlayings(), table.getWaitings(),
                                            table.getOwner());
				    
				    resMatchJoin.setCapacity(table.getMaximumPlayer());

                                    // send broadcast msg to friends
                                    room.broadcastMessage(broadcastMsg, aSession, true);

                                } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                                }
                                // Feedback to Player
                                aSession.write(resMatchJoin);
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.BAUCUA">
                        case ZoneID.BAUCUA: {

                            JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
//                            Vector<Room> joinedRoom = aSession.getJoinedRooms();
//                            if (joinedRoom.size() > 0) {
//                                resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                        "Bạn vẫn đang còn trong bàn chơi, vui lòng chờ hết ván!");
//                                aSession.write(resMatchJoin);
//                                return 1;
//                            }
                            if (newUser != null) {
                                long moneyOfPlayer = newUser.money;
                                long uid = newUser.mUid;
                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                BauCuaTable table = (BauCuaTable) room.getAttactmentData();
                                // Check money of player
                                if (moneyOfPlayer < table.getJoinMoney()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            table.getJoinMoneyErrorMessage());
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                } 
				else if (!table.canAddPlayer()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Chủ bàn không đủ tiền để thêm người nữa!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                } //Nếu bị đuổi quá 2 lần không cho  vào
                                if (table.isblk(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã bị chủ phòng đuổi quá 2 lần nên không vào lại được!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                if ((table.getPlayings().size() + table.getWaitings().size()) >= table.getMaximumPlayer()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đầy rồi, bạn thông cảm chờ nhé!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Bấm vào bàn n lần!!!
                                if (table.containPlayer(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã thoát khỏi bàn. Nên không thể quay lại!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
//                                //Nếu bị đuổi quá 2 lần không cho  vào
//                                if (table.isblk(uid)) {
//                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
//                                            "Bạn đã bị chủ phòng đuổi quá 2 lần nên không vào lại được!");
//                                    aSession.write(resMatchJoin);
//                                    return 1;
//                                }
                                int joinedCode = room.join(aSession);
                                // in cases
                                if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                    BauCuaPlayer newPlayer = new BauCuaPlayer(uid);

                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = table.firstCashBet;
                                    newPlayer.currentMatchID = rqMatchJoin.mMatchId;
                                    newPlayer.currentSession = aSession;
                                    table.join(newPlayer);
                                    if (table.isPlaying) {
                                        resMatchJoin.isObserve = true;
                                        resMatchJoin.time = (table.timeBet - (int) (table.timerAuto.getCurrentTime() / 1000));
                                        newPlayer.reset(table.firstCashBet);
                                    } else {
                                        resMatchJoin.isObserve = false;
                                        System.out.println("timerAuto.getCurrentTime() :" + table.timerAuto.getCurrentTime());
                                        System.out.println(" resMatchJoin.turn :" + resMatchJoin.turn);
                                    }
//                                    DatabaseDriver.logUserMatch(newPlayer.id, rqMatchJoin.mMatchId,
//                                            "ban la nguoi choi", table.firstCashBet, false, rqMatchJoin.mMatchId);
//
//                                    // broadcast's values
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                            uid, newUser.mUsername,
                                            newUser.level,
                                            newUser.avatarID,
                                            newUser.money,
                                            ZoneID.BAUCUA);
                                    // join's values
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            table.firstCashBet, aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    resMatchJoin.setCurrentBauCuaPlayers(table.getPlayings(), table.getWaitings(),
                                            table.getOwner());
				    resMatchJoin.setCapacity(table.getMaximumPlayer());
                                    // send broadcast msg to friends
                                    room.broadcastMessage(broadcastMsg, aSession, true);

                                } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                                }
                                // Feedback to Player
                                aSession.write(resMatchJoin);
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.POKER">
                        case ZoneID.POKER: {
                            JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                            if (newUser != null) {
                                long moneyOfPlayer = newUser.money;
                                long uid = newUser.mUid;
                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                PokerTable table = (PokerTable) room.getAttactmentData();
                                // Check money of player
                                if (moneyOfPlayer < table.getJoinMoney()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            table.getJoinMoneyErrorMessage());
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                if ((table.getPlayings().size() + table.getWaitings().size()) >= table.getMaximumPlayer()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đầy rồi, bạn thông cảm chờ nhé!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Bấm vào bàn n lần!!!
                                if (table.containPlayer(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã thoát khỏi bàn. Nên không thể quay lại!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Nếu bị đuổi quá 2 lần không cho  vào
                                if (table.isblk(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã bị chủ phòng đuổi quá 2 lần nên không vào lại được!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                int joinedCode = room.join(aSession);
                                // in cases
                                if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                    PokerPlayer newPlayer = new PokerPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = table.firstCashBet;
                                    newPlayer.currentMatchID = rqMatchJoin.mMatchId;
                                    newPlayer.currentSession = aSession;
                                    table.join(newPlayer);
                                    if (table.isPlaying) {
                                        resMatchJoin.isObserve = true;
                                        resMatchJoin.minBet = table.getPotMoney();
                                        resMatchJoin.cards = Utils.bytesToString(table.getCurrentPoker());
                                        resMatchJoin.turn = table.getCurrentTurnID();
                                    }
//                                    DatabaseDriver.logUserMatch(newPlayer.id, rqMatchJoin.mMatchId,
//                                            "ban la nguoi choi", table.firstCashBet, false, rqMatchJoin.mMatchId);
//
//                                    // broadcast's values
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                            uid, newUser.mUsername,
                                            newUser.level,
                                            newUser.avatarID,
                                            newUser.money,
                                            ZoneID.POKER);
                                    // join's values
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            table.firstCashBet, aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    resMatchJoin.setCurrentPlayersPoker(table.getPlayings(), table.getWaitings(),
                                            table.getOwner());
				    resMatchJoin.setCapacity(table.getMaximumPlayer());
                                    // send broadcast msg to friends
                                    room.broadcastMessage(broadcastMsg, aSession, true);

                                } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn chơi này đã đủ người, bạn vui lòng chọn bàn khác!");
                                }
                                // Feedback to Player
                                aSession.write(resMatchJoin);
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.XITO">
                        case ZoneID.XITO: {
                            JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);

                            if (newUser != null) {
                                long moneyOfPlayer = newUser.money;
                                long uid = newUser.mUid;
                                JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                XiToTable table = (XiToTable) room.getAttactmentData();
                                // Check money of player
                                if (moneyOfPlayer < table.getJoinMoney()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            table.getJoinMoneyErrorMessage());
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                if ((table.getPlayings().size() + table.getWaitings().size()) >= table.getMaximumPlayer()) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn này đã đầy rồi, bạn vui lòng chọn bàn khác!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Bấm vào bàn n lần!!!
                                if (table.containPlayer(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn vẫn còn trong bàn chơi, vui lòng chờ hết ván!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                //Nếu bị đuổi quá 2 lần không cho  vào
                                if (table.isblk(uid)) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bạn đã bị chủ phòng đuổi quá 2 lần nên không vào lại được!");
                                    aSession.write(resMatchJoin);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                int joinedCode = room.join(aSession);
                                // in cases
                                if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                    PokerPlayer newPlayer = new PokerPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = table.firstCashBet;
                                    newPlayer.currentMatchID = rqMatchJoin.mMatchId;
                                    newPlayer.currentSession = aSession;
                                    table.join(newPlayer);
                                    if (table.isPlaying) {
                                        resMatchJoin.isObserve = true;
                                        resMatchJoin.minBet = table.getPotMoney();
//                                        resMatchJoin.cards = Utils.bytesToString(table.getCurrentPoker());
                                        resMatchJoin.turn = table.getCurrentTurnID();
                                        resMatchJoin.mType = table.numRound;
                                    }
//                                    DatabaseDriver.logUserMatch(newPlayer.id, rqMatchJoin.mMatchId,
//                                            "ban la nguoi choi", table.firstCashBet, false, rqMatchJoin.mMatchId);
//
//                                    // broadcast's values
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                            uid, newUser.mUsername,
                                            newUser.level,
                                            newUser.avatarID,
                                            newUser.money,
                                            ZoneID.XITO);
                                    // join's values
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            room.getName(),
                                            table.firstCashBet, aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqMatchJoin.mMatchId);
                                    resMatchJoin.setCurrentPlayersPoker(table.getPlayings(), table.getWaitings(),
                                            table.getOwner());
				    resMatchJoin.setCapacity(table.getMaximumPlayer());
                                    // send broadcast msg to friends
                                    room.broadcastMessage(broadcastMsg, aSession, true);

                                } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                    resMatchJoin.setFailure(ResponseCode.FAILURE,
                                            "Bàn chơi này đã đủ người, bạn vui lòng chọn bàn khác!");
                                }
                                // Feedback to Player
                                aSession.write(resMatchJoin);
                            }
                            break;
                        }
			    // </editor-fold>
			    
			    // <editor-fold desc="case ZoneID.MAUBINH">
			case ZoneID.MAUBINH: {
                            break;
                        }
			    // </editor-fold>
			    
                        //TODO: Add more here
                        default:
                            break;
                    }
                } else { // send back only player
                    resWaitingAcceptJoin.setFailure(ResponseCode.FAILURE,
                            "Bàn đã bị hủy!");
                    aResPkg.addMessage(resWaitingAcceptJoin);
                }
            } else { // If player is joined an other room
                // Do nothing
                mLog.error("CO LOI XAY RA PHAI KO EM:player is joined an other room");
            }
            // return
        } catch (Throwable t) {
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
