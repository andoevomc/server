package dreamgame.business;

import dreamgame.business.channeling.ChannelingDoBusiness;
import dreamgame.config.DebugConfig;
import dreamgame.data.CPEntity;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.LoginResponse;
import dreamgame.protocol.messages.RegisterRequest;
import dreamgame.protocol.messages.RegisterResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.util.IPHelper;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(RegisterBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "RegisterBusiness - handleMessage");
	}
	
        int rtn = PROCESS_FAILURE;
        mLog.debug("[REGISTER] : Catch");

        MessageFactory msgFactory = aSession.getMessageFactory();

        RegisterResponse resRegister =
                (RegisterResponse) msgFactory.getResponseMessage(aReqMsg.getID());        
	try {
            RegisterRequest rqRegister = (RegisterRequest) aReqMsg;
	
	    
	  // check IP
	    String IP = IPHelper.parseIPFromString(aSession.getIP());
	    mLog.debug("Register: IP = " + IP + ", full = " + aSession.getIP());
	    // IP do bi banned?
	    boolean isBanned = DatabaseDriver.isIPBannedForRegistering(IP, new Date());
	    if (isBanned) {
		mLog.debug("this IP is banned... ");
		resRegister.setFailure(ResponseCode.FAILURE, "Bạn đã đăng ký quá nhiều account. Hệ thống không tải nổi !!!");
		aResPkg.addMessage(resRegister);
		return rtn;
	    }
	    
	    // check if IP do dang ky qua nhieu acc recently?
	    boolean isTooMuchAccount = DatabaseDriver.isIPReggedTooMuchAccountsAndTakeActionIfSo(IP, new Date());
	    if (isTooMuchAccount) {
		mLog.debug("this IP Registered too many accounts... Ban them all.");
		resRegister.setFailure(ResponseCode.FAILURE, "Bạn đã đăng ký quá nhiều account. Hệ thống không tải nổi !!!. Please fair play.");
		aResPkg.addMessage(resRegister);
		return rtn;
	    }
	 
	    
	 // OK -> continue registtering   
            String username = rqRegister.mUsername;
            username = username.trim().toLowerCase();
	    mLog.debug("username = " + username);
            if (! isUsernameOK(username)) {
		mLog.debug("wrong username, exit.");
                resRegister.setFailure(ResponseCode.FAILURE, "Username phải từ 3 ký tự trở lên và chỉ được phép chứa các ký tự a-z, 0-9 và dấu gạch dưới _");
		aResPkg.addMessage(resRegister);
		return rtn;
            }
            String password = rqRegister.mPassword;
            String phone = rqRegister.phone;
            String mail = rqRegister.mail;
            int age = rqRegister.mAge;
            int sex = 0;
            if (rqRegister.isMale) {
                sex = 1;
            }
	    String cp = rqRegister.cp;
	    CPEntity cpe = DatabaseDriver.getCPInfo(cp);
	    if (cpe == null) {
		mLog.debug("not exist cp = " + cp);
		resRegister.setFailure(ResponseCode.FAILURE, "Phiên bản bạn đang dùng là của một đối tác quá cũ và không thể đăng ký. Xin vui lòng đến http://thegioibai.com để download phiên bản mới nhất.");
		aResPkg.addMessage(resRegister);
		return rtn;
	    }
	    if (cpe.isOpenID) {
		mLog.debug("openID CP, cannot register, cp = " + username);
		resRegister.setFailure(ResponseCode.FAILURE, "Phiên bản này không cho phép đăng ký. Xin vui lòng trở về trang chủ để đăng ký mới.");
		aResPkg.addMessage(resRegister);
		return rtn;
	    }
	    	    
	    // check massive same username: acc, acc1, acc2, ...
//            String subUsername = username.substring(0, username.length() - 1);
//            System.out.println("subUsername: " + subUsername);
//            boolean userIsExist = false;
//            for (int i = 0; i <= 9; i++) {
//                if (DatabaseDriver.userIsExist(subUsername + i)) {
//                    userIsExist = true;
//                    break;
//                }
//            }
//            if (!userIsExist && !DatabaseDriver.userIsExist(username) && !DatabaseDriver.userIsExist(subUsername)) {
	    
	    long uid = 0;
	    // this cp provide their own API for register
	    if (cpe.isOpenIDOpen) {
		mLog.debug("isOpenIDOpen = true, this cp has it's own API for register");
		aSession.setCP(cp);
		String result = handleRegister_OpenIDOpen(aSession, cp, username, password);
		mLog.debug("result handleRegister_OpenIDOpen: " + result);
		if (result.startsWith("ok")) {
		    String[] arr = result.split(";");
		    try {
			uid = Long.parseLong(arr[1]);
		    } catch (Exception e) {
			resRegister.setFailure(ResponseCode.FAILURE, "Có lỗi DB xảy ra !!!");
			aResPkg.addMessage(resRegister);
			return PROCESS_OK;
		    }
		}
		else {
//			resLogin.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, xin vui lòng thử lại sau ít phút !!!");
		    resRegister.setFailure(ResponseCode.FAILURE, result);
		    aResPkg.addMessage(resRegister);
		    return PROCESS_OK;
		}
	    }
	    // normal register at thegioibai
	    else {
		if ( !DatabaseDriver.userIsExist(username) ) {
		    mLog.debug("registering at thegioibai");
		    uid = DatabaseDriver.registerUser(username, password, age, sex, mail, phone, rqRegister.mobileVersion, cp);
		    DatabaseDriver.updateCP(uid, rqRegister.cp);		    
		} else {
		    mLog.debug("existed -> nothing done");
		    resRegister.setFailure(ResponseCode.FAILURE, "Tài khoản đã có người đăng ký, bạn vui lòng đăng ký tài khoản khác!");
		    aResPkg.addMessage(resRegister);
		    return PROCESS_OK;
		}
	    }

	    LoginResponse resLogin = new LoginResponse();
	    DatabaseDriver.getConfigInfo(resLogin, rqRegister.cp);

	    resRegister.setSuccess(ResponseCode.SUCCESS, uid, 50000, 1, 1);

	    resRegister.smsContent = resLogin.smsContent;
	    resRegister.smsNumber = resLogin.smsActive;

	    resRegister.smsValue = resLogin.smsActiveValue;

	    mLog.debug("[REGISTER] : " + username + " Success");

	    // log it
	    DatabaseDriver.logRegisterByIP(cp, username, uid, new Date(), IP, rqRegister.clientType);
	    
//	    if (rqRegister.clientType.equalsIgnoreCase("android") || rqRegister.clientType.equalsIgnoreCase("iphone")) {
//		DatabaseDriver.activateUser(uid);
//	    }
//	    if (rqRegister.downloadid > 0) {
//		DatabaseDriver.updateDownloadMoreInfo(rqRegister.downloadid, "registry");
//	    }
            rtn = PROCESS_OK;
        } catch (Throwable t) {
            resRegister.setFailure(ResponseCode.FAILURE, "Dữ liệu bạn nhập không chính xác!");
            rtn = PROCESS_OK;
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resRegister != null) && (rtn == PROCESS_OK)) {
                aResPkg.addMessage(resRegister);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }
    
    private boolean isUsernameOK(String username) {
        Pattern pattern;
        Matcher matcher;
        String USERNAME_PATTERN = "^[a-z0-9_]{3,}$";
        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(username);
        return matcher.matches();
    }
    
    private String handleRegister_OpenIDOpen(ISession aSession, String cp, String username, String password) {
	String packageName = this.getClass().getPackage().getName() + ".channeling";
	String className = cp.substring(0, 1).toUpperCase() + cp.substring(1).toLowerCase();
	String fullClassName = packageName + "." + className;
	String result;
	try {
	    ChannelingDoBusiness business = (ChannelingDoBusiness) Class.forName(fullClassName).newInstance();
	    result = business.doRegister(aSession, username, password);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    mLog.debug(ex.getMessage());
	    return "Đã có lỗi xảy ra.";
	}
	return result;
    }
}
