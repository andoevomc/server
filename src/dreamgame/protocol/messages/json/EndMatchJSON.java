package dreamgame.protocol.messages.json;

import java.util.Enumeration;

//import dreamgame.oantuti.data.OTTPlayer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import phom.data.PhomPlayer;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.bacay.data.Poker;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.poker.data.Utils;
import dreamgame.tienlen.data.TienLenPlayer;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.SessionManager;
import java.sql.DriverManager;

public class EndMatchJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(EndMatchJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            EndMatchResponse matchEnded = (EndMatchResponse) aResponseMessage;
            encodingObj.put("code", matchEnded.mCode);
            if (matchEnded.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error", matchEnded.mErrorMsg);
            } else if (matchEnded.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("matchId", matchEnded.matchId);

                switch (matchEnded.zoneID) {
                    case ZoneID.BACAY: {
                        encodingObj.put("new_room_owner_id", matchEnded.roomOwnerID);
                        encodingObj.put("room_owner_cash", matchEnded.ownerMoney);
                        encodingObj.put("ownerPlus", matchEnded.owner.cash - matchEnded.owner.firstCash);
                        encodingObj.put("notEnoughMoney", matchEnded.owner.notEnoughMoney());
                        JSONArray moneys = new JSONArray();
                        for (BacayPlayer p : matchEnded.bacayPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("money", p.moneyForBet);
                            jO.put("uid", p.id);
                            jO.put("isWin", p.isWin);
                            jO.put("Cash", p.cash);
                            jO.put("notEnoughMoney", p.notEnoughMoney());
                            moneys.put(jO);
                        }
                        encodingObj.put("Winners", moneys);

                        JSONArray pokers = new JSONArray();
                        Enumeration<Long> key = matchEnded.pokers.keys();
                        while (key.hasMoreElements()) {
                            long id = key.nextElement();
                            Poker[] p = matchEnded.pokers.get(id);
                            if (p != null) {
                                JSONObject encode = new JSONObject();
                                encode.put("uid", id);
                                Poker first = p[0];
                                Poker second = p[1];
                                Poker third = p[2];
                                JSONArray arrValues = new JSONArray();
                                JSONObject jCell = new JSONObject();
                                jCell.put("number", first.getNum());
                                jCell.put("type", first.pokerTypeToInt(first.getType()));
                                arrValues.put(jCell);
                                encode.put("first_poker", arrValues);

                                arrValues = new JSONArray();
                                jCell = new JSONObject();
                                jCell.put("number", second.getNum());
                                jCell.put("type", second.pokerTypeToInt(second.getType()));
                                arrValues.put(jCell);
                                encode.put("second_poker", arrValues);

                                arrValues = new JSONArray();
                                jCell = new JSONObject();
                                jCell.put("number", third.getNum());
                                jCell.put("type", third.pokerTypeToInt(third.getType()));
                                arrValues.put(jCell);
                                encode.put("third_poker", arrValues);

                                pokers.put(encode);
                            }
                        }
                        encodingObj.put("Pokers", pokers);
                        break;
                    }
                    case ZoneID.OTT: {
			//deleted
                        break;
                    }
                    case ZoneID.COTUONG: {
                        encodingObj.put("idWin", matchEnded.idWin);
                        encodingObj.put("isDraw", matchEnded.isPeace);
                        encodingObj.put("ownerMoney", matchEnded.ownerMoney);
                        encodingObj.put("playerMoney", matchEnded.playerMoney);

                        break;
                    }
                    case ZoneID.PHOM: {
                        //phomWinner is null
                        encodingObj.put("winner", matchEnded.phomWinner.id);
                        encodingObj.put("u_type", matchEnded.uType);
                        if (matchEnded.newOwner > 0) {
                            encodingObj.put("newOwner", matchEnded.newOwner);
                        }

                        JSONArray players = new JSONArray();
                        for (PhomPlayer p : matchEnded.phomPlayers) {
//                            if (p.point > 0 || p.moneyCompute() != 0 || p.isWin) {
                                JSONObject jO = new JSONObject();
                                jO.put("uid", p.id);
                                jO.put("is_win", p.isWin);
                                jO.put("point", p.point);
                                jO.put("cards", p.allPokersToString());
                                jO.put("money", p.money);
                                
                                jO.put("userCash", p.cash);
                                
                                if (p.isAutoPlay)
                                    jO.put("hetTien", true);
                                else
                                    jO.put("hetTien", p.notEnoughMoney());
                                
                                players.put(jO);
//                            }
                        }
                        encodingObj.put("Players", players);
                        break;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        encodingObj.put("idWin", matchEnded.idWin);
                        encodingObj.put("perfectType", matchEnded.perfectType);
                        JSONArray players = new JSONArray();
//                        System.out.println("matchEnded.tienLenPlayers.size():" + matchEnded.tienLenPlayers.size());
                        for (TienLenPlayer p : matchEnded.tienLenPlayers) {
//                            System.out.println("p.notEnoughMoney(): " + p.notEnoughMoney());
                            if (p.notEnoughMoney()) {
                                JSONObject jO = new JSONObject();
                                jO.put("uid", p.id);
                                players.put(jO);

                            }
                        }
                        encodingObj.put("HetTienList", players);

                        //gửi tiền thực về cho client
                        JSONArray Moneys = new JSONArray();
                        for (TienLenPlayer p : matchEnded.tienLenPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("uid", p.id);
                            jO.put("cash", DatabaseDriver.getUserMoney(p.id));
                            Moneys.put(jO);

                        }
                        encodingObj.put("CashList", Moneys);
                        //end

                        //gửi stt tới về
                        JSONArray sttTois = new JSONArray();
                        for (TienLenPlayer p : matchEnded.tienLenPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("uid", p.id);
                            jO.put("sttToi", p.sttToi);
                            sttTois.put(jO);

                        }
                        encodingObj.put("toiList", sttTois);
                        //end
                        if (matchEnded.perfectType == 0) {
                            //Trường hợp có 2 người chơi mà chủ room thoát thì gửi về chủ room mới
                            System.out.println("chủ room mới là: " + matchEnded.newOwner);
                            if (matchEnded.newOwner > 0) {
                                encodingObj.put("newOwner", matchEnded.newOwner);
                            }
                            //người này thoát ra làm endgame khi còn 2 người chơi
                            encodingObj.put("uid", matchEnded.uid);
                            encodingObj.put("uidTurn", matchEnded.uidTurn);
                            encodingObj.put("lastCards", matchEnded.lastCards);
                            JSONArray resultTienlen = new JSONArray();
                            for (int i = 0; i < matchEnded.tienlenResult.size(); i++) {
                                JSONObject jO = new JSONObject();
                                Object[] o = matchEnded.tienlenResult.get(i);
                                long uid = Long.parseLong(o[0].toString());
                                jO.put("uid", uid);
                                jO.put("money", Long.parseLong(o[1].toString()));
                                jO.put("note", o[2].toString());
                                if (uid != matchEnded.idWin) {
                                    jO.put("cards", o[3].toString());
                                }
//                                if (o[3] == null) {
//                                    jO.put("cards", "");
//                                }
                                resultTienlen.put(jO);
                            }

                            encodingObj.put("result", resultTienlen);

                            //Chặt chém
                            if (matchEnded.fightInfo != null && matchEnded.fightInfo.size() > 0) {
                                encodingObj.put("isFight", true);
                                long[] data = matchEnded.fightInfo.get(0);
                                //người bị chặt
                                encodingObj.put("be_fight", data[0]);
                                //người  chặt
                                encodingObj.put("fighter", data[1]);
                                //tiền chặt
                                encodingObj.put("money", data[2]);
                                if (data.length == 5) {
                                    encodingObj.put("isOverFight", true);
                                    //người bị chặt trước
                                    encodingObj.put("pre_be_fight", data[3]);
                                    //trả lại tiền
                                    encodingObj.put("oldMoney", data[4]);
                                } else {
                                    encodingObj.put("isOverFight", false);
                                }
                            } else {
                                encodingObj.put("isFight", false);
                            }
                        } else {
                            JSONArray resultTienlen = new JSONArray();
                            for (int i = 0; i < matchEnded.tienlenResult.size(); i++) {
                                JSONObject jO = new JSONObject();
                                Object[] o = matchEnded.tienlenResult.get(i);
                                jO.put("uid", Long.parseLong(o[0].toString()));
                                jO.put("cards", o[1].toString());
                                jO.put("money", Long.parseLong(o[2].toString()));
                                jO.put("note", o[3].toString());
                                resultTienlen.put(jO);
                            }
                            encodingObj.put("result", resultTienlen);
                        }
                        break;
                    }
                    case ZoneID.POKER: {
//                        encodingObj.put("idWin", matchEnded.idWin);
                        JSONArray players = new JSONArray();
                        for (PokerPlayer p : matchEnded.pokerPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("uid", p.id);
                            jO.put("name", p.username);
                            jO.put("money", p.money);
                            jO.put("cash", p.cash);
                            jO.put("notEnoughMoney", p.notEnoughMoney());
                            jO.put("cards", Utils.bytesToString(p.myHand));
                            if (p.cardsType > 0 && !p.isFold && !p.isOutGame) {
                                jO.put("cardsType", p.cardsType);
                                if (p.cardsType > 52) {
                                    jO.put("focusCards", Utils.bytesToString(p.focusCards));
                                }
                            }
                            jO.put("isFold", p.isFold);
                            players.put(jO);
                        }
                        if (matchEnded.uid > 0) {
                            encodingObj.put("uid", matchEnded.uid);
                        }
                        encodingObj.put("poker", matchEnded.remainPoker);
                        encodingObj.put("playerList", players);
                        encodingObj.put("potMoney", matchEnded.money);
                        
                        if (matchEnded.newOwner > 0) {
                            encodingObj.put("newOwner", matchEnded.newOwner);
                        }
                    }
                    break;
                    case ZoneID.XITO: {
//                        encodingObj.put("idWin", matchEnded.idWin);
                        JSONArray players = new JSONArray();
                        for (PokerPlayer p : matchEnded.pokerPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("uid", p.id);
                            jO.put("name", p.username);
                            jO.put("money", p.money);
                            jO.put("cash", p.cash);
                            jO.put("notEnoughMoney", p.notEnoughMoney());
                            jO.put("cards", Utils.bytesToString(p.myHand));
                            if (p.cardsType > 0 && !p.isFold && !p.isOutGame) {
                                jO.put("cardsType", p.cardsType);
                                if (p.cardsType > 52) {
                                    jO.put("focusCards", Utils.bytesToString(p.focusCards));
                                }
                            }
                            jO.put("isFold", p.isFold);
                            players.put(jO);
                        }
                        if (matchEnded.uid > 0) {
                            encodingObj.put("uid", matchEnded.uid);
                        }
//                        encodingObj.put("poker", matchEnded.remainPoker);
                        if (matchEnded.newOwner > 0) {
                            encodingObj.put("newOwner", matchEnded.newOwner);
                        }
                        encodingObj.put("playerList", players);
                        encodingObj.put("potMoney", matchEnded.money);
                        break;
                    }

                    case ZoneID.BAUCUA: {
//                        encodingObj.put("idWin", matchEnded.idWin);
                        JSONArray players = new JSONArray();
                        for (BauCuaPlayer p : matchEnded.bauCuaPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("uid", p.id);
                            jO.put("name", p.username);
                            jO.put("money", p.money);
                            jO.put("cash", p.cash);
                            jO.put("notEnoughMoney", p.notEnoughMoney());
                            players.put(jO);
                        }
                        encodingObj.put("playerList", players);
                        JSONArray result = new JSONArray();
                        for (int i = 0; i < 3; i++) {
                            JSONObject jO = new JSONObject();
                            jO.put("" + i, matchEnded.result[i]);
                            result.put(jO);
                        }
                        encodingObj.put("result", result);

                    }
                    break;
                        case ZoneID.MAUBINH: {
                        break;
                    }  
                    default:
                        break;
                }

            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
