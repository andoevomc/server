/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author binh_lethanh
 */
public class CancelResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public long uid;
    public boolean isGamePlaying;
    public boolean isUserPlaying;
    public String message;
    public long newOwner = 0;
    //Thomc: for co tuong
    public long ownerMoney=-1;
    public long playerMoney=-1;
//Thomc
    //trường hợp đang đến lượt đi mà thoát game  thì chuyển lượt và newRound cho những người còn lại
    public long next_id = -1;
    public boolean isNewRound = false;
    public int zone_id;
    public long money = -1; //gửi tiền bị thua về khi thoát game Tiến lên lúc đang chơi
// tiến lên: chơi nhất-bét, gửi thông tin cóng/về thứ mấy
    public long stt = 0;

    public void setZone(int id) {
        zone_id = id;
    }

    public void setNextPlayer(long id, boolean isNewRound_) {
        this.next_id = id;
        this.isNewRound = isNewRound_;
    }

    public void setSuccess(int aCode, long id) {
        mCode = aCode;
        uid = id;
    }

    public void setUid(long id) {
        uid = id;
    }

    public void setMoneyEndMatch(long ownerMoney_, long playerMoney_) {
        System.out.println("setmoney!");
        ownerMoney = ownerMoney_;
        playerMoney = playerMoney_;
    }

    public void setUserPlaying(boolean play) {
        this.isUserPlaying = play;
    }

    public void setGamePlaying(boolean play) {
        this.isGamePlaying = play;
    }

    public void setMessage(String m) {
        message = m;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new CancelResponse();
    }
}
