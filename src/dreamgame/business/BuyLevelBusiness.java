package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.BuyLevelRequest;
import dreamgame.protocol.messages.BuyLevelResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class BuyLevelBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BuyLevelBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "BuyLevelBusiness - handleMessage");
	}
        mLog.debug("[BUY-LEVEL]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        BuyLevelResponse resBuyLevel =
                (BuyLevelResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            BuyLevelRequest rqBuyLevel = (BuyLevelRequest) aReqMsg;
            long uid = rqBuyLevel.uid;
            int currLevel = DatabaseDriver.getUserLevel(uid);
            if (currLevel == 12) {
                resBuyLevel.setFailure(ResponseCode.FAILURE, "Bạn đã ở trên đỉnh, không thể lên hơn được nữa");
            } else {
                long money = DatabaseDriver.getMoneyForUpdateLevel(currLevel + 1);
                long cashU = DatabaseDriver.getUserMoney(uid);
                if (cashU >= money) {
                    DatabaseDriver.updateLevel(uid,money,cashU-money);                    
                    resBuyLevel.setSuccess(ResponseCode.SUCCESS, cashU - money, currLevel + 2, money);
                } else {
                    resBuyLevel.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để nâng cấp.");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            resBuyLevel.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra khi bạn nâng cấp");
        } finally {
            if ((resBuyLevel != null)) {
                aResPkg.addMessage(resBuyLevel);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
