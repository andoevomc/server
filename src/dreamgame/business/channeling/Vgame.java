package dreamgame.business.channeling;

import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.util.IPHelper;
import java.util.HashMap;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author trungnm
 */
public class Vgame extends ChannelingDoBusiness {
    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ChannelingDoBusiness.class);
    private static final String APP_KEY = "5190af60715a9";
    private static final String APP_SECRET = "62d53903b7a63845a2d4665b62ece44f";
    
    public String doLogin(ISession aSession, UserEntity user, String username, String password) {
	mLog.debug("Vgame - doLogin:");
	String url = "http://api.vgame.vn/m/signin";
	HashMap<String,String> parameters = new HashMap<String,String>();
	String pass = (isValidMD5(password)) ? password : md5(password);
	parameters.put("LoginName", username);
	parameters.put("Password", pass);
	parameters.put("AppKey", APP_KEY);
	parameters.put("AppSecret", APP_SECRET);
	String result;
	JSONObject resultJson;
	try {
	    result = doHTTPPostRequest(url, parameters);
	    resultJson = new JSONObject(result);
	    int e = resultJson.getInt("e");
	    JSONObject userJson = resultJson.getJSONObject("r");
	    // success
	    if (e == 0) {
		// save info
		int cp_user_id = Integer.parseInt(userJson.getString("UserID"));
		String login_name = userJson.getString("LoginName");
		String IP = IPHelper.parseIPFromString(aSession.getIP());
		try {
		    long userID = user != null ? user.mUid : 0L;
		    userID = DatabaseDriver.saveInfoUserOpenIDOpen(aSession.getCP(), IP, userID, username, password, aSession.getMobileVer(), cp_user_id, login_name);
		    return "ok";
		} catch (Exception ex) {
		    mLog.debug(ex.getMessage());
		    return "Có lỗi Database !!!";
		}
	    }
	    // failed
	    else {
		return userJson.getString("Message");
	    }
	} catch (Exception e) {
	    mLog.debug(e.getMessage());
	    return "Có lỗi kết nối đến server VGame !!!";
	}
    }
    
    public String doRegister(ISession aSession, String username, String password) {
	mLog.debug("Vgame - doRegister:");
	String url = "http://api.vgame.vn/m/signup";
	HashMap<String,String> parameters = new HashMap<String,String>();
	String pass = (isValidMD5(password)) ? password : md5(password);
	parameters.put("LoginName", username);
	parameters.put("Password", pass);
	parameters.put("AppKey", APP_KEY);
	parameters.put("AppSecret", APP_SECRET);
	String result;
	JSONObject resultJson;
	// parse response
	try {
	    result = doHTTPPostRequest(url, parameters);
	    resultJson = new JSONObject(result);
	    int e = resultJson.getInt("e");
	    JSONObject userJson = resultJson.getJSONObject("r");
	    // success
	    if (e == 0) {
		// save info
		int cp_user_id = Integer.parseInt(userJson.getString("UserID"));
		String login_name = userJson.getString("LoginName");
		String IP = IPHelper.parseIPFromString(aSession.getIP());
		try {
		    long userID = DatabaseDriver.saveInfoUserOpenIDOpen(aSession.getCP(), IP, 0L, username, password, aSession.getMobileVer(), cp_user_id, login_name);
		    return "ok;" + userID;
		} catch (Exception ex) {
		    mLog.debug(ex.getMessage());
		    return "Có lỗi Database !!!";
		}
	    }
	    // failed
	    else {
		return userJson.getString("Message");
	    }
	} catch (Exception e) {
	    mLog.debug(e.getMessage());
	    return "Lỗi kết nối đến server VGame !!!";
	}
    }
    
    public String doCardCharge(ISession aSession, UserEntity user, String cardNumber, String cardSerial, String telco) {
	mLog.debug("Vgame - doCardCharge:");
	String url = "http://api.vgame.vn/m/charge";
	
	// prepare data
	String telcoVgameStyle = "";
	telco = telco.toLowerCase();
	if (telco.startsWith("vina")) {
	    telcoVgameStyle = "VNP";
	}
	else if (telco.startsWith("mobi")) {
	    telcoVgameStyle = "VMS";
	}
	else if (telco.startsWith("viettel")) {
	    telcoVgameStyle = "VTE";
	}	
	String checksum = md5(cardNumber + cardSerial + telcoVgameStyle + APP_SECRET);
	
	HashMap<String,String> parameters = new HashMap<String,String>();
	parameters.put("UserID", String.valueOf(user.cp_user_id));
	parameters.put("CardData", cardNumber);
	parameters.put("CardSerie", cardSerial);
	parameters.put("Telco", telcoVgameStyle);
	parameters.put("AppKey", APP_KEY);
	parameters.put("CS", checksum);
	String result;
	JSONObject resultJson;
	// make request & parse response
	try {
	    result = doHTTPPostRequest(url, parameters);
	    resultJson = new JSONObject(result);
	    int e = resultJson.getInt("e");
	    JSONObject userJson = resultJson.getJSONObject("r");
	    // success
	    if (e == 0) {
		// save info
		int amount = Integer.parseInt(userJson.getString("Amount"));
		try {
		    DatabaseDriver.chargeCardOpenIDOpen(aSession.getCP(), user, cardNumber, cardSerial, telco, amount);
		    return "ok;" + "Bạn đã nạp thành công thẻ " + amount + " VND.";
		} catch (Exception ex) {
		    mLog.debug(ex.getMessage());
		    return "Có lỗi Database !!!";
		}
	    }
	    // failed
	    else {
		return userJson.getString("Message");
	    }
	} catch (Exception e) {
	    mLog.debug(e.getMessage());
	    return "Lỗi kết nối đến server VGame !!!";
	}
    }
}
