/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Admin
 */
public class LostResponse extends AbstractResponseMessage {

    public String errMgs;
    public String lost_player_name;
    public long player_friend_id;
    public long ownerMoney;
    public long playerMoney;

    public IResponseMessage createNew() {
        return new LostResponse();
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        errMgs = aErrorMsg;
    }
    public void setMoneyEndMatch(long ownerMoney_, long playerMoney_) {
        ownerMoney = ownerMoney_;
        playerMoney = playerMoney_;
    }
    public void setSuccess(int aCode, long player_friend_id, String lost_player_name) {
        mCode = aCode;
        this.lost_player_name = lost_player_name;
        this.player_friend_id = player_friend_id;
    }
}
