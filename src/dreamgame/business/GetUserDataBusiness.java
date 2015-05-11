/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.data.ChargeHistoryEntity;
import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.protocol.messages.GetUserDataRequest;
import dreamgame.protocol.messages.GetUserDataResponse;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author thohd
 */
public class GetUserDataBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetUserDataBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetUserDataBusiness - handleMessage");
	}

        MessageFactory msgFactory = aSession.getMessageFactory();
        GetUserDataResponse res = (GetUserDataResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        GetUserDataRequest rq = (GetUserDataRequest) aReqMsg;
        try {
            
            if (rq.chargeAppleMoney)
            {
                /*if (!DatabaseDriver.allowIphoneCharge){
                    res.setFailure(ResponseCode.FAILURE, "Hệ thống nạp tiền qua iphone đang bảo trì!");                                
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }   */
                
//                if (aSession.getClientType().equalsIgnoreCase("iphone"))
//                {
//                    JSONArray ja=DatabaseDriver.iphoneChargeList;
//                    for (int i=0;i<ja.length();i++)
//                    {
//                        JSONObject jo=ja.getJSONObject(i);
//                        int id=jo.getInt("id");
//                        int money=jo.getInt("gameMoney");
//                        if (id==rq.cardId)
//                        {
//                            res.new_cash=DatabaseDriver.updateUserMoney(money, true, rq.uid, "Nap tien tu apple "+rq.cardId);
//                            res.chargeAppleMoney=true;
//                            res.mCode=1;
//                        }
//                    }
//                }
            }
	    else if (rq.chargeCard)
            {
                long uid=rq.uid;

                String resMsg=DatabaseDriver.requestCharge(uid, rq.code, rq.serial, rq.cardId,aSession.getUserEntity().cp );
                
                if (resMsg==null||resMsg.length()==0)
                {
                    res.setFailure(ResponseCode.FAILURE, "Hệ thống nạp thẻ hiện nay đang bảo trì.");
                }else{
                    res.setSuccess(ResponseCode.SUCCESS, resMsg,DatabaseDriver.getUserMoney(uid));
                }
                
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
            
            if (rq.getChargeHistory) {
                Vector<ChargeHistoryEntity> avaList = DatabaseDriver.getChargeHistory(aSession.getUID(), rq.start, rq.length);
                res.setChargeSuccess(ResponseCode.SUCCESS, avaList);
                res.newMoney = DatabaseDriver.getUserMoney(aSession.getUID());

                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
            /*
            if (rq.getUserAvatar > 0) {
                AvatarEntity av = DatabaseDriver.getAvatar(rq.getUserAvatar);
                boolean uped=false;
                if (av!=null){
                    System.out.println("Path : "+av.path +" : "+aSession.getMobile());
                    res.mCode = 1;

                    if (av.path.length()>0)
                    if ((new File(av.path)).exists()) {                    
                        uped=true;
                        aSession.writeImage(GetAvatarListBusiness.getImageDataBytes(av.path, true), MessagesID.GET_USER_AVATAR, rq.getUserAvatar, 
                                av.description);
                    }
                }
                if (!uped && aSession.getMobile() ){
                    String file=DatabaseDriver.avatar_path+rq.getUserAvatar+".png";
                    System.out.println("new path : "+file);
                    if ((new File(file)).exists()){
                        aSession.writeImage(GetAvatarListBusiness.getImageDataBytes(file, true), MessagesID.GET_USER_AVATAR, rq.getUserAvatar, 
                                "");
                    }
                }
            }
            
            if (rq.getImage > 0) {
                AvatarEntity av = DatabaseDriver.getAvatar(rq.getImage);
                 boolean uped=false;
                if (av!=null){
                    System.out.println("Path : "+av.path);
                    res.mCode = 1;
                    if ((new File(av.path)).exists()) {                    
                        uped=true;
                        aSession.writeImage(GetAvatarListBusiness.getImageDataBytes(av.path, true), aReqMsg.getID(), rq.uid, 
                                av.description);
                    }
                }
                
                if (!uped && aSession.getMobile() ){
                    String file=DatabaseDriver.avatar_path+rq.getImage+".png";
                    System.out.println("new path : "+file);
                    if ((new File(file)).exists()){
                        aSession.writeImage(GetAvatarListBusiness.getImageDataBytes(file, true), MessagesID.GET_USER_AVATAR, rq.getImage, 
                                "");
                    }
                }
                
            }
            */
        } catch (Throwable t) {
            res.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, bạn vui lòng thử lại!");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((res != null)) {
                aResPkg.addMessage(res);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}