/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.bacay.data;




import dreamgame.data.SimplePlayer;

import dreamgame.gameserver.framework.session.ISession;

/**
 *
 * @author binh_lethanh
 */
public class BacayPlayer extends SimplePlayer{
        public Poker[] playingCards;
        public int point;
        public int timeReq;
        public boolean isReady = true;
        public boolean isOwner;
        public long firstCash=0;
        public boolean isOutGame = false; //set true nếu thoát game
        public void setCurrentOwner(ISession currentOwner) {
			this.currentOwner = currentOwner;
		}
        /**
         * For Time Out 
         */
        public boolean isGetData = false;        
        public void setState(boolean is){
        	isGetData = is;
        }
        
        public void setCurrentMatchID(long currentMatchID) {
			this.currentMatchID = currentMatchID;
		}
        
        public BacayPlayer() {
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - BacayPlayer constructor");
	    }
            this.isGiveUp = false;
            this.timeReq = 0;
        }
        public void reset(long money){
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - reset: money = " + money);
	    }
        	point = 0;
        	isStop = false;
        	playingCards = new Poker[3];
        	moneyForBet = money;
        	timeReq = 0;
        	isGiveUp = false;
        	isGetData = false;
                firstCash=cash;
        }
        public void setReady(boolean isReady) {
			this.isReady = isReady;
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
        
        public void setCash(long c){
        	this.cash = c;
        }
	
        public BacayPlayer(long id) {
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - BacayPlayer constructor - id = " + id);
	    }
            this.id = id;
            this.isGiveUp = false;
            this.timeReq = 0;
        }
	
        public BacayPlayer(Poker[] inputPoker, long id, long minBet) {
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - BacayPlayer constructor - id = " + id + "; minBet = " + minBet);
	    }
            this.playingCards = inputPoker;
            this.id = id;
            this.moneyForBet = minBet;
            this.isGiveUp = false;
            this.timeReq = 0;
            
        }

        public void setPokers(String s){
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - setPokers: s = " + s);
	    }
            String[] Cards=s.split(" ");
            Poker[] pp = new Poker[3];
            
            for (int i=0;i<3;i++)
            {
                pp[i]=new Poker(Integer.parseInt(Cards[i]));
                System.out.println(pp[i]+" : "+pp[i].getNum()+" ; "+pp[i].getType());
            }

            this.playingCards = pp;
            compute();
            System.out.println(username + " : newPoint : "+point);
        }

        public void setPokers(Poker[] inputPoker){
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - setPokers : inputPoker size = " + inputPoker.length);
	    }
            this.playingCards = inputPoker;
            compute();
        }

        public int myCompute(){
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - myCompute");
	    }
            int po=0;
            for (Poker p : this.playingCards){
                po += p.getNum();
             }
            if((po % 10) == 0) {
            	po = 10;
            }else {
            	po = po % 10;
            }
            return po;
        }
        
        public void compute(){
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - compute");
	    }
            this.point=0;
            for (Poker p : this.playingCards){
                this.point += p.getNum();
             }
            if((this.point % 10) == 0) {
            	this.point = 10;
            }else {
            	this.point = this.point % 10;
            }

        }
        
        public Poker greatestPoker(){
            if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - greatestPoker");
	    }
	    
            Poker res = this.playingCards[0];
            for (int i = 1; i < 3; i ++){
                Poker p = this.playingCards[i];
                if (p.isGreater(res)) res = p;
            }
            System.out.println(username+" : Greatest : "+res);

            return res;

        }

        public void reSort(Poker[] sort) {
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - reSort");
	    }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3 - i - 1; j++) {
                    if (!sort[j].isGreater(sort[j+1])) {
                        Poker t=sort[j];
                        sort[j]=sort[j+1];
                        sort[j+1]=t;
                    }
                }
            }
        }
	
        public boolean isWin(BacayPlayer other){
	    if (dreamgame.config.DebugConfig.FOR_DEBUG) {
		System.out.println("--- BacayPlayer - isWin");
	    }
            Poker[] sort1=new Poker[3];
            Poker[] sort2=new Poker[3];
            for (int i=0;i<3;i++)
            {
                sort1[i]=playingCards[i];
                sort2[i]=other.playingCards[i];
            }
            reSort(sort1);
            reSort(sort2);
            
            System.out.println("isWin : "+username+" : "+this.point +" : "+other.username+" : "+other.point);
            if (this.point == other.point){
                //return this.greatestPoker().isGreater(other.greatestPoker());
                for (int i=0;i<3;i++)
                {
                    System.out.println(sort1[i]+" : "+sort2[i]+" ; "+sort1[i].isEqual(sort2[i]) +" : "+sort1[i].isGreater(sort2[i]));
                    if (!sort1[i].isEqual(sort2[i]))
                        return sort1[i].isGreater(sort2[i]);
                }
                return true;
            }else {
                return (this.point > other.point);
            }
        }
}
