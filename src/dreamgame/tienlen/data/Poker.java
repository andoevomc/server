/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.tienlen.data;


/**
 *
 * @author binh_lethanh
 */
public class Poker {
        private int num;
        private PokerType type;

        
        public Poker() {
        }
        public Poker(int n, PokerType t) {
            this.num = n;
            this.type = t;

        }

         public String toString() {
        String res = "";

        if (num==1) res += "At";
        else
        if (num==11) res += "J";
        else if(num == 12) res += "Q";
        else
        if (num==13) res += "K";
        else res += num;

        res += " " + pokerTypeToString();
        return res;
    }

    private String pokerTypeToString() {
        if (this.type == PokerType.Co) {
            return "co";
        } else if (this.type == PokerType.Ro) {
            return "ro";
        } else if (this.type == PokerType.Pic) {
            return "pic";
        } else if (this.type == PokerType.Tep) {
            return "tep";
        }
        return "";
    }
    
        public PokerType getType() {
			return type;
		}
        public int getNum() {
			return num;
		}
        private int typeToInt(){
        	if(this.type == PokerType.Pic){
        		return 1;
        	}else if(this.type == PokerType.Tep){
        		return 2;
        	}else if(this.type == PokerType.Ro){
        		return 3;
        	}else if(this.type == PokerType.Co){
        		return 4;
        	}else {
        		return 0;
        	}
        }
        public int toInt(){
        	switch (this.num) {
			case 1:
				return (11*4 + typeToInt());
			case 2:	
				return (12*4 + typeToInt());
			default:
				return ((this.num-3)*4 + typeToInt());
			}
        }
        public boolean isGreater(Poker other){
        	return (this.toInt() > other.toInt());
        }
    }
