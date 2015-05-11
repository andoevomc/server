package dreamgame.business;

import org.slf4j.Logger;

import phom.data.PhomPlayer;
import phom.data.PhomTable;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.CancelResponse;
import dreamgame.protocol.messages.OutResponse;
import dreamgame.protocol.messages.RestartRequest;
import dreamgame.protocol.messages.RestartResponse;
import dreamgame.protocol.messages.StartRequest;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaTable;
import dreamgame.config.DebugConfig;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.poker.data.PokerTable;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;

public class RestartBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(RestartBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "RestartBusiness - handleMessage");
	}
        mLog.debug("[RESTART] : Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        RestartResponse resMatchReturn = (RestartResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        CancelResponse resCancelMatch = (CancelResponse) msgFactory.getResponseMessage(MessagesID.MATCH_CANCEL);
        try {
            RestartRequest rqReturn = (RestartRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room room = bacayZone.findRoom(rqReturn.mMatchId);
            resMatchReturn.setZoneID(aSession.getCurrentZone());
            if (room != null) {
                mLog.debug("[RESTART] Current room = " + room.getName());

                switch (aSession.getCurrentZone()) {
                    case ZoneID.BACAY: {
                        BacayTable currentTable = (BacayTable) room.getAttactmentData();
			currentTable.resetForNewMatch();
                        // Remove player is not enough money
                        int i = 0;
                        mLog.debug("players size: " + currentTable.getPlayers().size());
                        while (i < currentTable.getPlayers().size()) {
                            BacayPlayer player = currentTable.getPlayers().get(i);
                            if (player.cash < currentTable.getMinBet()) {
                                OutResponse rqsOut =
                                        (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
                                rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
                                        "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
                                mLog.debug("Khong du tien roi: " + player.username + ":" + player.id);
                                ISession playerSession = aSession.getManager().findSession(player.id);
                                playerSession.write(rqsOut);
                                if (playerSession != null) {
                                    playerSession.leftRoom(rqReturn.mMatchId);
                                    room.left(playerSession);
                                }
                                //remove from players list
                                currentTable.removePlayer(player);
                                rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
                                        player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
                                room.broadcastMessage(rqsOut, aSession, true);
                                Thread.sleep(400);
                            } else {
                                i++;
                            }
                        }
                        //Reset table
//                        currentTable.resetForNewMatch();
                        if (currentTable.getPlayers().size() == 0) {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bàn chơi không còn người. Bạn chờ thêm chút nữa", true);
                        } else if (currentTable.canOwnerContinue()) {
                            // response to all current players
                            resMatchReturn.setSuccess(ResponseCode.SUCCESS, currentTable.getRoomOwner(),
                                    currentTable.getPlayers(), rqReturn.mMatchId, room.getName());

                            room.broadcastMessage(resMatchReturn, aSession, false);
                            IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
                            StartRequest rqMatchStart =
                                    (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
                            rqMatchStart.mMatchId = rqReturn.mMatchId;
                            try {
                                business.handleMessage(aSession, rqMatchStart, aResPkg);
                            } catch (ServerException se) {
                            }
                        } else {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để chơi tiếp.", false);
                            aSession.write(resMatchReturn);
                            resCancelMatch.setSuccess(ResponseCode.SUCCESS, aSession.getUID());
                            resCancelMatch.setGamePlaying(false);
                            resCancelMatch.setUserPlaying(false);
                            resCancelMatch.setMessage("Chủ bàn không còn đủ tiền để chơi tiếp.");
                            room.broadcastMessage(resCancelMatch, aSession, false);
                            room.allLeft();
                        }
                        break;
                    }
                    case ZoneID.PHOM: {
                        PhomTable table = (PhomTable) room.getAttactmentData();
                        //Remove player has not enough money
                        System.out.println("So nguoi trong room:" + table.getPlayings().size() + "  min bet  " + table.firstCashBet);
//                        try {
//                            for (PhomPlayer player : table.getPlayings()) {
//                                if (player.cash < table.firstCashBet) {
//                                    OutResponse rqsOut =
//                                            (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    mLog.debug("Khong du tien roi: " + player.username + ":" + player.id);
//                                    ISession playerSession = aSession.getManager().findSession(player.id);
//                                    playerSession.write(rqsOut);
//                                    if (playerSession != null) {
//                                        playerSession.leftRoom(rqReturn.mMatchId);
//                                        room.left(playerSession);
//                                    }
//                                    //remove from players list
//                                    table.remove(player);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    room.broadcastMessage(rqsOut, aSession, true);
//                                }
//                            }
//                        } catch (Exception eas) {
//                            eas.printStackTrace();
//                        }
                        if (table.getPlayings().size() + table.getWaitings().size() < 2) {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Chưa đủ người chơi.", false);
                        } else {
                            table.reset();
                            resMatchReturn.setPhomSuccess(ResponseCode.SUCCESS, table.owner,
                                    table.getPlayings(), rqReturn.mMatchId, room.getName());

                            //room.broadcastMessage(resMatchReturn, aSession, false);

                            IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
                            StartRequest rqMatchStart =
                                    (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
                            rqMatchStart.mMatchId = rqReturn.mMatchId;
                            try {
                                business.handleMessage(aSession, rqMatchStart, aResPkg);
                            } catch (ServerException se) {
                                se.printStackTrace();
                            }
                        }
                        break;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        TienLenTable table = (TienLenTable) room.getAttactmentData();
                        //Remove player has not enough money
                        System.out.println("So nguoi trong room:" + table.getPlayings().size() + "  min bet  " + table.firstCashBet);
//                        try {
//                            for (PhomPlayer player : table.getPlayings()) {
//                                if (player.cash < table.firstCashBet) {
//                                    OutResponse rqsOut =
//                                            (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    mLog.debug("Khong du tien roi: " + player.username + ":" + player.id);
//                                    ISession playerSession = aSession.getManager().findSession(player.id);
//                                    playerSession.write(rqsOut);
//                                    if (playerSession != null) {
//                                        playerSession.leftRoom(rqReturn.mMatchId);
//                                        room.left(playerSession);
//                                    }
//                                    //remove from players list
//                                    table.remove(player);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    room.broadcastMessage(rqsOut, aSession, true);
//                                }
//                            }
//                        } catch (Exception eas) {
//                            eas.printStackTrace();
//                        }
                        if (aSession.getUID() == table.owner.id) {
                            if (table.getPlayings().size() + table.getWaitings().size() < 2) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Chưa đủ người chơi.", false);
                            } else if (table.getOwner().cash < table.firstCashBet) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để chơi tiếp!", false);
                            } else {
//                                table.resetTable();
                                System.out.println("Tiến của chủ bàn: " + table.getOwner().cash);
                                System.out.println("Tiến cược: " + table.firstCashBet);
                                resMatchReturn.setTienLenSuccess(ResponseCode.SUCCESS, table.owner,
                                        table.getPlayings(), rqReturn.mMatchId, room.getName());
                                if (aSession.getMobile()) {
                                    aSession.write(resMatchReturn);
                                }
//                            room.broadcastMessage(resMatchReturn, aSession, false);
                                IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
                                StartRequest rqMatchStart =
                                        (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
                                rqMatchStart.mMatchId = rqReturn.mMatchId;
                                try {
                                    business.handleMessage(aSession, rqMatchStart, aResPkg);
                                } catch (ServerException se) {
                                    se.printStackTrace();
                                }
                            }
                        } else {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn, không được quyền bắt đầu!", false);
                        }
                        break;
                    }
                    case ZoneID.BAUCUA: {
                        BauCuaTable table = (BauCuaTable) room.getAttactmentData();
                        //Remove player has not enough money
//                        System.out.println("So nguoi trong room:" + table.getPlayings().size() + "  min bet  " + table.firstCashBet);
//                        try {
//                            for (PhomPlayer player : table.getPlayings()) {
//                                if (player.cash < table.firstCashBet) {
//                                    OutResponse rqsOut =
//                                            (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    mLog.debug("Khong du tien roi: " + player.username + ":" + player.id);
//                                    ISession playerSession = aSession.getManager().findSession(player.id);
//                                    playerSession.write(rqsOut);
//                                    if (playerSession != null) {
//                                        playerSession.leftRoom(rqReturn.mMatchId);
//                                        room.left(playerSession);
//                                    }
//                                    //remove from players list
//                                    table.remove(player);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    room.broadcastMessage(rqsOut, aSession, true);
//                                }
//                            }
//                        } catch (Exception eas) {
//                            eas.printStackTrace();
//                        }
                        if (aSession.getUID() == table.owner.id) {
                            if (table.getPlayings().size() + table.getWaitings().size() < 2) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Chưa đủ người chơi.", false);
                            } else if (table.getOwner().cash < table.firstCashBet) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để chơi tiếp!", false);
                            } else {
//                                table.resetTable();
//                                System.out.println("Tiến của chủ bàn: " + table.getOwner().cash);
//                                System.out.println("Tiến cược: " + table.firstCashBet);
//                                resMatchReturn.setTienLenSuccess(ResponseCode.SUCCESS, table.owner,
//                                        table.getPlayings(), rqReturn.mMatchId, room.getName());
//                                if (aSession.getMobile()) {
//                                    aSession.write(resMatchReturn);
//                                }
//                            room.broadcastMessage(resMatchReturn, aSession, false);
                                IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
                                StartRequest rqMatchStart =
                                        (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
                                rqMatchStart.mMatchId = rqReturn.mMatchId;
                                try {
                                    business.handleMessage(aSession, rqMatchStart, aResPkg);
                                } catch (ServerException se) {
                                    se.printStackTrace();
                                }
                            }
                        } else {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn, không được quyền bắt đầu!", false);
                        }
                        break;
                    }
                    case ZoneID.POKER: {
                        PokerTable table = (PokerTable) room.getAttactmentData();
                        //Remove player has not enough money
                        System.out.println("So nguoi trong room:" + table.getPlayings().size() + "  min bet  " + table.firstCashBet);
//                        try {
//                            for (PhomPlayer player : table.getPlayings()) {
//                                if (player.cash < table.firstCashBet) {
//                                    OutResponse rqsOut =
//                                            (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    mLog.debug("Khong du tien roi: " + player.username + ":" + player.id);
//                                    ISession playerSession = aSession.getManager().findSession(player.id);
//                                    playerSession.write(rqsOut);
//                                    if (playerSession != null) {
//                                        playerSession.leftRoom(rqReturn.mMatchId);
//                                        room.left(playerSession);
//                                    }
//                                    //remove from players list
//                                    table.remove(player);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    room.broadcastMessage(rqsOut, aSession, true);
//                                }
//                            }
//                        } catch (Exception eas) {
//                            eas.printStackTrace();
//                        }
                        if (aSession.getUID() == table.owner.id) {
                            if (table.getPlayings().size() + table.getWaitings().size() < 2) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Bàn chưa đủ người chơi!", false);
                            } else if (table.getOwner().cash < table.firstCashBet) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để chơi tiếp!", false);
                            } else {
////                                table.resetTable();
//                                System.out.println("Tiến của chủ bàn: " + table.getOwner().cash);
//                                System.out.println("Tiến cược: " + table.firstCashBet);
//                                resMatchReturn.setPokerSuccess(ResponseCode.SUCCESS, table.owner,
//                                        table.getPlayings(), rqReturn.mMatchId, room.getName());
//                                if (aSession.getMobile()) {
//                                    aSession.write(resMatchReturn);
//                                }
//                            room.broadcastMessage(resMatchReturn, aSession, false);
                                IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
                                StartRequest rqMatchStart =
                                        (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
                                rqMatchStart.mMatchId = rqReturn.mMatchId;
                                try {
                                    business.handleMessage(aSession, rqMatchStart, aResPkg);
                                } catch (ServerException se) {
                                    se.printStackTrace();
                                }
                            }
                        } else {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn!", false);
                        }
                        break;
                    }
                    case ZoneID.XITO: {
                        XiToTable table = (XiToTable) room.getAttactmentData();
                        //Remove player has not enough money
                        System.out.println("So nguoi trong room:" + table.getPlayings().size() + "  min bet  " + table.firstCashBet);
//                        try {
//                            for (PhomPlayer player : table.getPlayings()) {
//                                if (player.cash < table.firstCashBet) {
//                                    OutResponse rqsOut =
//                                            (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    mLog.debug("Khong du tien roi: " + player.username + ":" + player.id);
//                                    ISession playerSession = aSession.getManager().findSession(player.id);
//                                    playerSession.write(rqsOut);
//                                    if (playerSession != null) {
//                                        playerSession.leftRoom(rqReturn.mMatchId);
//                                        room.left(playerSession);
//                                    }
//                                    //remove from players list
//                                    table.remove(player);
//                                    rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                            player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                    room.broadcastMessage(rqsOut, aSession, true);
//                                }
//                            }
//                        } catch (Exception eas) {
//                            eas.printStackTrace();
//                        }
                        if (aSession.getUID() == table.owner.id) {
                            if (table.getPlayings().size() + table.getWaitings().size() < 2) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Bàn chưa đủ người chơi!", false);
                            } else if (table.getOwner().cash < table.firstCashBet) {
                                resMatchReturn.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để chơi tiếp!", false);
                            } else {
////                                table.resetTable();
//                                System.out.println("Tiến của chủ bàn: " + table.getOwner().cash);
//                                System.out.println("Tiến cược: " + table.firstCashBet);
//                                resMatchReturn.setPokerSuccess(ResponseCode.SUCCESS, table.owner,
//                                        table.getPlayings(), rqReturn.mMatchId, room.getName());
//                                if (aSession.getMobile()) {
//                                    aSession.write(resMatchReturn);
//                                }
//                            room.broadcastMessage(resMatchReturn, aSession, false);
                                IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
                                StartRequest rqMatchStart =
                                        (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
                                rqMatchStart.mMatchId = rqReturn.mMatchId;
                                try {
                                    business.handleMessage(aSession, rqMatchStart, aResPkg);
                                } catch (ServerException se) {
                                    se.printStackTrace();
                                }
                            }
                        } else {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn!", false);
                        }
                        break;
                    }
                    case ZoneID.COTUONG: {
                        CoTuongTable currentTable = (CoTuongTable) room.getAttactmentData();
//                        // Remove player is not enough money
//                        int i = 0;
//                        mLog.debug("players size: " + currentTable.getPlayers().size());
//                        while (i < currentTable.getPlayers().size()) {
//                            CoTuongPlayer player = currentTable.getPlayers().get(i);
//                            if (player.cash < currentTable.getMinBet()) {
//                                OutResponse rqsOut =
//                                        (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
//                                rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                        "Bạn không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                mLog.debug("Không đủ tiền rồi: " + player.username + ":" + player.id);
//                                ISession playerSession = aSession.getManager().findSession(player.id);
//                                playerSession.write(rqsOut);
//                                if (playerSession != null) {
//                                    playerSession.leftRoom(rqReturn.mMatchId);
//                                    room.left(playerSession);
//                                }
//                                //remove from players list
//                                currentTable.removePlayer(player);
//                                rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
//                                        player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
//                                room.broadcastMessage(rqsOut, aSession, true);
//                            } else {
//                                i++;
//                            }
//                        }
//
//                        if (currentTable.getPlayers().size() == 0) {
//                            resMatchReturn.setFailure(ResponseCode.FAILURE,
//                                    "Bàn chơi không còn người. Bạn chờ thêm chút nữa", true);
//                        } else if (currentTable.canOwnerContinue()) {
                        if (!currentTable.isPlayerReady) {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn chơi chưa sẵn sàng!", true);
                            aSession.write(resMatchReturn);
                        } else if (!currentTable.canPlayerContinue()) {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn chơi không đủ tiền chơi nữa!", true);
                            aSession.write(resMatchReturn);
                        } else if (currentTable.canOwnerContinue()) {
                            //Reset table
                            currentTable.resetBoard();
                            // response to all current players
                            currentTable.startMatch();
                            currentTable.mIsEnd = false;
                            resMatchReturn.available = currentTable.available;
                            resMatchReturn.totalTime = currentTable.totalTime;
                            resMatchReturn.begin_id = currentTable.getCurrPlayID();
                            resMatchReturn.setCoTuongSuccess(ResponseCode.SUCCESS, currentTable.getOwner(),
                                    currentTable.getPlayers(), rqReturn.mMatchId, room.getName());
                            room.broadcastMessage(resMatchReturn, aSession, true);
//                            IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_START);
//                            StartRequest rqMatchStart =
//                                    (StartRequest) msgFactory.getRequestMessage(MessagesID.MATCH_START);
//                            rqMatchStart.mMatchId = rqReturn.mMatchId;
//                            try {
//                                business.handleMessage(aSession, rqMatchStart, aResPkg);
//                            } catch (ServerException se) {
//                            }
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        } else {
                            resMatchReturn.setFailure(ResponseCode.FAILURE,
                                    "Bạn không đủ tiền để chơi tiếp.", false);
                            aSession.write(resMatchReturn);
//                            resCancelMatch.setSuccess(ResponseCode.SUCCESS, aSession.getUID());
//                            resCancelMatch.setGamePlaying(false);
//                            resCancelMatch.setUserPlaying(false);
//                            resCancelMatch.setMessage("Chủ bàn không còn đủ tiền để chơi tiếp.");
//                            room.broadcastMessage(resCancelMatch, aSession, false);
                            room.allLeft();
                        }
                        break;
                    }
                    //TODO: Add more here
                    default:
                        break;
                }


            } else {
                resMatchReturn.setFailure(ResponseCode.FAILURE,
                        "Bạn cần tham gia vào một trận trước khi chơi.", false);
            }

        } catch (Throwable t) {
            resMatchReturn.setFailure(ResponseCode.FAILURE, "Bị lỗi " + t.toString(), false);
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resMatchReturn != null)) {
                //aResPkg.addMessage(resMatchReturn);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
