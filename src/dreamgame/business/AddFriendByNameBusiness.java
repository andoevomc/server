package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AddFriendByNameRequest;
import dreamgame.protocol.messages.AddFriendByNameResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class AddFriendByNameBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AddFriendByNameBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "AddFriendByNameBusiness - handleMessage");
	}
        mLog.debug("[Add Friend] : Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        AddFriendByNameResponse resAddFriend = (AddFriendByNameResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            AddFriendByNameRequest rqAddFriend = (AddFriendByNameRequest) aReqMsg;
            long currID = aSession.getUID();
            UserEntity user = DatabaseDriver.getUserInfo(rqAddFriend.friendName);
            if (user != null) {
                if (!DatabaseDriver.isFriend(currID, user.mUid)) {
                    DatabaseDriver.addFriend(currID, user.mUid);
//                    DatabaseDriver.addFriend(user.mUid, currID);
                    resAddFriend.setSuccess(ResponseCode.SUCCESS, user);
                } else {
                    resAddFriend.setFailure(ResponseCode.FAILURE,
                            "Hai bạn đã là bạn của nhau rồi");
                }
            } else {
                resAddFriend.setFailure(ResponseCode.FAILURE,
                        "Không tìm được tên bạn ấy");
            }

        } catch (Exception e) {
            resAddFriend.setFailure(ResponseCode.FAILURE,
                    "Không tìm được tên bạn ấy");
        } finally {
            if ((resAddFriend != null)) {
                aResPkg.addMessage(resAddFriend);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
