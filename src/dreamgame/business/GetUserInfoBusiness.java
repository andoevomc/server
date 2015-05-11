package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetUserInfoRequest;
import dreamgame.protocol.messages.GetUserInfoResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class GetUserInfoBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetUserInfoBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetUserInfoBusiness - handleMessage");
	}
        mLog.debug("[GET USER INFOS]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetUserInfoResponse resGetUserInfo = (GetUserInfoResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            GetUserInfoRequest rqGetUserInfo = (GetUserInfoRequest) aReqMsg;
            long uid = rqGetUserInfo.mUid;
            long source_uid = aSession.getUID();
            mLog.debug("[GET USER INFOS]:" + uid);

            UserEntity user = DatabaseDriver.getUserInfo(uid);            
            if (user != null) 
            {
                ISession buddy = (ISession) aSession.getManager().findSession(user.mUid);
                if (buddy!=null) buddy.setUserEntity(user);

                boolean isFriend = false;
                if (uid == source_uid) {
                    isFriend = true;
                } else {
                    isFriend = DatabaseDriver.isFriend(source_uid, uid);
                }
                resGetUserInfo.setSuccess(ResponseCode.SUCCESS, user.mUid, user.mUsername,
                        user.mAge, user.mIsMale, user.money, user.playsNumber, user.avatarID, isFriend, user.level);
                resGetUserInfo.award=user.award;
                
            } else {// non-existed user
                resGetUserInfo.setFailure(ResponseCode.FAILURE, "Tài khoản này không tồn tại!");
            }

        } catch (Throwable t) {
            resGetUserInfo.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resGetUserInfo != null) ) {
                aResPkg.addMessage(resGetUserInfo);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
