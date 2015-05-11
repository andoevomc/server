package dreamgame.protocol.messages;

import java.util.Date;
import java.util.Vector;

import dreamgame.data.AdvEntity;
//import bacay.data.VersionEntity;
import dreamgame.data.ChargeCardEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONArray;

public class LoginResponse extends AbstractResponseMessage {

    public ChargeCardEntity[] chargeCards=new ChargeCardEntity[1];

    public String mErrorMsg;
    public long mUid;
    public long money;
    public int avatarID;
    public int level;
    public Vector<AdvEntity> advs;
//    public VersionEntity lVersion;
    public Date lastLogin;
    public String TuocVi;
    public int playNumber;
    public long moneyUpdateLevel;
    public boolean disconnect=false;

    public String smsActive="8500";
    public String smsNumber2="8500";
    public String smsNumber="8700";
    public String smsContent="VUI";
    public String adminMessage="";
    public String smsMessage="Bạn đã nạp tiền thành công. Tài khoản của bạn sẽ được công thêm 15k tiền.";
    public String smsMessage2="Bạn đã nạp tiền thành công. Tài khoản của bạn sẽ được công thêm 10k tiền.";

    public String smsValue="15";
    public String smsValue2="10";
    public String smsActiveValue="300";

    public boolean isActive=false;
    public boolean notActive=false;

    public String linkDown="";
    public String newVer="";

    public long lastRoom=-1;
    public int zone_id;
    public String lastRoomName="";

    public Date reset_gift_date;
    public int cash_gift=10000;
    public int max_gift=10000;

    public JSONArray jaOsCharge=null;
    
    public void setLastRoom(long l,String r,int z)
    {
        lastRoom=l;
        lastRoomName=r;
        zone_id=z;
    }
    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

//    public void setVersion(VersionEntity lV) {
//        this.lVersion = lV;
//    }

    public void setAdvs(Vector<AdvEntity> advs) {
        this.advs = advs;
    }

    public void setSuccess(int aCode, long aUid, long mn, int avatar, int lev, Date time, String Tuocvi, int playNumbers,long updatelevel) {
        mCode = aCode;
        mUid = aUid;
        money = mn;
        avatarID = avatar;
        level = lev;
        lastLogin = time;
        TuocVi = Tuocvi;
        playNumber = playNumbers;
        moneyUpdateLevel = updatelevel;
    }

    public IResponseMessage createNew() {
        return new LoginResponse();
    }
}
