package phom.data;

import java.util.ArrayList;
import java.util.Vector;

import dreamgame.data.Couple;
import dreamgame.data.MessagesID;
import dreamgame.data.SimplePlayer;
import dreamgame.data.Timer;
import dreamgame.protocol.messages.BocPhomRequest;
import dreamgame.protocol.messages.HaPhomRequest;
import dreamgame.protocol.messages.TurnRequest;

import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Random;

import java.util.logging.Level;
import dreamgame.gameserver.framework.common.LoggerContext;
import org.slf4j.Logger;

public class PhomPlayer extends SimplePlayer {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(PhomPlayer.class);
    public boolean isOwner;
    public boolean isObserve;
    public boolean haPhom = false;
    public boolean momStatus = false;
    public boolean uStatus = false;
    public boolean doneBocBai = false;
    public boolean doneHaBai = false;
    public boolean outOfTime = false;
    public int winOrder = 0;
    // An cay chot hoac cho an cay mat tien
    public ArrayList<Couple<Long, Long>> cashLost = new ArrayList<Couple<Long, Long>>();
    public ArrayList<Couple<Long, Long>> cashWin = new ArrayList<Couple<Long, Long>>();
    public long money = 0;
    public int numberCardPlay;
    //list of all current cards in hand
    public ArrayList<Poker> allCurrentCards = new ArrayList<Poker>();
    //list of playing cards
    public ArrayList<Poker> playingCards = new ArrayList<Poker>();
    public ArrayList<Poker> frontCards = new ArrayList<Poker>();
    //list of getting cards 
    public ArrayList<Poker> gettingCards = new ArrayList<Poker>();
    //list of eating cards
    public ArrayList<Poker> eatingCards = new ArrayList<Poker>();
    //list of offering cards - danh sach cac quan bai gui sang phom nguoi ha bai truoc
    public ArrayList<Poker> offeringCards = new ArrayList<Poker>();
    //list of phom
    public ArrayList<Phom> phoms = new ArrayList<Phom>();

    public long moneyCompute() {
        long res = 0;
        for (Couple<Long, Long> win : this.cashWin) {
            res += win.e2;
        }
        for (Couple<Long, Long> lost : this.cashLost) {
            res -= lost.e2;
        }
        return res;
    }

    public String phomToString(ArrayList<Phom> cards) {
        String res = "";
        for (Phom ph : cards) {
            if (cards.size() > 0 && res.length() > 0) {
                res = res + ";" + ph.toString();
            } else {
                res = res + ph.toString();
            }
        }
        return res;
    }

    public String cardToString(ArrayList<Poker> cards) {
        String res = "";
        if (cards.size() > 0) {
            res += cards.get(0).toInt();
            for (int i = 1; i < cards.size(); i++) {
                res += "#";
                res += cards.get(i).toInt();
            }
        }
        return res;
    }

    public String getUCards() {
        if (phoms.size() == 0) {
            return allPokersToString();
        }

        return "";
    }

    public String allPokersToString() {
        String res = "";
        if (this.allCurrentCards.size() > 0) {
            res += this.allCurrentCards.get(0).toInt();
            for (int i = 1; i < this.allCurrentCards.size(); i++) {
                res += "#";
                res += this.allCurrentCards.get(i).toInt();
            }
        }

        if (uStatus) {
            for (int i = 0; i < phoms.size(); i++) {
                if (res.length() > 0) {
                    res = res + "#" + phoms.get(i).toString();
                } else {
                    res = phoms.get(i).toString();
                }
            }
        }
        return res;
    }
    //final point 
    public int point;
    //stopping order - thu tu ha bai
    public int stoppingOrder;
    public boolean isLastMove = false;
    private boolean vuaAnPhaiKhong = false;
    /**
     * 0: Waiting 1: Vua boc 2: Vua an 3: Ha
     */
    public int status;
    /**
     * 0: Khong U 1: U 3 phom bt 11: U den 12: Tai gui u den 2: U khan 3: U 0
     * diem - gui het bai
     */
    public int uType = 0;
    public boolean isAutoPlay = false; // Khi user out, may se tu choi va bat flag nay len

    public PhomPlayer() {
    }

    public boolean getVuaAnPhaiKhong() {
        return vuaAnPhaiKhong;
    }

    public void setCurrentOwner(ISession currentOwner) {
        this.currentOwner = currentOwner;
    }

    public void waitMe(Timer t, long ms) {
        System.out.println("Waitme : " + ms);
        t.setTimer((int) ms);
        t.setRuning(true);
        t.reset();
        /*
         * try { //Thread.sleep(ms); t.wait(); } catch (InterruptedException ex)
         * {
         * java.util.logging.Logger.getLogger(PhomPlayer.class.getName()).log(Level.SEVERE,
         * null, ex);
        }
         */
    }

    public String getPhom() {
        String cards = "";
        if (eatingCards.size() == 0) {
            return "";
        }

        for (Poker po : eatingCards) {
            try {
                ArrayList<Phom> ps = getPhom(po, allCurrentCards);

                if (ps.size() > 0) {
                    Phom pp = ps.get(ps.size() - 1);
                    if (cards.length() == 0) {
                        cards = pp.toString();
                    } else {
                        cards = cards + ";" + pp.toString();
                    }
                }

            } catch (PhomException ex) {
                java.util.logging.Logger.getLogger(PhomPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        cards = cards.replace(" ", "");
        System.out.println("Cards : " + cards);

        return cards;
    }

    public ArrayList getPhomArrayList() {

        ArrayList<Poker> cards = new ArrayList();

        if (eatingCards.size() == 0) {
            return cards;
        }

        for (Poker po : eatingCards) {
            try {
                ArrayList<Phom> ps = getPhom(po, allCurrentCards);
                if (ps.size() > 0) {
                    Phom pp = ps.get(ps.size() - 1);
                    for (Poker p2 : pp.cards) {
                        cards.add(p2);
                    }
                }
            } catch (PhomException ex) {
                java.util.logging.Logger.getLogger(PhomPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return cards;
    }

    private ArrayList<ArrayList<Integer>> getCards(String input) throws Exception {
        ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
        String[] i1 = input.split(";");
        for (String i : i1) {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            String[] i2 = i.split("#");
            for (String j : i2) {
                temp.add(Integer.parseInt(j));
            }
            res.add(temp);
        }
        return res;
    }

    public void autoPlay(PhomTable phom, Timer t) {
        try {
            try {
                System.out.println("autoPlay! Came here : " + currentMatchID + " : " + id);
                //Boc
                //BocPhomBusiness b = new BocPhomBusiness();
                //BocPhomRequest rqBoc = new BocPhomRequest();
                //rqBoc.matchID = matchID;
                //b.handleMessage(currentOwner, rqBoc, null);

                //currentOwner.setUID(id);
                if (currentSession == null) {
                    System.out.println("OMG, currentSession it's null!");
                }
                if (currentOwner == null) {
                    System.out.println("OMG, currentOwner it's null!");
                }

                if (!isAutoPlay) {
                    outOfTime = true;
                }

                MessageFactory msgFactory = currentOwner.getMessageFactory();
                IResponsePackage responsePkg = currentOwner.getDirectMessages();
                IBusiness business = null;

                //MessagesID.BOC_PHOM
                if (allCurrentCards.size() < 10 && !doneBocBai) {

                    boolean canEat = phom.checkEatable(this, (byte) phom.currPoker.toInt());
                    System.out.println("canEat : " + canEat);
//                    if (botPlayer && canEat ){
//                        System.out.println("Bot eat !");
//                        AnPhomRequest rqBoc = (AnPhomRequest) msgFactory.getRequestMessage(MessagesID.AN_PHOM);
//                        
//                        rqBoc.matchID = currentMatchID;    
//                        rqBoc.botUid=this.id;
//                        business = msgFactory.getBusiness(MessagesID.AN_PHOM);
//                        business.handleMessage(currentOwner, rqBoc, responsePkg);
//                        
//                         
//                        
//                    }else{                    
                    BocPhomRequest rqBoc = (BocPhomRequest) msgFactory.getRequestMessage(MessagesID.BOC_PHOM);
                    rqBoc.matchID = currentMatchID;
                    rqBoc.uid = id;
                    business = msgFactory.getBusiness(MessagesID.BOC_PHOM);
                    business.handleMessage(currentOwner, rqBoc, responsePkg);

//                    }
//                    if (botPlayer)
//                        waitMe(t,4000);
//                        else
                    waitMe(t, 10000);

                    return;
                }

                if (phom.isHaBaiTurn() && phom.currentPlayer.id == id && !doneHaBai) {
                    System.out.println("Came here autoHabai : " + allCurrentCards.size());
                    System.out.println("haphom : " + haPhom);

                    HaPhomRequest rqTurn = (HaPhomRequest) msgFactory.getRequestMessage(MessagesID.HA_PHOM);
                    rqTurn.matchID = currentMatchID;

                    rqTurn.u = 0;

                    // rqTurn.cards1 = getPhom();
//                    if (botPlayer){
                        rqTurn.cards1 = phom.getCurrentPhom(this);
//                    }

                    if (!haPhom || rqTurn.cards1.length() > 0) {
                        if (rqTurn.cards1.length() > 0) {
                            rqTurn.cards = getCards(rqTurn.cards1);
                        }

                        rqTurn.uid = id;
                        business = msgFactory.getBusiness(MessagesID.HA_PHOM);
                        business.handleMessage(currentOwner, rqTurn, responsePkg);

                        if (rqTurn.cards1.length() == 0 && !haPhom) {
                            momStatus = true;
                        }
                        waitMe(t, 5000);
                        return;
                    }
                }

                if (phom.currentPlayer.id == id) {
                    System.out.println("Came here1 : " + allCurrentCards.size() + " matchId : " + currentMatchID + " " + phom.matchID);
                    //Play
                    currentMatchID = phom.matchID;

                    TurnRequest rqTurn = (TurnRequest) msgFactory.getRequestMessage(MessagesID.MATCH_TURN);
                    rqTurn.mMatchId = currentMatchID;
                    rqTurn.uid = id;
                    business = msgFactory.getBusiness(MessagesID.MATCH_TURN);

                    ArrayList<Poker> pv = getPhomArrayList();

                    int goodCard = 0;
                    int oldNum = 0;
                    int firstCard = 0;

                    for (int i = allCurrentCards.size() - 1; i >= 0; i--) {
                        rqTurn.phomCard = allCurrentCards.get(i).toInt();
                        boolean flag = true;

                        for (Poker p2 : pv) {
                            if (allCurrentCards.get(i).toInt() == p2.toInt()) {
                                flag = false;
                            }
                        }

                        if (flag && allCurrentCards.get(i).num > oldNum) {
                            oldNum = allCurrentCards.get(i).num;
                            goodCard = rqTurn.phomCard;
                            if (firstCard == 0) {
                                firstCard = goodCard;
                            }
                        }

                    }

                    Random r = new Random();

                    if (goodCard > 0) {

                        rqTurn.phomCard = goodCard;
                        if (r.nextBoolean()) {
                            rqTurn.phomCard = firstCard;
                        }

                        business.handleMessage(currentOwner, rqTurn, responsePkg);
                        return;
                    }

                }


            } catch (ServerException ex) {
                ex.printStackTrace();
                java.util.logging.Logger.getLogger(PhomPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void reset() {
        outOfTime = false;
        point = 0;
        isStop = false;
        momStatus = false;
        uStatus = false;
        allCurrentCards = new ArrayList<Poker>();
        playingCards = new ArrayList<Poker>();
        frontCards = new ArrayList<Poker>();
        gettingCards = new ArrayList<Poker>();
        offeringCards = new ArrayList<Poker>();
        eatingCards = new ArrayList<Poker>();
        phoms = new ArrayList<Phom>();
        cashLost = new ArrayList<Couple<Long, Long>>();
        cashWin = new ArrayList<Couple<Long, Long>>();
        isWin = false;
        money = 0;
        uType = 0;

        stoppingOrder = 0;//chua ha bai
        vuaAnPhaiKhong = false;
        isObserve = false;
        haPhom = false;

        doneHaBai = false;
        doneBocBai = false;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void setPhoms(ArrayList<Phom> input) {
        this.phoms = input;
    }

    private boolean isIn(Phom phom, Poker card) {
        for (Poker p : phom.cards) {
            if (p.toInt() == card.toInt()) {
                return true;
            }
        }
        return false;
    }

    private boolean isIn(Poker card) {
        for (Phom p : this.phoms) {
            if (isIn(p, card)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkEatCardInPhoms() {
        for (Poker p : this.eatingCards) {
            if (!isIn(p)) {
                return false;
            }
        }
        return true;
    }

    public boolean isUkhan() {

        if (allCurrentCards.size() < 9) {
            return false;
        }

        for (int i = 0; i < this.allCurrentCards.size() - 1; i++) {
            Poker pi = this.allCurrentCards.get(i);
            for (int j = i + 1; j < this.allCurrentCards.size(); j++) {
                Poker pj = this.allCurrentCards.get(j);
                if (pi.isCa(pj)) {
                    return false;
                }
            }
        }
        this.uType = 2;

        return true;
    }

    public void removePoker(Poker p) {
        for (int i = 0; i < allCurrentCards.size(); i++) {
            if (allCurrentCards.get(i).isEqual(p)) {
                allCurrentCards.remove(i);
                break;
            }
        }
    }

    public void removePoker(ArrayList<Poker> v, Poker p) {
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).isEqual(p)) {
                v.remove(i);
                break;
            }
        }
    }

    public void gui(ArrayList<Poker> cards) throws PhomException {
        //isLastMove = true;
        this.offeringCards.addAll(cards);
        computeFinalPoint();
    }

    public void guiED(int phomID, ArrayList<Poker> cards) throws PhomException {
        try {
            Phom p = this.phoms.get(phomID);
            p.gui(cards);
        } catch (Exception e) {
            throw new PhomException("Gui sai phom");
        }

    }

    public boolean checkU(int card, boolean isTaiGuiUDen, boolean isHa) throws PhomException {
        System.out.println("check U : " + card);
        if (card > 0) {
            Poker p = Utils.numToPoker(card);
            play(p);
        }

        Poker[] temp = Utils.arrayListToArray(this.allCurrentCards);

        //hatuan remove quicksort Error!
        //    Utils.quicksortPokers(0, temp.length - 1, temp);
        if (true) {
            if (this.eatingCards.size() == 3) {
                this.uType = 11;
            } else if (isHa && isTaiGuiUDen) {
                this.uType = 12;
            } else {
                this.uType = 1;
            }
            return true;
        }

        for (int i = 0; i < 3; i++) {
            ArrayList<Poker> cards = new ArrayList<Poker>();
            cards.add(temp[i * 3]);
            cards.add(temp[i * 3 + 1]);
            cards.add(temp[i * 3 + 2]);
            if (!Utils.checkPhom(cards)) {
                throw new PhomException("Khong phai phom");
            } else {
                if (!checkEatCardInPhoms()) {
                    throw new PhomException("Có cây ăn mà không nằm trong phỏm");
                }
                if (!checkDupPokerPhom(cards)) {
                    throw new PhomException("Khong duoc an 2 cay 1 phom");
                }
            }
        }
        if (this.eatingCards.size() == 3) {
            this.uType = 11;
        } else if (isHa && isTaiGuiUDen) {
            this.uType = 12;
        } else {
            this.uType = 1;
        }

        return true;
    }

    private boolean checkDupPokerPhom(ArrayList<Poker> input) {
        int index = 0;
        for (Poker p : input) {
            if (this.eatingCards.contains(p)) {
                index++;
            }
        }
        return (index < 2);
    }

    public boolean hasPoker(Poker p) {
        for (Poker po : this.allCurrentCards) {
            if (po.toInt() == p.toInt()) {
                return true;
            }
        }
        return false;
    }

    public void takeTenthPoker(Poker p) {
        //mLog.info(username + " Take tenth Cards : " + p + " : " + allCurrentCards.size() + "  ; " + showCards(allCurrentCards));
        this.allCurrentCards.add(p);
        //mLog.info(username + " Take tenth Cards : " + p + " : " + allCurrentCards.size() + "  ; " + showCards(allCurrentCards));
    }

    public void take(Poker p) {
        mLog.debug("Player " + id + ": Take : " + p.toString());
        vuaAnPhaiKhong = false;
        if (this.numberCardPlay >= 3) {
            status = 3;
        } else {
            status = 1;
        }
        this.allCurrentCards.add(p);
        this.gettingCards.add(p);
        doneBocBai = true;

        showCards();
    }

    public void eated(PhomPlayer player, boolean isChot, int index, boolean isAn) {

        if (isChot) {
            Couple<Long, Long> chot = new Couple<Long, Long>(this.id, this.moneyForBet * 4);
            player.cashLost.add(chot);
        } else if (isAn) {
            switch (index) {
                case 1:
                    player.cashLost.add(new Couple<Long, Long>(this.id, this.moneyForBet * 1));
                    break;
                case 2:
                    player.cashLost.add(new Couple<Long, Long>(this.id, this.moneyForBet * 2));
                    break;
                case 3:
                    player.cashLost.add(new Couple<Long, Long>(this.id, this.moneyForBet * 3));
                    break;
                default:
                    break;
            }
        }

        /*
         * if(index == 3){ Couple<Long, Long> uDen = new Couple<Long,
         * Long>(this.id, this.moneyForBet*5); player.cashLost.add(uDen);
        }
         */

    }

    public long eat(Poker p, PhomPlayer player, boolean isChot, boolean isAn) throws PhomException {
        long res = 0;
        vuaAnPhaiKhong = true;
        //status = 2;
        this.allCurrentCards.add(p);
        this.eatingCards.add(p);

        doneBocBai = true;
        removePoker(player.frontCards, p);

        System.out.println("eat : " + p + " ; " + eatingCards.size() + "  ; chot : " + isChot);
        long money_eat = 0;

        if (isChot) {
            money_eat = this.moneyForBet * 4;
        } else if (isAn) {
            money_eat = this.moneyForBet * this.eatingCards.size();
        }

        res += money_eat;



        return res;
    }

    public String showPureCards(ArrayList<Poker> allCurrentCards) {
        String s = "";
        for (int i = 0; i < allCurrentCards.size(); i++) {
            if (i == allCurrentCards.size() - 1) {
                s = s + (allCurrentCards.get(i).toInt());
            } else {
                s = s + (allCurrentCards.get(i).toInt() + " ");
            }
        }
        return s;
    }

    public String showCards(ArrayList<Poker> allCurrentCards) {
        String s = "";
        for (int i = 0; i < allCurrentCards.size(); i++) {
            s = s + (allCurrentCards.get(i).toString() + "|");
        }

        s = s + ("(");
        for (int i = 0; i < allCurrentCards.size(); i++) {
            if (i == allCurrentCards.size() - 1) {
                s = s + (allCurrentCards.get(i).toInt());
            } else {
                s = s + (allCurrentCards.get(i).toInt() + " ");
            }
        }
        s = s + (")");

        return s;
    }

    public void showCards() {
        mLog.info("[" + this.currentMatchID + "]" + "Player [" + id + "][" + username + "](" + allCurrentCards.size() + "):" + showCards(allCurrentCards));
    }

    public boolean notEnoughMoney() {
        if (cash < moneyForBet * 3) {
            return true;
        }

        return false;
    }

    public void play(Poker p) throws PhomException {
        //System.out.println("Player " + id + ": play : " + p.toString());
        if (hasPoker(p)) {
            //showCards();
            this.numberCardPlay++;

            this.allCurrentCards.remove(p);

            for (int i = 0; i < allCurrentCards.size(); i++) {
                if (allCurrentCards.get(i).isEqual(p)) {
                    allCurrentCards.remove(i);
                    break;
                }
            }

            this.playingCards.add(p);
            frontCards.add(p);

            /*
             * if(isLastMove){ computeFinalPoint(); if(this.point == 0){
             * this.uType = 3; }else { this.uType = 0; }
            }
             */

            //showCards();
            status = 0;

        } else {

            mLog.error("Quan bai ko ton tai : " + p + " ; " + currentSession.userInfo());
            mLog.error("[" + this.currentMatchID + "]" + "Player [" + id + "][" + username + "](" + allCurrentCards.size() + "):" + showCards(allCurrentCards) + "  ;  " + "Player " + id + ": play : " + p.toString());
            throw new PhomException("Khong tim thay cay bai!");

        }
    }

    public void setPoker(String s) {
        String[] cards = s.split(" ");
        for (int i = 0; i < cards.length; i++) {
            Poker p = Utils.numToPoker(Integer.parseInt(cards[i]));
            allCurrentCards.add(p);
        }
    }

    public void setPoker(ArrayList<Poker> restCards, String s) {

        System.out.println("restCards : " + restCards.size());
        System.out.println("allCurrentCards : " + allCurrentCards.size());

        String[] cards = s.split(" ");

        for (int i = 0; i < cards.length; i++) {
            Poker p = Utils.numToPoker(Integer.parseInt(cards[i]));
            allCurrentCards.add(p);
            //removePoker(Utils.numToPoker(Integer.parseInt(cards[i])));

            for (int j = 0; j < restCards.size(); j++) {
                if (restCards.get(j).toInt() == p.toInt()) {
                    restCards.remove(j);
                    break;
                }
            }
        }

        Random r = new Random();
        for (int i = 0; i < 9 - cards.length; i++) {
            int k = r.nextInt(restCards.size());
            allCurrentCards.add(restCards.get(k));
            restCards.remove(k);
        }

    }

    public void setPokers(ArrayList<Poker> inputPoker) {
        this.allCurrentCards = inputPoker;



    }

    public void setAvatarID(int avatarID) {
        this.avatarID = avatarID;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCash(long c) {
        this.cash = c;
    }

    public PhomPlayer(long id) {
        this.id = id;
        isStop = false;
        this.isGiveUp = false;
    }

    public void setMoney(long money) {
        moneyForBet = money;
    }

    public PhomPlayer(ArrayList<Poker> inputPoker, long id, long minBet) {
        this.allCurrentCards = inputPoker;
        this.id = id;
        this.moneyForBet = minBet;
        this.isGiveUp = false;
        isStop = false;
    }
    //calculate final point 

    public void computeFinalPoint() {
        //add all rest cards of player
        //remove all offering cards
        for (Poker p : this.offeringCards) {
            removePoker(p);
        }
        //remove all cards in phom
        int size = phoms.size();
        for (int i = 0; i < size; i++) {
            for (Poker p : this.phoms.get(i).cards) {
                //this.allCurrentCards.remove(p);
                removePoker(p);
            }
        }
        compute();
    }

    private void compute() {
        point = 0;
        if (momStatus) {
            this.point = 1000;
            return;
        }
        for (Poker p : this.allCurrentCards) {
            this.point += p.getNum();
        }

    }

    public void setStoppingOrder(int stoppingOrder) {
        this.stoppingOrder = stoppingOrder;
    }

    public boolean isWin(PhomPlayer other) {
        if (this.point == other.point) {
            return (this.stoppingOrder < other.stoppingOrder);
        } else {
            return (this.point < other.point);
        }
    }

    /**
     * An xong chua oanh. true: u false: bt
     */
    public boolean playAfterEat() throws PhomException {
        switch (this.eatingCards.size()) {
            case 1:
            case 2: {
                if (checkValid()) {
                    return false;
                } else {
                    throw new PhomException("An tam bay roi em!");
                }
            }
            case 3: {
                if (checkValid()) {
                    return true;
                } else {
                    throw new PhomException("An tam bay roi em!");
                }
            }
            default:
                throw new PhomException("Khong the nhu the dc!");
        }
    }

    public boolean checkValid() throws PhomException {
        ArrayList<Poker> input = this.allCurrentCards;
        ArrayList<Poker> eat = this.eatingCards;

        /*
         * for(int i = 0; i < phoms.size(); i++){ Phom ph = phoms.get(i); if
         * (dq(getSubPhom(phoms, i),removePokerFromList(eat),
         * removePhomFromCards(input, ph.cards))) return true;
        }
         */
        return dq(eat, input);
    }
    private ArrayList<Phom> listPhom = new ArrayList<Phom>();

    private boolean dq(ArrayList<Poker> eat, ArrayList<Poker> input)
            throws PhomException {
        if (eat.size() == 0) {
            return true;
        } else if (eat.size() == 1) {
            ArrayList<Phom> temp = getPhom(eat.get(0), input);
            if (temp.size() > 0) {
                System.out.println("====================");
                for (Phom p : temp) {
                    System.out.println(p.toString());
                    listPhom.add(p);
                }
                return true;
            } else {
                return false;
            }
        } else {
            ArrayList<Phom> phoms = getPhom(eat.get(0), input);
            for (int i = 0; i < phoms.size(); i++) {
                Phom ph = phoms.get(i);
                listPhom.add(ph);
                if (dq(removePokerFromList(eat),
                        removePhomFromCards(input, ph.cards))) {
                    return true;
                } else {
                    listPhom.remove(ph);
                }
            }
        }
        return false;
    }

    private ArrayList<Poker> removePokerFromList(ArrayList<Poker> input) {
        ArrayList<Poker> res = new ArrayList<Poker>();
        for (int i = 1; i < input.size(); i++) {
            res.add(input.get(i));
        }
        return res;
    }

    private ArrayList<Poker> removePhomFromCards(ArrayList<Poker> input, ArrayList<Poker> cards) {
        ArrayList<Poker> res = new ArrayList<Poker>();
        for (Poker p : input) {
            if (!cards.contains(p)) {
                res.add(p);
            }
        }
        return res;
    }
    /*
     * private ArrayList<Phom> getSubPhom(ArrayList<Phom> phoms, int index){
     * ArrayList<Phom> res = new ArrayList<Phom>(); for(int i = 0; i <
     * phoms.size(); i++){ if(i != index) res.add(phoms.get(i)); } return res;
    }
     */

    private ArrayList<Phom> getPhom(Poker card, ArrayList<Poker> input) throws PhomException {
        ArrayList<Phom> res = new ArrayList<Phom>();
        if (input.contains(card)) {
            res.addAll(getPhomNgang(card, input));
            res.addAll(getPhomDoc(card, input));
        }
        return res;
    }

    private ArrayList<Poker> getSubListDoc(ArrayList<Poker> input, int index, int length)
            throws PhomException {

        ArrayList<Poker> res = new ArrayList<Poker>();
        for (int i = index; i < index + length; i++) {
            res.add(input.get(i));
        }
        return res;
    }

    private ArrayList<Phom> getSubPhoms(ArrayList<Poker> input, int length, Poker card) throws PhomException {
        ArrayList<Phom> res = new ArrayList<Phom>();
        if (length <= input.size()) {
            for (int i = 0; i <= input.size() - length; i++) {
                ArrayList<Poker> temp = getSubListDoc(input, i, length);
                if (temp.contains(card)) {
                    res.add(new Phom(temp));
                }
            }
        }
        return res;
    }

    private ArrayList<Phom> getPhomDoc(Poker card, ArrayList<Poker> input) throws PhomException {
        ArrayList<Phom> res = new ArrayList<Phom>();
        ArrayList<Poker> temp = new ArrayList<Poker>();

        int index = card.num;
        while (true) {
            Poker p = new Poker(index - 1, card.type);
            if (hasPoker(p)) {
                temp.add(p);
                index--;
            } else {
                break;
            }
        }
        index = card.num;
        temp.add(card);
        while (true) {
            Poker p = new Poker(index + 1, card.type);
            if (hasPoker(p)) {
                temp.add(p);
                index++;
            } else {
                break;
            }
        }
        if (temp.size() >= 3) {
            for (int i = 3; i <= temp.size(); i++) {
                res.addAll(getSubPhoms(temp, i, card));
            }
        }
        return res;
    }

    private ArrayList<Poker> getSubListNgang(ArrayList<Poker> input, int index) {
        ArrayList<Poker> res = new ArrayList<Poker>();
        for (int i = 0; i < 4; i++) {
            if (i != index) {
                res.add(input.get(i));
            }
        }
        return res;
    }

    private ArrayList<Phom> getPhomNgang(Poker card, ArrayList<Poker> input) throws PhomException {
        ArrayList<Phom> res = new ArrayList<Phom>();
        ArrayList<Poker> temp = new ArrayList<Poker>();
        for (Poker p : input) {
            if (p.num == card.num) {
                temp.add(p);
            }
        }
        if (temp.size() == 3) {
            res.add(new Phom(temp));
        } else if (temp.size() == 4) {
            for (int i = 0; i < 4; i++) {
                if (temp.get(i).type != card.type) {
                    res.add(new Phom(getSubListNgang(temp, i)));
                }
            }
            res.add(new Phom(temp));
        }
        return res;
    }
    // Ha phom - khong gui tu dong - oanh luon
}
