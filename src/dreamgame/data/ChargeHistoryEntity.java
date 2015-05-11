/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.data;

/**
 *
 * @author admin
 */
public class ChargeHistoryEntity {

    public String phone="";
    public String time="";
    public int money=0;

    public ChargeHistoryEntity(String s1,String s2, int m)
    {
        phone=s1;
        time = s2;
        money = m;
    }

}
