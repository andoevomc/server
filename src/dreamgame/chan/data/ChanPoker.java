/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.chan.data;

/**
 *
 * @author Work
 */
public class ChanPoker {
    public int value;
    public int type;//0,1,2 - van van sach
    public int num;//2-9, 10=chi chi
        
    public String toString(){
        String t[]={"Va.n","Va(n","Sach"};
        
        if (value==25) return "Chi";
        
        String s=num+2+"_"+t[type];
        return s;
    }
    public boolean isEqual(ChanPoker other)
    {
        if (num==other.num && type==other.type)
            return true;
        return false;
    }
    public int toInt(){
        return value;
    }
            
    public static ChanPoker numToChanPoker(int input){       
        ChanPoker p=new ChanPoker();
        p.value=input;
        int t=p.value-1;
        p.type=t/8;
        p.num=t%8;
        
        if (input==25)
            p.num=10;
        
        return p;
    }
}
