package dreamgame.protocol.messages;

import java.util.Vector;

import dreamgame.bacay.data.Poker;

import dreamgame.chan.data.ChanPoker;
import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import java.util.ArrayList;

public class GetPokerResponse extends AbstractResponseMessage {

    public Poker first;
    public Poker second;
    public Poker third;
    public long uid;
    public String name;
    public ArrayList<ChanPoker> chanCards = new ArrayList<ChanPoker>();
    public ArrayList<phom.data.Poker> phomCards = new ArrayList<phom.data.Poker>();
    public ArrayList<dreamgame.tienlen.data.Poker> tienlenCards = new ArrayList<dreamgame.tienlen.data.Poker>();
    //Thomc for Tienlen
    public byte[] tienlenCards_new;
    public boolean isNewMatch = false;
    public long first_id = 0;
    public long matchNum = 0;
    public long order[] = new long[5];
    public int zoneID = 0;
    public byte[] pokerCards;
    public long minBet = 0, maxBet = 0;
    public long potMoney = 0;
//bau cua
    public int timeBet;
//Maubinh
    public byte[] maubinhCards;
    public int PlayingTime;

    public void setPhomCards(ArrayList<phom.data.Poker> cards) {
        this.phomCards = cards;
    }
//Thomc

    public void setTienLenCards(byte[] cards) {
        this.tienlenCards_new = cards;
    }

    public void setMauBinhCards(byte[] cards) {
        this.maubinhCards = cards;
    }

    public void setPokerCards(byte[] cards, int zone) {
        this.pokerCards = cards;
        zoneID = zone;
    }

    public void setMinMaxBet(long min, long max) {
        minBet = min;
        maxBet = max;
    }
    public long beginID = -1;

    public void setBeginID(long b) {
        beginID = b;
    }

    public void setSuccess(int aCode, long id, String n) {
        mCode = aCode;
        uid = id;
        name = n;

    }

    public void setSuccess(int aCode, long id, String n, Poker f, Poker s, Poker t) {
        mCode = aCode;
        uid = id;
        name = n;
        first = f;
        second = s;
        third = t;
    }

    public IResponseMessage createNew() {
        return new GetPokerResponse();
    }
}
