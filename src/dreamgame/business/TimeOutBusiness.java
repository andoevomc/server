/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.protocol.messages.TimeOutResponse;
import dreamgame.protocol.messages.TimeOutRequest;

import java.util.Vector;
import org.slf4j.Logger;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
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

/**
 *
 * @author Thomc
 */
public class TimeOutBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(TimeOutBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "TimeOutBusiness - handleMessage");
	}
        boolean isSuccess = false;
        MessageFactory msgFactory = aSession.getMessageFactory();
        TimeOutResponse resTimeOut = (TimeOutResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[ SUGGEST ]: Catch");
            TimeOutRequest rqTimeOut = (TimeOutRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqTimeOut.mMatchId);
            UserEntity newUser = DatabaseDriver.getUserInfo(rqTimeOut.player_friend_id);
            if (newUser != null) {
                if (currentRoom != null) {
                    switch (aSession.getCurrentZone()) {
                        case ZoneID.COTUONG: {
//                            CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                            long player_friend_id = rqTimeOut.player_friend_id;
//                            ISession playerFriendSession = aSession.getManager().findSession(player_friend_id);
                            resTimeOut.setSuccess(ResponseCode.SUCCESS, player_friend_id, newUser.mUsername);
//                            playerFriendSession.write(resTimeOut);
                            currentRoom.broadcastMessage(resTimeOut, aSession, false);
                            isSuccess = true;
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                    }
                } else {
                    resTimeOut.setFailure(ResponseCode.FAILURE,
                            "Bạn đã thoát khỏi room");
                }

            } else {
                resTimeOut.setFailure(ResponseCode.FAILURE, "Không tìm thấy bạn chơi nữa!");
            }
        } catch (Throwable t) {
            resTimeOut.setFailure(ResponseCode.FAILURE, "bị lỗi!");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if (resTimeOut != null && !isSuccess) {
                aResPkg.addMessage(resTimeOut);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
