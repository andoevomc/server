/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.cotuong.data.moveFromTo;
import dreamgame.poker.data.PokerPlayer;
import java.util.ArrayList;

/**
 *
 * @author binh_lethanh
 */
public class TurnRequest extends AbstractRequestMessage {

    public long mMatchId;
    public long money; // for bacay
    public int ottObject; // for ott
    public int phomCard; // for phom
    public long uid;
    public boolean isTimeout;
    public boolean isTimeoutTL = false;
    // Tien len
//    Thomc
//    public com.migame.tienlen.data.Poker[] tienlenCards ;
    public String tienlenCards;
    public boolean isGiveup;
    public boolean isDuoi, isChiu;
    // Caro
    public int mRow;
    public int mCol;
    public int mType;
    // CoTuong
    public moveFromTo mv = new moveFromTo();
//poker, xì tố
    public boolean isFold;
    public boolean isVisible = false;
    public long minBet, maxBet;
    public String poker;
    public long potMoney = 0;
    public ArrayList<PokerPlayer> pokerPlayers;
//    public ArrayList<XiToPlayer> xitoPlayers;
    public String betTypeDes = "";
    //xito
    public int visibleCard;
    public boolean isShow = false;
//bau cua
    public int piece, num;
    //mau binh
    public String chi1, chi2, chi3;

    public IRequestMessage createNew() {
        return new TurnRequest();
    }
}
