package dreamgame.data;

import java.util.Date;

public class UserEntity {

    public long mUid;
    public String mUsername;
    public String mPassword;
    public int mAge;
    public boolean mIsMale;
    public long money;
    public int level;
    public int avatarID;
    public int playsNumber;
    public int isLogin = 0;
    public Date lastLogin;
    public long lastMatch;
    public boolean isActive=false;
    public boolean isBanned=false;
    public String cp="";
    
    public long award=0;
    public int remain_gift=0;
    public int receive_gift=0;
    public int cp_user_id = 0;
    //public String TuocVi;
    public UserEntity() {
    }
}
