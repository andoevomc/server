/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phom.data;

/**
 * 
 * @author binh_lethanh
 */
public class Poker {

    public int num;
    public PokerType type;

    public Poker() {
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

    public Poker(int n) {
        this.num = n%13+1;
        int type1 = n/13;
        
        if (type1==0) type=PokerType.Pic;
        if (type1==1) type=PokerType.Tep;
        if (type1==2) type=PokerType.Ro;
        if (type1==3) type=PokerType.Co;
    }
    
    public Poker(int n, PokerType t) {
        this.num = n;
        this.type = t;
    }

    public PokerType getType() {
        return type;
    }

    public int getNum() {
        return num;
    }

    private int typeToInt() {
        if (this.type == PokerType.Pic) {
            return 2;
        } else if (this.type == PokerType.Tep) {
            return 1;
        } else if (this.type == PokerType.Ro) {
            return 3;
        } else if (this.type == PokerType.Co) {
            return 4;
        } else {
            return 0;
        }
    }

    public int toInt() {
        //return ((this.num - 1) * 4 + typeToInt());
        return (typeToInt() - 1) * 13 + this.num;
    }

    public boolean isEqual(Poker other)
    {
        if (num==other.num && type==other.type)
            return true;
        return false;
    }
    public boolean isCa(Poker other) {
        if (this.num == other.num) {
            return true;
        }
        if (((this.num - other.num <= 2) && (this.num - other.num >= -2))
                && (this.type == other.type)) {
            return true;
        }
        return false;
    }

    public boolean isGreater(Poker other) {
        return (this.toInt() > other.toInt());
    }
}
