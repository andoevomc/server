package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetFreeFriendListRequest;
import dreamgame.protocol.messages.GetFreeFriendListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.fake.ZoneConfigManager;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.SessionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class GetFreeFriendListBusiness extends AbstractBusiness {

    private static ConcurrentHashMap<Integer, ArrayList<UserEntity>> fakeUserMap = new ConcurrentHashMap<Integer, ArrayList<UserEntity>>();
    private static ConcurrentHashMap<Integer, ArrayList<String>> fakeUserMapNameList = new ConcurrentHashMap<Integer, ArrayList<String>>();
    private static ConcurrentHashMap<Integer, Date> fakeUserMapLastInit = new ConcurrentHashMap<Integer, Date>();
    
    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetFreeFriendListBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetFreeFriendListBusiness - handleMessage");
	}
        mLog.debug("[GET FREE FRIENDLIST]: Catch");
	
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetFreeFriendListResponse resGetFreeFriendList = (GetFreeFriendListResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            
            long uid = aSession.getUID();
            GetFreeFriendListRequest rqGet = (GetFreeFriendListRequest) msgFactory.getRequestMessage(aReqMsg.getID());
            int level = rqGet.level;
            mLog.debug("[GET FREE FRIENDLIST]: for " + uid);
            SessionManager manager = aSession.getManager();
	    
	    // find normal user
            Vector<UserEntity> res = new Vector<UserEntity>();
            res = manager.dumpFreeFriend(0, 20, aSession.getCurrentMoneyMatch(), aSession.getCurrentZone());
	    
	    // add fake user
	    Integer zoneID = new Integer(aSession.getCurrentZone());
	    ArrayList<UserEntity> fakeList = getFakeUserForInvite(zoneID);
	    res.addAll(fakeList);
	    
//	    res = manager.dumpFreeFriend(0, 20, level, aSession.getCurrentZone());
            resGetFreeFriendList.setSuccess(ResponseCode.SUCCESS, res);
            
        } 
	catch (Throwable t) {
            resGetFreeFriendList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } 
	finally {
            if ((resGetFreeFriendList != null)) {
                aResPkg.addMessage(resGetFreeFriendList);
            }
        }
	
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
    
    // fake user for invite
    private ArrayList<UserEntity> getFakeUserForInvite(Integer zoneID) {
	ArrayList<UserEntity> fakeList = fakeUserMap.get(zoneID);
	ArrayList<String> fakeNameList = fakeUserMapNameList.get(zoneID);
	Date lastInitTime = fakeUserMapLastInit.get(zoneID);
	Date now = new Date();
	
	// neu tu bay gio den lan refresh truoc da qua 15s thi init lai danh sach cho
	boolean isNeedToInit = true;
	if (lastInitTime != null) {
	    long amountOfSecondsPeriod = (now.getTime() - lastInitTime.getTime()) / 1000;
	    if (amountOfSecondsPeriod < 15)
		isNeedToInit = false;
	}
	
	// init nao
	if (isNeedToInit) {
	    // decide how many based on game
	    int noOfFakeUserMax = 0, noOfFakeUserMin;
	    switch (zoneID.intValue()) {
		case ZoneID.PHOM:
		case ZoneID.TIENLEN:
		case ZoneID.MAUBINH:
		    noOfFakeUserMin = 3;
		    noOfFakeUserMax = 7;
		    break;

		case ZoneID.XITO:
		case ZoneID.POKER:
		case ZoneID.TIENLEN_MB:
		    noOfFakeUserMin = 2;
		    noOfFakeUserMax = 5;
		    break;

		default:
		    noOfFakeUserMin = 1;
		    noOfFakeUserMax = 3;
		    break;
	    }
	    int noOfFakeUser = ZoneConfigManager.generateRandomNumber(noOfFakeUserMin, noOfFakeUserMax);
	    noOfFakeUser = ZoneConfigManager.noOfFakeUserOrTablesBasedOnCurrentTime(noOfFakeUser);
	    fakeNameList = ZoneConfigManager.generateAvailableOwnerNameList(noOfFakeUser, fakeNameList);
	    fakeList = DatabaseDriver.getFakeUserEntityForInvite(fakeNameList);
	    
	    fakeUserMap.put(zoneID, fakeList);
	    fakeUserMapNameList.put(zoneID, fakeNameList);
	    fakeUserMapLastInit.put(zoneID, new Date());
	}
	
	return fakeList;
    }
}
