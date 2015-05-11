package dreamgame.business;

import dreamgame.business.channeling.ChannelingDoBusiness;
import dreamgame.config.DebugConfig;
import dreamgame.data.CPEntity;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.BuyAvatarRequest;
import dreamgame.protocol.messages.BuyAvatarResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import gov.nist.siplite.address.UserInfo;
import org.json.JSONArray;
import org.json.JSONObject;

public class BuyAvatarBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BuyAvatarBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "BuyAvatarBusiness - handleMessage");
	}
        mLog.debug("[BUY_AVATAR]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        BuyAvatarResponse resBuyAvatar =
                (BuyAvatarResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            BuyAvatarRequest rqBuyAvatar = (BuyAvatarRequest) aReqMsg;
                       
            long uid = rqBuyAvatar.uid;
            int avatarID = rqBuyAvatar.avatarID;
            System.out.println("rqBuyAvatar.chargeAppleMoney : "+rqBuyAvatar.chargeAppleMoney);
            if (uid<=0) uid=aSession.getUID();

	    // đổi điểm ?
            if (rqBuyAvatar.change_award)
            {
		mLog.debug("BuyAvatarBusiness -  change_award");
                UserEntity u=DatabaseDriver.getUserInfo(uid);
                if (u.award >= DatabaseDriver.eventChangeAward) {
		    mLog.debug("do change");
                    DatabaseDriver.changeAwardMoney(uid);
                    resBuyAvatar.new_cash=DatabaseDriver.getUserMoney(uid);
                    resBuyAvatar.mCode=1;
                    resBuyAvatar.change_award=true;
                }
		else {
		    mLog.debug("Khong du diem");
                    resBuyAvatar.setFailure(ResponseCode.FAILURE, "Bạn không đủ điểm. Cần có "+DatabaseDriver.eventChangeAward);
                }
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
	    
	    // nạp tiền qua itunes (không dùng)
            if (rqBuyAvatar.chargeAppleMoney)
            {
		mLog.debug("charge Apple Money");
                resBuyAvatar.setFailure(ResponseCode.FAILURE, "Hệ thống nạp tiền qua iphone đang bảo trì!");
                
                if (true){
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return 1;
		}
                
                if (aSession.getClientType().equalsIgnoreCase("iphone"))
                {
                    JSONArray ja=DatabaseDriver.iphoneChargeList;
                    for (int i=0;i<ja.length();i++)
                    {
                        JSONObject jo=ja.getJSONObject(i);
                        int id=jo.getInt("id");
                        int money=jo.getInt("gameMoney");
                        if (id==rqBuyAvatar.cardId)
                        {
                            resBuyAvatar.new_cash=DatabaseDriver.updateUserMoney(money, true, uid, "Nap tien tu apple "+rqBuyAvatar.cardId);
                            resBuyAvatar.chargeAppleMoney=true;
                            resBuyAvatar.mCode=1;
                        }
                    }
                    
                }
            }
	    
	    // nạp thẻ thông thường
            else if (rqBuyAvatar.chargeCard)
            {
		mLog.debug("Charge cards");
		String cp = aSession.getCP();
		cp = cp == null ? "0" : cp;
		CPEntity cpe = DatabaseDriver.getCPInfo(cp);
		
		// cp cung cap api cho nap the
		if (cpe.isOpenIDOpen) {
		    mLog.debug("isOpenIDOpen = true, this cp has it's own API for charge Cards");
		    int cardId = rqBuyAvatar.cardId;
		    String cardName = DatabaseDriver.getCardName(cardId);
		    String result = handleChargeCard_OpenIDOpen(aSession, aSession.getUserEntity(), rqBuyAvatar.code, rqBuyAvatar.serial, cardName);
		    mLog.debug("result handleChargeCard_OpenIDOpen: " + result);
		    if (result.startsWith("ok")) {
			String[] arr = result.split(";");
			try {
			    String message = arr[1];
			    resBuyAvatar.setSuccess(ResponseCode.SUCCESS, message, DatabaseDriver.getUserMoney(uid));
			    return 1;
			} catch (Exception e) {
			    resBuyAvatar.setFailure(ResponseCode.FAILURE, "Có lỗi DB xảy ra !!!");
			    aResPkg.addMessage(resBuyAvatar);
			    return PROCESS_OK;
			}
		    }
		    else {
    //			resLogin.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, xin vui lòng thử lại sau ít phút !!!");
			resBuyAvatar.setFailure(ResponseCode.FAILURE, result);
			aResPkg.addMessage(resBuyAvatar);
			return PROCESS_OK;
		    }
		}
		// nap the qua thegioibai
		else {
		    String res=DatabaseDriver.requestCharge(uid, rqBuyAvatar.code, rqBuyAvatar.serial, rqBuyAvatar.cardId, aSession.getUserEntity().cp);

		    if (res==null || res.length()==0) {
			resBuyAvatar.setFailure(ResponseCode.FAILURE, "Hệ thống nạp thẻ hiện nay đang bảo trì.");
		    } else
			resBuyAvatar.setSuccess(ResponseCode.SUCCESS, res, DatabaseDriver.getUserMoney(uid));

		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return 1;
		}
            }
	    // mua avatar
            else if ((avatarID < 1) && (avatarID > 15)) {
                resBuyAvatar.setFailure(ResponseCode.FAILURE, "Bạn đưa lên Avatar sai rồi.");
            } 
	    else {
                long money = DatabaseDriver.getMoneyForUpdateAvatar(avatarID);
                long cashU = DatabaseDriver.getUserMoney(uid);
                // Set avatar
                if (cashU >= money) {
                    DatabaseDriver.updateAvatar(uid, avatarID,money,cashU-money);
                    resBuyAvatar.setSuccess(ResponseCode.SUCCESS, cashU - money, avatarID);
                } else {
                    resBuyAvatar.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền mua avatar này.");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            resBuyAvatar.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra với avatar bạn muốn mua.");
        } finally {
            if ((resBuyAvatar != null)) {
                aResPkg.addMessage(resBuyAvatar);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
    
    private String handleChargeCard_OpenIDOpen(ISession aSession, UserEntity user, String cardNumber, String cardSerial, String telco) {
	String cp = aSession.getCP();
	String packageName = this.getClass().getPackage().getName() + ".channeling";
	String className = cp.substring(0, 1).toUpperCase() + cp.substring(1).toLowerCase();
	String fullClassName = packageName + "." + className;
	String result;
	try {
	    ChannelingDoBusiness business = (ChannelingDoBusiness) Class.forName(fullClassName).newInstance();
	    result = business.doCardCharge(aSession, user, cardNumber, cardSerial, telco);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    mLog.debug(ex.getMessage());
	    return "Đã có lỗi xảy ra.";
	}
	return result;
    }
}
