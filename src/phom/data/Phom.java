package phom.data;

import java.util.ArrayList;

public class Phom {
    /*
     * true : bo ngang false: bo doc
     */

    public boolean type;
    public ArrayList<Poker> cards = new ArrayList<Poker>();
    public ArrayList<Poker> guis = new ArrayList<Poker>();

    public byte[] toArray() {
        byte[] res = new byte[cards.size() + guis.size()];
        int i=0;
        for (Poker p:cards)
        {
            res[i]=(byte)p.toInt();
            i++;
        }
        for (Poker p:guis)
        {
            res[i]=(byte)p.toInt();
            i++;
        }
        return res;
    }

    public int getType(int b) {
        return (b - 1) / 13;
    }

    public int getValue(int b) {
        return (b - 1) % 13;
    }

    public boolean okGui(byte card) {
        int count = 0;
        byte[] phom = toArray();      
        
        /*for (int i=0;i<phom.length;i++)
        {
            System.out.print(phom[i]+",");
        }
        System.out.println();*/
        
        for (int j = 0; j < phom.length; j++) {
            if (getValue(card) == getValue(phom[j])) {
                count++;
            }
        }
        
        if (count > 1) {
            return true;
        }
        
        count = 0;
        for (int j = 0; j < phom.length; j++) {
            if (getType(card) == getType(phom[j]) && Math.abs(getValue(card) - getValue(phom[j])) < 3) {
                count++;
            }
        }
        
        if (count > 1) {
            return true;
        }

        return false;
    }

    public String toString() {
        System.out.println("cards : " + cards.size());
        System.out.println("guis : " + guis.size());
        String res = "";
        if (cards.size() > 0) {
            res += cards.get(0).toInt();
            for (int i = 1; i < cards.size(); i++) {
                res += "#";
                res += cards.get(i).toInt();
            }
        }
        for (int i = 0; i < guis.size(); i++) {
            res += "#";
            res += guis.get(i).toInt();
        }
        return res;
    }

    public void gui(ArrayList<Poker> gui) throws PhomException {
        //ArrayList<Poker> temp = cards;
        //temp.addAll(guis);
        //temp.addAll(gui);
        guis.addAll(gui);
//		try{
//			if(Utils.checkPhom(temp)){
//				this.guis.addAll(gui);
//			}else {
//				throw new PhomException("Gui sai");
//			}
//		}catch (Exception e) {
//			throw new PhomException("Gui sai");
//		}

    }

    public Phom(ArrayList<Poker> input) throws PhomException {
        //try{
        //this.type = Utils.checkPhom(input);
        this.cards = input;
        //}catch (PhomException e) {
        //	//throw e;
        //}

    }
}
