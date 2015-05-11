package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetFrientListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.SessionManager;

import java.util.Vector;
import org.slf4j.Logger;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;

public class GetFrientListBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetFrientListBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetFrientListBusiness - handleMessage");
	}
        mLog.debug("[GET FRIENDLIST]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetFrientListResponse resGetFriendList = (GetFrientListResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            long uid = aSession.getUID();
            mLog.debug("[GET FRIENDLIST]: for uid = " + uid);
            Vector<UserEntity> frientlist = DatabaseDriver.getFastFrientList(uid);
            
	    // chi hien danh sach nhung ban dang online
            SessionManager manager = aSession.getManager();
	    Vector<UserEntity> res = manager.findUsersOnline(frientlist);
//            Vector<UserEntity> res = new Vector<UserEntity>();
//            for (UserEntity user : frientlist) {
//                ISession buddy = (ISession) manager.findSession(user.mUid);
//                if (buddy != null) {                    
//                    res.add(buddy.getUserEntity());
//                }
//            }
            resGetFriendList.setSuccess(ResponseCode.SUCCESS, res);
        } catch (Throwable t) {
            resGetFriendList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resGetFriendList != null)) {
                aResPkg.addMessage(resGetFriendList);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
