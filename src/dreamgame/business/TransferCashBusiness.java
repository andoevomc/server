package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.config.GameRuleConfig;
import dreamgame.data.LogType;
import org.slf4j.Logger;

//import bacay.data.LogType;
import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.TransferCashRequest;
import dreamgame.protocol.messages.TransferCashResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Date;

public class TransferCashBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(TransferCashBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "TransferCashBusiness - handleMessage");
	}
        // process's status
        int rtn = PROCESS_FAILURE;
        mLog.debug("[TRANSFER_CASH]: Catch");
        // 
        MessageFactory msgFactory = aSession.getMessageFactory();
        TransferCashResponse resTransferCash =
                (TransferCashResponse) msgFactory.getResponseMessage(aReqMsg.getID());

        try {
            TransferCashRequest rqBuyAvatar = (TransferCashRequest) aReqMsg;

            long s_uid = rqBuyAvatar.source_uid;
            long d_uid = rqBuyAvatar.desc_uid;
            long money = rqBuyAvatar.money;
	    
	    if (s_uid != aSession.getUID()) {
		resTransferCash.setFailure(ResponseCode.FAILURE, "Mailformed transfer cash request ??? Mr.Hacker.");
		aResPkg.addMessage(resTransferCash);
		return PROCESS_OK;
	    }
	    
            long currMoney = DatabaseDriver.getUserMoney(s_uid);
            boolean active = DatabaseDriver.getUserActive(d_uid);
            long status = DatabaseDriver.getUserGameStatus(aSession.getUID());
            //

            if (!active) {
                resTransferCash.setFailure(ResponseCode.FAILURE, "Người được chuyển tiền chưa kích hoạt tài khoản.");
            } else if (rqBuyAvatar.source_uid != aSession.getUID()) {
                resTransferCash.setFailure(ResponseCode.FAILURE, "ID người chuyển tiền không đúng!");
            } else if (status == 1) {
                resTransferCash.setFailure(ResponseCode.FAILURE, "Bạn không được chuyển tiền khi còn trong bàn chơi!");
            } else if (currMoney >= money) {
		if (currMoney < 100000) {                
                    resTransferCash.setFailure(ResponseCode.FAILURE, "Bạn cần có ít nhất 100.000 " + GameRuleConfig.MONEY_SYMBOL + " mới có thể chuyển tiền.");
                } else if (money <= 0) {
                    resTransferCash.setFailure(ResponseCode.FAILURE, "Tiền chuyển phải lớn hơn 0.");
                } else if (currMoney - money < 30000) {                 
		    resTransferCash.setFailure(ResponseCode.FAILURE, "Bạn chỉ được chuyển tối đa " + (currMoney - 30000) + " " + GameRuleConfig.MONEY_SYMBOL);
                } else {
		    // check if this destination uid have too many transfer in recently
		    boolean isTooManyTransferToThisUser = DatabaseDriver.isUserGotTooManyTransferInCashAndTakeAction(d_uid, new Date());		    
		    if ( ! isTooManyTransferToThisUser) {
			DatabaseDriver.transferMoney(s_uid, d_uid, money);
			resTransferCash.setSuccess(ResponseCode.SUCCESS, s_uid, d_uid, money, true);
		    }
                }

                /*
                 * DatabaseDriver.logUserVASC(s_uid, "Ban da chuyen so tien " +
                 * money + " cho " + d_uid, money, LogType.TRANSFER);
                 *
                 * DatabaseDriver.logUserVASC(d_uid, "Ban da duoc nhan so tien "
                 * + money + " tu " + s_uid, money, LogType.TRANSFERED);
                 */

            } else {
                resTransferCash.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để chuyển.");
            }

            rtn = PROCESS_OK;
        } catch (Throwable t) {
            resTransferCash.setFailure(ResponseCode.FAILURE, "Bị lỗi: ");
            mLog.debug("Bị lỗi: " + t.getMessage());
            rtn = PROCESS_OK;
        } finally {
            if ((resTransferCash != null) && (rtn == PROCESS_OK)) {
                aResPkg.addMessage(resTransferCash);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }
}
