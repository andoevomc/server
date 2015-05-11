/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.UpdateUserInfoRequest;
import dreamgame.protocol.messages.UpdateUserInfoResponse;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.util.codec.md5.MD5;
import org.slf4j.Logger;

/**
 *
 * @author Dinhpv
 */
public class UpdateUserInfoBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(UpdateUserInfoBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "UpdateUserInfoBusiness - handleMessage");
	}
        int rtn = PROCESS_FAILURE;
        mLog.debug("[UPDATE USER] : Catch");

        MessageFactory msgFactory = aSession.getMessageFactory();

        UpdateUserInfoResponse resUpdate =
                (UpdateUserInfoResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            UpdateUserInfoRequest rqRegister = (UpdateUserInfoRequest) aReqMsg;
            String email = rqRegister.email;
            //int age = rqRegister.age;
            //int sex = rqRegister.sex;
            String number = rqRegister.number;
            String newPassword = rqRegister.newPassword;
            String oldPassword = rqRegister.oldPassword;
            String username = aSession.getUserName();
            
            String md = MD5.md5Hex(oldPassword);

            if (DatabaseDriver.userIsExist(username)) {
                UserEntity user = DatabaseDriver.getUserInfo(username);
                if(user.mPassword.compareTo(oldPassword) == 0 || user.mPassword.compareTo(md) == 0){
                    DatabaseDriver.updateUser(aSession.getUID(), newPassword, email, number);
                    resUpdate.setSuccess(ResponseCode.SUCCESS);
                } else {
                    resUpdate.setFailure(ResponseCode.FAILURE, "Mật khẩu cũ không đúng");
                }
            } else {
                resUpdate.setFailure(ResponseCode.FAILURE, "Không tìm thấy tên bạn trong cơ sở dữ liệu!");
            }
            rtn = PROCESS_OK;
        } catch (Throwable t) {
            resUpdate.setFailure(ResponseCode.FAILURE, "Dữ liệu bạn nhập không chính xác!");
            rtn = PROCESS_OK;
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resUpdate != null) && (rtn == PROCESS_OK)) {
                aResPkg.addMessage(resUpdate);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }
}
