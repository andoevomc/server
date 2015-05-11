package dreamgame.business;

import dreamgame.data.MessagesID;
//import dreamgame.oantuti.data.OTTPlayer;
//import dreamgame.oantuti.data.OantutiTable;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.ReadyRequest;
import dreamgame.protocol.messages.ReadyResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;
//import bacay.data.MessagesID;
//import bacay.protocol.messages.AllReadyResponse;
//import dreamgame.chan.data.ChanPlayer;
//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import phom.data.PhomPlayer;
import phom.data.PhomTable;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;

public class ReadyBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(ReadyBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "ReadyBusiness - handleMessage");
	}
        boolean isFail = false;
        mLog.debug("[READY]: Catch ");
        MessageFactory msgFactory = aSession.getMessageFactory();
        ReadyResponse resReady =
                (ReadyResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            /*
             * AllReadyResponse broadcastMsg = (AllReadyResponse)
             * msgFactory.getResponseMessage(MessagesID.MATCH_ALLREADY);
             */
            ReadyRequest rqMatchNew = (ReadyRequest) aReqMsg;
            mLog.debug("[READY]: ID - " + rqMatchNew.uid);
            Zone zone = aSession.findZone(aSession.getCurrentZone());
            mLog.debug("[READY]: ZONE - " + zone.getZoneName() + " : " + aSession.getCurrentZone() + " : " + aSession.getCurrentZone());

            Room currRoom = zone.findRoom(rqMatchNew.matchID);
            switch (aSession.getCurrentZone()) {
                case ZoneID.BACAY: {
                    BacayTable newTable = (BacayTable) currRoom.getAttactmentData();
                    BacayPlayer player = newTable.findPlayer(rqMatchNew.uid);
                    if (player != null) {
                        player.setReady(true);
                    }
                    // sent success
                    resReady.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                    mLog.debug("Ready Success:");
                    break;
                }
                case ZoneID.OTT: {
		    //deleted
                    break;
                }
                case ZoneID.GAME_CHAN: {
                    // sent success

                    break;
                }
                case ZoneID.POKER: {
                    // sent success
                    if (currRoom == null) {
                        mLog.error("room is null : ");
                        resReady.setFailure(ResponseCode.FAILURE, "Bàn chơi này đã bị hủy!");
                        break;
                    }

                    PokerTable newTable = (PokerTable) currRoom.getAttactmentData();
                    PokerPlayer player = newTable.findPlayer(rqMatchNew.uid);

                    if (player == null) {
                        ISession is = aSession.getManager().findSession(rqMatchNew.uid);
                        if (is != null) {
                            currRoom.left(is);
                            is.leftRoom(currRoom.getRoomId());
                        }
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }

                    if (player.notEnoughMoney()) {
                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chơi tiếp!");
                    } else {
                        player.isReady = true;
                        resReady.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        mLog.debug("Ready Success:");

                        ReadyResponse broadcastMsg = (ReadyResponse) msgFactory.getResponseMessage(MessagesID.MATCH_READY);
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        currRoom.broadcastMessage(broadcastMsg, aSession, true);
                    }
                    break;
                }
                case ZoneID.XITO: {
                    // sent success
                    if (currRoom == null) {
                        mLog.error("room is null : ");
                        resReady.setFailure(ResponseCode.FAILURE, "Bàn chơi này đã bị hủy!");
                        break;
                    }

                    XiToTable newTable = (XiToTable) currRoom.getAttactmentData();
                    PokerPlayer player = newTable.findPlayer(rqMatchNew.uid);

                    if (player == null) {
                        ISession is = aSession.getManager().findSession(rqMatchNew.uid);
                        if (is != null) {
                            currRoom.left(is);
                            is.leftRoom(currRoom.getRoomId());
                        }
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }

                    if (player.notEnoughMoney()) {
                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chơi tiếp!");
                    } else {
                        player.isReady = true;
                        resReady.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        mLog.debug("Ready Success:");

                        ReadyResponse broadcastMsg = (ReadyResponse) msgFactory.getResponseMessage(MessagesID.MATCH_READY);
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        currRoom.broadcastMessage(broadcastMsg, aSession, true);
                    }
                    break;
                }
                case ZoneID.PHOM: {
                    // sent success
                    PhomTable newTable = (PhomTable) currRoom.getAttactmentData();
                    PhomPlayer player = newTable.findPlayer(rqMatchNew.uid);

                    if (player == null) {
                        ISession is = aSession.getManager().findSession(rqMatchNew.uid);
                        if (is != null) {
                            currRoom.left(is);
                            is.leftRoom(currRoom.getRoomId());
                        }
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }

                    if (player.cash < newTable.firstCashBet * 3) {
                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chơi tiếp!");
                        break;
                    }

                    player.isReady = true;
                    resReady.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                    resReady.mPlayerPhom = newTable.getPlayings();
                    resReady.mWaitingPlayerPhom = newTable.getWaitings();
                    mLog.debug("Ready Success:");

                    ReadyResponse broadcastMsg = (ReadyResponse) msgFactory.getResponseMessage(MessagesID.MATCH_READY);
                    broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                    currRoom.broadcastMessage(broadcastMsg, aSession, false);

                    break;
                }
                case ZoneID.TIENLEN_MB:
                case ZoneID.TIENLEN_DEMLA:
                case ZoneID.TIENLEN: {
                    // sent success
                    if (currRoom == null) {
                        mLog.error("room is null : ");
                        resReady.setFailure(ResponseCode.FAILURE, "Phòng chơi đã bị hủy rồi!");
                        break;
                    }

                    TienLenTable newTable = (TienLenTable) currRoom.getAttactmentData();
                    TienLenPlayer player = newTable.findPlayer(rqMatchNew.uid);

                    if (player == null) {
                        ISession is = aSession.getManager().findSession(rqMatchNew.uid);
                        if (is != null) {
                            currRoom.left(is);
                            is.leftRoom(currRoom.getRoomId());
                        }
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }

                    if (player.cash < newTable.firstCashBet * 3) {

                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chơi tiếp!");

                    } else {
                        player.isReady = true;
                        resReady.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        mLog.debug("Ready Success:");

                        ReadyResponse broadcastMsg = (ReadyResponse) msgFactory.getResponseMessage(MessagesID.MATCH_READY);
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        currRoom.broadcastMessage(broadcastMsg, aSession, true);
                    }
                    break;
                }
                case ZoneID.BAUCUA: {
                    // sent success
                    if (currRoom == null) {
                        mLog.error("room is null : ");
                        resReady.setFailure(ResponseCode.FAILURE, "Phòng chơi đã bị hủy rồi!");
                        break;
                    }

                    BauCuaTable newTable = (BauCuaTable) currRoom.getAttactmentData();
                    BauCuaPlayer player = newTable.findPlayer(rqMatchNew.uid);

                    if (player == null) {
                        ISession is = aSession.getManager().findSession(rqMatchNew.uid);
                        if (is != null) {
                            currRoom.left(is);
                            is.leftRoom(currRoom.getRoomId());
                        }
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }

                    if (player.cash < newTable.firstCashBet) {

                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chơi tiếp!");

                    } else {
                        player.isReady = true;
                        resReady.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        mLog.debug("Ready Success:");

                        ReadyResponse broadcastMsg = (ReadyResponse) msgFactory.getResponseMessage(MessagesID.MATCH_READY);
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        currRoom.broadcastMessage(broadcastMsg, aSession, true);
                    }
                    break;
                }

                case ZoneID.COTUONG: {
                    CoTuongTable newTable = (CoTuongTable) currRoom.getAttactmentData();
                    if (newTable.player.id != aSession.getUID()) {
                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không phải là người chơi!");
                        isFail = true;
                        aSession.write(resReady);
                    } else if (newTable.player.cash < newTable.firstCashBet) {
                        resReady.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chơi tiếp!");
                        isFail = true;
                        aSession.write(resReady);
                    } else {
                        newTable.isPlayerReady = true;
                        ReadyResponse broadcastMsg = (ReadyResponse) msgFactory.getResponseMessage(MessagesID.MATCH_READY);
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS, rqMatchNew.uid);
                        currRoom.broadcastMessage(broadcastMsg, aSession, true);
                        isFail = true;
                    }
                    break;
                }
                case ZoneID.MAUBINH: {
                    // sent success
                    break;
                }
                //TODO: Add more here
                default:
                    break;
            }


        } catch (Throwable t) {
            resReady.setFailure(ResponseCode.FAILURE, "Bị lỗi " + t.toString());
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resReady != null && !isFail)) {
                aResPkg.addMessage(resReady);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
