/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.data;

/**
 *
 * @author binh_lethanh
 */
public class ZoneIDConstant {

    public static final int ID_ZONE_GLOBAL = 0;
    public static final int ID_ZONE_PRIVATE_CHAT = 100;
    public static final int ID_ZONE_BACAY = 1;
    public static final int ID_ZONE_OTT = 2;
    public static final int ID_ZONE_BCTC = 3;
    public static final int ID_ZONE_CARO = 4;
    public static final int ID_ZONE_PHOM = 5;
    public static final int ID_ZONE_COTUONG = 6;

    public static String getZoneName(int zoneID) {
        switch (zoneID) {
            case ID_ZONE_BACAY:
                return "Ba Cây";
            case ID_ZONE_OTT:
                return "Oẳn tù tì";
            //Add more here
            case ID_ZONE_BCTC:
                return "BCTC";
            case ID_ZONE_CARO:
                return "Caro";
            case ID_ZONE_COTUONG:
                return "Cờ tướng";
            case ID_ZONE_PHOM:
                return "Phỏm";
            default:
                return "Toàn cục";
        }
    }
}
