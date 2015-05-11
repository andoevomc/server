package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.AvatarEntity;
import dreamgame.data.ChargeHistoryEntity;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetAvatarListRequest;
import dreamgame.protocol.messages.GetAvatarListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

import com.sun.midp.io.Base64;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import org.jboss.netty.channel.ChannelHandler;

public class GetAvatarListBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetAvatarListBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetAvatarListBusiness - handleMessage");
	}
        mLog.debug("[Get Avatar] : Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetAvatarListResponse resGetAvatarList =
                (GetAvatarListResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            GetAvatarListRequest rq = (GetAvatarListRequest) aReqMsg;
            System.out.println("rq.getChargeHistory : " + rq.getChargeHistory);



            if (aSession.getUserName().equalsIgnoreCase("admin") || aSession.getUserName().equalsIgnoreCase("admincp")
                    || aSession.getUserName().equalsIgnoreCase("admingmt")) {

                if (rq.updateConfig) {
                    DatabaseDriver.loadConfig();
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }
                if (rq.turnOffServer) {
                    DatabaseDriver.stopServer = true;
                    aSession.getManager().shutdown();
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }
                if (rq.notification) {
                    DatabaseDriver.loadConfig();
//                    resGetAvatarList.setNotice(DatabaseDriver.noticeText);
                    aSession.getManager().sendAllNotification();
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }

                if (rq.getGameInfo) {
                    resGetAvatarList.setGameInfo(ResponseCode.SUCCESS);
                    resGetAvatarList.totalMobileUser = aSession.getManager().numMobileUser();
                    resGetAvatarList.totalFlashUser = aSession.getManager().numUser() - resGetAvatarList.totalMobileUser;

                    Zone tienlenZone = aSession.findZone(ZoneID.TIENLEN);
                    Zone bacayZone = aSession.findZone(ZoneID.BACAY);
                    Zone phomZone = aSession.findZone(ZoneID.PHOM);

                    resGetAvatarList.totalRoom = tienlenZone.getNumRoom() + bacayZone.getNumRoom() + phomZone.getNumRoom();
                    resGetAvatarList.totalPhom = phomZone.getNumRoom();
                    resGetAvatarList.totalTienLen = tienlenZone.getNumRoom();

                }
            }

            if (rq.getImage) {
                resGetAvatarList.imageData = "";

                String filename = "6.png";
                BufferedImage img = ImageIO.read(new File(filename));
                int p = filename.lastIndexOf('.');
                String ext = filename.substring(p + 1, filename.length() - p + 1);
                System.out.println("ext : " + ext);

                ByteArrayOutputStream bas =
                        new ByteArrayOutputStream();

                ImageIO.write(img, ext, bas);
                byte[] data = bas.toByteArray();
                resGetAvatarList.getImage = true;
                resGetAvatarList.mCode = 1;
                System.out.println("data length : " + data.length);

                for (int i = 0; i < 10; i++) {
                    System.out.print(data[i] + " : ");
                }
                System.out.println();

                //resGetAvatarList.imageData=Base64.encodeHexString(data);  
                resGetAvatarList.imageData = Base64.encode(data, 0, data.length);

                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }

            if (rq.getChargeHistory) {
                Vector<ChargeHistoryEntity> avaList = DatabaseDriver.getChargeHistory(aSession.getUID(), rq.start, rq.length);
                resGetAvatarList.setChargeSuccess(ResponseCode.SUCCESS, avaList);
                resGetAvatarList.newMoney = DatabaseDriver.getUserMoney(aSession.getUID());

                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }


            Vector<AvatarEntity> avaList = DatabaseDriver.getAvatarList();
            resGetAvatarList.setSuccess(ResponseCode.SUCCESS, avaList);
        } catch (Exception e) {
            e.printStackTrace();

            mLog.debug("Get avatar list error:" + e.getCause());
            resGetAvatarList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu");
        } finally {
            if ((resGetAvatarList != null)) {
                aResPkg.addMessage(resGetAvatarList);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
