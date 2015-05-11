package dreamgame.databaseDriven;

import dreamgame.config.DebugConfig;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;


import dreamgame.data.AdvEntity;
import dreamgame.data.AvatarEntity;
import dreamgame.data.CPEntity;
import dreamgame.data.ChargeCardEntity;
import dreamgame.data.ChargeHistoryEntity;
import dreamgame.data.CuocEntity;
import dreamgame.data.MessageOfflineEntity;
import dreamgame.data.PostEntity;
import dreamgame.data.UserEntity;
import dreamgame.data.VersionEntity;
import dreamgame.protocol.messages.LoginResponse;
import dreamgame.gameserver.framework.servers.nettysocket.NettySocketServer;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.SessionManager;
import dreamgame.util.codec.md5.MD5;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
//import java.sql.Types;

public class DatabaseDriver {

    public static ArrayList<CuocEntity> cuocList = new ArrayList();
    public static long startServerTime = System.currentTimeMillis();
    public static JSONArray iphoneChargeList;
    public static JSONArray androidChargeList;
    public static boolean activeSystem = true;
    public static boolean eventAwardEnable = false;
    public static long eventPlayAward = 0;
    public static long eventChangeAward = 0;
    public static long eventChangeAwardMoney = 0;
    public static String helpContentAdd = "***GAME BẦU CUA TÔM CÁ|"
            + "Game bầu cua tôm cá |"
            + "I. Luật chơi|"
            + "- Một người làm cái, là chủ bàn. Chủ bàn sẽ được chuyển cho 1 trong những ngươi chơi còn lại cuối ván nếu chủ bàn hiện tại thoát. Nếu không ai đủ điều kiện làm chủ bàn, bàn chơi sẽ bị hủy sau khi hết ván.|"
            + "- Chủ bàn được quyền bắt đầu ván chơi, và không được đặt cược. Riêng game bầu cua không cần chờ người chơi sẵn sàng.|"
            + "- Người chơi sẽ đặt cược vào 6 cửa: Bầu, Cua, Tôm, Cá, Gà, Hươu bằng cách xoay hình muốn đặt về giữa màn hình (bấm phím trái/phải).|"
            + "- Sau đó chọn 'Đặt cửa' và danh sách mức đặt cứa sẽ hiện ra. Bạn bấm phím mũi tên trái hoặc phải để thay đổi mức cược (có 3 mức cược: 1, 5 và 10 lần tiền cược của bàn). Cuối cùng bầm 'Chọn' để đặt cược.|"
            + "- Khi chủ bàn bắt đầu ván chơi, chiếc bát bắt đầu rung trong 20 giây và bạn sẽ đặt cược trong thời gian này.|"
            + "- Nếu bạn vào bàn khi ván chơi đang trong thời gian đặt cược bạn có thể đặt cược như bình thường.|"
            + "II. Tính thắng thua|"
            + "- Sau 20 giây đặt cược, bát sẽ mở ra hình 3 ô kết quả.|"
            + "- Nếu cửa bạn đặt trùng với ô kết quả thì bạn sẽ ăn số GG = số lần đặt cửa x tiền cược bàn x số lần về của cửa đó.|"
            + "- VD bạn vào bàn 1.000 GG, đặt 5 lần vào Bầu, và trong kết quả trả về có 2 hình bầu, 1 hình hươu, bạn sẽ ăn: 5 x 1000 x 2 = 10.000 GG (có trừ 10% phế).|"
            + "- Ngược lại, nếu cửa bạn đặt không trùng với ô kết quả nào thì bạn sẽ bị mất số GG = số lần đặt cửa x tiền cược bàn.|"
            + "- Chủ bàn sẽ phải trả tiền bộ số tiền người chơi thắng và ăn toàn bộ số tiền người chơi bị thua.|";
    public static String helpContent = "***GAME PHỎM|"
            + "Game phỏm|"
            + "I. Luật chơi|"
            + "- Ban đầu mỗi người có 9 lá bài, ngoại trừ người đánh đầu có 10 lá bài. Những lá bài còn lại được đặt ở giửa bàn.|"
            + "- Người đi đầu đánh đi 1 lá bài trên tay của mình. Người kết tiếp có thể ăn lá bài đó nếu nó có thể hợp với bài trên tay thành một phỏm.|"
            + "- (Phỏm là: bộ 3 lá bài trở lên cùng số hoặc bộ ba lá bài trở lên cùng chất liên tục).|"
            + "- Nếu người kế tiếp không thể ăn hay không muốn ăn lá bài người vị trí trên đánh xuống, người đó phải bốc thêm 1 lá bài.|"
            + "- Ván bài kết thúc sớm khi có một người ù. Những lá bài trên tay người này có thể được sắp xếp thành phỏm và không dư lá bài nào.|"
            + "- Một trường hợp Ù đặc biệt khác là Ù Khan (sẽ update ở version 2). Cụ thể là một người chơi sau khi chia bài có 9 hoặc 10 lá bài trong đó không có 2 lá bài nào có khả năng sẽ ghép thêm với một lá khác thành một phỏm.|"
            + "- Nếu không có ai ù, ván bài sẽ kết thúc sau 4 vòng đánh. Trước khi đánh đi lá bài trong vòng 4, người chơi cần hạ tất cả những phỏm mình có cho mọi người biết và có thể ký gửi những quân bài có trên bài của mình vào phỏm của người chơi hà trước đó.|"
            + "II. Tính thắng thua|"
            + "- Trong một ván bài thường khi không có ai ù, điểm của mỗi người chơi là tổng số điểm các con bài còn lại trên tay của người đó. Khi tính điểm, các cây bài J, Q, K được tính 11, 12, và 13 điểm.|"
            + "- Tổng số điểm của bài càng ít thì càng tốt, và người ít điểm nhất sẽ thắng. Một trường hợp ngoại lệ là nếu 1 người không hạ được phỏm nào, người đó được gọi là bị móm. Người bị móm luôn thua người không bị móm cho dù có tổng số điểm ít hơn.|"
            + "- Trường hợp tất cả những người chơi đều móm thì phần thắng sẽ thuộc về người hạ bài móm đầu tiên (thông báo móm đầu tiên).|"
            + "- Trường hợp đồng điểm nếu 2 người có cùng tổng số điểm, người nào hạ trước sẽ là người dành chiến thắng.|"
            + "III. Cách tính tiền|"
            + "- Bị ăn cây thứ nhất, thì mất 1 lần tiền cược. Bị ăn cây thứ 2 thì mất 2 lần tiền cược|"
            + "- Bị ăn cây ở vòng áp cuối(cây chốt) thì mất 4 lần tiền cược.|"
            + "- Về Nhì mất 1 lần tiền cược. Về ba mất 2 lần tiền cược về Bét mất 3 lần tiền cược.|"
            + "- Móm(trên bài không có phỏm nào) thì mất 4 lần tiền cược.|"
            + "- Ù thì ăn được mỗi nhà 5 lần tiền cược. Nếu bị Ù đền (đánh 3 quân cho người Ù ăn) thì phải đền thay cả làng.|"
            + //Tiến lên
            "***GAME TIẾN LÊN MIỀN NAM|"
            + "Game tiến lên miền nam|"
            + "I. Luật chơi|"
            + "- Một bộ bài (52 lá) được chia đều cho bốn người chơi, mỗi người 13 lá bài (trường hợp chơi không đủ bốn người thì mỗi người cũng chỉ được nhận 13 lá, các lá còn dư được để riêng ra)|"
            + "- Người có lá bài 3 Bích được đánh trước ở ván đầu tiên. Từ ván thứ 2 trở đi người nào thắng ván trước đó được đánh bài trước.|"
            + "- Khi người chơi đánh ra một quân bài/nhóm bài, người tiếp theo phải bắt bằng quân bài/nhóm bài lớn hơn.|"
            + "- Những nhóm có cùng số lượng bài và giá trị của những là bài như nhau thì nhóm mạnh hơn là nhóm có lá bài cao nhất có chất mạnh hơn.|"
            + "- Người đánh hết bài trước là người về Nhất, những người còn lại bị xử thua và trừ tiền theo kiểu đếm lá|"
            + "II. Cách tính tiền|"
            + "Khi thua,  đếm số bài trên tay để nhân với tiền cược đầu ván, ra số tiền người chơi bị thua.|"
            + "- Nếu thối 2 đen: Tính 2 lá.|"
            + "- Thối 2 đỏ, ba đôi thông: 5 lá.|"
            + "- Thối tứ quý: 7 lá.|"
            + "- Thối 4 đôi thông: 13 lá.|"
            + //#ifndef InetDeploy
            //Ba cây
            "***GAME BA CÂY|"
            + "Game ba cây|"
            + "I. Luật chơi|"
            + "- Chỉ sử dụng các lá bài A>2>3>4>5>6>7>8>9 ( A là 1 điểm )|"
            + "- Khi trận đấu bắt đầu mỗi người sẽ được chia 3 quân bài|"
            + "- Mỗi người được chia 3 quân bài sau đó cộng điểm với nhau. Nếu tổng điểm lớn hơn 10, sẽ chỉ lấy tính hàng đơn vị|"
            + "- Lần lượt từng người sẽ tố với chủ phòng|"
            + "- Đánh tới : Đến lượt người nào tố với chú phòng, sẽ có các tùy chọn bỏ (tức không chấp nhận tố tiếp), cân cửa (tức đồng ý chơi với số tiền mà đối phương tố)và tố thêm, mỗi người có thể tố không giới hạn với chủ phòng. |"
            + "- Thời gian tố là 30s, quá thời gian đó xem như bạn bỏ (tức bạn không tố tiếp).|"
            + "- Nếu trong ván chơi và trận đấu đã bắt đầu mà bạn thoát thì sẽ bị xử thua và mất số tiền đã tố (nếu như tố rồi) hoặc tiền tiền đặt cửa (nếu chưa tố).|"
            + "- Trong trường hợp bạn bị mất kết nối thì sẽ vẫn tính điểm để so sánh với đối thủ của mình và tính thắng thua.|"
            + "II. Cách tính tiền|"
            + "- Mọi người chơi đều đánh với nhà cái, ai lớn điểm hơn là thắng|"
            + "- Trong trường hợp 2 người cùng số điểm sẽ so chất để phân định thắng thua : rô > cơ > bích > tép .Nếu có cùng tổng điểm và chất, sẽ so sánh giá trị của quân bài, ai có quân bài lớn hơn sẽ thắng . Ngoại trừ quân A rô. A rô là quân lớn nhất khi xét trường hợp bằng điểm.|"
            + //#endif
            //Cờ Tướng
            "***GAME CỜ TƯỚNG|"
            + "Game cờ tướng|"
            + "I. Luật chơi|"
            + "1.Tùy chọn tạo phòng:   |"
            + "   1.1   Chấp xe, pháo mã|"
            + "   1.2   Số tiền cho mỗi ván  1.2   Số tiền cho mỗi ván|"
            + "2.Một người có thể tạo phòng chơi với số tiền thắng thua cho mỗi ván được chọn theo cấp bậc, mời bạn vào chơi hoặc người khác xin chơi, nếu đồng ý người nào thì người đó làm đối thủ của chủ phòng. Các user khác chỉ là người xem và chat chít.|"
            + "3.Người chơi có quyền vào xem một phòng đang chơi và tán ngẩu. Nếu được chủ bàn mời chơi thì sẽ vào làm người chơi. Hoặc xin chơi mà được chủ bàn chấp nhận|"
            + "4.Có thể cho tùy chọn chấp:|"
            + "  4.1 Chấp 1 xe  |"
            + "  4.2 Chấp 1 pháo  |"
            + "  4.2 Chấp 1 mã  |"
            + "5.Một phòng chỉ bị hủy khi chủ phòng thoát hoặc chủ phòng hết tiền chơi.|"
            + "6.Mỗi lượt đi của một người chơi là 2 phút, quá 2 phút không đánh sẽ bị xử thua.  |"
            + "7.Hai người chơi có quyền xin hòa, xin thua.  |"
            + "8.Người nào thua sẽ mất số tiền bằng số tiền cược khi chủ phòng tạo ra chọn. Nếu hòa thì hai bên sẽ không mất gì.  |"
            + "II. Luật thắng thua|"
            + "   8.1 Người chơi bị chiếu tướng mà tướng không còn nước đi sẽ thua.  |"
            + "   8.2 Người chơi nào thoát ra khỏi phòng chơi xem như thua khi trận đấu đang diễn ra.|";
    public static int phantram = 5;
    public static int maxRoom = 12;
    public static int maxChannel = 4;
    public static boolean choiDemLa = true;
    public static String event_message = "";
    public static String first_login_message = "";
    public static int enable_event_gift = 0;
    public static int event_login_value = 10000;
    public static String noticeText = "";
    public static boolean newAllowSms = true;
    public static boolean allowSmsCharge = true;
    public static boolean allowCardCharge = true;
    
    public static boolean allowIphoneCharge = true;
    public static String chargeUrl = "http://thegioibai.com/nap-the-mobile";
    public static Connection conn = null;
    public static Connection conn_download = null;
    public static String url, db_password, db_username, url_download;
    public static boolean serverDebug = false;
    public static boolean chargeRegister = false;
    public static boolean doubleMD5 = false;
    public static boolean update_download = false;
    public static boolean log_user = true;
    public static boolean log_code = true;         // log code match!
    public static boolean log_all_code = false;    //log all code to debug log
    public static boolean stopServer = false;    //log all code to debug log
    public static NettySocketServer server;
    private static long timeWaitPlaying = 15;//in minutes

    public static SessionManager sm=null;
    public static void setUserSessionMoney(long uid,long newcash){
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - setUserSessionMoney: uid = " + uid + "; newcash = " + newcash);
	}
	
        if (sm!=null){
             ISession is=sm.findSession(uid);
             if (is!=null)
                 is.getUserEntity().money=newcash;
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
     }
    
    public static long saveInfoUserOpenIDOpen(String cp, String ip, long userID, String username, String password, String mobileVersion, int cpUserID, String loginName)
	    throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - saveInfoUserOpenIDOpen");
	}
        //user MD5
        password = MD5.md5Hex(password);
        int mobile = 0;
        if (mobileVersion.length() > 0) {
            mobile = 1;
        }

	// existed -> update
	if (userID > 0) {
	    String query = "UPDATE `user` SET `Password`=? WHERE `UserID`=? AND `cp`=? ;";
	    CallableStatement cs = conn.prepareCall(query);
	    cs.clearParameters();
	    
	    cs.setString(1, password);
	    cs.setLong(2, userID);
	    cs.setString(3, cp);
	    
	    cs.executeUpdate();
	    cs.clearParameters();
	    cs.close();
	    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	    return userID;
	}
	
	// eles insert new;
  String cpUsername = username + "@" + cp;	
  System.out.println("call Register!");
  String query = "insert into user (Name,Password,isMobile,cp,cp_user_id,ip) values (?,?,?,?,?,?);";
  CallableStatement cs = conn.prepareCall(query);
  cs.clearParameters();

  cs.setString(1, cpUsername);
  cs.setString(2, password);
  cs.setInt(3, mobile);
	cs.setString(4, cp);
	cs.setInt(5, cpUserID);
	cs.setString(6, ip);
  cs.executeUpdate();

        System.out.println("Create userstt, cash!");
        long k = getUserID(cpUsername);
        System.out.println("user id : " + k);

        query = "insert into userstt (Userid,PlaysNumber) VALUES (" + k + ", 0);";
        cs = conn.prepareCall(query);
        cs.clearParameters();
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return k;
    }
    
    // Fix
    public static long registerUser(String username, String password, int sex,
            int age, String mail, String phone, String mobileVersion, String cp) throws Exception {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - registerUser");
	}
        //user MD5
        password = MD5.md5Hex(password);
        int mobile = 0;
        if (mobileVersion.length() > 0) {
            mobile = 1;
        }

        //e.printStackTrace();
        System.out.println("call Register!");

        String query = "insert into user (Name,Password,Sex,Age,Email,PhoneNumber,isMobile,cp) values (?,?,?,?,?,?,?,?);";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();

        cs.setString(1, username);
        cs.setString(2, password);
        cs.setInt(3, sex);
        cs.setInt(4, age);
        cs.setString(5, mail);
        cs.setString(6, phone);
        cs.setInt(7, mobile);
	cs.setString(8, cp);
        //cs.executeUpdate();
        cs.executeUpdate();


        System.out.println("Create userstt, cash!");
        //INSERT INTO `userstt` VALUES(uid, 0, 0, 10000, 1, 1);
        long k = getUserID(username);
        System.out.println("user id : " + k);

        query = "insert into userstt (Userid,PlaysNumber) VALUES (" + k + ", 0);";
        cs = conn.prepareCall(query);
        cs.clearParameters();
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return k;

    }

    public static void updateDownloadMoreInfo(int downloadid, String column) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateDownloadMoreInfo: downloadid = " + downloadid + "; column = " + column);
	}
	
        if ( ! update_download) {
	    System.out.println("! update_download");
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return;
        }

	System.out.println("do it");	
        try {
            if (conn_download.isClosed()) {
                conn_download = DriverManager.getConnection(url_download, db_username, db_password);
                System.out.println("Reconnect!");
            }

            if (downloadid <= 0) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return;
            }

            String query = "UPDATE dis_log_download SET "
                    + column + "=1 WHERE id=?;";
            PreparedStatement st = conn_download.prepareStatement(query);

            st.setLong(1, downloadid);
            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateDownloadSetup(int downloadid, String version) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateDownloadSetup: downloadid = " + downloadid + "; version = " + version);
	}
	
        if (!update_download) {
	    System.out.println("NOT update_download");
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return;
        }

	System.out.println("do it");
        try {
            if (conn_download.isClosed()) {
                conn_download = DriverManager.getConnection(url_download, db_username, db_password);
                System.out.println("Reconnect!");
            }

            if (downloadid <= 0) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return;
            }

            String query = "UPDATE dis_log_download SET "
                    + "version=?,setup=1,setup_time=NOW() WHERE id=?;";
            PreparedStatement st = conn_download.prepareStatement(query);
            st.setString(1, version);
            st.setLong(2, downloadid);
            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateUserZone(long uid, int zone) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUserZone: uid = " + uid + "; zone = " + zone);
	}
        try {
            if (uid <= 0) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return;
            }
            String query = "UPDATE user SET "
                    + "zone=" + zone + " WHERE UserID=?;";
            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, uid);
            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void clearResetServer() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - clearResetServer");
	}
        try {

            String query = "UPDATE user SET "
                    + "isOnline =0,isPlaying=0,zone=0 WHERE 1=1;";

            PreparedStatement st = conn.prepareStatement(query);
            st.executeUpdate();
            st.clearParameters();
            st.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void resetGiftDaily(int gift) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - resetGiftDaily: gift = " + gift);
	}
//        try {
//            System.out.println("Reset Gift daily!");
//            String query = "UPDATE user SET "
//                    + "receive_gift=0,remain_gift=" + gift + " WHERE 1=1;";
//
//            PreparedStatement st = conn.prepareStatement(query);
//            st.executeUpdate();
//            st.clearParameters();
//            st.close();
//
//            query = "UPDATE cp SET "
//                    + "reset_gift_time= NOW() WHERE 1=1;";
//
//            st = conn.prepareStatement(query);
//            st.executeUpdate();
//            st.clearParameters();
//            st.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateGiftInfo(long uid, int receive_gift, int remain_gift) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateGiftInfo: uid = " + uid + "; receive_gift = " + receive_gift + " remain_gift = " + remain_gift);
	}
        try {
            System.out.println("User off : " + uid);
            int value = 0;

            if (uid <= 0) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return;
            }

            String query = "UPDATE user SET "
                    + "receive_gift= ?, remain_gift= ? WHERE UserID=?;";

            PreparedStatement st = conn.prepareStatement(query);

            st.setInt(1, receive_gift);
            st.setInt(2, remain_gift);
            st.setLong(3, uid);

            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateCashOnly(long uid, int cash) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateCashOnly: uid = " + uid + "; cash = " + cash);
	}
	
        try {
            System.out.println("User off : " + uid);
            int value = 0;

            if (uid <= 0) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return;
            }

            String query = "UPDATE userstt SET "
                    + " cash = cash+ " + cash + "  WHERE UserID = ?; ";

            PreparedStatement st = conn.prepareStatement(query);

            //st.setInt(1, cash);
            st.setLong(1, uid);

            st.executeUpdate();

            st.clearParameters();
            st.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String query = "INSERT into logvasc values (?,now(),?,?,?,?);";
            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, uid);
            st.setLong(2, cash);
            st.setString(3, "Set money to (Gift) : " + cash);
            st.setLong(4, 8);
            st.setLong(5, cash);

            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateUserOnline(long uid, boolean status) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUserOnline: uid = " + uid + "; status = " + status);
	}
	
        try {
            System.out.println("User off : " + uid);
            int value = 0;
            if (status) {
                value = 1;
            }
            if (uid <= 0) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return;
            }

            String query = "UPDATE user SET "
                    + "isOnline='" + value + "' WHERE UserID=?;";

            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, uid);
            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateCP(long uid, String cp) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateCP : uid = " + uid + "; cp = " + cp);
	}
        try {
            String query = "UPDATE user SET "
                    + "cp='" + cp + "' WHERE UserID=?;";

            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, uid);
            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateUser(long uid, String pass, String email, String number) throws Exception {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUser: ...");
	}
	
        String emailStr = "";
        String numberStr = "";

        pass = MD5.md5Hex(pass);

        if (email.compareTo("") != 0) {
            emailStr += ", Email='" + email + "', ";
        }

        if (number.compareTo("") != 0) {
            numberStr += " PhoneNumber='" + number + "'";
        }

        String query = "UPDATE user SET "
                + "Password='" + pass + "'" + emailStr + numberStr + " WHERE UserID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, uid);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    // Fix

    public static Vector<AdvEntity> getAdvInfos() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	 
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAdvInfos");
	}
        Vector<AdvEntity> res = new Vector<AdvEntity>();
        String query = "SELECT * FROM adv;";
        PreparedStatement st = conn.prepareStatement(query);
        ResultSet rs = st.executeQuery();

        if (rs != null && rs.first()) {
            AdvEntity v = new AdvEntity();
            v.detail = rs.getString("detail");
            v.link = rs.getString("link");
            res.add(v);
        }

        st.clearParameters();
        st.close();
        rs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    // Fix

//    public static boolean checkFlashVersion(String ver) {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - checkFlashVersion: version = " + ver);
//	}
//	
//        if (true) {
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//            return true;
//        }
//
//        System.out.println("Check Flash version : " + ver);
//        try {
//            String query = "SELECT * FROM version where Description like '%flash%';";
//            PreparedStatement st = conn.prepareStatement(query);
//            ResultSet rs = st.executeQuery();
//            if (rs != null && rs.next()) {
//                String link = rs.getString("linkDown");
//                System.out.println("Current Ver : " + link);
//
//                st.clearParameters();
//                st.close();
//                rs.close();
//
//                if (link.equalsIgnoreCase(ver)) {
//                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                    return true;
//                } else {
//                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                    return false;
//                }
//            }
//            st.clearParameters();
//            st.close();
//            rs.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//            return true;
//        }
//        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//        return true;
//    }

    public static void checkMobileVersion(LoginResponse res, String cp, String clientType, String version) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - checkMobileVersion: res.uid = " + res.uid + "; cp = " + cp + "; clientType = " + clientType + "; version = " + version);
	}
	
        System.out.println("Check Mobile version : " + clientType + ", " + version);
        try {
            
	    CallableStatement cstmt = conn.prepareCall("{call usp_get_link_download_new_version(?, ?, ?)}");
	    cstmt.setString(1, cp);
	    cstmt.setString(2, clientType);
	    cstmt.setString(3, version);
	    ResultSet rs = cstmt.executeQuery();
//	    String query = "SELECT * FROM version where Description like '%mobile%';";
//            PreparedStatement st = conn.prepareStatement(query);
//            ResultSet rs = st.executeQuery();
            if (rs != null && rs.next()) {
                String link = rs.getString("linkDown");
                String newVer = rs.getString("newVer");
                System.out.println("Current Ver : " + newVer);
//                if (!newVer.equalsIgnoreCase(ver)) {
		if ( ! link.equals("")) {
                    res.newVer = newVer;
                    res.linkDown = link;
                }
            }
//            st.clearParameters();
//            st.close();
            rs.close();
	    cstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

//    public static Vector<VersionEntity> getVersionInfos() throws Exception {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getVersionInfos");
//	}
//        Vector<VersionEntity> res = new Vector<VersionEntity>();
//        String query = "SELECT * FROM version;";
//        PreparedStatement st = conn.prepareStatement(query);
//        ResultSet rs = st.executeQuery();
//        if (rs != null && rs.first()) {
//            VersionEntity v = new VersionEntity();
//            v.desc = rs.getString("Description");
//            v.id = rs.getInt("VersionID");
//            v.link = rs.getString("linkDown");
//            v.createDate = rs.getDate("CreateDate");
//            res.add(v);
//        }
//        st.clearParameters();
//        st.close();
//        rs.close();
//        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//        return res;
//    }

//    public static VersionEntity getLatestVersion() throws Exception {
//        VersionEntity v = new VersionEntity();
//        v.id = 0;
//        Vector<VersionEntity> vs = getVersionInfos();
//        for (VersionEntity v1 : vs) {
//            if (v1.id > v.id) {
//                v = v1;
//            }
//        }
//        return v;
//    }
    
    public static boolean isUserGotTooManyTransferInCashAndTakeAction(long d_uid, Date aDate) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - isUserGotTooManyTransferInCashAndTakeAction: d_uid = " + d_uid);
	}
        int NO_1DAY = 15, NO_3DAY = 20, NO_1MONTH = 40;
	
	boolean isTooMuch = false;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");	
	Calendar c = Calendar.getInstance(); 
	c.setTime(aDate); 
	c.add(Calendar.MONTH, -1);
	Date lastMonth = c.getTime();
	
	c.setTime(aDate);
	c.add(Calendar.DATE, 1);
	Date nextDay = c.getTime();
	
	c.setTime(aDate);
	c.add(Calendar.DATE, -3);
	Date last3day = c.getTime();
	
	c.setTime(aDate);
	c.add(Calendar.DATE, -1);
	Date last1Day = c.getTime();
	
	// check theo thang
        String query = "SELECT COUNT(`id`) AS `count` FROM `log_transfer_gold` WHERE `des_uid`=? AND `date`>?  AND `date`<?;";	
        PreparedStatement ps = conn.prepareStatement(query);
	ps.setLong(1, d_uid);
	ps.setString(2, sdf.format(lastMonth));
	ps.setString(3, sdf.format(nextDay));
	
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            int count = rs.getInt("count");
	    if (count >= NO_1MONTH) {
		isTooMuch = true;
	    }	    
	    rs.close();		    
        }
	
	// check theo 3 ngay
	if ( ! isTooMuch) {
	    ps = conn.prepareStatement(query);
	    ps.setLong(1, d_uid);
	    ps.setString(2, sdf.format(last3day));
	    ps.setString(3, sdf.format(nextDay));
	    rs = ps.executeQuery();
	    if (rs != null && rs.first()) {
		int count = rs.getInt("count");
		if (count >= NO_3DAY) {
		    isTooMuch = true;
		}
		rs.close();		    
	    }	
	}
	
	// check theo 1 ngay
	if ( ! isTooMuch) {
	    ps = conn.prepareStatement(query);
	    ps.setLong(1, d_uid);
	    ps.setString(2, sdf.format(last1Day));
	    ps.setString(3, sdf.format(nextDay));
	    rs = ps.executeQuery();
	    if (rs != null && rs.first()) {
		int count = rs.getInt("count");
		if (count >= NO_1DAY) {
		    isTooMuch = true;
		}
		rs.close();		    
	    }	
	}
	
	// neu chuyen qua nhieu -> take action, truong hop co ke co tinh chuyen 
	// tien cho 1 acc de acc day bi ban, khong the ban des_uid duoc
	if ( isTooMuch ) {
	    // ban 1 loat acc
//	    query = "UPDATE `user` SET `is_active` = 99 WHERE `UserID`=?;";
//	    ps = conn.prepareStatement(query);
//	    ps.setLong(1, d_uid);
//	    ps.executeUpdate();
//	    ps.clearParameters();
	}
	
	if (ps != null) {
	    ps.clearParameters();
	    ps.close();
	}
	
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return isTooMuch;
    }
    
    public static void transferMoney(long s_uid, long d_uid, long money) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - transferMoney: ...");
	}
        /*
         * String query = "{ call Transfer(?,?,?) }"; CallableStatement cs =
         * conn.prepareCall(query); //cs.registerOutParameter(1, Types.INTEGER);
         *
         * cs.setLong(1, money); cs.setLong(2, s_uid); cs.setLong(3, d_uid);
         * cs.executeQuery();
         */
	
	UserEntity srcUser = getUserInfo(s_uid);
	UserEntity desUser = getUserInfo(d_uid);
	
	// update money
        updateUserMoney(money, false, s_uid, "Chuyen tien cho user : " + desUser.mUsername + "(" + d_uid + ")");
        updateUserMoney(money * 90 / 100, true, d_uid, "Nhan duoc tien tu user : " + srcUser.mUsername + "(" + s_uid + ")");

	// log this transaction
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	String date = sdf.format(new Date());
	sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
	String datetime = sdf.format(new Date());
        String query = "INSERT INTO `log_transfer_gold` (`src_uid`,`src_username`,`des_uid`,`des_username`,`money`,`date`,`datetime`,`src_money_before`,`des_money_before`)"
			+ " VALUES (?,?,?,?,?,?,?,?,?);";	
        PreparedStatement ps = conn.prepareStatement(query);
	ps.setLong(1, s_uid);
	ps.setString(2, srcUser.mUsername);
	ps.setLong(3, d_uid);
	ps.setString(4, desUser.mUsername);
	ps.setLong(5, money);
	ps.setString(6, date);
	ps.setString(7, datetime);
	ps.setLong(8, srcUser.money);
	ps.setLong(9, desUser.money);
	ps.executeUpdate();
	ps.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    //Fix

//    public static void updateUserEndGame(long id, boolean win) throws Exception {
//        System.out.println("updateUserEndGame : " + id + " : " + win);
//
//        String query = "UPDATE g_user SET playing_game=0,totalGame=totalGame+1 WHERE userid=?;";
//        if (win) {
//            query = "UPDATE g_user SET playing_game=0,totalGame=totalGame+1,totalWin=totalWin+1 WHERE userid=?;";
//        }
//
//        PreparedStatement st = conn.prepareStatement(query);
//
//        st.setLong(1, id);
//        st.executeUpdate();
//
//        st.clearParameters();
//        st.close();
//
//    }
    public static void updateUserGameStatus(long id, long status) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUserGameStatus: id = " + id + "; status = " + status);
	}

        String query = "UPDATE user SET isplaying=?,start_playing_time=now() WHERE userid=?;";

        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, status);
        st.setLong(2, id);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}

    }

//    public static void updateAllUserGameStatus(long status) throws Exception {
//
//        String query = "UPDATE user SET isplaying=? ;";
//
//        PreparedStatement st = conn.prepareStatement(query);
//        st.setLong(1, status);
//        st.executeUpdate();
//
//        st.clearParameters();
//        st.close();
//
//    }
    public static long getUserGameStatus(long aUid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserGameStatus: uid = " + aUid);
	}
	
        if (true) {
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return 0;
        }
        try {
            long res = 0;
            long minute = 0;
            String query = "SELECT isplaying, now() as current, start_playing_time FROM user WHERE userid=?;";
            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, aUid);

            ResultSet rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res = rs.getLong("isplaying");
                minute = rs.getTimestamp("current").getTime() - rs.getTimestamp("start_playing_time").getTime();
                minute = minute / (60 * 1000);
                System.out.println("playing_game: " + res + "-----minute: " + minute);
                if (res == 1 && minute >= timeWaitPlaying) {
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 0;
                }
            }

            st.clearParameters();
            st.close();
            rs.close();

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return res;
        } catch (Exception e) {
//            e.printStackTrace();
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return 0;
        }
    }

    public static long getMoneyForUpdateLevel(int level) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMoneyForUpdateLevel: level = " + level);
	}
        long res = 0;
        String query = "SELECT money FROM updatelevelmoney WHERE level=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, level);
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("money");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static long getMoneyForUpdateAvatar(int avatar) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMoneyForUpdateAvatar: avatar = " + avatar);
	}
        long res = 0;
        String query = "SELECT money FROM updateavatarmoney WHERE avatar=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, avatar);
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("money");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    //load config 1 a time
    public static void loadConfig() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - loadConfig");
	}

        //getLotery(0);

        iphoneChargeList = getAppleCharge("iphone");
        androidChargeList = getAppleCharge("android");

        loadChanCuoc();
        long res = 0;
        try {
            String query = "SELECT * FROM config";
            PreparedStatement ps = conn.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs != null && rs.first()) {
                //res = rs.getLong("UserID");
                try {
                    maxRoom = rs.getInt("maxRoom");
                } catch (Exception e) {
                }
                System.out.println("maxRoom : " + maxRoom);

                try {
                    maxChannel = rs.getInt("maxChannel");
                    System.out.println("maxChannel : " + maxChannel);
                } catch (Exception e) {
                }

                try{
                    allowIphoneCharge= rs.getInt("allowIphoneCharge") == 1;
                }catch (Exception e) {
                }
                try {                    
                    log_all_code = rs.getInt("log_all_code") == 1;
                    log_user = rs.getInt("log_user") == 1;
                    log_code = rs.getInt("log_code") == 1;
                    System.out.println("log_all_code : " + log_all_code);
                    System.out.println("log_code : " + log_code);

                } catch (Exception e) {
                }

                try {
                    enable_event_gift = rs.getInt("enable_event_gift");
                    event_login_value = rs.getInt("event_login_value");
                    event_message = rs.getString("event_message");
                    first_login_message = rs.getString("first_login_message");

                    activeSystem = rs.getInt("activeSystem") == 1;

                    eventPlayAward = rs.getInt("eventPlayAward");
                    eventChangeAward = rs.getInt("eventChangeAward");
                    eventChangeAwardMoney = rs.getInt("eventChangeAwardMoney");
                    eventAwardEnable = rs.getInt("eventAwardEnable") == 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            ps.clearParameters();
            ps.close();
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DB cp not exist!");
        }

	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void getPreConfig() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getPreConfig");
	}
//        System.out.println("helpC : " + helpContent);

        loadConfig();
        LoginResponse resLogin = new LoginResponse();
        getConfigInfo(resLogin, "0");
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static String requestCharge(long id, String s1, String s2, int type, String cp) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - requestCharge: id = " + id + "; s1 = " + s1 + "; s2 = " + s2 + "; type = " + type + "; cp = " + cp);
	}
        try {
            String res = "";
            long start = System.currentTimeMillis();

            String request = "?u=" + id + "&p=" + type + "&n=" + s1 + "&s=" + s2 + "&cp=" + cp;
            System.out.println("request: " + request);
            System.out.println("chargeUrl: " + chargeUrl);

            URL yahoo = new URL(chargeUrl + request);
            System.out.println("yahoo: " + yahoo);

            URLConnection yc = yahoo.openConnection();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    yc.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                res = res + inputLine;
            }
            System.out.println("Request time : " + (System.currentTimeMillis() - start) + " ms.");
            in.close();

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return null;
    }

    public static void getCardChargeInfo(LoginResponse resLogin) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getCardChargeInfo: resLogin.uid = " + resLogin.uid);
	}
	
        try {

            Vector<AvatarEntity> res = new Vector<AvatarEntity>();
            String query = "SELECT * FROM charge_cards WHERE enable = 1;";
            PreparedStatement st = conn.prepareStatement(query);

            ResultSet rs = st.executeQuery();

            rs.last();
            int size = rs.getRow();
            System.out.println("Found : " + size + " card data");
            resLogin.chargeCards = new ChargeCardEntity[size];

            int index = 0;
            rs.beforeFirst();
            while (rs.next()) {
                resLogin.chargeCards[index] = new ChargeCardEntity();
                System.out.println("index : " + index + "_" + resLogin.chargeCards[index].cardId);
                resLogin.chargeCards[index].cardId = rs.getInt("cardId");
                resLogin.chargeCards[index].cardName = rs.getString("subMenu");
                resLogin.chargeCards[index].len1 = rs.getInt("len1");
                resLogin.chargeCards[index].len2 = rs.getInt("len2");
                resLogin.chargeCards[index].cardMsg = rs.getString("cardMsg");
                System.out.println("Card : " + index + ": " + resLogin.chargeCards[index].cardName);
                index++;

            }

            st.clearParameters();
            st.close();
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DB cp not exist!");
        }

	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void getConfigInfo(LoginResponse resLogin, String cpID) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getConfigInfo: resLogin.uid = " + resLogin.uid + "; cp = " + cpID);
	}
        try {

            try {
                getCardChargeInfo(resLogin);
            } catch (Exception e) {
                e.printStackTrace();
            }

            long res = 0;
            try {
                if (cpID.length() == 0) {
                    cpID = "0";
                }

                String query = "SELECT * FROM cp WHERE cpID=?;";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, cpID);

                ResultSet rs = ps.executeQuery();
                if (rs != null && rs.first()) {
                    //res = rs.getLong("UserID");
                    resLogin.smsContent = rs.getString("smsContent");
                    resLogin.smsNumber = rs.getString("smsNumber");

                    resLogin.smsMessage = rs.getString("smsMessage");

                    //noticeText
                    try {
                        noticeText = rs.getString("noticeText");
                        System.out.println("noticeText : " + noticeText);
                    } catch (Exception e) {
                    }
                    try {
                        helpContent = rs.getString("helpContent");
                        System.out.println("helpContent : " + helpContent);
                    } catch (Exception e) {
                    }


                    try {
                        maxRoom = rs.getInt("maxRoom");
                        System.out.println("maxRoom : " + maxRoom);
                    } catch (Exception e) {
                    }

                    try {
                        newAllowSms = (rs.getInt("newAllowSms") == 1);
                        System.out.println("newAllowSms : " + newAllowSms);
                    } catch (Exception e) {
                    }

                    try {
                        allowSmsCharge = (rs.getInt("allowSmsCharge") == 1);
                        System.out.println("allowSmsCharge : " + allowSmsCharge);
                    } catch (Exception e) {
                    }

                    try {
                        allowCardCharge = (rs.getInt("allowCardCharge") == 1);
                        System.out.println("allowCardCharge : " + allowCardCharge);
                    } catch (Exception e) {
                    }

                    try {
                        chargeUrl = rs.getString("chargeUrl");
                        System.out.println("chargeUrl : " + chargeUrl);
                    } catch (Exception e) {
                    }

                    try {
                        resLogin.max_gift = rs.getInt("max_gift");
                        resLogin.cash_gift = rs.getInt("cash_gift");
                    } catch (Exception e) {
                    }

                    try {
                        resLogin.reset_gift_date = rs.getDate("reset_gift_time");
                        System.out.println("Re set time : " + resLogin.reset_gift_date);
                        Date d = new Date();
                        Calendar t = Calendar.getInstance();
                        t.setTime(resLogin.reset_gift_date);

                        Calendar c = Calendar.getInstance();
                        //System.out.println("t : "+t);
                        //System.out.println("c : "+c);
                        if (c.get(Calendar.DAY_OF_YEAR) != t.get(Calendar.DAY_OF_YEAR)) {
                            resetGiftDaily(resLogin.max_gift);
                        }
                        //c.get(Calendar.YEAR) + "_" + (c.get(Calendar.MONTH)+1) + "_" + c.get(Calendar.DAY_OF_MONTH);


                    } catch (Exception e) {
                        e.printStackTrace();
                        resetGiftDaily(resLogin.max_gift);
                    }

                    try {
//                        resLogin.linkDown = rs.getString("link");
                        resLogin.smsMessage2 = rs.getString("smsMessage2");
                        resLogin.smsNumber2 = rs.getString("smsNumber2");
                        resLogin.smsActive = rs.getString("smsActive");
                        resLogin.smsValue = rs.getString("smsValue");
                        resLogin.smsValue2 = rs.getString("smsValue2");
                        resLogin.smsActiveValue = rs.getString("smsvalueactive");
                    } catch (Exception e) {
                    }
                }



//                query = "update cp set helpContent=? WHERE cpID=?;";
//                ps = conn.prepareStatement(query);
//                ps.setString(2, cpID);
//                ps.setString(1, helpContentAdd + helpContent);
//                ps.execute();
                ps.clearParameters();
                ps.close();
                rs.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("DB cp not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    // Fix
    public static long getUserID(String username) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserID: username = " + username);
	}
        long res = 0;
        String query = "SELECT UserID FROM user WHERE Name=?;";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("UserID");
        }

        ps.clearParameters();
        ps.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    // Fix

    public static long getMatchID(long matchID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMatchID: matchID = " + matchID);
	}
        long res = 0;
        String query = "SELECT MatchIDAuto FROM `match` WHERE MatchID=?;";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setLong(1, matchID);
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("MatchIDAuto");
        }

        ps.clearParameters();
        ps.close();
        rs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static int getUserLevel(long aUid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserLevel: uid = " + aUid);
	}
        int res = 0;
        String query = "SELECT Level FROM userstt WHERE UserID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, aUid);

        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getInt("Level");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static boolean getUserActive(long aUid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserActive: uid = " + aUid);
	}
	
        boolean res = false;

        String query = "SELECT is_active FROM user WHERE UserID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, aUid);

        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = (rs.getLong("is_active") == 1);
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static long getUserMoney(long aUid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserMoney: uid = " + aUid);
	}
        long res = 0;
        String query = "SELECT * FROM userstt WHERE UserID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, aUid);

        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("Cash");

            int playAward = rs.getInt("playAward");
            if (playAward >= eventPlayAward && DatabaseDriver.eventAwardEnable) {
                System.out.println("Increase aWard");
                updateAward(aUid);
            }

        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static void updateLevel(long uid, long money, long cash) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateLevel: uid = " + uid + "; money = " + money + "; cash = " + cash);
	}
        /*
         * String query = "{ call UpdateLevel(?) }"; CallableStatement cs =
         * conn.prepareCall(query); cs.setLong(1, uid); cs.executeUpdate();
         */

        try {
            String query = "UPDATE userstt SET level=level+1, cash=cash-" + money + " WHERE UserID= " + uid;
            PreparedStatement st = conn.prepareStatement(query);
            st.executeUpdate();
            st.clearParameters();
            st.close();

            logVasc(-money, uid, "Mua level ", cash, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    //Fix

    public static void updateAvatar(long uid, int avatarID, long money, long cash) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateAvatar: uid = " + uid + "; avatarID = " + avatarID + "; money = " + money + "; cash = " + cash);
	}
        try {
            String query = "UPDATE userstt SET AvatarID=" + avatarID + ", cash=cash-" + money + " WHERE UserID= " + uid;
            PreparedStatement st = conn.prepareStatement(query);
            st.executeUpdate();
            st.clearParameters();
            st.close();            
            
            logVasc(-money, uid, "Mua avatar " + avatarID, uid, 2);

        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    //Fix

    public static void updatePlayAward(long id) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updatePlayAward: uid = " + id);
	}
        try {
            String query = "UPDATE userstt SET playAward=playAward+1 WHERE UserID= " + id;
            PreparedStatement st = conn.prepareStatement(query);
            st.executeUpdate();
            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void logVasc(long money, long uid, String desc, long cash, int trans_type) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logVasc: uid = " + uid + "; money = " + money + "; desc = " + desc + "; cash = " + cash + "; trans_type = " + trans_type);
	}
        try {
            String query = "INSERT into logvasc values (?,now(),?,?,?,?);";

            PreparedStatement st = conn.prepareStatement(query);

            st.setLong(1, uid);
            st.setLong(2, money);
            st.setString(3, desc);
            st.setLong(4, trans_type);
            st.setLong(5, cash);

            st.executeUpdate();

            st.clearParameters();
            st.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

//    public static long updateUserMoney(long money, long uid)
//            throws Exception {
//	
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUserMoney: uid = " + uid + "; mone = " + money);
//	}
//
//        String query = "UPDATE `userstt` SET `Cash`=`Cash`+? WHERE UserID=?;";
//        PreparedStatement st = conn.prepareStatement(query);
//        st.setLong(1, money);
//        st.setLong(2, uid);
//        st.executeUpdate();
//	st.clearParameters();
//	
//	query = "SELECT `Cash` FROM `userstt` WHERE `UserID`=?;";
//	st = conn.prepareStatement(query);
//	st.setLong(1, uid);		
//        ResultSet rs = st.executeQuery();
//	long currentCash = 0;
//        if (rs != null && rs.first()) {
//            currentCash = rs.getLong("Cash");
//        }
//        st.close();
//
//        setUserSessionMoney(uid, currentCash);
//        
//        logVasc(money, uid, desc, currentCash, 6);
//        /*
//         * try { query = "INSERT into logvasc values (?,now(),?,?,?,?);"; st =
//         * conn.prepareStatement(query); st.setLong(1, uid); st.setLong(2,
//         * money); st.setString(3, desc); st.setLong(4, 6); st.setLong(5, cash);
//         *
//         * st.executeUpdate();
//         *
//         * st.clearParameters(); st.close(); } catch (Exception e) {
//         * e.printStackTrace(); }
//         */
//
//        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//        return cash;
//    }
    
    public static long updateUserMoney(long money, boolean isWin, long uid, String desc)
            throws Exception {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUserMoney: uid = " + uid + "; mone = " + money + "; isWin = " + isWin + "; desc = " + desc);
	}

        long cash = getUserMoney(uid);
        System.out.println("current cash : " + cash);
        System.out.println("TRANFER:" + uid + "_________" + cash + "  ;  " + money + " :  " + isWin);

        if (isWin) {
            cash = cash + money;
            //cash += (long) (money * ((100 - phantram) / 100));
        } else {
            money = -money;
            cash += money;
        }
        if (cash < 0) {
            cash = 0;
        }

        System.out.println("TRANFER:" + uid + "_________" + cash);

        String query = "UPDATE userstt SET Cash=?,PlaysNumber=PlaysNumber+1,playAward=playAward+1 WHERE UserID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, cash);
        st.setLong(2, uid);
        st.executeUpdate();

        st.clearParameters();
        st.close();

        setUserSessionMoney(uid, cash);
        
        logVasc(money, uid, desc, cash, 6);
        /*
         * try { query = "INSERT into logvasc values (?,now(),?,?,?,?);"; st =
         * conn.prepareStatement(query); st.setLong(1, uid); st.setLong(2,
         * money); st.setString(3, desc); st.setLong(4, 6); st.setLong(5, cash);
         *
         * st.executeUpdate();
         *
         * st.clearParameters(); st.close(); } catch (Exception e) {
         * e.printStackTrace(); }
         */

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return cash;
    }
    //Fix

    public static boolean userIsExist(String username) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - userIsExist: username = " + username);
	}
        String query = "SELECT * FROM user WHERE Name=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setString(1, username);
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            st.clearParameters();
            st.close();
            rs.close();
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return true;
        } else {
            st.clearParameters();
            st.close();
            rs.close();
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return false;
        }
    }

    public static void changeAwardMoney(long id) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - changeAwardMoney: uid = " + id);
	}
        try {
            String query = "UPDATE userstt SET award=award-" + eventChangeAward + ",Cash=Cash+" + eventChangeAwardMoney + " WHERE UserID=" + id;
            PreparedStatement st = conn.prepareStatement(query);

            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void updateAward(long id) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateAward: uid = " + id);
	}
        try {
            String query = "UPDATE userstt SET playAward=0,award=award+1 WHERE UserID=" + id;
            PreparedStatement st = conn.prepareStatement(query);

            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static CPEntity getCPInfo(String cpID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getCPInfo: cpID = " + cpID);
	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
        try {
            CPEntity res = new CPEntity();
            String query = "SELECT * FROM cp WHERE cpID=?;";
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, cpID);

            ResultSet rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.id = rs.getInt("id");
                res.cpID = rs.getString("cpID");
		res.cpName = rs.getString("cpName");
		res.isEnabled = rs.getInt("enable") > 0;
		res.isOpenID = rs.getInt("isOpenID") > 0;
		res.isUseEmailForLogin = rs.getInt("isUseEmailForLogin") > 0;
		res.linkDownloadNewVersion = rs.getString("linkDownloadNewVersion");
//		res.isPrivateUserBase = rs.getInt("isPrivateUserBase") > 0;
		res.isOpenIDOpen = rs.getInt("isOpenIDOpen") > 0;
            } 
	    else {
                res = null;
            }

	    rs.close();
            st.clearParameters();
            st.close();
            
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return res;
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error!");
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);

            System.out.println("Reconnect succesful!");

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getCPInfo(cpID);
            //throw new Exception();
        }
        //return null;
    }
    
    public static ArrayList<CPEntity> getAllCPInfo() throws Exception {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAllCPInfo: ");
//	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
	ArrayList<CPEntity> resArr = new ArrayList<CPEntity>();
	
        try {
            String query = "SELECT * FROM cp;";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            if (rs != null) {
		while (rs.next()) {
		    CPEntity res = new CPEntity();
		    res.id = rs.getInt("id");
		    res.cpID = rs.getString("cpID");
		    res.cpName = rs.getString("cpName");
		    res.isEnabled = rs.getInt("enable") > 0;
		    res.isOpenID = rs.getInt("isOpenID") > 0;
		    res.isUseEmailForLogin = rs.getInt("isUseEmailForLogin") > 0;
		    res.linkDownloadNewVersion = rs.getString("linkDownloadNewVersion");
		    resArr.add(res);
    //		res.isPrivateUserBase = rs.getInt("isPrivateUserBase") > 0;
		}
            } 

	    rs.close();
            st.clearParameters();
            st.close();
            
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return resArr;
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error com.mysql.jdbc.exceptions.jdbc4.CommunicationsException!, message: " + e.getMessage());
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect succesful!");
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getAllCPInfo();
            //throw new Exception();
        }
        //return null;
    }
    
    public static HashSet<Long> getAllBetsValue() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAllBetsValue: ");
	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
	
	HashSet<Long> resSet = new HashSet<Long>();
	
        try {
            String query = "SELECT `MinBook` FROM `roomtype` UNION SELECT `MinBook` FROM `roomtype_poker`;";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            if (rs != null) {
		while (rs.next()) {
		    Long l = rs.getLong("MinBook");
		    resSet.add(l);
    //		res.isPrivateUserBase = rs.getInt("isPrivateUserBase") > 0;
		}
            } 

	    rs.close();
            st.clearParameters();
            st.close();
            
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return resSet;
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error com.mysql.jdbc.exceptions.jdbc4.CommunicationsException!, message: " + e.getMessage());
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect succesful!");
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getAllBetsValue();
            //throw new Exception();
        }
        //return null;
    }
    
    public static UserEntity getUserInfo(String username) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserInfo: username = " + username);
	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
        try {
            UserEntity res = new UserEntity();
            String query = "SELECT * FROM user WHERE Name=?;";
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, username);

            ResultSet rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.mPassword = rs.getString("Password");
                res.mUid = rs.getLong("UserID");
                res.mUsername = rs.getString("Name");
                res.mAge = rs.getInt("Age");
		res.cp = rs.getString("cp");
                try {

                    res.lastLogin = rs.getDate("LastTime");
                    res.isActive = (rs.getInt("is_active") == 1);
                    res.isBanned = (rs.getInt("is_active") == 99);

                    res.receive_gift = rs.getInt("receive_gift");
                    res.remain_gift = rs.getInt("remain_gift");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                res.mIsMale = (rs.getInt("Sex") == 1);

                try {
                    res.lastMatch = rs.getLong("lastMatch");
                    System.out.println("lastMatch : " + res.lastMatch);
                } catch (Exception e) {
                }

            } else {
                st.clearParameters();
                st.close();
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return null;
            }

            st.clearParameters();
            st.close();
            rs.close();

            query = "SELECT * FROM userstt WHERE UserID=?;";
            st = conn.prepareStatement(query);
            st.clearParameters();
            st.setDouble(1, res.mUid);
            rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.award = rs.getInt("award");
                res.avatarID = rs.getInt("AvatarID");
                res.level = rs.getInt("Level");
                res.money = rs.getLong("Cash");
                res.playsNumber = rs.getInt("PlaysNumber");

                int playAward = rs.getInt("playAward");
                if (playAward >= eventPlayAward && DatabaseDriver.eventAwardEnable) {
                    System.out.println("Increase aWard");
                    updateAward(res.mUid);
                }

            }

            st.clearParameters();
            st.close();
            rs.close();

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return res;
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error!");
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);

            System.out.println("Reconnect succesful!");

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getUserInfo(username);
            //throw new Exception();
        }
        //return null;
    }
    
    public static UserEntity getUserInfo(String username, CPEntity cp) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserInfo: username = " + username + "; cpID = " + cp.cpID);
	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
        try {
            UserEntity res = new UserEntity();
	    String query;
	    PreparedStatement st;
	    if (cp.isUseEmailForLogin) {
		query = "SELECT * FROM user WHERE Email=? AND cp=?;";
		st = conn.prepareStatement(query);
		st.setString(1, username);
		st.setString(2, cp.cpID);
	    }
	    else {
		String usernameFix = username + "@" + cp.cpID;
		query = "SELECT * FROM user WHERE Name=? AND cp=?;";
		st = conn.prepareStatement(query);
		st.setString(1, usernameFix);
		st.setString(2, cp.cpID);
	    }

            ResultSet rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.mPassword = rs.getString("Password");
                res.mUid = rs.getLong("UserID");
                res.mUsername = rs.getString("Name");
                res.mAge = rs.getInt("Age");
		res.cp = rs.getString("cp");
                try {

                    res.lastLogin = rs.getDate("LastTime");
                    res.isActive = (rs.getInt("is_active") == 1);
                    res.isBanned = (rs.getInt("is_active") == 99);

                    res.receive_gift = rs.getInt("receive_gift");
                    res.remain_gift = rs.getInt("remain_gift");
		    
		    res.cp_user_id = rs.getInt("cp_user_id");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                res.mIsMale = (rs.getInt("Sex") == 1);

                try {
                    res.lastMatch = rs.getLong("lastMatch");
                    System.out.println("lastMatch : " + res.lastMatch);
                } catch (Exception e) {
                }

            } else {
                st.clearParameters();
                st.close();
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return null;
            }

            st.clearParameters();
            st.close();
            rs.close();

            query = "SELECT * FROM userstt WHERE UserID=?;";
            st = conn.prepareStatement(query);
            st.clearParameters();
            st.setDouble(1, res.mUid);
            rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.award = rs.getInt("award");
                res.avatarID = rs.getInt("AvatarID");
                res.level = rs.getInt("Level");
                res.money = rs.getLong("Cash");
                res.playsNumber = rs.getInt("PlaysNumber");

                int playAward = rs.getInt("playAward");
                if (playAward >= eventPlayAward && DatabaseDriver.eventAwardEnable) {
                    System.out.println("Increase aWard");
                    updateAward(res.mUid);
                }

            }

            st.clearParameters();
            st.close();
            rs.close();

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return res;
        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error!");
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);

            System.out.println("Reconnect succesful!");

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getUserInfo(username, cp);
            //throw new Exception();
        }
        //return null;
    }
    

    public static String getTuocvi() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getTuocvi");
	}
	
        try {
            String query = "SELECT * FROM level;";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            query = "";
            while (rs.next()) {
                query = query + "+" + rs.getInt("LevelID") + "_" + rs.getString("Description");
            }
            query = query.substring(1);

            st.clearParameters();
            st.close();
            rs.close();


            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return query;
        } catch (Exception e) {
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return "";
        }
    }

    public static UserEntity getUserInfo(long uid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserInfo: uid = " + uid);
	}
	
        try {

            UserEntity res = new UserEntity();
            res.mUid = uid;
            String query = "SELECT * FROM user WHERE UserID=?;";
            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, uid);
            ResultSet rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.mPassword = rs.getString("Password");
                res.mUsername = rs.getString("Name");
                res.mAge = rs.getInt("Age");
                try {
                    res.lastLogin = rs.getDate("LastTime");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                res.mIsMale = (rs.getInt("Sex") == 1);

                try {
                    res.lastMatch = rs.getLong("lastMatch");
                    System.out.println("lastMatch : " + res.lastMatch);
                } catch (Exception e) {
                }

            } else {
                st.clearParameters();
                st.close();
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return null;
            }

            System.out.println("getUserInfo : " + uid + "  ; " + res.mUsername);

            st.clearParameters();
            st.close();
            rs.close();

            query = "SELECT * FROM userstt WHERE UserID=?;";
            st = conn.prepareStatement(query);
            st.clearParameters();
            st.setLong(1, uid);
            rs = st.executeQuery();
            if (rs != null && rs.first()) {
                res.avatarID = rs.getInt("AvatarID");
                res.level = rs.getInt("Level");
                res.money = rs.getLong("Cash");
                res.playsNumber = rs.getInt("PlaysNumber");
                res.award = rs.getInt("award");

                int playAward = rs.getInt("playAward");
                System.out.println("playAward : " + playAward);
                System.out.println("eventPlayAward : " + eventPlayAward);

                if (playAward >= eventPlayAward && DatabaseDriver.eventAwardEnable) {
                    System.out.println("Increase aWard");
                    updateAward(res.mUid);
                }

            }

            st.clearParameters();
            st.close();
            rs.close();

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return res;

        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);

            if (update_download) {
                conn_download.close();
                conn_download = DriverManager.getConnection(url_download, db_username, db_password);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return null;
    }
    /*
     * public static void updateLoginStatus(IConnection aConn, long uid, int
     * status) throws DBException { String query = "UPDATE user SET isLogin=?
     * WHERE UserID=?;"; IStatement st = aConn.prepareStatement(query);
     * st.setInt(1, status); st.setLong(2, uid); st.executeUpdate(); }
     */
    //Fix

    public static long getLastRoom(long uid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getLastRoom: uid = " + uid);
	}

        String query = "select * from `matchuser` where `MatchIDAuto`=(select max(`MatchIDAuto`) from matchuser where UserID = " + uid + ")";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        long res = -1;
        if (rs != null && rs.first()) {
            res = rs.getLong("MatchIDAuto");
        }
        ps.clearParameters();
        ps.close();
        rs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static void updateLastTimeLogin(long uid, String ip, boolean mobile, String device, String screen, String currentTime) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateLastTimeLogin: uid = " + uid + "; ip = " + ip + "; mobile = " + mobile + "; device = " + device + "; screen = " + screen);
	}
	
        try {
            int value = 0;
            if (mobile) {
                value = 1;
            }
            if (ip.contains(":")) {
                ip = ip.substring(0, ip.indexOf(":"));
            }
            ip = ip.replace("/", "");
            String query = "UPDATE user SET LastTime='" + currentTime + "',isOnline=1,ip='" + ip + "',isMobile=" + value + ",device=?,screen=?  WHERE UserID=?;";

//            String query = "UPDATE user SET LastTime=now(),isOnline=1,ip='" + ip + "',isMobile=" + value + ",device=?,screen=?  WHERE UserID=?;";
            System.out.println("query : " + query);
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, device);
            st.setString(2, screen);
            st.setLong(3, uid);
            st.executeUpdate();

            st.clearParameters();
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    //Fix

    public static void loadChanCuoc() {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - loadChanCuoc");
	}
        try {
            String query = "SELECT * FROM chan_table ";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                CuocEntity jo = new CuocEntity();
                jo.id = rs.getInt("id");
                jo.name = rs.getString("name");
                jo.description = rs.getString("description");
                jo.point = rs.getInt("point");
                jo.dich = rs.getInt("dich");
                System.out.println(jo.name + "," + jo.point + "," + jo.dich);
                cuocList.add(jo);
            }
            st.clearParameters();
            st.close();
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DB Chan Cuoc not exist!");
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static JSONArray getAppleCharge(String type) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAppleCharge" + "; type = " + type);
	}
	
        try {
            JSONArray ja = new JSONArray();
            String query = "SELECT * FROM `iphoneCharge` where `clientType`=?";
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, type);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                JSONObject jo = new JSONObject();
                jo.put("id", rs.getInt("id"));
                jo.put("osMoney", rs.getInt("osMoney"));
                jo.put("gameMoney", rs.getInt("gameMoney"));
                ja.put(jo);
            }

            st.clearParameters();
            st.close();
            rs.close();
            System.out.println(ja);

            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return ja;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DB cp not exist!");
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return null;
    }

    public static void removeFriend(long currUID, long friendUID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - removeFriend" + "; currUID = " + currUID + "; friendUID = " + friendUID);
	}
        Vector<UserEntity> friends = getFastFrientList(currUID);
        String text = "";

        for (int i = 0; i < friends.size(); i++) {
            UserEntity friend = friends.get(i);
            if (friend.mUid != friendUID) {
                text += friend.mUid + ",";
            }
        }
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }

        String query = "UPDATE users SET UserIDs='" + text + "' WHERE UserID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, currUID);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    //Fix

    public static void addFriend(long currUID, long friendUID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - addFriend" + "; currUID = " + currUID + "; friendUID = " + friendUID);
	}
        String query = "SELECT * FROM users WHERE UserID=?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, currUID);
        ResultSet rs = st.executeQuery();
        String text = "";
        if (rs != null) {
            if (rs.first()) {
		String userIDStr = rs.getString("UserIDs");
		userIDStr = userIDStr.equals("") ? "" : userIDStr + ",";
                text = userIDStr + friendUID;
                query = "UPDATE users SET UserIDs='" + text + "' WHERE UserID=?;";
                st = conn.prepareStatement(query);
                st.clearParameters();
                st.setLong(1, currUID);
                st.executeUpdate();
            } else {
                query = "INSERT INTO users values(?,?);";
                text = "" + friendUID;
                st = conn.prepareStatement(query);
                st.clearParameters();
                st.setLong(1, currUID);
                st.setString(2, text);
                st.executeUpdate();
            }

        } else {
            query = "INSERT INTO users values(?,?);";
            text = "" + friendUID;
            st = conn.prepareStatement(query);
            st.clearParameters();
            st.setLong(1, currUID);
            st.setString(2, text);
            st.executeUpdate();
        }

        st.clearParameters();
        st.close();
        rs.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    //Fix

    public static Vector<UserEntity> getFrientList(long aid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getFrientList" + "; aid = " + aid);
	}
        Vector<UserEntity> users = new Vector<UserEntity>();

        String query = "SELECT UserIDs FROM users WHERE UserID=?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, aid);

        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            String text = rs.getString("UserIDs");
            ArrayList<Long> temp = friendList(text);
            for (long id : temp) {
                UserEntity user = getUserInfo(id);
                if (user != null) {
                    users.add(user);
                }
            }
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return users;
    }

    public static Vector<UserEntity> getFastFrientList(long aid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getFastFrientList" + "; aid = " + aid);
	}
        Vector<UserEntity> users = new Vector<UserEntity>();

        String query = "SELECT UserIDs FROM users WHERE UserID=?";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, aid);

        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            String text = rs.getString("UserIDs");

            ArrayList<Long> temp = friendList(text);
            for (long id : temp) {
                //UserEntity user = getUserInfo(id);
                UserEntity user = new UserEntity();
                user.mUid = id;
                if (user != null) {
                    users.add(user);
                }

            }
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return users;
    }

    private static ArrayList<Long> friendList(String input) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - friendList" + "; input = " + input);
	}
        ArrayList<Long> res = new ArrayList<Long>();
        String[] temp = input.split(",");
        for (String s : temp) {
            try {
                long l = Long.parseLong(s);
                res.add(l);
            } catch (Exception e) {
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static boolean isFriend(long source_uid, long uid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - isFriend" + "; source_uid = " + source_uid + "; uid = " + uid);
	}
        boolean res = false;
        Vector<UserEntity> friends = getFastFrientList(source_uid);
        for (UserEntity u : friends) {
            if (u.mUid == uid) {
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return true;
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static long getUserMoney(String username) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserMoney" + "; username = " + username);
	}
        long res = 0;
        long uid = 0;
        String query = "SELECT UserID, Name, Password FROM user WHERE Name=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setString(1, username);

        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            uid = rs.getLong("UserID");
        }

        st.clearParameters();
        st.close();
        rs.close();

        query = "SELECT * FROM userstt WHERE UserID=?";
        st = conn.prepareStatement(query);
        st.clearParameters();
        st.setLong(1, uid);

        rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("Cash");

            int playAward = rs.getInt("playAward");
            if (playAward >= eventPlayAward && DatabaseDriver.eventAwardEnable) {
                System.out.println("Increase aWard");
                updateAward(uid);
            }
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static Vector<AvatarEntity> getAvatarList() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAvatarList");
	}
        Vector<AvatarEntity> res = new Vector<AvatarEntity>();
        String query = "SELECT * FROM avatar;";
        PreparedStatement st = conn.prepareStatement(query);

        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("AvatarID");
            String desc = rs.getString("Description");
            long money = DatabaseDriver.getMoneyForUpdateAvatar(id);
            AvatarEntity av = new AvatarEntity(id, desc, money);
            res.add(av);
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static Vector<ChargeHistoryEntity> getChargeHistory(long uid, int start, int len) throws Exception {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getChargeHistory" + "; uid = " + uid + "; start = " + start + "; len = " + len);
	}
        Vector<ChargeHistoryEntity> res = new Vector<ChargeHistoryEntity>();
        String query = "SELECT * FROM charge_report where user_id= " + uid + " ORDER BY datetime DESC LIMIT " + start + "," + len + "  ; ";
        System.out.println("query : " + query);

        PreparedStatement st = conn.prepareStatement(query);

        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            String s1 = rs.getString("phone_number");
            String s2 = rs.getString("datetime");
            int money = rs.getInt("money");

            ChargeHistoryEntity av = new ChargeHistoryEntity(s1, s2, money);
            res.add(av);
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }
    //Fix

    public static long logMatch(long ownerUID, long matchID, int roomType, String desc)
            throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logMatch" + "; ownerUID = " + ownerUID + "; matchID = " + matchID + "; roomType = " + roomType + "; desc = " + desc);
	}
//        System.out.println("Log match : " + matchID);
        //added by tuanha
        //if (true) return 5;
        try {
            String query = "{ call LogMatch(?,?,?,?) }";
            CallableStatement cs = conn.prepareCall(query);
            cs.clearParameters();

            cs.setLong(1, matchID);
            cs.setLong(2, ownerUID);
            cs.setInt(3, roomType);
            cs.setString(4, desc);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                try {
                    long l = rs.getLong("MatchIDAuto");
                    cs.clearParameters();
                    cs.close();
                    rs.close();

                    System.out.println("new Match Id : " + l);

                    if (l > 0) {
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return l;
                    } else {
                        System.out.println("Problem error!");
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return getMatchID(matchID);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Fail to log match!");
            //e.printStackTrace();

            String query = "INSERT INTO `match` (MatchID,OwnerMatchUserId,RoomTypeID,Description,DateTime) values (?,?,?,?,NOW()); ";
            CallableStatement cs = conn.prepareCall(query);
            cs.clearParameters();

            cs.setLong(1, matchID);
            cs.setLong(2, ownerUID);
            cs.setInt(3, roomType);
            cs.setString(4, desc);

            //cs.executeUpdate();
            cs.executeUpdate();

            cs.clearParameters();
            cs.close();
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getMatchID(matchID);
        }


        System.out.println("Fail to create room Id.");
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 0;
    }
    // Fix

    public static long newlogMatch(long ownerUID, long matchID, int roomType, String desc)
            throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - newlogMatch" + "; ownerUID = " + ownerUID + "; matchID = " + matchID + "; roomType = " + roomType + "; desc = " + desc);
	}
//        System.out.println("Log match : " + matchID);
        //added by tuanha
        //if (true) return 5;

        System.out.println("Fail to log match!");
        //e.printStackTrace();

        String query = "INSERT INTO `match` (MatchID,OwnerMatchUserId,RoomTypeID,Description,DateTime) values (?,?,?,?,NOW()); ";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();

        cs.setLong(1, matchID);
        cs.setLong(2, ownerUID);
        cs.setInt(3, roomType);
        cs.setString(4, desc);

        //cs.executeUpdate();
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return getMatchID(matchID);

    }

    public static void logUserMatch(long uid, long matchID, String desc,
            long money, boolean isWin, long matchIDAuto) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logUserMatch" + "; uid = " + uid + "; matchID = " + matchID + "; desc = " + desc + "; money = " + money + "; isWin = " + isWin + "; matchIDAuto = " + matchIDAuto);
	}
//        System.out.println("logUserMatch : " + uid);
        int win = 0;
        if (isWin) {
            win = 1;
        }
        String query = "{ call LogMatchuser(?,?,?,?,?,?,?) }";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();

        cs.setLong(1, uid);
        cs.setLong(2, matchID);
        cs.setInt(3, win);
        cs.setLong(4, money);
        cs.setString(5, desc);
        cs.setInt(6, phantram);
        cs.setLong(7, matchIDAuto);
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }
    // Fix

    public static void logUserVASC(long uid, String desc, long money, int logType)
            throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logUserVASC" + "; uid = " + uid + "; desc = " + desc + "; money = " + money + "; logType = " + logType);
	}
        String query = "{ call LogVASC(?,?,?,?) }";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();

        cs.setLong(1, uid);
        cs.setLong(2, money);
        cs.setString(3, desc);
        cs.setInt(4, logType);

        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static long getMoneyForRoom(int roomType)
            throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMoneyForRoom" + "; roomType = " + roomType);
	}
        long res = 0;

        String query = "SELECT MinBook FROM roomtype where RoomTypeID=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, roomType);
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("MinBook");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;

    }
    
    public static long getMoneyForRoomByMoneyAndChannelPoker(long money, int channel) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMoneyForRoomByMoneyAndChannelPoker" + "; money = " + money + "; channel = " + channel);
	}
        long res = 0;

        String query = "SELECT MinBook FROM `roomtype_poker` where MinBook=? AND channel=?;";
        PreparedStatement st = conn.prepareStatement(query);
	st.setLong(1, money);
        st.setInt(2, channel);	
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("MinBook");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	
        return res;
    }
	    
    public static long getMoneyForRoomByMoneyAndChannel(long money, int channel) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMoneyForRoomByMoneyAndChannel" + "; money = " + money + "; channel = " + channel);
	}
        long res = 0;

        String query = "SELECT MinBook FROM roomtype where MinBook=? AND channel=?;";
        PreparedStatement st = conn.prepareStatement(query);
	st.setLong(1, money);
        st.setInt(2, channel);	
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getLong("MinBook");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	
        return res;
    }
    
    //Fix

    public static Vector<UserEntity> getRichests() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getRichests");
	}
        Vector<UserEntity> res = new Vector<UserEntity>();
        String query = "{ call TopRich() }";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            UserEntity user = new UserEntity();
            long uid = rs.getLong("UserID");
            user = getUserInfo(uid);
            if (user != null) {
                res.add(user);
            }
        }

        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static Vector<UserEntity> getBestPlayer() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getBestPlayer");
	}
        Vector<UserEntity> res = new Vector<UserEntity>();
        String query = "{ call TopLevel() }";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            UserEntity user = new UserEntity();
            long uid = rs.getLong("UserID");
            user = getUserInfo(uid);
            if (user != null) {
                res.add(user);
            }
        }

        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static Vector<UserEntity> getMostPlaying() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMostPlaying");
	}
        Vector<UserEntity> res = new Vector<UserEntity>();
        String query = "{ call TopPlaysNumber() }";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();
        ResultSet rs = cs.executeQuery();
        while (rs.next()) {
            UserEntity user = new UserEntity();
            long uid = rs.getLong("UserID");
            user = getUserInfo(uid);
            if (user != null) {
                res.add(user);
            }
        }

        rs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static boolean matchIsExist(long matchID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - matchIsExist" + "; matchID = " + matchID);
	}
        String query = "{ call FindMatch(?) }";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();
        cs.setLong(1, matchID);
        ResultSet rs = cs.executeQuery();
        if (rs != null && rs.first()) {
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return true;
        }

        rs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return false;
    }

    public static Hashtable<Integer, Long>[] getRoomMoneyList() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getRoomMoneyList");
	}
	
        Hashtable<Integer, Long>[] res = new Hashtable[4];
	for (int i = 0; i < 4; i++) {
	    res[i] = new Hashtable<Integer, Long>();
	}
	
        String query = "SELECT * FROM roomtype;";
        PreparedStatement st = conn.prepareStatement(query);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int roomType = rs.getInt("RoomTypeID");
            long money = rs.getLong("MinBook");
	    int channel = rs.getInt("channel");
            res[channel - 1].put(roomType, money);
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }    
    
    public static Hashtable<Integer, Long>[] getRoomPokerMoneyList() throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getRoomPokerMoneyList");
	}
	
        Hashtable<Integer, Long>[] res = new Hashtable[4];
	for (int i = 0; i < 4; i++) {
	    res[i] = new Hashtable<Integer, Long>();
	}
	
        String query = "SELECT * FROM roomtype_poker;";
        PreparedStatement st = conn.prepareStatement(query);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            int roomType = rs.getInt("RoomTypeID");
            long money = rs.getLong("MinBook");
	    int channel = rs.getInt("channel");
            res[channel - 1].put(roomType, money);
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static void insertSuggestion(long uid, String note) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - insertSuggestion" + "; uid = " + uid + "; note = " + note);
	}
        String query = "INSERT INTO suggestion VALUES(?,now(),?);";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, uid);
        st.setString(2, note);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static Vector<PostEntity> getPostList(int start, int len) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getPostList" + "; start = " + start + "; len = " + len);
	}
        Vector<PostEntity> res = new Vector<PostEntity>();
        
	//quanghv: edit 12/12/2013
	//String query = "SELECT * FROM posts ORDER BY IDPosts DESC LIMIT ?,?;";
        //PreparedStatement st = conn.prepareStatement(query);
	//st.setInt(1, start);
        //st.setInt(2, len);
        //ResultSet rs = st.executeQuery();
	String query = "{ call sv_GetPost() }";
	CallableStatement cs = conn.prepareCall(query);
	cs.clearParameters();
	ResultSet rs = cs.executeQuery();
	// end edit
        
        while (rs.next()) {
            try {
                PostEntity v = new PostEntity();
//                System.out.println(rs.getString("Username") + ": " + getUserInfo(rs.getString("Username")).avatarID);
                v.avatarID = getUserInfo(rs.getString("Username")).avatarID;
                v.postID = rs.getInt("IDPosts");
                v.content = rs.getString("Content");
                v.postDate = rs.getLong("Date");
                v.title = rs.getString("Username");
                v.isNewComment = rs.getInt("isNewComment");
                res.add(v);
            } catch (Exception eee) {
                eee.printStackTrace();
            }


        }

        //st.clearParameters();
        //st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static Vector<PostEntity> getCommentList(long uid, int postID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getCommentList" + "; uid = " + uid + "; postID = " + postID);
	}
        Vector<PostEntity> res = new Vector<PostEntity>();
        String query = "SELECT * FROM posts WHERE IDPosts =?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, postID);
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            PostEntity v = new PostEntity();
            v.avatarID = getUserInfo(rs.getString("Username")).avatarID;
            v.postID = rs.getInt("IDPosts");
            v.content = rs.getString("Content");
            v.postDate = rs.getLong("Date");
            v.title = rs.getString("Username");
            res.add(v);
        }

        st.clearParameters();
        st.close();
        rs.close();

        query = "SELECT * FROM comments WHERE IDPosts =?;";
        st = conn.prepareStatement(query);
        st.setInt(1, postID);
        rs = st.executeQuery();

        while (rs.next()) {
            PostEntity v = new PostEntity();
            v.avatarID = getUserInfo(rs.getString("Username")).avatarID;
            v.postID = rs.getInt("IDPosts");
            v.content = rs.getString("Comment");
            v.postDate = rs.getLong("Date");
            v.title = rs.getString("Username");
            res.add(v);
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static void insertPost(String username, String note) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - insertPost" + "; username = " + username + "; note = " + note);
	}
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis();
        String query = "INSERT INTO posts(Content,Date,Username) VALUES(?,?,?);";
        PreparedStatement st = conn.prepareStatement(query);
        st.setString(1, note);
        st.setLong(2, time);
        st.setString(3, username);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void insertComment(int idPost, String userName, String comment) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - insertComment" + "; idPost = " + idPost + "; userName = " + userName + "; comment = " + comment);
	}
        String query = "INSERT INTO comments(IDPosts,Username,Comment,Date) VALUES(?,?,?,?);";
        PreparedStatement st = conn.prepareStatement(query);
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis();

        st.setInt(1, idPost);
        st.setString(2, userName);
        st.setString(3, comment);
        st.setLong(4, time);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}

    }

    /*
     * public static void updateUserCash(long id, int cash) throws Exception {
     *
     * String query = "UPDATE userstt SET Cash=Cash + ? WHERE UserID=?;";
     *
     * PreparedStatement st = conn.prepareStatement(query); st.setInt(1, cash);
     * st.setLong(2, id); st.executeUpdate();
     *
     * st.clearParameters(); st.close();
     *
     * }
     */
    public static void updateUserLastMatch(long id, long matchID) throws Exception {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateUserLastMatch" + "; id = " + id + "; matchID = " + matchID);
	}
	
        String query = "UPDATE user SET lastMatch=? WHERE UserID=?;";

        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, matchID);
        st.setLong(2, id);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void activateUser(long id) throws Exception {

	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - activateUser" + "; id = " + id);
	}
        String query = "UPDATE user SET is_active=1 WHERE UserID=?;";

        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, id);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}

    }

    public static void updateNewComment(int idPost, int status) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateNewComment" + "; idPost = " + idPost + "; status = " + status);
	}
        String query = "UPDATE posts SET isNewComment=? WHERE IDPosts=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, status);
        st.setInt(2, idPost);
        st.executeUpdate();

        st.clearParameters();
        st.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static String getUserNameByPost(int idPost) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getUserNameByPost" + "; idPost = " + idPost);
	}
        String query = "SELECT Username FROM posts  WHERE IDPosts=?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setInt(1, idPost);
        String res = "undefined";
        ResultSet rs = st.executeQuery();
        if (rs != null && rs.first()) {
            res = rs.getString("Username");
        }

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    public static void insertOfflineMess(long uid, long desID, String mess) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - insertOfflineMess" + "; uid = " + uid + "; desID = " + desID + "; mess = " + mess);
	}
        String query = "insert into offlinemessage (userIDSend,userIDReceive,mes,datetimeSend) values (?,?,?,now());";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();
        cs.setLong(1, uid);
        cs.setLong(2, desID);
        cs.setString(3, mess);
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static void deleteAllMessByDesID(long desID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - deleteAllMessByDesID" + "; desID = " + desID);
	}
        String query = "DELETE FROM offlinemessage WHERE userIDReceive=?;";
        CallableStatement cs = conn.prepareCall(query);
        cs.clearParameters();
        cs.setLong(1, desID);
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
    }

    public static JSONArray getLotery(int date) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getLotery" + "; date = " + date);
	}
        //0=today,1=yesterday...
        String lottype[] = {"GDB", "G1", "G2", "G3", "G4", "G5", "G6", "G7"};
//        System.out.println("*********Get Lotery***************");
        try {
            String query = "SELECT * from lottery  WHERE DATE(insertDate)= DATE_ADD(CURDATE(),INTERVAL - " + date + " DAY);";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            JSONArray ja = null;
            if (rs.first()) {
                ja = new JSONArray();
                for (int i = 0; i < lottype.length; i++) {
                    JSONObject jo = new JSONObject();
                    jo.put("name", lottype[i]);
                    jo.put("val", rs.getString(lottype[i]));
                    System.out.println("value : " + lottype[i] + " : " + rs.getString(lottype[i]));
                    ja.put(jo);
                }
            }
            rs.close();
            st.close();
            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return ja;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return null;
    }

    public static Vector<MessageOfflineEntity> getMessageOffline(long desID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getMessageOffline" + "; desID = " + desID);
	}
        Vector<MessageOfflineEntity> res = new Vector<MessageOfflineEntity>();
        String query = "SELECT * FROM offlinemessage WHERE userIDReceive =?;";
        PreparedStatement st = conn.prepareStatement(query);
        st.setLong(1, desID);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            MessageOfflineEntity v = new MessageOfflineEntity();
            v.sendID = rs.getLong("userIDSend");
            v.datetime = rs.getDate("datetimeSend");
            v.mess = rs.getString("mes");
            v.sendName = getUserInfo(v.sendID).mUsername;
            res.add(v);
        }
        deleteAllMessByDesID(desID);

        st.clearParameters();
        st.close();
        rs.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return res;
    }

    //Thomc
    public static ArrayList<Long> getFriendsSession(long aid) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getFriendsSession" + "; aid = " + aid);
	}
        ArrayList<Long> list = new ArrayList();
        Vector<UserEntity> users = new Vector<UserEntity>();
        try {
            String query = "SELECT UserIDs FROM users WHERE UserID=?";
            PreparedStatement st = conn.prepareStatement(query);
            st.setLong(1, aid);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String text = rs.getString("UserIDs");
                list = friendList(text);
            }

            st.clearParameters();
            st.close();
            rs.close();
        } catch (Exception e) {
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return list;
    }
    
    //Trungnm
    // save statistic data to db
    public static void logStatisticToDB (HashMap map, int[] gameToStatistic, int noOfRoom, int maxNoOfPlayersPerTable) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logStatisticToDB");
	}
	
	//get current date, time, hour, halfHour, tenMinute to insert to DB
	String date, time;
	int hour, halfHour, tenMinute, minute;
		
	Date now = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	date = sdf.format(now);
	sdf.applyPattern("HH:mm");
	time = sdf.format(now);
	sdf.applyPattern("H");
	hour = Integer.parseInt(sdf.format(now));
	sdf.applyPattern("m");
	minute = Integer.parseInt(sdf.format(now));
	halfHour = minute <= 30 ? 30 : 60;
	tenMinute = minute == 0 ? 60 : ((int)(minute / 10) + 1) * 10;
	
	String query;
	PreparedStatement st = null;
	int insertedRows;
	int[] insertedRowsArr;
	
	// countTotal
	long total = (Long) map.get("total");
	long totalIOS = (Long) map.get("totalIOS");
	long totalAndroid = (Long) map.get("totalAndroid");
	long totalJ2ME = (Long) map.get("totalJ2ME");
	long totalFlash = (Long) map.get("totalFlash");
	long totalFacebook = (Long) map.get("totalFacebook");
	long totalYahoo = (Long) map.get("totalYahoo");
	long totalGoogle = (Long) map.get("totalGoogle");
	long totalTheGioiBai = (Long) map.get("totalTheGioiBai");
	
	try {
	    if (conn.isClosed()) {
		conn = DriverManager.getConnection(url, db_username, db_password);
	    }
	    // date, time, hour, halfHour, tenMinute, total, totalIOS, totalAndroid, totalJ2ME, totalFlash, totalFacebook, totalYahoo, totalGoogle, totalTheGioiBai
	    query = "INSERT INTO `countTotal`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`total`,`totalIOS`,`totalAndroid`,`totalJ2ME`,`totalFlash`,`totalFacebook`,`totalYahoo`,`totalGoogle`,`totalTheGioiBai`)"
			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    st.setString(1, date);
	    st.setString(2, time);
	    st.setInt(3, hour);
	    st.setInt(4, halfHour);
	    st.setInt(5, tenMinute);
	    st.setLong(6, total);
	    st.setLong(7, totalIOS);
	    st.setLong(8, totalAndroid);
	    st.setLong(9, totalJ2ME);
	    st.setLong(10, totalFlash);
	    st.setLong(11, totalFacebook);
	    st.setLong(12, totalYahoo);
	    st.setLong(13, totalGoogle);
	    st.setLong(14, totalTheGioiBai);
	    
	    insertedRows = st.executeUpdate();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countGame
	int[] countGame = (int[]) map.get("countGame");
	try {
	    // date, time, hour, halfHour, tenMinute, gameZoneID, count
	    query = "INSERT INTO `countGame`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`gameZoneID`,`count`)"
			+ " VALUES (?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    for (int i = 0; i < gameToStatistic.length; i ++) {
		st.setString(1, date);
		st.setString(2, time);
		st.setInt(3, hour);
		st.setInt(4, halfHour);
		st.setInt(5, tenMinute);
		st.setInt(6, gameToStatistic[i]);
		st.setInt(7, countGame[gameToStatistic[i]]);
		st.addBatch();
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countRoom
	int[][] countRoom = (int[][]) map.get("countRoom");
	try {
	    // date, time, hour, halfHour, tenMinute, gameZoneID, room, count
	    query = "INSERT INTO `countRoom`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`gameZoneID`,`room`,`count`)"
			+ " VALUES (?,?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    for (int i = 0; i < gameToStatistic.length; i ++) {
		for (int j = 0; j < noOfRoom + 1; j++) {
		    st.setString(1, date);
		    st.setString(2, time);
		    st.setInt(3, hour);
		    st.setInt(4, halfHour);
		    st.setInt(5, tenMinute);
		    st.setInt(6, gameToStatistic[i]);
		    st.setInt(7, j);
		    st.setInt(8, countRoom[gameToStatistic[i]][j]);
		    st.addBatch();
		}
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countCP
	HashMap countCP = (HashMap) map.get("countCP");
	try {
	    // date, time, hour, halfHour, tenMinute, cpID, count
	    query = "INSERT INTO `countCP`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`cpID`,`count`)"
			+ " VALUES (?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    Set<String> keySet = countCP.keySet();
	    for (String cpID : keySet) {
		long count = (Long) countCP.get(cpID);
		st.setString(1, date);
		st.setString(2, time);
		st.setInt(3, hour);
		st.setInt(4, halfHour);
		st.setInt(5, tenMinute);
		st.setString(6, cpID);
		st.setLong(7, count);
		st.addBatch();
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countMinBet
	HashMap countMinBet = (HashMap) map.get("countMinBet");
	try {
	    // date, time, hour, halfHour, tenMinute, minBet, count
	    query = "INSERT INTO `countMinBet`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`minBet`,`count`)"
			+ " VALUES (?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    Set<Long> keySet = countMinBet.keySet();
	    for (Long minBet : keySet) {
		long count = (Long) countMinBet.get(minBet);
		st.setString(1, date);
		st.setString(2, time);
		st.setInt(3, hour);
		st.setInt(4, halfHour);
		st.setInt(5, tenMinute);
		st.setString(6, String.valueOf(minBet));
		st.setLong(7, count);
		st.addBatch();
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countMinBetByGame
	HashMap[] countMinBetByGame = (HashMap[]) map.get("countMinBetByGame");
	if (countMinBetByGame != null) {
	    try {
		// date, time, hour, halfHour, tenMinute, gameZoneID, minBet, count
		query = "INSERT INTO `countMinBetByGame`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`gameZoneID`,`minBet`,`count`)"
			    + " VALUES (?,?,?,?,?,?,?,?);";
		st = conn.prepareStatement(query);
		for (int i = 0; i < gameToStatistic.length; i ++) {
		    HashMap hm = countMinBetByGame[gameToStatistic[i]];
		    Set<Long> keySet = hm.keySet();
		    for (Long minBet : keySet) {
			long count = (Long) hm.get(minBet);
			st.setString(1, date);
			st.setString(2, time);
			st.setInt(3, hour);
			st.setInt(4, halfHour);
			st.setInt(5, tenMinute);
			st.setInt(6, gameToStatistic[i]);
			st.setString(7, String.valueOf(minBet));
			st.setLong(8, count);
			st.addBatch();
		    }
		}
		insertedRowsArr = st.executeBatch();
		st.clearParameters();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	
	// countCapacity
	int[] countCapacity = (int[]) map.get("countCapacity");
	try {
	    // date, time, hour, halfHour, tenMinute, no1, no2, no3, no4, no5, no6, no7, no8, no9, no10
	    query = "INSERT INTO `countCapacity`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`no1`,`no2`,`no3`,`no4`,`no5`,`no6`,`no7`,`no8`,`no9`,`no10`)"
			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    st.setString(1, date);
	    st.setString(2, time);
	    st.setInt(3, hour);
	    st.setInt(4, halfHour);
	    st.setInt(5, tenMinute);
	    for (int i = 1; i < maxNoOfPlayersPerTable + 1; i++ ) {
		st.setInt(5 + i, countCapacity[i]);
	    }
	    insertedRows = st.executeUpdate();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countCapacityByGame
	int[][] countCapacityByGame = (int[][]) map.get("countCapacityByGame");
	if (countCapacityByGame != null) {
	    try {
		// date, time, hour, halfHour, tenMinute, gameZoneID, no1, no2, no3, no4, no5, no6, no7, no8, no9, no10
		query = "INSERT INTO `countCapacityByGame`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`gameZoneID`,`no1`,`no2`,`no3`,`no4`,`no5`,`no6`,`no7`,`no8`,`no9`,`no10`)"
			    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";	    
		st = conn.prepareStatement(query);
		for (int i = 0; i < gameToStatistic.length; i++) {
		    st.setString(1, date);
		    st.setString(2, time);
		    st.setInt(3, hour);
		    st.setInt(4, halfHour);
		    st.setInt(5, tenMinute);
		    st.setInt(6, gameToStatistic[i]);
		    for (int j = 1; j < maxNoOfPlayersPerTable + 1; j++ ) {
			st.setInt(6 + j, countCapacityByGame[gameToStatistic[i]][j]);
		    }
		    st.addBatch();
		}
		insertedRowsArr = st.executeBatch();
		st.clearParameters();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	
	// countIP
	HashMap countIP = (HashMap) map.get("countIP");
	try {
	    // date, time, hour, halfHour, tenMinute, minBet, count
	    query = "INSERT INTO `countIP`(`date`,`time`,`hour`,`halfHour`,`tenMinute`,`totalUniqueIPs`,`detailIPs`)"
			+ " VALUES (?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    Set<String> keySet = countIP.keySet();
	    int totalUniqueIPS = keySet.size();
	    String detailIPs = "";
	    JSONObject jsobj = new JSONObject();
	    long count;
	    for (String IP : keySet) {
		count = (Long) countIP.get(IP);
		jsobj.put(IP, count);
	    }
	    detailIPs = jsobj.toString();
	    st.setString(1, date);
	    st.setString(2, time);
	    st.setInt(3, hour);
	    st.setInt(4, halfHour);
	    st.setInt(5, tenMinute);
	    st.setInt(6, totalUniqueIPS);
	    st.setString(7, detailIPs);
	    st.executeUpdate();
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// finally -> close statement
	try {
	    if (st != null) {
		st.close();
	    }
	} catch (SQLException ex) {
	    ex.printStackTrace();
	}
	
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }
    
    // save statistic realtime data to db
    public static void logStatisticRealtimeToDB (HashMap map, int[] gameToStatistic, int noOfRoom, int maxNoOfPlayersPerTable) {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logStatisticRealtimeToDB");
//	}
	
	String query;
	PreparedStatement st = null;
	int insertedRows;
	int[] insertedRowsArr;
	
	// countTotal
	long total = (Long) map.get("total");
	long totalIOS = (Long) map.get("totalIOS");
	long totalAndroid = (Long) map.get("totalAndroid");
	long totalJ2ME = (Long) map.get("totalJ2ME");
	long totalFlash = (Long) map.get("totalFlash");
	long totalFacebook = (Long) map.get("totalFacebook");
	long totalYahoo = (Long) map.get("totalYahoo");
	long totalGoogle = (Long) map.get("totalGoogle");
	long totalTheGioiBai = (Long) map.get("totalTheGioiBai");
	
	try {
	    if (conn.isClosed()) {
		conn = DriverManager.getConnection(url, db_username, db_password);
	    }
	    
	    query = "DELETE FROM `countRealtimeTotal`";
	    st = conn.prepareStatement(query);
	    insertedRows = st.executeUpdate();
	    
	    // total, totalIOS, totalAndroid, totalJ2ME, totalFlash, totalFacebook, totalYahoo, totalGoogle, totalTheGioiBai
	    query = "INSERT INTO `countRealtimeTotal`(`total`,`totalIOS`,`totalAndroid`,`totalJ2ME`,`totalFlash`,`totalFacebook`,`totalYahoo`,`totalGoogle`,`totalTheGioiBai`)"
			+ " VALUES (?,?,?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    st.setLong(1, total);
	    st.setLong(2, totalIOS);
	    st.setLong(3, totalAndroid);
	    st.setLong(4, totalJ2ME);
	    st.setLong(5, totalFlash);
	    st.setLong(6, totalFacebook);
	    st.setLong(7, totalYahoo);
	    st.setLong(8, totalGoogle);
	    st.setLong(9, totalTheGioiBai);
	    
	    insertedRows = st.executeUpdate();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countGame
	int[] countGame = (int[]) map.get("countGame");
	try {
	    query = "DELETE FROM `countRealtimeGame`";
	    st = conn.prepareStatement(query);
	    insertedRows = st.executeUpdate();
	    
	    // gameZoneID, count
	    query = "INSERT INTO `countRealtimeGame`(`gameZoneID`,`count`)"
			+ " VALUES (?,?);";
	    st = conn.prepareStatement(query);
	    for (int i = 0; i < gameToStatistic.length; i ++) {
		st.setInt(1, gameToStatistic[i]);
		st.setInt(2, countGame[gameToStatistic[i]]);
		st.addBatch();
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// countCP
	HashMap countCP = (HashMap) map.get("countCP");
	try {
	    query = "DELETE FROM `countRealtimeCP`";
	    st = conn.prepareStatement(query);
	    insertedRows = st.executeUpdate();
	    
	    // cpID, count
	    query = "INSERT INTO `countRealtimeCP` (`cpID`,`count`)"
			+ " VALUES (?,?);";
	    st = conn.prepareStatement(query);
	    Set<String> keySet = countCP.keySet();
	    for (String cpID : keySet) {
		long count = (Long) countCP.get(cpID);
		st.setString(1, cpID);
		st.setLong(2, count);
		st.addBatch();
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// list user online
	ArrayList<HashMap> userList = (ArrayList<HashMap>) map.get("userList");
	try {
	    query = "DELETE FROM `countRealtimeListUsersOnline`";
	    st = conn.prepareStatement(query);
	    insertedRows = st.executeUpdate();
	    
	    // username, userID, cp, loginTime, IP, deviceType, clientVersion, device, screenSize
	    query = "INSERT INTO `countRealtimeListUsersOnline` (`username`,`userID`,`cp`,`loginTime`,`IP`,`deviceType`,`clientVersion`,`device`,`screenSize`,`gameZoneID`,`room`,`table`)"
			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
	    st = conn.prepareStatement(query);
	    for (HashMap user : userList) {
		st.setString(1, (String) user.get("username"));
		st.setLong(2, (Long) user.get("userID"));
		st.setString(3, (String) user.get("cp"));
		st.setString(4, (String) user.get("loginTime"));
		st.setString(5, (String) user.get("ip"));
		st.setString(6, (String) user.get("deviceType"));
		st.setString(7, (String) user.get("clientVersion"));
		st.setString(8, (String) user.get("device"));
		st.setString(9, (String) user.get("screenSize"));
		st.setLong(10, (Long) user.get("gameZoneID"));
		st.setLong(11, (Long) user.get("room"));
		st.setString(12, (String) user.get("table"));
		st.addBatch();
	    }
	    insertedRowsArr = st.executeBatch();
	    st.clearParameters();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	// finally -> close statement
	try {
	    if (st != null) {
		st.close();
	    }
	} catch (SQLException ex) {
	    ex.printStackTrace();
	}
	
//	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }
    
    // get all Message Admin
    public static ArrayList<HashMap> getAllMessageAdmin() throws Exception {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAllMessageAdmin: ");
//	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
	ArrayList<HashMap> resArr = new ArrayList<HashMap>();
	
        try {
            String query = "SELECT * FROM `messageAdmin`;";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            if (rs != null) {
		while (rs.next()) {
		    HashMap res = new HashMap();
		    res.put("id", rs.getInt("id"));
		    res.put("time", rs.getDate("time"));
		    res.put("adminUsername", rs.getString("adminUsername"));
		    res.put("message", rs.getString("message"));
		    res.put("receiveList", rs.getString("receiveList").toLowerCase());
		    resArr.add(res);
    //		res.isPrivateUserBase = rs.getInt("isPrivateUserBase") > 0;
		}
            } 

	    rs.close();
            st.clearParameters();
            st.close();
            
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return resArr;
        } 
	catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error com.mysql.jdbc.exceptions.jdbc4.CommunicationsException!, message: " + e.getMessage());
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect succesful!");
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getAllMessageAdmin();
            //throw new Exception();
        }
    }
    
    // update Message Admin table after sending the message
    public static void updateDBAfterMessageSent(HashMap message) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getAllMessageAdmin: ");
	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
	
	int id = ((Integer) message.get("id")).intValue();
	
	// add that message to table `messageAdminSent` for keeping track of it
	String query = "INSERT INTO `messageAdminSent` (`time`,`adminUsername`,`message`,`receiveList`) " +
		    "( SELECT `time`,`adminUsername`,`message`,`receiveList` FROM `messageAdmin` WHERE `id` = ?);";
	PreparedStatement st = conn.prepareStatement(query);
	st.setInt(1, id);
	int affectedRows = st.executeUpdate();
	st.clearParameters();
	
	// delete sent message from table `messageAdmin`
	query = "DELETE FROM `messageAdmin` WHERE `id` = ?;";
	st = conn.prepareStatement(query);
	st.setInt(1, id);
	affectedRows = st.executeUpdate();
	st.clearParameters();
	
	// add that message to table `messageAdminSent` for keeping track of it
//	query = "INSERT INTO `messageAdminSent` (`time`,`adminUsername`,`message`,`receiveList`) VALUES (?,?,?,?);";
//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	String time = sdf.format((Date) message.get("time"));
//	st = conn.prepareStatement(query);
//	st.setString(1, time);
//	st.setString(2, (String) message.get("adminUsername"));
//	st.setString(3, (String) message.get("message"));
//	st.setString(4, (String) message.get("receiveList"));
//	affectedRows = st.executeUpdate();
//	st.clearParameters();
	
	st.close();
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }
    
    // get all disconnect list
    public static ArrayList<HashMap> getDisconnectList() throws Exception {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getDisconnectList: ");
//	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
	ArrayList<HashMap> resArr = new ArrayList<HashMap>();
	
        try {
            String query = "SELECT * FROM `disconnectUser`;";
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            if (rs != null) {
		while (rs.next()) {
		    HashMap res = new HashMap();
		    res.put("id", rs.getInt("id"));
		    res.put("time", rs.getDate("time"));
		    res.put("adminUsername", rs.getString("adminUsername"));
		    res.put("kickList", rs.getString("kickList").toLowerCase());
		    resArr.add(res);
    //		res.isPrivateUserBase = rs.getInt("isPrivateUserBase") > 0;
		}
            } 

	    rs.close();
            st.clearParameters();
            st.close();
            
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return resArr;
        } 
	catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            System.out.println("Error com.mysql.jdbc.exceptions.jdbc4.CommunicationsException!, message: " + e.getMessage());
            conn.close();
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect succesful!");
//            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
            return getAllMessageAdmin();
            //throw new Exception();
        }
    }
    
    // update db after disconnecting user
    public static void updateDBAfterDisconnectUser(HashMap dis) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - updateDBAfterDisconnectUser: ");
	}
	
        if (conn.isClosed()) {
            conn = DriverManager.getConnection(url, db_username, db_password);
            System.out.println("Reconnect!");
        }
	
	int id = ((Integer) dis.get("id")).intValue();
	
	// add that kick to table `disconnectUserDone` for keeping track of it
	String query = "INSERT INTO `disconnectUserDone` (`time`,`adminUsername`,`kickList`) " +
		    "( SELECT `time`,`adminUsername`,`kickList` FROM `disconnectUser` WHERE `id` = ?);";
	PreparedStatement st = conn.prepareStatement(query);
	st.setInt(1, id);
	int affectedRows = st.executeUpdate();
	st.clearParameters();
	
	// delete sent message from table `messageAdmin`
	query = "DELETE FROM `disconnectUser` WHERE `id` = ?;";
	st = conn.prepareStatement(query);
	st.setInt(1, id);
	affectedRows = st.executeUpdate();
	st.clearParameters();
	
	st.close();
	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }
    
    // log itunes charge: return -1 if failed, return product money game if ok
    public static int logItunesCharge(String uniqueIdentifier, String transactionID, Date purchaseTime, int quantity, int productID, String username  ) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - logItunesCharge:");
	}
        
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int result = -1;
        CallableStatement cstmt = null;
        ResultSet rs = null;
        try {
	    cstmt = conn.prepareCall("{call usp_log_itunes_charge(?, ?, ?, ?, ?, ?)}");
	    cstmt.setString(1, uniqueIdentifier);
	    cstmt.setString(2, transactionID);
	    cstmt.setString(3, sdf.format(purchaseTime));
            cstmt.setInt(4, quantity);
            cstmt.setInt(5, productID);
            cstmt.setString(6, username);
            System.out.println(cstmt.toString());
	    rs = cstmt.executeQuery();
//	    String query = "SELECT * FROM version where Description like '%mobile%';";
//            PreparedStatement st = conn.prepareStatement(query);
//            ResultSet rs = st.executeQuery();
            if (rs != null && rs.next()) {
                result = rs.getInt("return_status");
                if (result == 1) {
                    result = rs.getInt("product_money_game");
                }
            }
//            st.clearParameters();
//            st.close();
//            rs.close();
//	    cstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        finally {
            try {
                if (rs != null)
                    rs.close();
                if (cstmt != null)
                    cstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	
	if (DebugConfig.FOR_DEBUG) {
	    DebugConfig.CALL_LEVEL = previousMethodCallLevel;
	}
        return result;
    }
    
    // get cardName for given cardID
    public static String getCardName(int cardID) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getCardName: cardID = " + cardID);
	}
        String name = "";
        String query = "SELECT `cardName` FROM `charge_cards` WHERE `cardID`=?;";
        PreparedStatement ps = conn.prepareStatement(query);
	ps.setInt(1, cardID);
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            name = rs.getString("cardName");
	    rs.close();
        }

        ps.clearParameters();
        ps.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return name;
    }
    
    // get game money equilavent to give card amount
    public static int getGameMoneyValueForCardAmount(int amount) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - getGameMoneyValueForCardAmount: amount = " + amount);
	}
        int money = 0;
        String query = "SELECT `money` FROM `charge_role` WHERE `amount`=?;";
        PreparedStatement ps = conn.prepareStatement(query);
	ps.setInt(1, amount / 1000);
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            money = rs.getInt("money") * 1000;
	    rs.close();
        }

        ps.clearParameters();
        ps.close();        

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return money;
    }
    
    // charge card for openIDOpen
    public static void chargeCardOpenIDOpen(String cp, UserEntity user, String cardNumber, String cardSerial, String telco, int amount) 
	    throws Exception
    {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - chargeCardOpenIDOpen");
	}

	CallableStatement cs;
	
	// get card value based on amount
	int gameMoney = DatabaseDriver.getGameMoneyValueForCardAmount(amount);
	
	// money of user before card charge
	long moneyBefore = 0, moneyAfter = 0;
	String query = "SELECT `cash` FROM `userstt` WHERE `UserID`=?;";
	cs = conn.prepareCall(query);
	cs.setLong(1, user.mUid);
	ResultSet rs = cs.executeQuery();
        if (rs != null && rs.first()) {
            moneyBefore = rs.getLong("cash");
	    moneyAfter = moneyBefore + gameMoney;
	    rs.close();
        }	
	
	// update GG for that user
	query = "UPDATE `userstt` SET `cash`=`cash`+? WHERE `UserID`=?;";
	cs = conn.prepareCall(query);
        cs.clearParameters();
	cs.setInt(1, gameMoney);
	cs.setLong(2, user.mUid);
	cs.executeUpdate();
	
	
	
	// log it
	String now = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
        System.out.println("log it!");
        query = "INSERT INTO `log_card_charge_openIDOpen` (`username`,`userID`,`cp`,`cpUserID`,`time`,`cardNumber`,`cardSerial`,`cardType`,`amount`,`moneyInGame`,`userMoneyBefore`,`userMoneyAfter`)"
			    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
        cs = conn.prepareCall(query);
        cs.clearParameters();

        cs.setString(1, user.mUsername);
        cs.setLong(2, user.mUid);
	cs.setString(3, cp);
	cs.setInt(4, user.cp_user_id);
	cs.setString(5, now);
	cs.setString(6, cardNumber);
	cs.setString(7, cardSerial);
	cs.setString(8, telco);
	cs.setInt(9, amount);
	cs.setInt(10, gameMoney);
	cs.setLong(11, moneyBefore);
	cs.setLong(12, moneyAfter);
	
        cs.executeUpdate();

        cs.clearParameters();
        cs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }
    
    // check if this ip is banned for registering
    public static boolean isIPBannedForRegistering(String ip, Date aDate) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - isIPBannedForRegistering: ip = " + ip);
	}
	
	boolean isBanned = true;
	String dateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(aDate);
        String query = "SELECT COUNT(`id`) AS `count` FROM `register_ip_banned` WHERE `ip`=? AND `end_date`>?;";
        PreparedStatement ps = conn.prepareStatement(query);
	ps.setString(1, ip);
	ps.setString(2, dateStr);
	
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            isBanned = rs.getInt("count") > 0;
	    rs.close();
        }

        ps.clearParameters();
        ps.close();

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return isBanned;
    }
    
    // check if this ip register too many accounts recently and take action if it happened
    // 20 acc 1 ngay, 25 acc 3 ngay, 40 acc 1 thang
    public static boolean isIPReggedTooMuchAccountsAndTakeActionIfSo(String ip, Date aDate) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - isIPReggedTooMuchAccounts: ip = " + ip);
	}
	
	int NO_1DAY = 20, NO_3DAY = 25, NO_1MONTH = 40;
	
	boolean isTooMuch = false;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");	
	Calendar c = Calendar.getInstance(); 
	c.setTime(aDate); 
	c.add(Calendar.MONTH, -1);
	Date lastMonth = c.getTime();
	
	c.setTime(aDate);
	c.add(Calendar.DATE, 1);
	Date nextDay = c.getTime();
	
	c.setTime(aDate);
	c.add(Calendar.DATE, -3);
	Date last3day = c.getTime();
	
	c.setTime(aDate);
	c.add(Calendar.DATE, -1);
	Date last1Day = c.getTime();
	
	Date dateFromWhichAccountWillBeBanned = null;
	
	// check theo thang
        String query = "SELECT COUNT(`id`) AS `count` FROM `register_ip_log` WHERE `ip`=? AND `date`>?  AND `date`<?;";	
        PreparedStatement ps = conn.prepareStatement(query);
	ps.setString(1, ip);
	ps.setString(2, sdf.format(lastMonth));
	ps.setString(3, sdf.format(nextDay));
	
        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.first()) {
            int count = rs.getInt("count");
	    if (count >= NO_1MONTH) {
		isTooMuch = true;
		dateFromWhichAccountWillBeBanned = lastMonth;
	    }	    
	    rs.close();		    
        }
	
	// check theo 3 ngay
	if ( ! isTooMuch) {
	    ps = conn.prepareStatement(query);
	    ps.setString(1, ip);
	    ps.setString(2, sdf.format(last3day));
	    ps.setString(3, sdf.format(nextDay));
	    rs = ps.executeQuery();
	    if (rs != null && rs.first()) {
		int count = rs.getInt("count");
		if (count >= NO_3DAY) {
		    isTooMuch = true;
		    dateFromWhichAccountWillBeBanned = last3day;
		}
		rs.close();		    
	    }	
	}
	
	// check theo 1 ngay
	if ( ! isTooMuch) {
	    ps = conn.prepareStatement(query);
	    ps.setString(1, ip);
	    ps.setString(2, sdf.format(last1Day));
	    ps.setString(3, sdf.format(nextDay));
	    rs = ps.executeQuery();
	    if (rs != null && rs.first()) {
		int count = rs.getInt("count");
		if (count >= NO_1DAY) {
		    isTooMuch = true;
		    dateFromWhichAccountWillBeBanned = last1Day;
		}
		rs.close();		    
	    }	
	}
	
	// neu da dang ky qua nhieu -> spam -> ban 1 loat acc ma ip do da dang ky vao nhung ngay gan day va ban IP trong 1 tuan
	if ( isTooMuch ) {
	    // ban 1 loat acc
	    query = "UPDATE `user` SET `is_active` = 99 WHERE `UserID` IN (SELECT `userID` FROM `register_ip_log` WHERE `ip`=? AND `date`>? AND `date`<?);";
	    ps = conn.prepareStatement(query);
	    ps.setString(1, ip);
	    ps.setString(2, sdf.format(dateFromWhichAccountWillBeBanned));
	    ps.setString(3, sdf.format(nextDay));
	    ps.executeUpdate();
	    ps.clearParameters();
	    
	    // insert ip do vao danh sach ban
	    c.setTime(aDate);
	    c.add(Calendar.DATE, 7);
	    Date dateEndBan = c.getTime();
	    query = "INSERT INTO `register_ip_banned` (`ip`,`end_date`) VALUES (?,?);";
	    ps = conn.prepareStatement(query);
	    ps.setString(1, ip);
	    ps.setString(2, sdf.format(dateEndBan));
	    ps.executeUpdate();
	}
	
	if (ps != null) {
	    ps.clearParameters();
	    ps.close();
	}
	
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	return isTooMuch;
    }
    
    // log ip after registering
    public static void logRegisterByIP(String cp, String username, long userID, Date date, String ip, String device)
	throws Exception
    {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "DatabaseDriver - chargeCardOpenIDOpen");
	}

	CallableStatement cs;
	
	// log it
	String dateS = (new SimpleDateFormat("yyyy-MM-dd")).format(date);
	String datetimeS = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
        System.out.println("log it!");
        String query = "INSERT INTO `register_ip_log` (`cp`,`username`,`userID`,`datetime`,`date`,`ip`,`device`)"
			    + " VALUES (?,?,?,?,?,?,?);";
        cs = conn.prepareCall(query);
        cs.clearParameters();

	cs.setString(1, cp);
        cs.setString(2, username);
        cs.setLong(3, userID);
	cs.setString(4, datetimeS);
	cs.setString(5, dateS);
	cs.setString(6, ip);
	cs.setString(7, device);
	
        cs.executeUpdate();
        cs.clearParameters();
        cs.close();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }
    
    // init name list in Database
    public static ArrayList<String> initOwnerNameListInDatabase(ArrayList<String> nameList) throws Exception {
	ArrayList<String> botNameList = new ArrayList<String>();
	
	// check if name existed in `user_bot` table
	StringBuilder sb = new StringBuilder("SELECT `Name` FROM `user_bot` WHERE `Name` IN (''");
	for (String str : nameList) {
	    sb.append(",'").append(str).append("'");
	}
	sb.append(");");
	
	String query = sb.toString();
	CallableStatement st = conn.prepareCall(query);
        ResultSet rs = st.executeQuery();
	ArrayList<String> existedBotNameList = new ArrayList<String>();
	while (rs.next()) {
            String name = rs.getString("Name");
	    existedBotNameList.add(name);
        }
	rs.close();
        st.clearParameters();
	botNameList.addAll(existedBotNameList);
	
	// names that not existed in user_bot table but existed in xml file
	ArrayList<String> notExistedBotNameList = new ArrayList<String>(nameList);
	notExistedBotNameList.removeAll(existedBotNameList);
	
	// check if name existed in `user` table
	sb = new StringBuilder("SELECT `Name` FROM `user` WHERE `Name` IN (''");
	for (String str : notExistedBotNameList) {
	    sb.append(",'").append(str).append("'");
	}
	sb.append(");");
	
	query = sb.toString();
        st = conn.prepareCall(query);
        rs = st.executeQuery();
	ArrayList<String> existedNameList = new ArrayList<String>();
	while (rs.next()) {
            String name = rs.getString("Name");
	    if (name != null) {
		name = name.trim().toLowerCase();
		if (!name.equals("")) {
		    existedNameList.add(name);
		}
	    }
        }
	rs.close();
        st.clearParameters();
        

	// names that not existed in table `user` and table `user_bot` -> creat them
	ArrayList<String> notExistedNameList = new ArrayList<String>(notExistedBotNameList);
	notExistedNameList.removeAll(existedNameList);
	String password, password_md5;
	int cash, numberOfPlays;
	for (String name : notExistedNameList) {
	    // tao moi username do nao
	    password = String.valueOf(generateRandomNumber(12345678, 87654321));
	    password_md5 = MD5.md5Hex(password);
	    cash = generateRandomNumber(700000, 1500000);
	    numberOfPlays = generateRandomNumber(123, 345);
	    st = conn.prepareCall("{call usp_create_bot_user(?, ?, ?, ?, ?)}");
	    st.setString(1, name);
	    st.setString(2, password);
	    st.setString(3, password_md5);
	    st.setInt(4, cash);
	    st.setInt(5, numberOfPlays);
	    rs = st.executeQuery();
            if (rs != null && rs.next()) {
                int status = rs.getInt("return_status");
		if (status > 0) {
		    botNameList.add(name);
		}
            }
            rs.close();
	}
	
	st.clearParameters();
	st.close();
	
	return botNameList;
    }
    
    // generate random number
    private static int generateRandomNumber(int aStart, int aEnd) {
	if ( aStart > aEnd ) {
	    throw new IllegalArgumentException("Start cannot exceed End.");
	}
	return aStart + (int)(Math.random() * ((aEnd - aStart) + 1));
    }
    
    // get fake user info based for GetFreeFriendListBusiness based on give list of fake user name
    public static ArrayList<UserEntity> getFakeUserEntityForInvite(ArrayList<String> nameList) {
	ArrayList<UserEntity> resultList = new ArrayList<UserEntity>();
	try {
	    StringBuilder sb = new StringBuilder("SELECT u.`UserID`,u.`Name`,us.`AvatarID`,us.`Level`,us.`Cash`,us.`PlaysNumber` FROM `user` u LEFT JOIN `userstt` us ON u.UserID = us.UserID WHERE u.`Name` IN (''");
	    for (String name : nameList) {
		sb.append(",'").append(name).append("'");
	    }
	    sb.append(");");
            String query = sb.toString();
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();
	    while (rs.next()) {
		UserEntity res = new UserEntity();
		res.mUid = rs.getLong("UserID");
                res.mUsername = rs.getString("Name");
		res.avatarID = rs.getInt("AvatarID");
		res.level = rs.getInt("Level");
		res.money = rs.getLong("Cash");
		res.playsNumber = rs.getInt("PlaysNumber");
		resultList.add(res);
            }
	    
	    rs.close();
	    st.clearParameters();
            st.close();
	    
            return resultList;

        } catch (Exception e) {
            return new ArrayList<UserEntity>();
        }
    }
    
    // is given uid belonged to fake user
    public static boolean isFakeUser(long uid) throws Exception {
	boolean isFake = false;
	String query = "SELECT COUNT(*) AS `count` FROM `user_bot` WHERE `Name` = (SELECT `Name` FROM `user` WHERE `UserID` = ?);";
	PreparedStatement st = conn.prepareStatement(query);
	st.setLong(1, uid);
	ResultSet rs = st.executeQuery();
	if (rs != null && rs.next()) {
	    int i = rs.getInt("count");
	    isFake = i > 0;
	}

	rs.close();
	st.clearParameters();
	st.close();
	
	return isFake;
    }
}
