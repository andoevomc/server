package dreamgame.business;

import org.slf4j.Logger;

import phom.data.PhomPlayer;
import phom.data.PhomTable;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.KickOutRequest;
import dreamgame.protocol.messages.KickOutResponse;
import dreamgame.protocol.messages.OutResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;
import dreamgame.config.DebugConfig;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class KickOutBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(KickOutBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "KickOutBusiness - handleMessage");
	}
        mLog.debug("[KICK OUT] : Catch");
        boolean isFail = true;
        MessageFactory msgFactory = aSession.getMessageFactory();
        KickOutResponse resKickOut = (KickOutResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {

            KickOutRequest rqKickOut = (KickOutRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqKickOut.mMatchId);
            resKickOut.matchId = rqKickOut.mMatchId;

            if (currentRoom != null) {
                OutResponse broadcastMsg = (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
                broadcastMsg.matchId = rqKickOut.mMatchId;

                switch (aSession.getCurrentZone()) {
                    case ZoneID.BACAY: {
                        if (currentRoom != null) {
                            // broadcast response message
                            BacayTable currentTable = (BacayTable) currentRoom.getAttactmentData();
                            if (rqKickOut.uid != currentTable.getRoomOwner().id) {
                                BacayPlayer player = currentTable.findPlayer(rqKickOut.uid);
                                if (currentTable.getIsPlaying() || !(currentTable.isPlayerInPlayingList(rqKickOut.uid))) {
                                    resKickOut.setFailure(ResponseCode.FAILURE, "Bàn đang chơi và "
                                            + player.username + " đang chơi. Bạn không thể đuổi hắn ra ngoài được. Chờ hết ván đi!");
                                } else {
                                    try {
                                        currentTable.removePlayer(player);
                                    } catch (Exception e12) {
                                        e12.printStackTrace();
                                        currentTable.removePlayerToWaitingList(player);
                                    }
                                    broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqKickOut.uid, player.username + " bị chủ bàn đá ra ngoài",
                                            player.username, 1);
                                    // send broadcast msg to friends
                                    currentRoom.broadcastMessage(broadcastMsg, aSession, true);
                                    try {
                                        ISession buddySession = aSession.getManager().findSession(rqKickOut.uid);
                                        System.out.println(buddySession.getID() + "&&&&&&&&&&&&&&&" + rqKickOut.uid);
                                        currentRoom = buddySession.leftRoom(rqKickOut.mMatchId);
                                        currentRoom.left(buddySession);

                                    } catch (Exception eww) {
                                        eww.printStackTrace();
                                    }
                                    resKickOut.setSuccess(ResponseCode.SUCCESS);
                                }
                            } else {
                                resKickOut.setFailure(ResponseCode.FAILURE, "Bạn không phải là chủ bàn - không có quyền đuổi người khác!");
                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE, "Bạn đã thoát khỏi bàn!");
                        }
                        break;
                    }
                    case ZoneID.PHOM: {
                        PhomTable currentTable = (PhomTable) currentRoom.getAttactmentData();
                        if (aSession.getUID() == currentTable.owner.id) {
                            PhomPlayer player = (PhomPlayer) currentTable.findPlayer(rqKickOut.uid);
                            if (player == null) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Người chơi này đã thoát rồi!");
                            } else if (currentTable.isPlaying) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bàn đang chơi và "
                                        + player.username
                                        + " đang chơi. Bạn không thể đuổi hắn ra ngoài được. Chờ hết ván đi!");
                            } else {
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        rqKickOut.uid,
                                        player.username
                                        + " bị chủ bàn đá ra ngoài", player.username, 1);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);

                                resKickOut.setSuccess(ResponseCode.SUCCESS);

                                ISession ps = aSession.getManager().findSession(rqKickOut.uid);
                                if (ps != null) {
                                    Room room = ps.leftRoom(rqKickOut.mMatchId);

                                    if (room != null) {
                                        room.left(ps);
                                    } else {
                                        mLog.error("Kick out error room is null : " + rqKickOut.mMatchId);
                                    }
                                }
                                currentTable.remove(player);

                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn - không có quyền đuổi người khác!");
                        }
                        break;
                    }
                    case ZoneID.MAUBINH: {
                        break;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        TienLenTable currentTable = (TienLenTable) currentRoom.getAttactmentData();
//                        if (aSession.getUID() == currentTable.owner.id || rqKickOut.isAutoKickOut) {

                        if (aSession.getUID() == currentTable.owner.id) {
                            TienLenPlayer player = (TienLenPlayer) currentTable.findPlayer(rqKickOut.uid);
                            if (player == null) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Người chơi này đã thoát rồi!");
                            } else if (currentTable.isPlaying) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bàn đang chơi và "
                                        + player.username
                                        + " đang chơi. Bạn không thể đuổi hắn ra ngoài được. Chờ hết ván đi!");
                            } else {
                                String note = player.username + " bị chủ bàn đá ra ngoài";
                                if (rqKickOut.isAutoKickOut) {
                                    note = player.username + " không đủ tiền chơi tiếp!";
                                    if (rqKickOut.newOwner > 0) {
                                        broadcastMsg.newRoomOwner = rqKickOut.newOwner;
                                    }
                                }
                                currentTable.updateBlackList(rqKickOut.uid);
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        rqKickOut.uid,
                                        note, player.username, 1);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);

                                resKickOut.setSuccess(ResponseCode.SUCCESS);

                                ISession ps = aSession.getManager().findSession(rqKickOut.uid);
                                if (ps != null) {
                                    Room room = ps.leftRoom(rqKickOut.mMatchId);

                                    if (room != null) {
                                        room.left(ps);
                                    } else {
                                        mLog.error("Kick out error room is null : " + rqKickOut.mMatchId);
                                    }
                                }


                                player.isOutGame = true;
                                currentTable.remove(player);

                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn - không có quyền đuổi người khác!");
                        }
                        break;
                    }
                    case ZoneID.POKER: {
                        PokerTable currentTable = (PokerTable) currentRoom.getAttactmentData();
//                        if (aSession.getUID() == currentTable.owner.id || rqKickOut.isAutoKickOut) {

                        if (aSession.getUID() == currentTable.owner.id) {
                            PokerPlayer player = (PokerPlayer) currentTable.findPlayer(rqKickOut.uid);
                            if (player == null) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Người chơi này đã thoát khỏi bàn!");
                            } else if (currentTable.isPlaying) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bạn không thể đuổi người chơi khi bàn đang chơi!");
                            } else {
                                String note = player.username + " bị chủ bàn đuổi ra khỏi bàn";
                                if (rqKickOut.isAutoKickOut) {
                                    note = player.username + " không đủ tiền để chơi tiếp!";
                                    if (rqKickOut.newOwner > 0) {
                                        broadcastMsg.newRoomOwner = rqKickOut.newOwner;
                                    }
                                }
                                currentTable.updateBlackList(rqKickOut.uid);
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        rqKickOut.uid,
                                        note, player.username, 1);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);

                                resKickOut.setSuccess(ResponseCode.SUCCESS);

                                ISession ps = aSession.getManager().findSession(rqKickOut.uid);
                                if (ps != null) {
                                    Room room = ps.leftRoom(rqKickOut.mMatchId);

                                    if (room != null) {
                                        room.left(ps);
                                    } else {
                                        mLog.error("Kick out error room is null : " + rqKickOut.mMatchId);
                                    }
                                }


                                player.isOutGame = true;
                                currentTable.remove(player);

                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn nên không đuổi người chơi được!");
                        }
                        break;
                    }
                    case ZoneID.XITO: {
                        XiToTable currentTable = (XiToTable) currentRoom.getAttactmentData();
//                        if (aSession.getUID() == currentTable.owner.id || rqKickOut.isAutoKickOut) {

                        if (aSession.getUID() == currentTable.owner.id) {
                            PokerPlayer player = (PokerPlayer) currentTable.findPlayer(rqKickOut.uid);
                            if (player == null) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Người chơi này đã thoát khỏi bàn!");
                            } else if (currentTable.isPlaying) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bạn không thể đuổi người chơi khi bàn đang chơi!");
                            } else {
                                String note = player.username + " bị chủ bàn đuổi ra khỏi bàn chơi";
                                if (rqKickOut.isAutoKickOut) {
                                    note = player.username + " không đủ tiền để chơi tiếp!";
                                    if (rqKickOut.newOwner > 0) {
                                        broadcastMsg.newRoomOwner = rqKickOut.newOwner;
                                    }
                                }
                                currentTable.updateBlackList(rqKickOut.uid);
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        rqKickOut.uid,
                                        note, player.username, 1);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);

                                resKickOut.setSuccess(ResponseCode.SUCCESS);

                                ISession ps = aSession.getManager().findSession(rqKickOut.uid);
                                if (ps != null) {
                                    Room room = ps.leftRoom(rqKickOut.mMatchId);

                                    if (room != null) {
                                        room.left(ps);
                                    } else {
                                        mLog.error("Kick out error room is null : " + rqKickOut.mMatchId);
                                    }
                                }


                                player.isOutGame = true;
                                currentTable.remove(player);

                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn nên không được đuổi người chơi!");
                        }
                        break;
                    }
                    case ZoneID.BAUCUA: {
                        BauCuaTable currentTable = (BauCuaTable) currentRoom.getAttactmentData();
//                        if (aSession.getUID() == currentTable.owner.id || rqKickOut.isAutoKickOut) {

                        if (aSession.getUID() == currentTable.owner.id) {
                            BauCuaPlayer player = (BauCuaPlayer) currentTable.findPlayer(rqKickOut.uid);
                            if (player == null) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Người chơi này đã thoát rồi!");
                            } else if (currentTable.isPlaying) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bàn đang chơi và "
                                        + player.username
                                        + " đang chơi. Bạn không thể đuổi hắn ra ngoài được. Chờ hết ván đi!");
                            } else {
                                String note = player.username + " bị chủ bàn đuổi ra khỏi bàn";
                                if (rqKickOut.isAutoKickOut) {
                                    note = player.username + " không đủ tiền chơi tiếp!";
                                    if (rqKickOut.newOwner > 0) {
                                        broadcastMsg.newRoomOwner = rqKickOut.newOwner;
                                    }
                                }
                                currentTable.updateBlackList(rqKickOut.uid);
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        rqKickOut.uid,
                                        note, player.username, 1);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);

                                resKickOut.setSuccess(ResponseCode.SUCCESS);

                                ISession ps = aSession.getManager().findSession(rqKickOut.uid);
                                if (ps != null) {
                                    Room room = ps.leftRoom(rqKickOut.mMatchId);

                                    if (room != null) {
                                        room.left(ps);
                                    } else {
                                        mLog.error("Kick out error room is null : " + rqKickOut.mMatchId);
                                    }
                                }


                                player.isOutGame = true;
                                currentTable.remove(player);

                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn - không có quyền đuổi người khác!");
                        }
                        break;
                    }

                    case ZoneID.COTUONG: {
                        CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                        CoTuongPlayer leftPlayer = new CoTuongPlayer(rqKickOut.uid);
                        if (aSession.getUID() == currentTable.owner.id) {
                            if (rqKickOut.uid != currentTable.player.id) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Người chơi này đã thoát rồi!");
                            } else if (!currentTable.mIsEnd) {
                                resKickOut.setFailure(
                                        ResponseCode.FAILURE, "Bạn không thể đuổi người chơi khi bàn đang chơi!");
                            } else {
                                String note = currentTable.player.username + " bị chủ bàn đá ra ngoài";

                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        rqKickOut.uid,
                                        note, currentTable.player.username, 1);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);
                                ISession leftSession = aSession.getManager().findSession(leftPlayer.id);
                                currentRoom.left(leftSession);
                                leftSession.leftRoom(rqKickOut.mMatchId);
//                                currentTable.removeAllPlayer();
                                currentTable.player_list.remove(leftPlayer);
                                currentTable.resetBoard();
                                currentTable.isFull = false;
                                currentTable.isFullPlayer = false;
                                currentTable.isPlaying = false;
//                                resKickOut.setSuccess(ResponseCode.SUCCESS);

                                isFail = false;
                            }
                        } else {
                            resKickOut.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn - không có quyền đuổi người khác!");
                        }
                        break;
                    }
                    //TODO: Add more here
                    default:
                        break;
                }

            } else {
                resKickOut.setFailure(ResponseCode.FAILURE,
                        "Bạn đã thoát khỏi bàn!");
            }

        } catch (Throwable t) {
            resKickOut.setFailure(ResponseCode.FAILURE, "Bị lỗi "
                    + t.toString());
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resKickOut != null) && isFail) {
                aResPkg.addMessage(resKickOut);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
