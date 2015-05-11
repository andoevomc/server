package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.data.AvatarEntity;
import dreamgame.data.ChargeHistoryEntity;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetAvatarListResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public boolean getHistroy = false;
    public boolean getGameInfo = false;
    public boolean getImage = false;
    //Thomc
    public boolean notification = false;
    public Vector<AvatarEntity> mAvatarList;
    public Vector<ChargeHistoryEntity> mChargeHistory;
    public long newMoney = 0;
    public long totalMobileUser = 0;
    public long totalFlashUser = 0;
    public long totalRoom = 0;
    public long totalTienLen = 0;
    public long totalPhom = 0;
    public String imageData = "";
    public boolean isFriendNotice;

    public void setSuccess(int aCode, Vector<AvatarEntity> aAvatarList) {
        mCode = aCode;
        mAvatarList = aAvatarList;
    }

    public void setGameInfo(int aCode) {
        mCode = aCode;
        getGameInfo = true;
    }

    public void setNotice(String mNotice, boolean isFriendNotice) {
        mCode = 1;
        notification = true;
        this.msgNotification = mNotice;
        this.isFriendNotice = isFriendNotice;
    }

    public void setChargeSuccess(int aCode, Vector<ChargeHistoryEntity> ac) {
        mCode = aCode;
        mChargeHistory = ac;
        getHistroy = true;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetAvatarListResponse();
    }
}
