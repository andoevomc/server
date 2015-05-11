/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.data;

/**
 *
 * @author binh_lethanh
 */
public class ZoneID {

    public static final int GLOBAL = 0;
    public static final int BACAY = 1;
    public static final int CARO = 2;
    public static final int BAUCUA = 3;
    public static final int PHOM = 4;
    public static final int TIENLEN = 5;
    public static final int COTUONG = 6;
    public static final int TIENLEN_DEMLA = 7;
    public static final int TIENLEN_MB = 8;
    public static final int OTT = 9;
    public static final int POKER = 10;
    public static final int XITO = 11;
    public static final int GAME_CHAN = 12;
    public static final int MAUBINH = 13;
    public static final int PRIVATE_CHAT = 100;

    public static String getZoneName(int zoneID) {
        switch (zoneID) {
            case BACAY:
                return "Ba Cây";
            case OTT:
                return "Oẳn tù tì";
            //Add more here
            case POKER:
                return "POKER";
            case XITO:
                return "Xì tố";
            case CARO:
                return "Caro";
            case COTUONG:
                return "Cờ Tướng";
            case PHOM:
                return "Phỏm";
            case TIENLEN:
                return "Tiến lên miền nam";
            case TIENLEN_MB:
                return "Tiến lên miền bắc";
            case BAUCUA:
                return "Bầu cua";
            case GAME_CHAN:
                return "Game Chắn";
            case MAUBINH:
                return "Mậu Binh";
            default:
                return "Toàn cục";
        }
    }
}
