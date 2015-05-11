package dreamgame.business;

import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.AdvEntity;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.LoginRequest;
import dreamgame.protocol.messages.LoginResponse;
//import bacay.data.VersionEntity;

import com.mchange.v1.util.ArrayUtils;
import dreamgame.business.channeling.ChannelingDoBusiness;
import dreamgame.config.DebugConfig;
import dreamgame.config.GameRuleConfig;
import dreamgame.data.CPEntity;
import dreamgame.data.ZoneID;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.util.codec.md5.MD5;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import javax.imageio.ImageIO;

public class LoginBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(LoginBusiness.class);

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }

    public static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "LoginBusiness - handleMessage");
	}
	
        int rtn = PROCESS_FAILURE;
        mLog.debug("[LOGIN]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        LoginResponse resLogin = (LoginResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        LoginRequest rqLogin = (LoginRequest) aReqMsg;
        try {
            // request message and its values
            String username = rqLogin.mUsername.trim();
            String password = rqLogin.mPassword;
	    String md = MD5.md5Hex(password);
//            mLog.debug("[LOGIN]: Username - " + username);
//            boolean versionOK = true;

	    // send version but no username ??? (WTF) (j2me: Context.preConnect())
            if (rqLogin.mobileVersion.length() > 0 && rqLogin.mUsername.length() == 0) {
                DatabaseDriver.updateDownloadSetup(rqLogin.downloadid, rqLogin.mobileVersion);

                /*
                 * String msg="I love you"; BufferedImage img = null; try { img
                 * = ImageIO.read(new File("Background.jpg"));
                 * ByteArrayOutputStream bas = new ByteArrayOutputStream();
                 * ImageIO.write(img, "jpg", bas); byte[] data =
                 * bas.toByteArray();
                 *
                 * byte[] len = intToByteArray(data.length); byte[] dest=new
                 * byte[4+data.length];
                 *
                 * System.out.println("ori len : "+byteArrayToInt(len));
                 * len[0]=25;
                 *
                 * System.out.print("Data : "); for (int i=0;i<10;i++)
                 * System.out.print(data[i] + " "); System.out.println();
                 *
                 * System.out.print("Len : "); for (int i=0;i<4;i++)
                 * System.out.print(len[i] + " "); System.out.println();
                 *
                 * System.arraycopy(len, 0, dest, 0, 4); System.arraycopy(data,
                 * 0, dest, 4, data.length);
                 *
                 * System.out.println("File len : "+data.length);
                 * System.out.println("data len : "+dest.length);
                 *
                 * System.out.print("dest : "); for (int i=0;i<14;i++)
                 * System.out.print(dest[i] + " "); System.out.println();
                 *
                 * aSession.writeImage(dest);
                 *
                 * } catch (IOException e) { e.printStackTrace(); }
                 */

                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 0;
            }


	    mLog.debug("[LOGIN]: cp = " + rqLogin.cp + "; username = " + rqLogin.mUsername);
	    aSession.setCP(rqLogin.cp);
	    // version?
            if (rqLogin.mobileVersion.length() > 0) {
		if (rqLogin.clientType.equalsIgnoreCase("iphone")) {
		   aSession.setIphone();
		} else if (rqLogin.clientType.equalsIgnoreCase("android")) {
//                System.out.println("setAndroid!");
                    aSession.setAndroid();
	        }
                mLog.debug("[LOGIN]: Mobile Ver :  - " + rqLogin.mobileVersion + "; clientType: " + rqLogin.clientType);
                aSession.setMobile(rqLogin.mobileVersion);
                aSession.setScreenSize(rqLogin.screen);
                DatabaseDriver.checkMobileVersion(resLogin, rqLogin.cp, rqLogin.clientType, rqLogin.mobileVersion);
            } 
	    else if (rqLogin.flashVersion.length() > 0) {
                mLog.debug("[LOGIN]: Flash Ver :  - " + rqLogin.flashVersion);
            } 
	    else {
                aSession.setMobile("");
                mLog.debug("[LOGIN]: Very Old Flash Ver!! :  - ");
            }

	    // get CP info 
	    CPEntity cp = DatabaseDriver.getCPInfo(rqLogin.cp);
	    if ( ! cp.isEnabled) {
		resLogin.setFailure(ResponseCode.FAILURE, "Phiên bản game hiện tại của bạn đã cũ và có lỗi. hãy vào www.thegioibai.com từ điện thoại để tải phiên bản mới nhất.");
		aResPkg.addMessage(resLogin);
		return PROCESS_OK;
	    }
	    
	    UserEntity user = null;
	    // cp la openID provider, sử dụng hệ thống user base riêng ?
	    if ( cp.isOpenID) {
		mLog.debug("isOpenID = true. Use user base of " + cp.cpID);
		// kiểm tra thông tin user đã lưu trong hệ thống trước đó không
		user = DatabaseDriver.getUserInfo(username, cp);
		
		// nếu không lưu trong hệ thống hoac username va password sai thi truy van den he thong openID de xac thuc
		if ( user == null || (!user.mPassword.equals(password) && !user.mPassword.equals(md)) ) {
		    // request to nodejs service to verify partner's user
		    String nodejsUrl = generateNodeJSUrl(cp, username, password);
		    mLog.debug("url nodejs: " + nodejsUrl);
		    String result = doGETHTTPRequest(nodejsUrl);
		    mLog.debug("result nodejs: " + result);
		    if (result.equals("ok")) {
			// xác thực xong rồi thì lấy thông tin người đó ra từ db lại nào
			user = DatabaseDriver.getUserInfo(username, cp);
		    } else if (result.equals("wrong")) {
			resLogin.setFailure(ResponseCode.FAILURE, "Sai mật khẩu !!!");
			aResPkg.addMessage(resLogin);
			return PROCESS_OK;
		    } else {
			resLogin.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, xin vui lòng thử lại sau ít phút !!!");
			aResPkg.addMessage(resLogin);
			return PROCESS_OK;
		    }
		} // end if user == null
		
	    } // end if isOpenID
	    else if (cp.isOpenIDOpen) {	// this cp provide api for signup, login, charge card ?
		mLog.debug("isOpenIDOpen = true. Use API for user action at " + cp.cpID);
		// kiểm tra thông tin user đã lưu trong hệ thống trước đó không
		user = DatabaseDriver.getUserInfo(username, cp);
		
		// nếu không lưu trong hệ thống hoặc user/password sai thì request đến openID provider để xác thực lại
		if ( user == null || (!user.mPassword.equals(password) && !user.mPassword.equals(md)) ) {
		    String result = handleLogin_OpenIDOpen(aSession, user, cp.cpID, username, password);
		    mLog.debug("result handleLogin_OpenIDOpen: " + result);
		    if (result.equals("ok")) {
			// xác thực xong rồi thì lấy thông tin người đó ra từ db lại nào
			user = DatabaseDriver.getUserInfo(username, cp);
		    }
//		    else if (result.equals("wrong")) {
//			resLogin.setFailure(ResponseCode.FAILURE, "Sai mật khẩu !!!");
//			aResPkg.addMessage(resLogin);
//			return PROCESS_OK;
//		    } 
		    else {
//			resLogin.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, xin vui lòng thử lại sau ít phút !!!");
			resLogin.setFailure(ResponseCode.FAILURE, result);
			aResPkg.addMessage(resLogin);
			return PROCESS_OK;
		    }
		}
	    } // end if isOpenIDOpen
	    else { // cp sử dụng hệ thống user base chung cua thegioibai
		mLog.debug("Use user base of thegioibai.com");
		user = DatabaseDriver.getUserInfo(username);
	    }
	    
	    

	    // server is down?
	    if (aSession.getManager().shutDown || DatabaseDriver.stopServer) {
		resLogin.setFailure(ResponseCode.FAILURE, "Server đang tiến hành bảo trì! Bạn hãy thử đăng nhập lại trong vòng vài phút nữa!");
	    } 
	    // non-existed user
	    else if (user == null) { // non-existed user
		resLogin.setFailure(ResponseCode.FAILURE, "Bạn chưa đăng ký!");
	    } 
	    // valid password
	    else if (user.mPassword.equals(md)
			|| user.mPassword.equals(password)
		) {
		ISession temp = aSession.getManager().findSession(user.mUid);

		// da ton tai session voi username nay -> da dang nhap o noi khac
		if (temp != null) {                 
		    temp.leaveAllRoom(aResPkg); 
		    temp.writeMessage("Bạn đã bị ngắt kết nối vì đăng nhập từ nơi khác.");                 
		    Thread.sleep(100); 
		    temp.close(); 
		    temp=null; 
		}
//		else {

//		if (temp != null && temp.realDead()) {
//
//		    mLog.info("Close relogin session : " + temp.userInfo());
//		    resLogin.setFailure(ResponseCode.FAILURE, "Tên này đã đăng nhập! Nếu bạn vừa thóat ra thì hay chờ 1 chút rồi thử lại!");
//
//		    Vector<Room> joinedRoom = temp.getJoinedRooms();
//		    if (joinedRoom != null && joinedRoom.size() > 0) {
//			mLog.info("Error : " + temp.getUserName() + " in room : " + joinedRoom.get(0).getName() + " ; roomID : " + joinedRoom.get(0).getRoomId());
//			temp.leaveAllRoom(aResPkg);
//
//		    } else {
//			temp.close();
//		    }
//
//		} else if (temp != null && !temp.getUserName().equalsIgnoreCase("")) {
////                    if (!temp.getUserName().equalsIgnoreCase("")) {
//		    System.out.println("Last Acess : " + temp.getLastAccessTime());
//		    System.out.println("Last Acess : " + temp.getLastAccessTime().getTime());
//		    System.out.println("Idle : " + (System.currentTimeMillis() - temp.getLastAccessTime().getTime()) + " ms!");
//		    System.out.println("Expired : " + temp.isExpired());
//
//		    System.out.println("ping : " + temp.realDead());
//		    resLogin.setFailure(ResponseCode.FAILURE, "Tên này đã đăng nhập! Nếu bạn vừa thóat ra thì hay chờ 1 chút rồi thử lại!");
//
//		    temp.ping(aSession);
////                    }
//		} else {

		// tien hanh dang nhap
		    DatabaseDriver.getConfigInfo(resLogin, rqLogin.cp);

		    aSession.setActive(user.isActive);
		    aSession.setGiftInfo(user.receive_gift, user.remain_gift, resLogin.max_gift, resLogin.cash_gift);

		    // banned user
		    if (user.isBanned) {
			long cash = DatabaseDriver.getUserMoney(user.mUid);
			if (cash > 0) {
			    DatabaseDriver.updateUserMoney(cash, false, user.mUid, "Set tien ve 0, vi hack!");
			}
			resLogin.setFailure(ResponseCode.FAILURE, "Tài khoản của bạn đã bị khóa do hack hay lợi dụng lỗi Game.");
		    } 
		    // mobile user not active?
		    else if (!user.isActive && DatabaseDriver.chargeRegister && aSession.getMobile()) {
			//resLogin.smsValue2;
			resLogin.notActive = true;
			resLogin.smsNumber = resLogin.smsActive;
			resLogin.setFailure(ResponseCode.FAILURE, "Tài khoản của bạn chưa được kích hoạt. Hãy soạn tin nhắn : "
				+ resLogin.smsContent + " " + user.mUsername + " gửi tới số " + resLogin.smsNumber + " để kích hoạt. Bạn sẽ nhận được ngay "
				+ resLogin.smsActiveValue + "000 " + GameRuleConfig.MONEY_SYMBOL +  " trong tài khoản.");

		    } 
		    // ok -> login
		    else {

//                    VersionEntity latest = DatabaseDriver.getLatestVersion();
			Vector<AdvEntity> advs = DatabaseDriver.getAdvInfos();
			long moneyUpdateLevel = DatabaseDriver.getMoneyForUpdateLevel(user.level + 1);
			resLogin.setSuccess(ResponseCode.SUCCESS, user.mUid,
				user.money, user.avatarID, user.level, user.lastLogin, DatabaseDriver.getTuocvi(), user.playsNumber, moneyUpdateLevel);
			resLogin.setAdvs(advs);
//                    resLogin.setVersion(latest);
			System.out.println("last login : [" + user.lastLogin + "]");

			// user vua dang ky xong thi khong tang tien cho lan dau dang nhap,
			// user dang nhap lan dau trong ngay thi duoc tang tien.
			if (user.lastLogin != null) {
			    Calendar c = Calendar.getInstance();

			    //c.setTime(today);
			    //c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			    String DATE_FORMAT = "yyyy-MM-dd";
			    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

			    String today = sdf.format(c.getTime());

			    System.out.println("today : [" + today + "]");
			    System.out.println("last : [" + user.lastLogin.toString() + "]");

			    // mobile user, login lan dau trong ngay thi tang tien
			    if (DatabaseDriver.enable_event_gift == 1 && aSession.getMobile()) {
				if (user.lastLogin.toString().equalsIgnoreCase(today)) {
				    if (resLogin.newVer.length() == 0 && DatabaseDriver.event_message.length() > 0) {
					System.out.println("Last login is today!");
					String msg = DatabaseDriver.event_message;
					msg = msg.replace("***", "" + DatabaseDriver.event_login_value);

					if (rqLogin.updateLoginMessage) {
					    resLogin.adminMessage = msg;
					} else {
					    aSession.writeMessage(msg);
					}
				    }

				} else {
				    String msg = DatabaseDriver.first_login_message;
				    msg = msg.replace("***", "" + DatabaseDriver.event_login_value);

				    if (msg.length() > 0) {
					resLogin.money=DatabaseDriver.updateUserMoney(DatabaseDriver.event_login_value, true, user.mUid, "Tang tien event lan dau login!");
					if (rqLogin.updateLoginMessage) {
					    resLogin.adminMessage = msg;
					} else {
					    aSession.writeMessage(msg);
					}
				    }
				}
			    }
			}
			// user vua dang ky xong va dang nhap
			else {
			    // TODO: delete this sau 11h 15/05/2013
			    // event anh The anh: tang 500k gold cho lan dang nhap dau tien, tu 11h 14/05/2013 - 11h 15/05/2013
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			    Date now = new Date();
			    String date = sdf.format(now);
			    sdf.applyPattern("H");
			    int hour = Integer.parseInt(sdf.format(now));
			    if ( (date.equals("2013-05-14") && hour > 10) || (date.equals("2013-05-16") && hour < 11) ) {
				// cong 500k vao tai khoan nguoi dung
				int event_money = 500000;
				resLogin.money = DatabaseDriver.updateUserMoney(event_money, true, user.mUid, "Tang tien event dang ky moi tang 500k tu 14/05/2013 - 15/05/2013!");
				String msg = "Chào mừng bạn đến với Thế Giới Bài. Đây là lần đăng nhập đầu tiên của bạn - hệ thống gửi tặng bạn 500.000 Gold và cơ hội nhận iphone 5 cực hot. Chúc bạn có những phút thư giãn tuyệt vời cùng TheGioiBai.";
				if (rqLogin.updateLoginMessage) {
				    resLogin.adminMessage = msg;
				} else {
				    aSession.writeMessage(msg);
				}
			    }
			}

			aSession.setCP(user.cp);
			aSession.setCPEntity(cp);
			aSession.setLoginTime(new Date());
			aSession.setDevice(rqLogin.device);
			
			aSession.setUID(user.mUid);
			aSession.setUserName(user.mUsername);
			aSession.setLoggedIn(true);
			
			aSession.setCurrentZone(ZoneID.GLOBAL);

			aSession.setUserEntity(user);

			String s = "";
			if (aSession.getMobile()) {
			    s = "/ ClientVer :" + aSession.getMobileVer();
			}

			/**
			 * Thomc fix*
			 */
			Calendar c = Calendar.getInstance();

			//c.setTime(today);
			//c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			String DATE_FORMAT = "yyyy-MM-dd";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

			String today = sdf.format(c.getTime());
			DatabaseDriver.updateLastTimeLogin(user.mUid, aSession.getIP(), aSession.getMobile(), rqLogin.device + s, rqLogin.screen, today);

			aSession.setClientType(rqLogin.clientType);
			if (rqLogin.clientType.equalsIgnoreCase("iphone")) {
			    resLogin.jaOsCharge = DatabaseDriver.getAppleCharge("iphone");
			    aSession.setIphone();
			}

			if (rqLogin.clientType.equalsIgnoreCase("android")) {
			    resLogin.jaOsCharge = DatabaseDriver.getAppleCharge("android");
//			    System.out.println("setAndroid!");
			    aSession.setAndroid();
			}

			if (rqLogin.downloadid > 0) {
			    DatabaseDriver.updateDownloadMoreInfo(rqLogin.downloadid, "login");
			}
		    }

		    //long lastRoom=user.lastMatch;
		    //System.out.println("Last Match : "+lastRoom);                    

		    /*
		     * Zone phomZone = aSession.findZone(ZoneID.PHOM); // get
		     * the current room to notify to the opponent Room room =
		     * phomZone.findRoom(lastRoom); if (room != null) {
		     * PhomTable table = (PhomTable) room.getAttactmentData();
		     * if (table.isPlaying && table.containPlayer(user.mUid)) {
		     * resLogin.setLastRoom(room.getRoomId(), room.getName(),
		     * ZoneID.PHOM); } }
		     */
		    aSession.setFriendSession(DatabaseDriver.getFriendsSession(user.mUid));
		    aSession.sendFriendNotification(user.mUsername + " đăng nhập game!");
//		}
	    } 
	    // sai mat khau
	    else {
		resLogin.setFailure(ResponseCode.FAILURE, "Sai mật khẩu?");
	    }
	    // 	return
	    rtn = PROCESS_OK;
        } catch (Throwable t) {
            resLogin.setFailure(ResponseCode.FAILURE, "Hiện tại không đăng nhập được. Bạn hãy thử lại xem!");
            aSession.setLoggedIn(false);
            aSession.setCommit(false);
            rtn = PROCESS_OK;
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resLogin != null) && (rtn == PROCESS_OK)) {
                aResPkg.addMessage(resLogin);

//                if (aSession.isLoggedIn()) {
//                    if (rqLogin.mobileVersion.equals("1.0") || rqLogin.mobileVersion.equals("1.1")) {
//                        LoginResponse resVer = (LoginResponse) msgFactory.getResponseMessage(aReqMsg.getID());
//                        resVer.setFailure(ResponseCode.FAILURE, "Phiên bản game hiện tại của bạn đã cũ và có lỗi. hãy vào www.thegioibai.com từ điện thoại để tải phiên bản mới nhất.");
//                        aResPkg.addMessage(resVer);
//                    }
//                }

            }
	    System.out.println("no of users: " + aSession.getManager().numUser());
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }
    
    // generate nodejs url for handling openID authentication
    private String generateNodeJSUrl(CPEntity cp, String username, String password) {
	try {
	    String url = "http://127.0.0.1:3322/abc?";
	    String params = "cp=" + URLEncoder.encode(cp.cpID, "UTF-8");
	    if (cp.isUseEmailForLogin)
		params += "&email=" + URLEncoder.encode(username, "UTF-8");
	    else
		params += "&username=" + URLEncoder.encode(username, "UTF-8");
	    params += "&password=" + URLEncoder.encode(password, "UTF-8");
	    return url + params;
	} catch (UnsupportedEncodingException ex) {
	    ex.printStackTrace();
	    return "";
	}
    }
    
    private String doGETHTTPRequest(String urlToRead) {
      URL url;
      HttpURLConnection conn;
      BufferedReader rd;
      String line;
      String result = "";
      try {
         url = new URL(urlToRead);
         conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         while ((line = rd.readLine()) != null) {
            result += line;
         }
         rd.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

    private String handleLogin_OpenIDOpen(ISession aSession, UserEntity user, String cp, String username, String password) {
	String packageName = this.getClass().getPackage().getName() + ".channeling";
	String className = cp.substring(0, 1).toUpperCase() + cp.substring(1).toLowerCase();
	String fullClassName = packageName + "." + className;
	String result;
	try {
	    ChannelingDoBusiness business = (ChannelingDoBusiness) Class.forName(fullClassName).newInstance();
	    result = business.doLogin(aSession, user, username, password);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    mLog.debug(ex.getMessage());
	    return "Đã có lỗi xảy ra.";
	}
	return result;
    }
}
