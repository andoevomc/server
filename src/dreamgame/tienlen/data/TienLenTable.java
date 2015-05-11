package dreamgame.tienlen.data;

import dreamgame.config.GameRuleConfig;
import java.util.ArrayList;

import org.slf4j.Logger;

import dreamgame.gameserver.framework.common.LoggerContext;

import dreamgame.data.SimpleTable;
import dreamgame.data.Timer;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;

public class TienLenTable extends SimpleTable {

    public final static int CARDS_NULL = 0, CARDS_SINGLE = 1, CARDS_COUPLE = 2, CARDS_XAMCO = 3,
            CARDS_TUQUY = 4, CARDS_SERIAL = 5, CARDS_SERIAL_COUPLE = 6;
    public final static int PERFECT_TUQUY = 7, PERFECT_3SERIAL_COUPLE = 8, PERFECT_5SERIAL_COUPLE = 9, PERFECT_6COUPLE = 10, PERFECT_SANHRONG = 11;
    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(TienLenTable.class);
    private ArrayList<TienLenPlayer> playings = new ArrayList<TienLenPlayer>();
    private ArrayList<TienLenPlayer> waitings = new ArrayList<TienLenPlayer>();
    private int currentIndexOfPlayer;
    @SuppressWarnings("unused")
    private int preIndexOfPlayer;
    private int preTypeOfCards = -1;
    private Poker[] currCards;
    public TienLenPlayer owner;
    private TienLenPlayer currPlayer;
    public TienLenPlayer winner;
//    private boolean isPerfect = false;
    private Timer timerAuto = new Timer(ZoneID.TIENLEN, 10000);
    public boolean fightOccur = false; // Mỗi lần xảy ra chặt chém mất tiền thì set là true;
//Kiểu chơi nhất-nhì- ba-bét
    public int sttFirst = 0, sttLast = 0;
    public int numPlayer = 0; //số người chơi lúc bắt đầu ván
//    public boolean choiDemLa = DatabaseDriver.choiDemLa;//
    public boolean choiDemLa = false;
    public boolean lastPerfect = false;
    public ArrayList<TienLenPlayer> toiList = new ArrayList<TienLenPlayer>();//gửi danh sách những người cóng, hoặc tới Tới lúc turn
    private boolean isTienLenMB = false;//
    int tienLenTax = 10;        
    
    public boolean roomIsFull() {
        return ((getPlayings().size() + getWaitings().size()) >= getMaximumPlayer());
    }

    public TienLenPlayer getCurrPlayer() {
        return currPlayer;
    }

    public TienLenPlayer getOwner() {
        return owner;
    }

    public int getCurrentIndexOfPlayer() {
        return currentIndexOfPlayer;
    }

    public ArrayList<TienLenPlayer> getPlayings() {
        return playings;
    }

    public ArrayList<TienLenPlayer> copPlayerList() {
        ArrayList<TienLenPlayer> list = new ArrayList<TienLenPlayer>();
        for (TienLenPlayer p : playings) {
            list.add(p);
        }
        return list;
    }

    public void setOrder(long[] order) {
        for (int i = 0; i < order.length; i++) {
            order[i] = 0;
        }
        int i = 0;
        for (TienLenPlayer p : playings) {
            if (i < order.length) {
                order[i] = p.id;
            }

            i++;
        }
    }

    public boolean isAllReady() {
        for (int i = 0; i < playings.size(); i++) {
            TienLenPlayer player = playings.get(i);
            if ((!player.isReady) && (player.id != this.owner.id)) {
                return false;
            }
        }
        return true;
    }

    public boolean containPlayer(long id) {
        for (int i = 0; i < playings.size(); i++) {
            if (playings.get(i).id == id) {
                return true;
            }
        }
        for (int i = 0; i < waitings.size(); i++) {
            if (waitings.get(i).id == id) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<TienLenPlayer> getWaitings() {
        return waitings;
    }
    // Create table: owner, money, matchID, numberPlayer

    public TienLenTable(TienLenPlayer ow, long money, long match, int numPlayer) {
        logdir = "tienlen_log";
        initLogFile();
        startTime = System.currentTimeMillis();
        this.owner = ow;
        this.firstCashBet = money;
        this.matchID = match;
        this.maximumPlayer = numPlayer;
        this.playings.add(ow);
        timerAuto.setRuning(false);
        timerAuto.start();
    }

//    // for making fake table only
//    public TienLenTable(TienLenPlayer ow, long money, long match, int numPlayer, boolean isFake) {
//	if (isFake) {
//	    startTime = System.currentTimeMillis();
//	    this.owner = ow;
//	    this.firstCashBet = money;
//	    this.matchID = match;
//	    this.maximumPlayer = numPlayer;
////	    this.playings.add(ow);
//	}
//	else {
//	    logdir = "tienlen_log";
//	    initLogFile();
//	    startTime = System.currentTimeMillis();
//	    this.owner = ow;
//	    this.firstCashBet = money;
//	    this.matchID = match;
//	    this.maximumPlayer = numPlayer;
//	    this.playings.add(ow);
//	    timerAuto.setRuning(false);
//	    timerAuto.start();
//	}
//    }
    
    // Player join
    public void join(TienLenPlayer player) throws TienLenException {
        synchronized (playings) {
            for (TienLenPlayer p : playings) {
                if (p.id == player.id) {
                    //user da ton tai!
                    throw new TienLenException("User da ton tai.");
                }
            }
        }
        if (this.isPlaying) {
            if (this.playings.size() + this.waitings.size() < 4) {
                player.isObserve = true;
                this.waitings.add(player);
            } else {
                throw new TienLenException("Phong da thua nguoi roi ban");
            }
        } else if (this.playings.size() < 4) {
            this.playings.add(player);
        } else {
            throw new TienLenException("Phong da thua nguoi roi ban");
        }
    }

    //Player removed
    public void removePlayer(long id) {

        try {
            for (TienLenPlayer p : playings) {
                if (p.id == id) {
                    remove(p);
                    return;
                }
            }
            for (TienLenPlayer p : waitings) {
                if (p.id == id) {
                    remove(p);
                    return;
                }
            }
        } catch (Exception e) {
            logCode("Error. Not found player : " + id);
        }
    }

    public void remove(TienLenPlayer player) throws TienLenException {
        try {
            synchronized (this.playings) {

                logCode("Remove player : " + player.id);
                for (TienLenPlayer p : this.playings) {
                    if (p.id == player.id) {
                        playings.remove(player);
                        return;
                    }
                }
                for (TienLenPlayer p : this.waitings) {
                    if (p.id == player.id) {
                        waitings.remove(p);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            mLog.error(e.getMessage() + " : " + player.id);
//            if (player!=null)
//                mLog.error(turnInfo()+" : "+player.id);
//            else
//                mLog.error(turnInfo()+" : remove Null");
//
//            throw new PhomException(e.getMessage());
        }
    }
//
//    public void setNewStarter(TienLenPlayer player) {
//        int index;
//        try {
//            index = getUserIndex(player.id);
//
//            if (index >= 0) {
//                setNewStarter(index, player);
//            } else {
//                setNewStarter(0, playings.get(0));
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
////            java.util.logging.Logger.getLogger(TienLenPlayer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    private void setNewStarter(int index, TienLenPlayer player) {
//        this.currentIndexOfPlayer = index;
//        this.currentPlayer = playings.get(index);
//        this.firstRoundIndex = index;
//        this.prePlayer = this.currentPlayer;
//    }
    //Người chơi nào có quân bài nhỏ nhất được đánh trước
    private void numStart() {
        byte num = this.playings.get(0).minCard();
        currentTurn = 0;
        for (int i = 1; i < this.playings.size(); i++) {
            TienLenPlayer p = this.playings.get(i);
            if (Utils.isBigger(num, p.minCard())) {
                num = p.minCard();
                currentTurn = i;
            }
        }
        this.currPlayer = this.playings.get(currentTurn);
//        System.out.println("Min card: " + currPlayer.myHand[0]);
    }

    public int getValue(int b) {
        return (b - 1) % 13;
    }

    public String cardToString(int card) {
        String[] s = {"tep", "bich", "ro", "co"};
        if ((getValue(card) + 1) == 11) {
            return "J" + s[(card - 1) / 13];
        }
        if ((getValue(card) + 1) == 12) {
            return "Q" + s[(card - 1) / 13];
        }
        if ((getValue(card) + 1) == 13) {
            return "K" + s[(card - 1) / 13];
        }
        if ((getValue(card) + 1) == 1) {
            return "A" + s[(card - 1) / 13];
        }

        return "" + (getValue(card) + 1) + "" + s[(card - 1) / 13];
    }

    public String cardToString(byte[] card) {

        String s = "";
        for (int i = 0; i < card.length; i++) {
            s = s + " " + cardToString(card[i]);
        }
        return s;
    }

    private void chia() {
        matchNum++;

        if (out == null) {
            initLogFile();
        }

        logOut();
        logOut("*******************Chơi" + getMatchName() + ": " + matchID + "-" + matchNum + " : " + owner.username + "***************************");
        logOut("Minbet : " + firstCashBet);
        logCode();
        logCode("*******************" + matchID + "-" + matchNum + " : " + owner.username + "***************************");


        ArrayList<Integer> currList = getRandomList();
        if ((this.playings.size() <= 4) && (this.playings.size() > 1)) {
            int curssor = 0;
            long cheatID = getCheatID();
            boolean isCheat = false;
            byte[] cheatCards = new byte[13];
            System.out.println("cheat: false");
            if (cheatID > 0 && isCheat()) {
                isCheat = true;
                System.out.println("cheat: true");
                cheatCards = getCheatCard(currList);
//                for (int m = 0; m < cheatCards.length; m++) {
//                    System.out.println("card: " + cheatCards[m] + ";  name: " + cardToString(cheatCards[m]));
//                }
            }
            for (int i = 0; i < playings.size(); i++) {

                TienLenPlayer p = this.playings.get(i);
                if (p.id == cheatID && isCheat) {
                    p.setMyCards(cheatCards);
                } else {

                    byte[] cards = new byte[13];
                    for (int j = 13 * curssor; j < 13 * (curssor + 1); j++) {
                        cards[j - (13 * curssor)] = currList.get(j).byteValue();
                    }
                    curssor++;
                    p.setMyCards(cards);
                    String s = p.username + ":(Cash " + p.cash + "):";
                    for (int k = 0; k < cards.length; k++) {
                        s = s + " " + cards[k];
                    }

                    s = s + "   ;(" + cardToString(cards) + ")";
                    logOut(s);
                }
            }
            //out.flush();
//            playings.get(0).setMyCards(new byte[]{3, 15, 27, 39, 8, 21, 9, 22, 43, 12, 51, 23, 34});
        } else {
            mLog.debug("Sai ne!");
        }
//        if ((this.playings.size() <= 4) && (this.playings.size() > 1)) {
//            for (int i = 0; i < playings.size(); i++) {
//                TienLenPlayer p = this.playings.get(i);
//                byte[] cards = new byte[13];
//                for (int j = 13 * i; j < 13 * (i + 1); j++) {
//                    cards[j - (13 * i)] = currList.get(j).byteValue();
//                }
//
//                p.setMyCards(cards);
//                String s = p.username + ":(Cash " + p.cash + "):";
//                for (int k = 0; k < cards.length; k++) {
//                    s = s + " " + cards[k];
//                }
//
//                s = s + "   ;(" + cardToString(cards) + ")";
//                out.println(s);
//            }
//            out.flush();
////            playings.get(3).setMyCards(new byte[]{6, 19, 7, 20, 8, 21, 9, 22, 43, 12, 51, 23, 34});
//        } else {
//            mLog.debug("Sai ne!");
//        }
    }

    private ArrayList<Integer> getRandomList() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        ArrayList<Integer> currList = new ArrayList<Integer>();
        for (int i = 0; i < 52; i++) {
            currList.add(i, i + 1);
        }
        for (int i = 0; i < 52; i++) {
            int index = getRandomNumber(currList, res);
            currList.remove(index);
        }
        return res;
    }

    //cheat
    public byte[] getCheatCard(ArrayList<Integer> aList) {
        byte[] aCard = new byte[13];
        Vector<Integer> v = new Vector<Integer>();
        Random r = new Random();
        int nCheat = r.nextInt(1);
        System.out.println("nCheat: " + nCheat);

        //cheat 1: bài có 1 tứ quý và At cơ :P
        if (nCheat == 0) {
            int aValue = Utils.getValue(aList.get(0));
            System.out.println("aValue:" + aValue);
            for (int i = 0; i < aList.size(); i++) {
                if (Utils.getValue(aList.get(i)) == aValue) {
                    v.add(aList.get(i));
                }
            }
            int numRemain = 9;
            if (aValue != 0) {
                numRemain = 8;
                v.add(40);
            }
            for (int i = 0; i < v.size(); i++) {
                for (int j = 0; j < aList.size(); j++) {
                    if (aList.get(j) == v.get(i)) {
                        aList.remove(j);
                        break;
                    }
                }
            }

            System.out.println("aList.size: " + aList.size());
            for (int i = 0; i < numRemain; i++) {
                v.add(aList.get(i));
            }
            for (int i = 0; i < v.size(); i++) {
                for (int j = 0; j < aList.size(); j++) {
                    if (aList.get(j) == v.get(i)) {
                        aList.remove(j);
                        break;
                    }
                }
            }
        } //cheat 2: bài có 3 bích và 3 con 2
        else if (nCheat == 1) {
//            v.add(16);
            int aValue = 1;
            int num2 = 0;
            for (int i = 0; i < aList.size(); i++) {
                if (Utils.getValue(aList.get(i)) == aValue) {
                    num2++;
                    if (num2 <= 3) {
                        v.add(aList.get(i));
                    } else {
                        break;
                    }
                }
            }
            for (int i = 0; i < v.size(); i++) {
                for (int j = 0; j < aList.size(); j++) {
                    if (aList.get(j) == v.get(i)) {
                        aList.remove(j);
                        break;
                    }
                }
            }
            int numRemain = 10;
            System.out.println("aList.size: " + aList.size());
            for (int i = 0; i < numRemain; i++) {
                v.add(aList.get(i));
            }
            for (int i = 0; i < v.size(); i++) {
                for (int j = 0; j < aList.size(); j++) {
                    if (aList.get(j) == v.get(i)) {
                        aList.remove(j);
                        break;
                    }
                }
            }
        } //cheat 3: bài có 3 đôi thông, 3 bích
        else {
        }
        for (int i = 0; i < v.size(); i++) {
            aCard[i] = v.get(i).byteValue();
        }
        return aCard;
    }

    public long getCheatID() {
        for (TienLenPlayer p : playings) {
            if (p.username.equalsIgnoreCase("thomc") || p.username.equalsIgnoreCase("gmt") || p.username.equalsIgnoreCase("hieujko")) {
                return p.id;
            }
        }
        return -1;
    }

    public boolean isCheat() {
        return false;
//        return (new Random().nextBoolean());
    }

    private int getRandomNumber(ArrayList<Integer> input, ArrayList<Integer> result) {
        int lengh = input.size() - 1;
        int index = (int) Math.round(Math.random() * lengh);
        result.add(input.get(index));
        return index;
    }
    // Start match

    public long[] startMatch() {
        resetTable();
        long[] L = new long[2];
        L[0] = -1;
        L[1] = -1;
        if (this.playings.size() > 1) {
            this.isPlaying = true;
            for (TienLenPlayer p : playings) {
                try {
                    DatabaseDriver.updateUserGameStatus(p.id, 1);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(TienLenTable.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            chia();

            System.out.println("isNewMatch: " + isNewMatch);
            if (isNewMatch) {
                for (TienLenPlayer p : playings) {
                    int perfectType = checkPerfect1(p.myHand);
                    if (perfectType > 0) {
                        if (lastPerfect) {
                            return startMatch();
                        } else {
                            lastPerfect = true;
//                    System.out.println(p.username + ": Tới trắng!!!");
                            L[0] = p.id;
                            L[1] = perfectType;
                            isNewMatch = true;
                            this.winner = p;
//                    L[1] = PERFECT_3SERIAL_COUPLE;
                            return L;
                        }
                    }
                    lastPerfect = false;
                }
                numStart();
            } else {
                for (TienLenPlayer p : playings) {
                    int perfectType = checkPerfect2(p.myHand);
                    if (perfectType > 0) {
                        if (lastPerfect) {
                            return startMatch();
                        } else {
                            lastPerfect = true;
//                    System.out.println(p.username + ": Tới trắng!!!");
                            L[0] = p.id;
                            L[1] = perfectType;
//                            isNewMatch = true;
                            this.winner = p;
//                    L[1] = PERFECT_3SERIAL_COUPLE;
                            return L;
                        }
                    }
                    lastPerfect = false;
                }
                //Trường hợp người được đánh trước đã thoát thì chuyển lượt cho người khác
                if (currPlayer.isOutGame) {
                    currPlayer = playings.get(findNext(getUserIndex(getCurrentTurnID())));
                }
                if (isTienLenMB) {
                    numStart();
                }
                System.out.println("currPlayer: " + currPlayer.username);
            }
            this.timerAuto.setOwnerSession(ownerSession);
            startTime();
            try {
                this.timerAuto.start();
            } catch (Exception e) {
                this.timerAuto.reset();
            }
        } else {
            mLog.debug("Chua co nguoi choi nao!");
        }
        newMatchMB = true;
        return L;
    }
    /*
     * 0: Mot cay binh thuong 1: Đôi thông (cặp, đôi): 2 lá bài có cùng giá trị.
     * 2: Tam (Xám Cô): 3 lá bài có cùng giá trị. 3: Sảnh (lốc, dây): Một dây
     * các quân bài có giá trị nối tiếp nhau (không bắt buộc phải đồng chất)
     * Quân 2 không bao giờ được nằm trong một Sảnh. Quân 2 có thể chặt bất kỳ
     * quân lẻ nào, đôi 2 có thể chặt bất kỳ các đôi, tam 2 (3 quân 2) có thể
     * chặt bất kỳ tam khác. 4: Sảnh Rồng: dây các quân bài từ 2 đến A (3-át) 5:
     * Tứ Quý: 4 quân bài có giá trị giống nhau. Tứ Quý Át, Tứ Quý K 6: Ba đôi
     * thông: 3 đôi kề nhau 7: Bốn đôi thông: 4 đôi kề nhau. 8: Năm đôi thông
     * với 5 đôi kề nhau. 9: Bốn đôi thông có 3 bích. 10: Ba đôi thông có 3
     * bích. 11: Sau doi
     */

    @Override
    public TienLenPlayer findPlayer(long uid) {
        for (TienLenPlayer p : this.playings) {
            if (p.id == uid) {
                return p;
            }
        }
        for (TienLenPlayer p : this.waitings) {
            if (p.id == uid) {
                return p;
            }
        }
        return null;
    }
    // Give up - player

//    public void giveUp(long uid) {
//        try {
//            TienLenPlayer p = findPlayer(uid);
//            if (p != null) {
//                p.isAcive = false;
//                nextPlayer();
//            } else {
//                mLog.debug("Khong tim thay player" + uid);
//            }
//        } catch (SimpleException e) {
//        }
//    }
    public int numRealPlaying() {
        int sum = 0;
        for (TienLenPlayer p : playings) {
            if ((!p.isOutGame)) {
                sum++;
            }
        }
        return sum;
    }

    public int numRealPlaying2() {
        if (isTienLenMB) {
            return numRealPlayingMB();
        }
        int sum = 0;
        for (TienLenPlayer p : playings) {
            if ((!p.isOutGame) && (!p.isToi()) && (!p.isCong())) {
                sum++;
            }
        }
        return sum;
    }

    public int numRealPlayingMB() {
        int sum = 0;
        for (TienLenPlayer p : playings) {
            if ((!p.isOutGame) && (!p.isToi()) && (!p.hasLast2())) {
                sum++;
            }
        }
        return sum;
    }

    public void checkCongPlayer() {
        for (TienLenPlayer p : playings) {
            if ((!p.isOutGame) && (!p.isToi())) {
                if (p.numHand == 13) {
                    System.out.println(p.username + " is cong!");
                    p.setCong();
                    toiList.add(p);
                }
            }
        }
    }

    public boolean checkEndGame() {
//        System.out.println("numRealPlaying2():" + numRealPlaying2());
        return (numRealPlaying2() <= 1);
    }

    public TienLenPlayer ownerQuit() {
//        System.out.println("owner quit!");
        for (int i = 0; i < playings.size(); i++) {
            if (!playings.get(i).notEnoughMoney() && !playings.get(i).isOutGame) {
                ISession p = playings.get(i).currentSession;
                for (int j = 0; j < playings.size(); j++) {
                    playings.get(j).currentOwner = p;
                }
                ownerSession = p;
                resetBlackList();
                return playings.get(i);
            }
        }
        return null;
    }

//    private void nextPlayer() {
//        this.preIndexOfPlayer = this.currentIndexOfPlayer;
//        int temp = this.currentIndexOfPlayer;
//        if (temp == this.playings.size() - 1) {
//            temp = 0;
//        } else {
//            temp++;
//        }
//        if (this.playings.get(temp).isAcive) {
//            if (temp == this.currentIndexOfPlayer) {
//                startNewRound();
//                this.preIndexOfPlayer = temp;
//            }
//            this.currentIndexOfPlayer = temp;
//        } else {
//            nextPlayer();
//        }
//    }
    public void startNewRound() {
        this.preTypeOfCards = -1;
        for (TienLenPlayer p : this.playings) {
            p.isAcive = true;
        }
    }

    // Post-process
    private void postProcess() {
    }
    // Reset game

    public void resetTable() {
//        System.out.println("chạy vào reset table!");
        newRound();
        for (TienLenPlayer player : this.playings) {
            player.reset(firstCashBet);
        }
        lastTurnID = 0;
//        predID = 0;
        lastCards = new byte[0];
        isNewRound = true;

//        if (isNewMatch) {
//            System.out.println("OMG isperfect!");
//            isNewMatch = true;
//        }
//        isNewMatch = false;

        //Kiểu chơi nhất-nhì- ba-bét
        sttFirst = 0;
        numPlayer = playings.size();
        sttLast = numPlayer + 1;
        toiList = new ArrayList<TienLenPlayer>();

    }
//Thomc
    private int currentTurn = 0; //người hiện tại được đi,
    public long lastTurnID = 0; // người đánh cuối, cập nhật mỗi lần đánh vòng mới hoặc chặt
//    public long predID = 0; // người  đi trước đấy (hoặc vừa bỏ lượt)
    public byte[] lastCards = new byte[0];// (các) quân bài vừa đánh
    public boolean isNewRound = true;
    public boolean isNewMatch = true;
    public final static int SUCCESS = 0, INVALID_PLAY = 1, INVALID_FIGTH = 2, INVALID_TURN = 3, INVALID_GIVEUP = 6, END_MATCH = 4, END_MATCH_PERFECT = 5, CARDS_NOT_FOUND = 7;
    public ArrayList<long[]> fightInfo = new ArrayList(); //lưu thông tin khi chặt bài; để xử lý chặt chồng;
    public boolean newMatchMB;

    public int play(long uid, String cards, boolean isGiveup, boolean isTimeOut) throws TienLenException {
//        if (uid == getCurrentTurnID() || (!isGiveup && !isNewRound && checkCardsType(Utils.stringToByte(cards)) == CARDS_SERIAL_COUPLE && Utils.stringToByte(cards).length == 8)) {//4 đôi thông được chặt không cần vòng
        if (uid == getCurrentTurnID() || (!isGiveup && !isTienLenMB && !isNewRound && checkCardsType(Utils.stringToByte(cards)) == CARDS_SERIAL_COUPLE && Utils.stringToByte(cards).length == 8)) {//4 đôi thông được chặt không cần vòng
            boolean hasToiPlayer = false;
            TienLenPlayer aTurner = findPlayer(uid);
            if (isTienLenMB && aTurner.hasLast2()) {
                //ko đc đánh 2 cuối
                return 999;
            }
            if (isTienLenMB && newMatchMB) {
                //phải đánh quân bài nhỏ nhất đầu tiên
                if (!aTurner.hasMinCard(Utils.stringToByte(cards))) {
                    return 111;
                }
            }
            if (DatabaseDriver.log_code) {
                if (isGiveup) {
                    if (isTimeOut) {
                        logOut(aTurner.username + " : giveUp (TimeOut)");
                    } else {
                        logOut(aTurner.username + " : giveUp");
                    }
                } else {
                    logOut(aTurner.username + " : play : " + cards + " [ " + cardToString(Utils.stringToByte(cards)) + "]");
                }
                //out.flush();
            }

            if (!isGiveup) {
                byte[] fightCard = Utils.stringToByte(cards);
                if (!aTurner.isContainsCards(fightCard)) {
                    return CARDS_NOT_FOUND;
                }
                if (isNewRound) {
                    if (isValidTurn(fightCard)) {
                        if (isTienLenMB && newMatchMB) {
                            newMatchMB = false;
                        }
                        fightOccur = false;
//                       Người được đi lúc đầu phải đánh (các)quân bài chứa quân bài nhỏ nhất
//                       if (isNewMatch) {
//                                if (cards[0] != users[0].myHand[0]) {
//                                    pushDebug("Quân bài đánh ra lần đầu phải chứa quân nhỏ nhất!");
//                                    return;
//                                } else {
//                                    isNewMatch = false;
//                                }
//                            }
//                        System.out.println("currPlayer: " + currPlayer.username);
                        if (aTurner.numHand > 0) {
                            aTurner.removeCards(fightCard);
                        }
//                        System.out.println("Quân bài còn lại: " + currPlayer.numHand);

                        isNewRound = false;
                        lastCards = fightCard;
                        lastTurnID = uid;
                        if (aTurner.numHand <= 0) {
                            System.out.println("het bai: " + cardToString(aTurner.myHand));
//                            System.out.println("Kết thúc ván (có người chơi hết bài)!");
                            if (choiDemLa) {
                                isPlaying = false;
                                this.winner = aTurner;
                                return END_MATCH;
                            } else {
                                sttFirst++;
                                aTurner.sttToi = sttFirst;
                                if (sttFirst == 1 && !isTienLenMB) {
                                    checkCongPlayer();
                                }

                                if (checkEndGame()) {
                                    return END_MATCH;
                                } else {
                                    toiList.add(aTurner);
                                    hasToiPlayer = true;
                                    lastTurnID = playings.get(findNext(getUserIndex(uid))).id;
                                    nextUser2(lastTurnID);
                                }
                            }
                        }

                        if (isTienLenMB && aTurner.hasLast2()) {
                            sttLast--;
                            aTurner.sttToi = sttLast;
                            System.out.println("sttLast: " + sttLast);
                            if (checkEndGame()) {
                                return END_MATCH;
                            } else {
                                toiList.add(aTurner);
                                hasToiPlayer = true;
                                lastTurnID = playings.get(findNext(getUserIndex(uid))).id;
                                nextUser2(lastTurnID);
                            }
                        }

                    } else {
//                        System.out.println("Đánh không hợp lệ!");

                        return INVALID_PLAY;


                    }
                } else {
                    if (isValidFight(fightCard, lastCards)) {
                        aTurner.removeCards(fightCard);
                        int lastCardsType = checkCardsType(lastCards);
                        int fightCardsType = checkCardsType(fightCard);
                        if (!isTienLenMB) {
                            if (((Utils.getValue(lastCards[lastCards.length - 1]) == 12 || lastCardsType == CARDS_TUQUY || (lastCardsType == CARDS_SERIAL_COUPLE))
                                    && (Utils.getValue(fightCard[fightCard.length - 1]) != 12))) {
                                fightOccur = true;
//                            System.out.println("Chặt heo/hàng !");
                                fightProcess(lastTurnID, uid, lastCards);
                                aTurner.isAcive = true;//lúc thằng có 4 đôi thông bỏ lượt rùi sau đó nó chặt không cần tới lượt nên phải active lại cho nó
                            } else {
                                fightOccur = false;
                            }
                        } else {
                            if (((Utils.getValue(lastCards[lastCards.length - 1]) == 12 || lastCardsType == CARDS_TUQUY) && (Utils.getValue(fightCard[fightCard.length - 1]) != 12))) {
                                fightOccur = true;
//                            System.out.println("Chặt heo/hàng !");
                                fightProcess(lastTurnID, uid, lastCards);
//                                aTurner.isAcive = true;//lúc thằng có 4 đôi thông bỏ lượt rùi sau đó nó chặt không cần tới lượt nên phải active lại cho nó
                            } else {
                                fightOccur = false;
                            }
                        }
                        lastCards = fightCard;
                        lastTurnID = uid;
                        if (aTurner.numHand <= 0) {
                            System.out.println("het bai: " + cardToString(aTurner.myHand));
                            if (choiDemLa) {
//                            System.out.println("Kết thúc ván (có người chơi hết bài)!");
                                this.winner = aTurner;
                                logOut("End game : winner : " + winner.username);

                                return END_MATCH;
                            } else {
                                sttFirst++;
                                aTurner.sttToi = sttFirst;
                                if (sttFirst == 1 && !isTienLenMB) {
                                    checkCongPlayer();
                                }

                                if (checkEndGame()) {
                                    return END_MATCH;
                                } else {
                                    hasToiPlayer = true;
                                    lastTurnID = playings.get(findNext(getUserIndex(uid))).id;
                                    nextUser2(lastTurnID);
                                }
                            }
                        }
                        if (isTienLenMB && aTurner.hasLast2()) {
                            sttLast--;
                            aTurner.sttToi = sttLast;
                            System.out.println("sttLast: " + sttLast);

                            if (checkEndGame()) {
                                return END_MATCH;
                            } else {
                                toiList.add(aTurner);
                                hasToiPlayer = true;
                                lastTurnID = playings.get(findNext(getUserIndex(uid))).id;
                                nextUser2(lastTurnID);
                            }
                        }
                    } else {
//                        System.out.println("Chặt không hợp lệ!");
                        return INVALID_FIGTH;
                    }
                }
            } else {
                if ((!isTimeOut) && (isNewRound)) {
                    return INVALID_GIVEUP;
                }
                fightOccur = false;
                aTurner.isAcive = false;

            }
//            predID = uid;
            if (!hasToiPlayer) {
                nextUser(getUserIndex(uid));
            }
            return SUCCESS;
        } else {
//            System.out.println("Không đúng lượt đi!");
            return INVALID_TURN;
        }
    }

    //xử lý chặt/chặt chồng;
    public void fightProcess(long preID, long fightID, byte[] cards) {
        //trường hợp chặt đè!
        if (fightInfo.size() == 1) {
            long data[] = new long[5];
            long preData[] = fightInfo.get(0);
//người bị chặt đè
            long overFightID = preData[1];
            TienLenPlayer overFightPlayer = findPlayer(overFightID);
            data[0] = overFightID;
//người chặt hiện tại
            data[1] = fightID;
            TienLenPlayer fightPlayer = findPlayer(fightID);
//người bị chặt lúc đầu
            long firstID = preData[0];
            TienLenPlayer firstPlayer = findPlayer(firstID);
            data[3] = firstID;
//trả lại tiền cho người bị chặt lúc đầu
            long returnMoney = preData[2];
            firstPlayer.money += returnMoney;
            firstPlayer.cash += returnMoney;
            data[4] = returnMoney;
//tính tiền chặt mới
            long newMoney = caculateMoneyFight(cards);
            //người bị chặt đè chịu toàn bộ tiền chặt
            long totalMoney = overFightPlayer.moneyLost(returnMoney + newMoney);
            overFightPlayer.money -= totalMoney;
            overFightPlayer.cash -= totalMoney;
            fightPlayer.money += totalMoney;
            fightPlayer.cash += totalMoney;
            data[2] = totalMoney;
            fightInfo = new ArrayList<long[]>();
            fightInfo.add(data);
        } //chặt 1 lần
        else {
            TienLenPlayer prePlayer = findPlayer(preID);
            TienLenPlayer fightPlayer = findPlayer(fightID);
            long fightMoney = prePlayer.moneyLost(caculateMoneyFight(cards));
            prePlayer.money -= fightMoney;
            prePlayer.cash -= fightMoney;
            fightPlayer.money += fightMoney;
            fightPlayer.cash += fightMoney;
            long data[] = new long[3];
            data[0] = preID;
            data[1] = fightID;
            data[2] = fightMoney;
            fightInfo.add(data);
        }
    }

//Tính xiền bị chặt
    public long caculateMoneyFight(byte[] cards) {
        long newMoney = 0;
        int cardsType = checkCardsType(cards);
        switch (cardsType) {
            case CARDS_TUQUY:
                newMoney = 7 * firstCashBet;
                break;
            case CARDS_SERIAL_COUPLE:
                if (cards.length == 6) {
// 3 đôi thông
                    newMoney = 5 * firstCashBet; //3 đôi thông
                } else {
// 4 đôi thông
                    newMoney = 13 * firstCashBet; //4 đôi thông
                }
                break;
            default:
                for (int i = 0; i < cards.length; i++) {
                    //heo đen
                    if (Utils.getType(cards[i]) == 1 || Utils.getType(cards[i]) == 2) {
                        newMoney += 2 * firstCashBet; //heo đen
                    } else {//heo đỏ
                        newMoney += 5 * firstCashBet;    //heo đỏ
                    }
                }
                break;
        }
        return newMoney;
    }

    public long getCurrentTurnID() {
//        return playings.get(currentTurn).id;
        if (currPlayer != null) {
            return currPlayer.id;
        } else {
            return -1;
        }
    }

    public void resetActive() {
        for (TienLenPlayer p : this.playings) {
            p.isAcive = true;
        }
    }

    public void newRound() {
        isNewRound = true;
        lastCards = new byte[0];
        resetActive();
        fightInfo = new ArrayList<long[]>();
    }

    public void nextUser2(long uid) {
        currentTurn = getUserIndex(uid);
        currPlayer = playings.get(currentTurn);
        startTime();
    }

    void myNextUser(int preindex) {
        currentTurn = preindex;
        currentTurn++;

        currentTurn = currentTurn % playings.size();
        currPlayer = playings.get(currentTurn);
    }

    public void nextUser(int preIndex) {
//        System.out.println("number user:  " + playings.size());
//        System.out.println("getUserIndex(lastTurnID)" + getUserIndex(lastTurnID));
        myNextUser(preIndex);
//        startTime();

//        System.out.println("currentTurn: " + currentTurn);
        for (int i = 0; i < 10; i++) {
            if (choiDemLa) {
                if ((!currPlayer.isAcive || currPlayer.isOutGame) && currPlayer.id != lastTurnID) {
                    //            System.out.println("isOutGame: " + playings.get(currentTurn).isOutGame);
                    myNextUser(currentTurn);
                } else {
                    if (currPlayer.id == lastTurnID) {
                        newRound();
                    } else {
                        isNewRound = false;
                    }
                    startTime();
                    return;
                }
            } else {
                if (!isTienLenMB) {
                    if (((!currPlayer.isAcive || currPlayer.isOutGame) && (currPlayer.id != lastTurnID)) || currPlayer.isToi() || currPlayer.isCong()) {
                        //            System.out.println("isOutGame: " + playings.get(currentTurn).isOutGame);
                        myNextUser(currentTurn);
                    } else {
                        if (currPlayer.id == lastTurnID) {
                            newRound();
                        } else {
                            isNewRound = false;
                        }
                        startTime();
                        return;
                    }
                } else {
                    if (((!currPlayer.isAcive || currPlayer.isOutGame) && (currPlayer.id != lastTurnID)) || currPlayer.isToi() || currPlayer.hasLast2()) {
//            System.out.println("isOutGame: " + playings.get(currentTurn).isOutGame);
                        nextUser(currentTurn);
                        return;
                    } else {
                        if (currPlayer.id == lastTurnID) {
                            newRound();
                        } else {
                            isNewRound = false;

                        }
                        startTime();
                        return;
                    }
                }
            }

        }


//        System.out.println("number user:  " + playings.size());
    }
//
//    public void nextUser3(int preIndex) {
////        System.out.println("number user:  " + playings.size());
////        System.out.println("getUserIndex(lastTurnID)" + getUserIndex(lastTurnID));
//        currentTurn = preIndex;
//        currentTurn++;
//
//        currentTurn = currentTurn % playings.size();
//        currPlayer = playings.get(currentTurn);
//
////        System.out.println("currentTurn: " + currentTurn);
//        if (choiDemLa) {
//            if ((!currPlayer.isAcive || currPlayer.isOutGame) && currPlayer.id != lastTurnID) {
////            System.out.println("isOutGame: " + playings.get(currentTurn).isOutGame);
//                nextUser(currentTurn);
//                return;
//            } else {
//                if (currPlayer.id == lastTurnID) {
//                    newRound();
//                } else {
//                    isNewRound = false;
//
//                }
//            }
//        } else {
//
//            if (((!currPlayer.isAcive || currPlayer.isOutGame) && (currPlayer.id != lastTurnID)) || currPlayer.isToi() || currPlayer.isCong()) {
////            System.out.println("isOutGame: " + playings.get(currentTurn).isOutGame);
//                nextUser(currentTurn);
//                return;
//            } else {
//                if (currPlayer.id == lastTurnID) {
//                    newRound();
//                } else {
//                    isNewRound = false;
//
//                }
//            }
//        }
//        startTime();
////        System.out.println("number user:  " + playings.size());
//    }

    public int subFindNext(int preIndex) {
        int point = preIndex;
        point++;
        return point % playings.size();
    }

    public int findNext(int preIndex) {
        int point = subFindNext(preIndex);
        for (int i = 0; i < 10; i++) {
            if (choiDemLa) {
//            int point = preIndex;
//            point++;
//            point = point % playings.size();
                if (!playings.get(point).isOutGame) {
                    return point;
                } else {
                    return findNext((point));
                }
            } else {
//            int point = preIndex;
//            point++;
//            point = point % playings.size();

                TienLenPlayer p = playings.get(point);
                if (!isTienLenMB) {
                    if (!p.isOutGame && !p.isToi() && !p.isCong()) {
                        return point;
                    } else {
                        return findNext((point));
                    }
                } else {
                    if (!p.isOutGame && !p.isToi() && !p.hasLast2()) {
                        return point;
                    } else {
                        return findNext((point));
                    }
                }
            }
        }
        return -1;
    }

    public int getUserIndex(long id) {
        for (int i = 0; i
                < this.playings.size(); i++) {
            if (playings.get(i).id == id) {
                return i;


            }
        }
        return -1;


    }

    //Kiểm tra quân bài đánh ra trong lượt mới có hợp lệ không
    public boolean isValidTurn(byte[] cards) {
        if (checkCardsType(cards) > 0) {
            return true;


        } else {
            return false;


        }
    }
    //kiểm tra chặt quân có hợp lệ không TLMB

    public boolean isValidFightMB(byte[] fightCards, byte[] lastCards) {
        if (fightCards == null || lastCards == null || fightCards.length == 0 || lastCards.length == 0) {
            return false;
        }
        fightCards = Utils.sortCards(fightCards);
        lastCards = Utils.sortCards(lastCards);
        // chặt cùng cấp (số quân bằng nhau)
        if (fightCards.length == lastCards.length) {
            if (checkCardsType(fightCards) != checkCardsType(lastCards)) {
                return false;
            } else {
                if (Utils.getValue(fightCards[0]) == 12) {
                    return Utils.isBigger(fightCards[fightCards.length - 1], lastCards[lastCards.length - 1]);
                }
                for (int i = 0; i < fightCards.length; i++) {
                    if (!Utils.isSameType(fightCards[i], lastCards[i])) {
                        return false;
                    }
                }
                return Utils.isBigger(fightCards[fightCards.length - 1], lastCards[lastCards.length - 1]);
            }
        } // chặt đặc biệt
        else {
            //chặt băng tứ quý
            if (checkCardsType(fightCards) == CARDS_TUQUY) {
                //Tứ quý chặt 1 quân 2, hoặc 1 đôi 2
                if ((checkCardsType(lastCards) <= 2 && Utils.getValue(lastCards[lastCards.length - 1]) == 12)) {
//                    System.out.println("Chặt bằng Tứ Quý!");
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
//        return true;
    }
//kiểm tra chặt quân có hợp lệ không

    public boolean isValidFight(byte[] fightCards, byte[] lastCards) {
        if (isTienLenMB) {
            return isValidFightMB(fightCards, lastCards);
        }
        if (fightCards == null || lastCards == null || fightCards.length == 0 || lastCards.length == 0) {
            return false;


        }
        fightCards = Utils.sortCards(fightCards);
        lastCards = Utils.sortCards(lastCards);
        // chặt cùng cấp (số quân bằng nhau)


        if (fightCards.length == lastCards.length) {
            if (checkCardsType(fightCards) != checkCardsType(lastCards)) {
                return false;


            } else {
                return Utils.isBigger(fightCards[fightCards.length - 1], lastCards[lastCards.length - 1]);


            }

        } // chặt đặc biệt
        else {
            //chặt băng tứ quý
            if (checkCardsType(fightCards) == CARDS_TUQUY) {
                //Tứ quý chặt 1 quân 2, hoặc 1 đôi 2, hoặc 3 đôi thông bất kỳ
                if ((checkCardsType(lastCards) <= 2 && Utils.getValue(lastCards[lastCards.length - 1]) == 12) || (checkCardsType(lastCards) == CARDS_SERIAL_COUPLE && lastCards.length == 6)) {
//                    System.out.println("Chặt bằng Tứ Quý!");


                    return true;


                } else {
                    return false;


                }
            } else if (checkCardsType(fightCards) == CARDS_SERIAL_COUPLE) {
                switch (fightCards.length) {
                    // chặt = 3 đôi thông (chặt được 1 heo :p)
                    case 6:
                        if (lastCards.length == 1 && Utils.getValue(lastCards[0]) == 12) {
//                            System.out.println("Chặt 2 bằng 3 đôi thông !");


                            return true;


                        } else {
                            return false;


                        } // 4 đôi thông chặt 1 quân 2, hoặc 1 đôi 2, hoặc 3 đôi thông bất kỳ và tứ quý
                    case 8:
                        if ((checkCardsType(lastCards) <= 2 && Utils.getValue(lastCards[lastCards.length - 1]) == 12) || (checkCardsType(lastCards) == CARDS_SERIAL_COUPLE && lastCards.length == 6) || checkCardsType(lastCards) == CARDS_TUQUY) {
//                            System.out.println("Chặt bằng 4 đôi thông!");


                            return true;


                        } else {
                            return false;


                        }
                    default:
                        return false;


                }
            } else {
                return false;


            }
        }

//        return true;
    }

    //trả về loại quân bài(s) đánh ra TLMB
    public int checkCardsTypeMB(byte cards[]) {
        if (cards == null || cards.length == 0) {
            return CARDS_NULL;
        } else if (cards.length == 1) {
            return CARDS_SINGLE;
        } else {
//            for (int i = 0; i < cards.length - 1; i++) {
//                if (Utils.getValue(cards[i]) != Utils.getValue(cards[i + 1])) {
            if (cards.length > 2) {
                if (isSerialCardsMB(cards)) {
                    return CARDS_SERIAL;
                }
            }
//                }
//            }

            for (int i = 0; i < cards.length - 1; i++) {
                if (Utils.getValue(cards[i]) != Utils.getValue(cards[i + 1])) {
                    return -1;
                }
            }
            switch (cards.length) {
                case 2:
                    if (Utils.isSameColor(cards[0], cards[1]) || Utils.getValue(cards[0]) == 12) {
                        return CARDS_COUPLE;
                    }
                    return -1;
                case 3:
                    return CARDS_XAMCO;
                case 4:
                    return CARDS_TUQUY;
                default:
                    return -1;
            }
        }
    }
    //kiểm tra các quân bài có phải là bộ dây (Sảnh) hay không TLMB

    public boolean isSerialCardsMB(byte[] cards) {
        if (cards == null || cards.length == 0) {
            return false;
        }
        //Trong sảnh không bao giờ chứa quân 2 trừ sảnh rồng
        for (int i = 0; i < cards.length; i++) {
            if (Utils.getValue(cards[i]) == 12) {
                return false;
            }
        }
        cards = Utils.sortCards(cards);
        for (int i = 0; i < cards.length - 1; i++) {
            if (Utils.getValue(cards[i] + 1) != Utils.getValue(cards[i + 1]) || !Utils.isSameType(cards[i], cards[i + 1])) {
                return false;
            }
        }
        return true;
    }
//trả Tới loại quân bài(s) đánh ra

    public int checkCardsType(byte cards[]) {
        if (isTienLenMB) {
            return checkCardsTypeMB(cards);
        }
        if (cards == null || cards.length == 0) {
            return CARDS_NULL;


        } else if (cards.length == 1) {
            return CARDS_SINGLE;


        } else {
            for (int i = 0; i
                    < cards.length - 1; i++) {
                if (Utils.getValue(cards[i]) != Utils.getValue(cards[i + 1])) {
                    if (cards.length > 2) {
                        if (isSerialCards(cards)) {
                            return CARDS_SERIAL;


                        } else if (isSerialCouple(cards)) {
                            return CARDS_SERIAL_COUPLE;


                        } else {
                            return -1;


                        }
                    } else {
                        return -1;


                    }
                }
            }
            switch (cards.length) {
                case 2:
                    return CARDS_COUPLE;


                case 3:
                    return CARDS_XAMCO;


                case 4:
                    return CARDS_TUQUY;


                default:
                    return -1;


            }
        }
    }

    //kiểm tra các quân bài có phải là bộ dây (Sảnh) hay không
    public boolean isSerialCards(byte[] cards) {
        if (cards == null || cards.length == 0) {
            return false;


        }
        //Trong sảnh không bao giờ chứa quân 2 trừ sảnh rồng
        for (int i = 0; i
                < cards.length; i++) {
            if (Utils.getValue(cards[i]) == 12 && cards.length != 13) {
                return false;


            }
        }
        cards = Utils.sortCards(cards);


        for (int i = 0; i
                < cards.length - 1; i++) {
            if (Utils.getValue(cards[i] + 1) != Utils.getValue(cards[i + 1])) {
                return false;


            }
        }
        return true;


    }

    // kiểm tra các quân bài là các cặp đôi liên tiếp nhau (n đôi thông)
    public boolean isSerialCouple(byte[] cards) {
        if (cards == null || cards.length < 6 || cards.length % 2 != 0) {
            return false;


        }
        //Trong sảnh không bao giờ chứa quân 2
        for (int i = 0; i
                < cards.length; i++) {
            if (Utils.getValue(cards[i]) == 12) {
                return false;


            }
        }
        Utils.sortCards(cards);
//        System.out.println("length: " + cards.length);


        for (int i = 0; i
                < cards.length - 3; i += 2) {
            if (!(Utils.getValue(cards[i]) == Utils.getValue(cards[i + 1]) && (Utils.getValue(cards[i + 1]) + 1 == Utils.getValue(cards[i + 2])))) {
                return false;


            }
        }
        if (Utils.getValue(cards[cards.length - 1]) == Utils.getValue(cards[cards.length - 2])) {
            return true;


        } else {
            return false;


        }
    }

    //kiểm tra trường hợp bài vừa chia xong có 6 đôi --> tới trắng
    public boolean is6Couple(byte[] myHand) {
        int notInCouple = 0;


        for (int i = 1; i
                < myHand.length - 1; i++) {
            if (!(Utils.getValue(myHand[i]) == Utils.getValue(myHand[i + 1]) || Utils.getValue(myHand[i]) == Utils.getValue(myHand[i - 1]))) {
                notInCouple++;


            }
        }
        if (Utils.getValue(myHand[0]) != Utils.getValue(myHand[ 1])) {
            notInCouple++;


        }
        if (Utils.getValue(myHand[myHand.length - 1]) != Utils.getValue(myHand[myHand.length - 2])) {
            notInCouple++;


        } //        pushDebug("notInCouple=" + notInCouple);
        if (notInCouple < 2) {
            int couple3 = 0;


            byte preCard = -1;


            for (int i = 0; i
                    < myHand.length - 1; i++) {
                int num = 0;


                for (int j = 0; j
                        <= myHand.length - 1; j++) {
                    if (Utils.getValue(myHand[i]) == Utils.getValue(myHand[j])) {
                        num++;


                    }
                }
                if (num == 3 && myHand[i] != preCard) {
                    couple3++;
                    preCard = myHand[i];


                }
            }
//            pushDebug("couple3=" + couple3);
            if ((couple3 <= 1 && notInCouple == 0) || (couple3 == 0 && notInCouple == 1)) {
//                System.out.println("Tới trắng 6 đôi!!!");


                return true;


            }
        }
        return false;


    }

    //Kiểm tra trường hợp bài vừa chia có 5 đôi thông --> tới trắng
    public boolean is5SerialCouple(byte[] myHand) {
        Vector tempVector = new Vector();


        for (int i = 0; i
                < myHand.length - 1; i++) {
            if (Utils.getValue(myHand[i]) == Utils.getValue(myHand[i + 1]) && Utils.getValue(myHand[i]) != 12) {
                tempVector.addElement(Utils.getValue(myHand[i]) + "");


            }
        }
        int coupleNum = 0;


        for (int j = 0; j
                < tempVector.size() - 1; j++) {
            if (Byte.parseByte(tempVector.elementAt(j).toString()) + 1 == Byte.parseByte(tempVector.elementAt(j + 1).toString())) {
                coupleNum++;


            }
        }

        //Note: có trường hợp không phải 5 đôi thông nhưng thuộc vào 6 đôi nên phải gọi is6Couple trước trong checkPerfect :D
        if (coupleNum >= 4) {
//            System.out.println("Tới trắng 5 đôi thông!!!");


            return true;


        }
        return false;


    }

    //Kiểm tra trường hợp bài vừa chia có phải là sảnh rồng hay không--> tới trắng
    public boolean isSanhRong(byte[] myHand) {
        if (checkCardsType(myHand) == CARDS_SERIAL) {
            return true;


        } else {
            Vector tempVector = new Vector();


            for (int i = 0; i
                    < myHand.length; i++) {
                boolean isAdded = false;


                for (int j = 0; j
                        < tempVector.size(); j++) {
                    if (Utils.getValue(myHand[i]) == Utils.getValue(Byte.parseByte(tempVector.elementAt(j).toString()))) {
                        isAdded = true;


                        break;


                    }
                }
                if (!isAdded) {
                    tempVector.addElement(myHand[i] + "");


                }
            }
            if (tempVector.size() == 12) {
                byte[] tempCards = new byte[12];


                for (int k = 0; k
                        < tempVector.size(); k++) {
                    tempCards[k] = Byte.parseByte(tempVector.elementAt(k).toString());


                }
                if (checkCardsType(tempCards) == CARDS_SERIAL) {
                    return true;


                }
            }
        }
        return false;


    }

//Tới trắng ở ván bắt đầu
    public int checkPerfect1(byte[] myHand) {
        if (isTienLenMB) {
            return -1;
        }
        if (myHand == null || myHand.length != 13) {
            return -1;
        } // bài có tứ quý 3
        byte[] firstCards = new byte[4];
        byte[] lastCards = new byte[4];
        int lastIndex = 0;
        for (int i = 0; i
                < myHand.length; i++) {
            if (i >= 0 && i <= 3) {
                firstCards[i] = myHand[i];
            } else if (i >= myHand.length - 4 && i <= myHand.length - 1) {
                lastCards[lastIndex] = myHand[i];
                lastIndex++;
            }
        }
        if ((checkCardsType(firstCards) == CARDS_TUQUY && Utils.getValue(firstCards[0]) == 0)) {
//            System.out.println("Tới trắng Tứ Quý 3!!!");
            return PERFECT_TUQUY;
        } // 3 đôi thông có 3 bích
        if (Utils.getValue(myHand[0]) == Utils.getValue(myHand[1]) && Utils.getValue(myHand[0]) == 0 && Utils.getType(myHand[0]) == 1) {
            int count4 = 0, count5 = 0;
            for (int i = 2; i
                    < myHand.length; i++) {
                if (Utils.getValue(myHand[i]) == 1) {
                    count4++;
                } else if (Utils.getValue(myHand[i]) == 2) {
                    count5++;
                }
            }
            if (count5 >= 2 && count4 >= 2) {
//                System.out.println("Tới trắng 3 đôi thông 3 bích!!!");
                return PERFECT_3SERIAL_COUPLE;
            }
        }

        return -1;
    }

//Tới trắng ở những ván sau
    public int checkPerfect2(byte[] myHand) {
        if (isTienLenMB) {
            return -1;
        }
        if (myHand == null || myHand.length != 13) {
            return -1;
        }// bài có tứ quý 2
        byte[] firstCards = new byte[4];
        byte[] lastCards = new byte[4];
        int lastIndex = 0;
        for (int i = 0; i
                < myHand.length; i++) {
            if (i >= 0 && i <= 3) {
                firstCards[i] = myHand[i];
            } else if (i >= myHand.length - 4 && i <= myHand.length - 1) {
                lastCards[lastIndex] = myHand[i];
                lastIndex++;
            }
        }
        if ((checkCardsType(lastCards) == CARDS_TUQUY && Utils.getValue(lastCards[0]) == 12)) {
//            System.out.println("Tới trắng Tứ Quý 2!!!");
            return PERFECT_TUQUY;
        }
        //6 đôi
        if (is6Couple(myHand)) {
//            System.out.println("Tới trắng 6 đôi!!!");
            return PERFECT_6COUPLE;
        } // 5 đôi thông
        if (is5SerialCouple(myHand)) {
//            System.out.println("Tới trắng 5 đôi thông!!!");
            return PERFECT_5SERIAL_COUPLE;
        } //Sảnh rồng
        if (isSanhRong(myHand)) {
            System.out.println("Tới trắng Sảnh rồng!!!");
            return PERFECT_SANHRONG;
        }
        return -1;


    }

    /*
     * Tới trắng - kịch bản của Tầmtay //kiểm tra trường hợp thắng ngay khi chia
     * bài xong(tới trắng) public int checkPerfect(byte[] myHand) { if (myHand
     * == null || myHand.length != 13) { return -1; } // bài có tứ quý 2 hoặc tứ
     * quý 3 byte[] firstCards = new byte[4]; byte[] lastCards = new byte[4];
     * int lastIndex = 0; for (int i = 0; i < myHand.length; i++) { if (i >= 0
     * && i <= 3) { firstCards[i] = myHand[i]; } else if (i >= myHand.length - 4
     * && i <= myHand.length - 1) { lastCards[lastIndex] = myHand[i];
     * lastIndex++; } } if ((checkCardsType(firstCards) == CARDS_TUQUY &&
     * Utils.getValue(firstCards[0]) == 0) || (checkCardsType(lastCards) ==
     * CARDS_TUQUY && Utils.getValue(lastCards[0]) == 12)) { //
     * System.out.println("Tới trắng Tứ Quý!!!"); return PERFECT_TUQUY; } // 3
     * đôi thông có 3 bích if (Utils.getValue(myHand[0]) ==
     * Utils.getValue(myHand[1]) && Utils.getValue(myHand[0]) == 0 &&
     * Utils.getType(myHand[0]) == 1) { int count4 = 0, count5 = 0; for (int i =
     * 2; i < myHand.length; i++) { if (Utils.getValue(myHand[i]) == 1) {
     * count4++; } else if (Utils.getValue(myHand[i]) == 2) { count5++; } } if
     * (count5 >= 2 && count4 >= 2) { // System.out.println("Tới trắng 3 đôi
     * thông 3 bích!!!"); return PERFECT_3SERIAL_COUPLE; } } //6 đôi if
     * (is6Couple(myHand)) { // System.out.println("Tới trắng 6 đôi!!!"); return
     * PERFECT_6COUPLE; } // 5 đôi thông if (is5SerialCouple(myHand)) { //
     * System.out.println("Tới trắng 5 đôi thông!!!"); return
     * PERFECT_5SERIAL_COUPLE; } //Sảnh rồng if (isSanhRong(myHand)) {
     * System.out.println("Tới trắng Sảnh rồng!!!"); return PERFECT_SANHRONG; }
     * return -1;
     *
     *
     * }
     */
    //gửi Tới thông tin và bài của người chơi trong trường hợp tới trắng!
    public ArrayList<Object[]> GetEndGamePerfect(long idWin) {
        ArrayList arr = new ArrayList();
        long winMoney = 0;
        for (TienLenPlayer p : playings) {


            if (p.id != idWin) {
                Object[] o = new Object[4];
                o[0] = p.id;
                o[1] = Utils.bytesToString(p.myHand);
                long lostMoney = p.moneyLost(26 * firstCashBet);// thua trắng = 26 lá
                winMoney += lostMoney;
                p.money -= lostMoney;
                p.cash -= lostMoney;
                o[2] = -lostMoney;
                o[3] = p.username + ": -" + lostMoney + " " + GameRuleConfig.MONEY_SYMBOL + " thua trắng!";
                arr.add(o);
            }

        }
        winner.money += winMoney;

        updateCash();

        if (winMoney > 0) {
            winMoney = winMoney - (winMoney * 5 / 100);
        }
        Object[] winnerO = new Object[4];
        winnerO[0] = idWin;
        winnerO[1] = Utils.bytesToString(winner.myHand);
        winnerO[2] = winMoney;
        winnerO[3] = winner.username + ": +" + winMoney + " " + GameRuleConfig.MONEY_SYMBOL + " Tới trắng!";
        arr.add(winnerO);
        resetPlayer();
        timerAuto.setRuning(false);
        return arr;
    }

    public void updateGift() {
//        for (TienLenPlayer p : playings) {
//            if (p.cash == 0) {
//                ISession is = p.currentSession;
//                if (is != null) {
//                    if (is.getRemainGift() > 0) {
//                        logOut("Gift to " + p.username + " -> " + is.getCashGift());
//                        is.setGiftInfo(1, is.getRemainGift());
//                        DatabaseDriver.updateGiftInfo(p.id, 1, is.getRemainGift());
//                    }
//                }
//            }
//        }
    }

    public Object[] countCardMB(TienLenPlayer p) {
        int count_2red = 0;//số  quân heo đỏ
        int count_2black = 0;//số  quân heo đen
        int count_tuquy = 0;// số tứ quý trong bài
        long lostMoney = 0;
        String note = "";
        if (p.myHand == null || p.numHand == 0) {
            return null;
        } else {
//            if (p.numHand == 13) {
////                p.setCong();
//                note += "phạt cóng! ";
//                lostMoney += 4 * firstCashBet; //phạt cóng = 26 lá
//            }
            for (int i = 0; i < p.numHand; i++) {
                if (Utils.getValue(p.myHand[i]) == 12) {
                    if (Utils.getType(p.myHand[i]) == 1 || Utils.getType(p.myHand[i]) == 2) {
                        count_2black++;
                        lostMoney += 2 * firstCashBet; //heo đen = 2 lá
                    } else {
                        count_2red++;
                        lostMoney += 5 * firstCashBet; //heo đỏ = 5 lá
                    }
                }
            }
            if (count_2black > 0 || count_2red > 0) {
                note += "thối ";
                if (count_2black > 0) {
                    note += count_2black + " hai đen, ";
                }
                if (count_2red > 0) {
                    note += count_2red + " hai đỏ, ";
                }
            }
            if (p.numHand >= 4) {
                int[] appearRate = new int[12];// tần suất xuất hiện các quân bài
                for (int i = 0; i
                        < 12; i++) {
                    appearRate[i] = 0;
                }

                for (int j = 0; j
                        < p.numHand; j++) {
                    if (Utils.getValue(p.myHand[j]) != 12) {
                        appearRate[Utils.getValue(p.myHand[j])]++;
                    }
                }

                //kiểm tra có 4 đôi thông không
                if (p.numHand >= 4) {

                    //kiểm tra tứ quý
                    for (int i = 0; i
                            < appearRate.length; i++) {
                        if (appearRate[i] == 4) {
                            appearRate[i] = 0;
                            count_tuquy++;
                            lostMoney += 7 * firstCashBet; //tứ quý = 7 lá
                            //money=money+7;
                        }
                    }
                    if (count_tuquy > 0) {
                        note += "thối " + count_tuquy + " tứ quý, ";
                    }
                }
            }
        }
//        System.out.println(result);
        lostMoney = p.moneyLost(lostMoney);
//        System.out.println("lostMoney: " + lostMoney);

//        if (p.numHand == 13) {
//            Object[] o = new Object[4];
//            o[0] = p.id;
//            o[1] = lostMoney;
//            p.money -= lostMoney;
//            System.out.println(" p.money: " + p.money);
//            p.cash -= lostMoney;
//            if (p.money > 0) {
//                p.money = p.money - (p.money * DatabaseDriver.taxPlayGame / 100);
//                note = p.username + ": +" + p.money + "$, " + note;
//            } else {
//                note = p.username + ": " + p.money + "$, " + note;
//            }
//            o[2] = note;
//            o[3] = Utils.bytesToString(p.myHand);
//            return o;
//        } else {
        Object[] o = new Object[2];
        o[0] = -lostMoney;
        o[1] = note;
        return o;
//        }
    }
    //kiểu chơi TLMB

    public ArrayList<Object[]> GetEndGameMB() {
        System.out.println("GetEndGameMB");
        ArrayList arr = new ArrayList();

        long winMoney = 0;   //Tiền thối heo, tứ quý của các nhà cộng hết cho người về nhất.
//        boolean hasCong = false;
        for (TienLenPlayer p : playings) {
            if (p.sttToi != 1) {
                String note = "";
//                String thoi3Bich = "";
                //phạt cóng = 4 lần tiền cược + đếm heo/hàng :X
//                if (p.isCong()) {
//                    hasCong = true;
////                    p.sttToi = 5;
//                    Object[] Oj = countCardMB(p);
//                    winMoney += Long.parseLong(Oj[1].toString());
//                    Oj[1] = p.money;
//                    arr.add(Oj);
//                } else {
//                if (p.isCong()) {p.sttToi=4}
                if (p.sttToi == 0) {
                    sttFirst++;
                    p.sttToi = sttFirst;
                }
                //                    //phạt thối 3 bích = 3 lần tiền cược
                //                    if (p.numHand == 1 && p.myHand[0] == 16) {
                //                        Object[] Oj = new Object[4];
                //                        Oj[0] = p.id;
                //                        long lostMoney = p.moneyLost(3 * firstCashBet);
                //
                //                        p.money -= lostMoney;
                //                        Oj[1] = p.money;
                //                        if (p.money > 0) {
                //                            note = p.username + ": +" + p.money + "$, " + strToi(p.sttToi) + ", thối 3 bích.";
                //                        } else {
                //                            note = p.username + ": " + p.money + "$, " + strToi(p.sttToi) + ", thối 3 bích.";
                //                        }
                //                        Oj[2] = note;
                //                        Oj[3] = Utils.bytesToString(p.myHand);
                //                        arr.add(Oj);
                //                    }
//                    else {
                if (p.sttToi != 1) {
                    Object[] Oj = new Object[4];
                    Oj[0] = p.id;
                    long lostMoney = toMoney(p.sttToi);//Tiền tương ứng với thứ tự về
                   if (lostMoney < 0) {
                        lostMoney = -p.moneyLost(Math.abs(lostMoney));
                        System.out.println("p.cash"+p.cash);
                        System.out.println("lostMoney"+lostMoney);
                        p.cash -= Math.abs(lostMoney);
                        System.out.println("p.cash after: "+p.cash);
                    }
                    Object[] noteObj = countCardMB(p);
                    long addMoney = 0;
                    String noteAdd = "";
                    if (noteObj != null) {
                        addMoney = Long.parseLong(noteObj[0].toString());//Tiền thối 2, tứ quý
                        winMoney += Math.abs(addMoney);
                        lostMoney += addMoney;
                        noteAdd = noteObj[1].toString();
                    }
//                    //phạt thối 3 bích = 3 lần tiền cược
//                    if (p.numHand == 1 && p.myHand[0] == 16) {
//                        long thoi3Money = p.moneyLost(3 * firstCashBet);
//                        winMoney += thoi3Money;
//                        lostMoney = lostMoney - thoi3Money;
//                        thoi3Bich = ", thối 3 bích";
//                    }
//                    if (lostMoney < 0) {
//                        lostMoney = -p.moneyLost(Math.abs(lostMoney));
//                    }
                    p.money += lostMoney;
                    if (p.money > 0) {
                        p.money = p.money - (p.money * tienLenTax / 100);
                        note = p.username + ": +" + p.money + " " + GameRuleConfig.MONEY_SYMBOL  +  ", " + strToi(p.sttToi) + ", " + noteAdd;
                    } else {
                        note = p.username + ": " + p.money + " " + GameRuleConfig.MONEY_SYMBOL + ", " + strToi(p.sttToi) + ", " + noteAdd;
                    }
                    Oj[1] = p.money;
                    Oj[2] = note;
                    Oj[3] = Utils.bytesToString(p.myHand);
                    arr.add(Oj);
//                    }
//                }
                }
            } else {
                winner = p;
            }
        }
        for (TienLenPlayer p : playings) {
            if (p.sttToi == 1) {
                winner = p;
                break;
            }
        }
//        if (hasCong && (numPlayer == 3)) {
//            winMoney = winMoney + toMoney(1);
//        } else {
        winMoney = winMoney + toMoney(1);
//        }
//        if (winMoney < 0) {
//            winMoney = winner.moneyLost(winMoney);
//        }

        System.out.println("winMoney:" + winMoney);
        winner.money += winMoney;
        if (winner.money < 0) {
            winner.money = -winner.moneyLost(-winner.money);
        }
        System.out.println(" winner.money:" + winner.money);
        updateCash();
//        if (winner.money > 0) {
//            winner.money = winner.money - (winner.money * DatabaseDriver.taxPlayGame / 100);
//        }
//        winner.cashWin.add(new Couple<Long, Long>(winner.id, winMoney));
        if (winner.money > 0) {
            winner.money = winner.money - (winner.money * tienLenTax / 100);
        }
        Object[] winnerO = new Object[3];
        winnerO[0] = winner.id;
        winnerO[1] = winner.money;
        if (winner.money > 0) {
//            winner.money = winner.money - (winner.money * DatabaseDriver.taxPlayGame / 100);
            winnerO[2] = winner.username + ": +" + winner.money + " " + GameRuleConfig.MONEY_SYMBOL + ", Về nhất";
        } else {
            winnerO[2] = winner.username + ": " + winner.money + " " + GameRuleConfig.MONEY_SYMBOL + ", Về nhất";
        }
        arr.add(winnerO);
        resetPlayer();
        timerAuto.setRuning(false);
        System.out.println("arr.size: " + arr.size());
        return arr;
    }
    //kiểu chơi nhất nhì ba bét

    public ArrayList<Object[]> GetEndGame2() {
        if (isTienLenMB) {
            return GetEndGameMB();
        }
        System.out.println("getendgame2");
        ArrayList arr = new ArrayList();
        long winMoney = 0;
        boolean hasCong = false;
        for (TienLenPlayer p : playings) {
            if (p.sttToi != 1) {
                String note = "";
                String thoi3Bich = "";
                //phạt cóng = 4 lần tiền cược + đếm heo/hàng :X
                if (p.isCong()) {
                    hasCong = true;
//                    p.sttToi = 5;
                    Object[] Oj = countCard(p);
                    winMoney += Long.parseLong(Oj[1].toString());
                    Oj[1] = p.money;
                    arr.add(Oj);
                } else {
                    if (p.sttToi == 0) {
                        sttFirst++;
                        p.sttToi = sttFirst;
                    } //                    //phạt thối 3 bích = 3 lần tiền cược
                    //                    if (p.numHand == 1 && p.myHand[0] == 16) {
                    //                        Object[] Oj = new Object[4];
                    //                        Oj[0] = p.id;
                    //                        long lostMoney = p.moneyLost(3 * firstCashBet);
                    //
                    //                        p.money -= lostMoney;
                    //                        Oj[1] = p.money;
                    //                        if (p.money > 0) {
                    //                            note = p.username + ": +" + p.money + "$, " + strToi(p.sttToi) + ", thối 3 bích.";
                    //                        } else {
                    //                            note = p.username + ": " + p.money + "$, " + strToi(p.sttToi) + ", thối 3 bích.";
                    //                        }
                    //                        Oj[2] = note;
                    //                        Oj[3] = Utils.bytesToString(p.myHand);
                    //                        arr.add(Oj);
                    //                    }
//                    else {

                    Object[] Oj = new Object[4];
                    Oj[0] = p.id;
                    long lostMoney = toMoney(p.sttToi);
                    //phạt thối 3 bích = 3 lần tiền cược
                    if (p.numHand == 1 && p.myHand[0] == 16) {
                        long thoi3Money = p.moneyLost(3 * firstCashBet);
                        winMoney += thoi3Money;
                        lostMoney = lostMoney - thoi3Money;
                        thoi3Bich = ", thối 3 bích";
                    }
                    if (lostMoney < 0) {
                        lostMoney = p.moneyLost(lostMoney);
                    }
                    p.money += lostMoney;

                    if (p.money > 0) {
                        note = p.username + ": +" + p.money + " " + GameRuleConfig.MONEY_SYMBOL + ", " + strToi(p.sttToi) + thoi3Bich;
                    } else {
                        note = p.username + ": " + p.money + " " + GameRuleConfig.MONEY_SYMBOL + ", " + strToi(p.sttToi) + thoi3Bich;
                    }
                    Oj[1] = p.money;
                    Oj[2] = note;
                    Oj[3] = Utils.bytesToString(p.myHand);
                    arr.add(Oj);
//                    }
                }
            } else {
                winner = p;
            }
        }
//        for (TienLenPlayer p : playings) {
//            if (p.sttToi == 1) {
//                winner = p;
//                break;
//            }
//        }
        if (hasCong && (numPlayer == 3)) {
            winMoney = winMoney + toMoney(1);
        } else {
            winMoney = winMoney + toMoney(1);
        }
        if (winMoney < 0) {
            winMoney = winner.moneyLost(winMoney);
        }
        winner.money += winMoney;
        updateCash();
        if (winner.money > 0) {
            winner.money = winner.money - (winner.money * tienLenTax / 100);
        }
//        winner.cashWin.add(new Couple<Long, Long>(winner.id, winMoney));
        Object[] winnerO = new Object[3];
        winnerO[0] = winner.id;
        winnerO[1] = winner.money;
        if (winner.money > 0) {
            winnerO[2] = winner.username + ": +" + winner.money + " " + GameRuleConfig.MONEY_SYMBOL + ", Tới nhất!";
        } else {
            winnerO[2] = winner.username + ": " + winner.money + " " + GameRuleConfig.MONEY_SYMBOL + ", Tới nhất!";
        }
        arr.add(winnerO);
        resetPlayer();
        timerAuto.setRuning(false);
        System.out.println("arr.size: " + arr.size());
        return arr;
    }

    public long toMoney(int sttToi) {

        switch (numPlayer) {
            /*
             * Ván 4 người: Nhất ăn gấp đôi (2000), nhì = tiền chơi (1000), ba
             * thua 1000, tư thua 2000. Người nhất và nhì bị trừ tiền xâu (10%)
             * Ván 3 ngừời: Nhất ăn gấp ba (3000), nhì = tiền, 3 thua (2000).
             * Ván 2 người: Nhất ăn gấp đôi (2000), nhì thua gấp đôi (2000)
             */
            case 4:
                if (sttToi == 1) {
                    return 2 * firstCashBet;
                }
                if (sttToi == 2) {
                    return (long) (firstCashBet * 0.9);
                } else if (sttToi == 3) {
                    return -firstCashBet;
                } else if (sttToi == 4) {
                    return -firstCashBet * 2;
                }
            case 3:
                if (sttToi == 1) {
                    return 3 * firstCashBet;
                } else if (sttToi == 2) {
                    return -firstCashBet;
                } else if (sttToi == 3) {
                    return -firstCashBet * 2;
                }
            case 2:
                if (sttToi == 1) {
                    return 2 * firstCashBet;
                } else if (sttToi == 2) {
                    return -2 * firstCashBet;
                }
            default:
                return -1;

        }

    }

    public String strToi(int sttToi) {

        switch (sttToi) {
            case 1:
                return "Về nhất!";
            case 2:
//                if (numPlayer >= 3) {
                return "Về nhì";
//                } else {
//                    return "Tới bét";
//                }
            case 3:
//                if (numPlayer == 4) {
//                    return "Tới ba";
//                } else {
                return "Về ba";
//                }
            case 4:
                return "Về bét";
            default:
                return "";
        }
    }
//  trả Tới kết quả lúc hết ván

    public ArrayList<Object[]> GetEndGame(long idWin) {
        ArrayList arr = new ArrayList();
        long winMoney = 0;
        for (TienLenPlayer p : playings) {
            if (p.id != idWin) {
                Object[] Oj = countCard(p);
                winMoney += Long.parseLong(Oj[1].toString());
                Oj[1] = p.money;
                arr.add(Oj);
            }
        }
        winner.money += winMoney;
        updateCash();
        if (winner.money > 0) {
            winner.money = winner.money - (winner.money * tienLenTax / 100);
        }
//        winner.cashWin.add(new Couple<Long, Long>(winner.id, winMoney));
        Object[] winnerO = new Object[3];
        winnerO[0] = idWin;
        winnerO[1] = winner.money;
        if (winner.money > 0) {
            winnerO[2] = findPlayer(idWin).username + ": +" + winner.money + " " + GameRuleConfig.MONEY_SYMBOL + ", Tới nhất!";
        } else {
            winnerO[2] = findPlayer(idWin).username + ": " + winner.money + " " + GameRuleConfig.MONEY_SYMBOL + ", Tới nhất!";
        }
        arr.add(winnerO);
        resetPlayer();
        timerAuto.setRuning(false);
        return arr;
    }

//    public void autoKickOut(long id) {
//
//        ISession session = ownerSession;
//        IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();
//        MessageFactory msgFactory = session.getMessageFactory();
//        KickOutRequest reqKickOut = (KickOutRequest) msgFactory.getRequestMessage(MessagesID.KICK_OUT);
//        reqKickOut.mMatchId = this.matchID;
//        reqKickOut.uid = id;
//        reqKickOut.isAutoKickOut = true;
//        if (id == owner.id) {
//            owner.isOutGame = true;
//            TienLenPlayer p1 = ownerQuit();
//            if (p1 != null) {
//                owner = p1;
//                reqKickOut.newOwner = p1.id;
//            }
//        }
//        IBusiness business = null;
//        // Check if timeout
//        if (reqKickOut.uid != -1) {
//            try {
//                business = msgFactory.getBusiness(MessagesID.KICK_OUT);
//                business.handleMessage(session, reqKickOut, responsePkg);
//            } catch (ServerException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    public void resetPlayer() {
//remove người chơi thoát giữa chừng để chuẩn bị ván mới
        ArrayList<TienLenPlayer> needRemovePlayer = new ArrayList<TienLenPlayer>();
        for (TienLenPlayer p : playings) {
            if (p.isOutGame || p.notEnoughMoney()) {
//                System.out.println(p.username + " bị remove!");
                needRemovePlayer.add(p);
//                System.out.println(" playings.size()= " + playings.size());
//                System.out.println(" currPlayer: " + currPlayer.username);
            }
        }
        if (needRemovePlayer.size() > 0) {
            for (int i = 0; i < needRemovePlayer.size(); i++) {
//                System.out.println((TienLenPlayer) needRemovePlayer.elementAt(i) + " bị remove!");
                playings.remove(needRemovePlayer.get(i));
//                System.out.println(" playings.size() sau khi remove= " + playings.size());
            }
        }

        playings.addAll(waitings);
        waitings.clear();
//        ArrayList<TienLenPlayer> kickOutList = new ArrayList<TienLenPlayer>();
//        for (int i = 0; i < playings.size(); i++) {
//            TienLenPlayer p = (TienLenPlayer) playings.get(i);
//            if (p.cash < firstCashBet) {
//                kickOutList.add(p);
//            }
//        }
//        if (kickOutList.size() > 0) {
//            for (int i = 0; i < kickOutList.size(); i++) {
//                autoKickOut(kickOutList.get(i).id);
//            }
//        }
    }
    // đếm heo/hàng trong bài còn lại khi kết thúc ván

    public Object[] countCard(TienLenPlayer p) {
        int count_2red = 0;//số  quân heo đỏ
        int count_2black = 0;//số  quân heo đen
        int count_tuquy = 0;// số tứ quý trong bài
        int count_3couple = 0;// số 3 đôi thông trong bài
        long lostMoney = 0;
        String note = "";
        if (p.myHand == null || p.numHand == 0) {
            return null;
        } else {
            if (p.numHand == 13) {
//                p.setCong();
                note += "phạt cóng! ";
                lostMoney += 26 * firstCashBet; //phạt cóng = 26 lá
            } else {
                note += "còn " + p.numHand + " lá, ";
                lostMoney += p.numHand * firstCashBet;
            }
            for (int i = 0; i
                    < p.numHand; i++) {
                if (Utils.getValue(p.myHand[i]) == 12) {
                    if (Utils.getType(p.myHand[i]) == 1 || Utils.getType(p.myHand[i]) == 2) {
                        count_2black++;
                        lostMoney += 2 * firstCashBet; //heo đen = 2 lá
                    } else {
                        count_2red++;
                        lostMoney += 5 * firstCashBet; //heo đỏ = 5 lá
                    }
                }
            }
            if (count_2black > 0 || count_2red > 0) {
                note += "thối ";
                if (count_2black > 0) {
                    note += count_2black + " heo đen, ";
                }
                if (count_2red > 0) {
                    note += count_2red + " heo đỏ, ";
                }
            }
            if (p.numHand >= 4) {
                int[] appearRate = new int[12];// tần suất xuất hiện các quân bài
                for (int i = 0; i
                        < 12; i++) {
                    appearRate[i] = 0;
                }

                for (int j = 0; j
                        < p.numHand; j++) {
                    if (Utils.getValue(p.myHand[j]) != 12) {
                        appearRate[Utils.getValue(p.myHand[j])]++;
                    }
                }

                //kiểm tra có 4 đôi thông không
                if (p.numHand >= 4) {
                    for (int i = 0; i
                            < appearRate.length - 4; i++) {
                        if (appearRate[i] >= 2 && appearRate[i + 1] >= 2 && appearRate[i + 2] >= 2 && appearRate[i + 3] >= 2) {
                            for (int j = 0; j
                                    < 4; j++) {
                                appearRate[i + j] = appearRate[i + j] - 2;
                            }
                            lostMoney += 13 * firstCashBet; //4 đôi thông = 13 lá
                            note += "bốn đôi thông, ";
                            //money=money+13;
                        }
                    }
                    //kiểm tra tứ quý
                    for (int i = 0; i
                            < appearRate.length; i++) {
                        if (appearRate[i] == 4) {
                            appearRate[i] = 0;
                            count_tuquy++;
                            lostMoney += 7 * firstCashBet; //tứ quý = 7 lá
                            //money=money+7;
                        }
                    }
                    if (count_tuquy > 0) {
                        note += count_tuquy + "tứ quý, ";
                    }
                    //kiểm tra có 3 đôi thông không
                    for (int i = 0; i
                            < appearRate.length - 3; i++) {
                        if (appearRate[i] >= 2 && appearRate[i + 1] >= 2 && appearRate[i + 2] >= 2) {
                            count_3couple++;
                            lostMoney += 5 * firstCashBet; //3 đôi thông = 5 lá
                        }
                    }
                    if (count_3couple > 0) {
                        note += count_3couple + " bộ 3 đôi thông";
                    }
                }
            }
        }
//        System.out.println(result);
        lostMoney = p.moneyLost(lostMoney);
//        System.out.println("lostMoney: " + lostMoney);
        Object[] o = new Object[4];
        o[0] = p.id;
        o[1] = lostMoney;
        p.money -= lostMoney;
//        System.out.println(" p.money: " + p.money);
        p.cash -= lostMoney;
        if (p.money > 0) {
            note = p.username + ": +" + p.money + " " + GameRuleConfig.MONEY_SYMBOL  + ", " + note;
        } else {
            note = p.username + ": " + p.money  + " " + GameRuleConfig.MONEY_SYMBOL + ", " + note;
        }

        o[2] = note;
        o[3] = Utils.bytesToString(p.myHand);

        return o;

    }

    public void updateCashQuitPlayerMB(TienLenPlayer p) {
//        long lostMoney = 0;
//        Object[] noteObj = countCardMB(p);
//        lostMoney = Long.parseLong(noteObj[0].toString());//Tiền thối 2, tứ quý
        sttLast--;
        p.sttToi = sttLast;
//        lostMoney += toMoney(p.sttToi);
//        lostMoney = p.moneyLost(lostMoney);
//        long updateMoney = p.money - lostMoney;
//        out.println(p.username + " thoát giữa chừng Miền Bắc -> " + updateMoney);
////       out.flush();
//        String desc = "Choi game Tien len matchID : " + ownerRoom.matchId;
//        try {
//            DatabaseDriver.updateUserGameMoney(updateMoney, true, p.id, desc, p.currentOwner.getCurrentZone());
//        } catch (Exception ee) {
//        }
    }

    public void updateCashQuitPlayer(TienLenPlayer p) {
        if (isTienLenMB) {
            updateCashQuitPlayerMB(p);
            return;
        }
        long lostMoney = 0;
        boolean isCong = false;
        if (p.numHand == 13) {
            lostMoney += 26 * firstCashBet; //phạt cóng = 26 lá
            isCong = true;
            if (!choiDemLa) {
                sttLast--;
                p.setCong();
            }


        } else {
            if (choiDemLa) {
                lostMoney += p.numHand * firstCashBet;
            }
        }
        if (choiDemLa || isCong) {
            for (int i = 0; i
                    < p.numHand; i++) {
                if (Utils.getValue(p.myHand[i]) == 12) {
                    if (Utils.getType(p.myHand[i]) == 1 || Utils.getType(p.myHand[i]) == 2) {
                        lostMoney += 2 * firstCashBet; //heo đen = 2 lá
                    } else {
                        lostMoney += 5 * firstCashBet; //heo đỏ = 5 lá
                    }
                }
            }
            if (p.numHand >= 4) {
                int[] appearRate = new int[12];// tần suất xuất hiện các quân bài
                for (int i = 0; i
                        < 12; i++) {
                    appearRate[i] = 0;
                }

                for (int j = 0; j
                        < p.numHand; j++) {
                    if (Utils.getValue(p.myHand[j]) != 12) {
                        appearRate[Utils.getValue(p.myHand[j])]++;
                    }
                }

                //kiểm tra có 4 đôi thông không
                if (p.numHand >= 4) {
                    for (int i = 0; i
                            < appearRate.length - 4; i++) {
                        if (appearRate[i] >= 2 && appearRate[i + 1] >= 2 && appearRate[i + 2] >= 2 && appearRate[i + 3] >= 2) {
                            for (int j = 0; j
                                    < 4; j++) {
                                appearRate[i + j] = appearRate[i + j] - 2;
                            }
                            lostMoney += 13 * firstCashBet; //4 đôi thông = 13 lá
                        }
                    }
                    //kiểm tra tứ quý
                    for (int i = 0; i
                            < appearRate.length; i++) {
                        if (appearRate[i] == 4) {
                            appearRate[i] = 0;
                            lostMoney += 7 * firstCashBet; //tứ quý = 7 lá
                            //money=money+7;
                        }
                    }

                    //kiểm tra có 3 đôi thông không
                    for (int i = 0; i
                            < appearRate.length - 3; i++) {
                        if (appearRate[i] >= 2 && appearRate[i + 1] >= 2 && appearRate[i + 2] >= 2) {
                            lostMoney += 5 * firstCashBet; //3 đôi thông = 5 lá
                        }
                    }

                }

            }
        } else {
            System.out.println("Thoát giữa chừng Tới thứ: " + sttLast);
            sttLast--;
            p.sttToi = sttLast;
            lostMoney = toMoney(p.sttToi);
        }
//        System.out.println(result);
        lostMoney = p.moneyLost(lostMoney);
        long updateMoney = p.money - lostMoney;
        System.out.println("Thoát giữa chừng tiền cộng trừ: " + updateMoney);
        String desc = "Choi game Tien len matchID : " + matchID;
        try {
            DatabaseDriver.updateUserMoney(updateMoney, true, p.id, desc);
            DatabaseDriver.updateUserGameStatus(p.id, 0);

        } catch (Exception ee) {
        }

        updateGift();

    }

    public void startTime() {
        timerAuto.setTimer(20000);
        timerAuto.setTienLenPlayer(currPlayer);
        timerAuto.setTienLenTable(this);
        timerAuto.setRuning(true);
        timerAuto.reset();
    }

    private void updateCash() {
        startTime = System.currentTimeMillis();
        //TODO:
        for (int i = 0; i < playings.size(); i++) {
            try {
                long plus = playings.get(i).money;
                String desc = "Choi game " + getMatchName() + " matchID : " + matchID + "-" + matchNum + " . (cash : " + playings.get(i).cash + ")";
                if (playings.get(i).id == winner.id) {
                    if (plus > 0) {
                        plus = plus - plus * 10 / 100;
                    }
                    System.out.println("Tiền người thắng được cộng: " + plus);
                    playings.get(i).cash = DatabaseDriver.getUserMoney(winner.id) + plus;
                }
//                playings.get(i).cash = playings.get(i).cash + plus;

                logOut("End : " + playings.get(i).username + " : " + plus + " -> " + playings.get(i).cash);
                //người thoát giữa chừng đã bị trừ tiền lúc thoát rồi
                int zoneID = ZoneID.TIENLEN;
////                if (choiDemLa) {
////                    zoneID = ZoneID.TIENLEN_DEMLA;
////                } else if 
//                if (isTienLenMB) {
//                    zoneID = ZoneID.TIENLEN_MB;
//                }
                if (!playings.get(i).isOutGame) {
                    DatabaseDriver.updateUserMoney(plus, true, playings.get(i).id, desc);
                } else {
                    if (isTienLenMB) {
                        DatabaseDriver.updateUserMoney(plus, true, playings.get(i).id, desc);
                    }
                }
                DatabaseDriver.updateUserGameStatus(playings.get(i).id, 0);

            } catch (Exception ex) {
//                java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        updateGift();
        logOut("End Game DB Processing : " + (System.currentTimeMillis() - startTime) + "ms");

        if (DatabaseDriver.log_code) {
            out.flush();
            out_code.flush();
        }

    }

    public void setDemLa(boolean b) {
        this.choiDemLa = b;
    }

    public void setTLMB(boolean isTLMB) {
        this.isTienLenMB = isTLMB;
    }

    public boolean isTLMB() {
        return this.isTienLenMB;
    }

    public String getMatchName() {
//        if (choiDemLa) {
//            return "Tiến Lên Miền Nam ";
//        }
        if (isTienLenMB) {
            return "Tien len Mien Bac";
        }
        return "Tien Len Mien Nam";
    }

    public void destroy() {
        for (TienLenPlayer p : playings) {

            try {
                DatabaseDriver.updateUserGameStatus(p.id, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        try {
            timerAuto.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.destroy();
//        System.out.println("Destroy : " + this.name);


        //timer.destroy();
    }

    public JSONArray getPlayerName() {
        JSONArray ja = new JSONArray();

        try {

            for (TienLenPlayer p : playings) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);
            }
            for (TienLenPlayer p : waitings) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return ja;
    }
    
    @Override
    public long getJoinMoney() {
        return GameRuleConfig.MONEY_TIMES_BET_TO_JOIN * firstCashBet;
    }
    
    @Override
    public String getJoinMoneyErrorMessage() {
	return "Bạn cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_JOIN + " lần tiền cược của bàn để tham gia!";
    }
}
