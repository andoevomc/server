/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.bacay.data;

/**
 *
 * @author binh_lethanh
 */
public class Poker {

    private int num;
    private PokerType type;

    public Poker(int p)
    {
        
        int temp = p-1;
        int type1 = temp/10;
        
        num = temp%10+1;
        
        switch(type1){
            case 1:
                type = PokerType.Pic;
                break;
            case 0:
                type = PokerType.Tep;
                break;
            case 2:
                type = PokerType.Ro;
                break;
            case 3:
                type = PokerType.Co;
                break;
            default:
                type = PokerType.Pic;
        }        
    }
    public Poker() {
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
        return (typeToInt() - 1) * 10 + this.num;
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

    public boolean isEqual(Poker other) {
        if (num==other.getNum() && type == other.getType())
            return true;
        
        return false;
    }
    public boolean isGreater(Poker other) {
        if (type == PokerType.Ro) {
            if (this.num == 1) {
                return true;
            } else if (other.num == 1 && other.type == PokerType.Ro) {
                return false;
            }
            else
            if (type == other.getType())
            {
                return (this.num > other.num);
            }else
                return true;
        } else 
        if (type == other.type)    
        {
            return (this.num > other.num);
        }else
        {
            return comparePokerType(this.type, other.type);
        }
    }

    private boolean comparePokerType(PokerType own, PokerType other) {
        int ownNum = pokerTypeToInt(own);
        int otherNum = pokerTypeToInt(other);
        return (ownNum > otherNum);
    }

    public int pokerTypeToInt(PokerType t) {
        if (t == PokerType.Pic) {
            return 2;
        } else if (t == PokerType.Tep) {
            return 1;
        } else if (t == PokerType.Co) {
            return 3;
        } else if (t == PokerType.Ro) {
            return 4;
        } else {
            BacayTable.mLog.debug("So sanh Poker Type sai");
            return 0;
        }
    }
}
