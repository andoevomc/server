/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.config;

import dreamgame.data.ZoneID;

/**
 *
 * @author Administrator
 */
public class GameRuleConfig {
    public static final int MONEY_TIMES_BET_TO_JOIN = 10;	// Tiền người chơi có cần lớn hơn ít nhất bao nhiêu lần so với tiền cược của bàn để có thể tham gia    
    public static final String MONEY_SYMBOL = "G";		// 
    public static final int MONEY_TIMES_BET_TO_CREATE = 10;	// 
    
    // money required to join for each game
    public static long getRequiredMoneyToJoin(int gameZoneID, long minBet) {
	long money = 0;
	switch (gameZoneID) {
	    case ZoneID.COTUONG:
	    case ZoneID.CARO:
		money = minBet;
		break;
		
	    case ZoneID.BACAY:
	    case ZoneID.OTT:
	    case ZoneID.GAME_CHAN:
	    case ZoneID.PHOM:
	    case ZoneID.TIENLEN_MB:
	    case ZoneID.TIENLEN_DEMLA:
	    case ZoneID.TIENLEN:
	    case ZoneID.BAUCUA:
	    case ZoneID.POKER:
	    case ZoneID.XITO:
	    case ZoneID.MAUBINH:
	    default:
		money = GameRuleConfig.MONEY_TIMES_BET_TO_JOIN * minBet;
		break;
	}
	return money;
    }    
}
