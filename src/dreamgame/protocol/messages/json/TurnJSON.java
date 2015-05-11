/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.Utils;
import dreamgame.protocol.messages.TurnRequest;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.tienlen.data.TienLenPlayer;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.Zone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author binh_lethanh
 */
public class TurnJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(TurnJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // request messsage
            TurnRequest matchTurn = (TurnRequest) aDecodingObj;
            // parsing
            matchTurn.mMatchId = jsonData.getLong("match_id");
            matchTurn.uid = jsonData.getLong("uid");

            if (jsonData.has("isDuoi")) {
                matchTurn.isDuoi = jsonData.getBoolean("isDuoi");
            }
            if (jsonData.has("card")) {
                //phom
                try {
                    matchTurn.phomCard = jsonData.getInt("card");
                } catch (Exception e) {
                }
            }//phom
            else {
                //Caro
                try {
                    matchTurn.mRow = jsonData.getInt("row");
                    matchTurn.mCol = jsonData.getInt("col");
                    matchTurn.mType = jsonData.getInt("type");
                } catch (Exception e) {
                }
                //Co tuong
                try {
                    //System.out.println("VAO DAY KHONG MAY OI. TURN cua CO TUONG");
                    matchTurn.mv.fromCol = jsonData.getInt("fromCol");
                    matchTurn.mv.fromRow = jsonData.getInt("fromRow");
                    matchTurn.mv.toCol = jsonData.getInt("toCol");
                    matchTurn.mv.toRow = jsonData.getInt("toRow");
                } catch (Exception e123) {
                    //e123.printStackTrace();
                }
                //Tienlen
                try {
                    String cards = jsonData.getString("cards");
                    matchTurn.tienlenCards = cards;

//                    Thomc
//                    String[] card = com.migame.tienlen.data.Utils.stringSplit(cards, "#");
//                    matchTurn.tienlenCards = new com.migame.tienlen.data.Poker[card.length];
//                    for (int i = 0; i < card.length; i++) {
//                        matchTurn.tienlenCards[i] = com.migame.tienlen.data.Utils.numToPoker(Byte.parseByte(card[i]));
//                    }

                } catch (Exception eTienlen) {
                }
                try {
                    matchTurn.isGiveup = jsonData.getBoolean("isGiveup");
                } catch (Exception eTienlen) {
                }
                //bacay + bctc +poker
                try {
                    matchTurn.money = jsonData.getLong("money");
                } catch (Exception e) {
                }

                //ott
                try {
                    matchTurn.ottObject = jsonData.getInt("object");
                } catch (Exception e) {
                }
                //poker
                try {
                    matchTurn.isFold = jsonData.getBoolean("isFold");


                } catch (Exception e) {
                }



                //bau cua
                try {
                    matchTurn.piece = jsonData.getInt("piece");
                    matchTurn.num = jsonData.getInt("num");
                } catch (Exception e) {
                }
            }
            //xito
            try {
                matchTurn.isShow = jsonData.getBoolean("isShow");
                matchTurn.visibleCard = jsonData.getInt("card");
            } catch (Exception e) {
            }
            try {
                matchTurn.chi1 = jsonData.getString("chi1");
            } catch (Exception e) {
            }
            try {
                matchTurn.chi2 = jsonData.getString("chi2");
            } catch (Exception e) {
            }
            try {
                matchTurn.chi3 = jsonData.getString("chi3");
            } catch (Exception e) {
            }
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            TurnResponse matchTurn = (TurnResponse) aResponseMessage;
            encodingObj.put("code", matchTurn.mCode);
            encodingObj.put("matchId", matchTurn.matchId);

            if (matchTurn.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchTurn.mErrorMsg);
            } else if (matchTurn.mCode == ResponseCode.SUCCESS) {
                //encodingObj.put("is_end", matchTurn.mIsEnd);
                switch (matchTurn.zoneID) {
                    case ZoneID.BACAY:
                        encodingObj.put("stt_to", matchTurn.sttTo);
                        encodingObj.put("money", matchTurn.money);
                        encodingObj.put("uid", matchTurn.nextID);
                        encodingObj.put("pre_uid", matchTurn.preID);
                        encodingObj.put("timeReq", matchTurn.timeReq);
                        break;
                    case ZoneID.GAME_CHAN:
                    case ZoneID.PHOM: {
                        encodingObj.put("card", matchTurn.phomCard);
                        encodingObj.put("canEat", matchTurn.canEat);
                        encodingObj.put("next_id", matchTurn.nextID);
                        encodingObj.put("curr_id", matchTurn.preID);
                        encodingObj.put("deck", matchTurn.deck);

                        encodingObj.put("u", matchTurn.u);
                        encodingObj.put("phom", matchTurn.phom);

                        if (matchTurn.zoneID == ZoneID.GAME_CHAN) {
                            encodingObj.put("isDuoi", matchTurn.isDuoi);
                            encodingObj.put("isChiu", matchTurn.isChiu);
                        }


                        break;
                    }
                    case ZoneID.OTT:
                        break;
                    case ZoneID.COTUONG:
                        encodingObj.put("uid", matchTurn.nextID);
                        encodingObj.put("fromCol", matchTurn.mv.fromCol);
                        encodingObj.put("fromRow", matchTurn.mv.fromRow);
                        encodingObj.put("toCol", matchTurn.mv.toCol);
                        encodingObj.put("toRow", matchTurn.mv.toRow);
                        encodingObj.put("remainTime", matchTurn.remainTime);
                        break;
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
//                        Thomc
//                        encodingObj.put("number_card", matchTurn.tienlenCards.length);
//                        String data = "" + matchTurn.tienlenCards[0];
//                        for (int i = 1; i < matchTurn.tienlenCards.length; i++) {
//                            data += "#" + matchTurn.tienlenCards[i];
//                        }
//                        encodingObj.put("cards", data);
                        encodingObj.put("cards", matchTurn.tienlenCards);
                        encodingObj.put("next_id", matchTurn.nextID);
                        encodingObj.put("isNewRound", matchTurn.isNewRound);
                        encodingObj.put("currID", matchTurn.currID);
                        encodingObj.put("isGiveup", matchTurn.isGiveup);
                        //Chặt chém
                        if (matchTurn.fightInfo.size() > 0) {
                            encodingObj.put("isFight", true);
                            long[] data = matchTurn.fightInfo.get(0);
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
                        //gửi stt tới về
                        JSONArray sttTois = new JSONArray();
                        for (TienLenPlayer p : matchTurn.toiPlayers) {
                            JSONObject jO = new JSONObject();
                            jO.put("uid", p.id);
                            jO.put("sttToi", p.sttToi);
                            sttTois.put(jO);

                        }
                        encodingObj.put("toiList", sttTois);
                        break;
                    }
                    case ZoneID.BAUCUA: {
                        encodingObj.put("uid", matchTurn.currID);
                        encodingObj.put("piece", matchTurn.piece);
                        encodingObj.put("num", matchTurn.num);
                        //for owner
                        encodingObj.put("total", matchTurn.totalPiece);
                        encodingObj.put("cash", matchTurn.cash);
                        break;
                    }
                    case ZoneID.CARO: {
			//deleted
                        break;
                    }
                    case ZoneID.POKER: {
                        encodingObj.put("betDes", matchTurn.betTypeDes);
                        encodingObj.put("isNewRound", matchTurn.isNewRound);
                        encodingObj.put("isSendAll", false);
                        if (matchTurn.isGiveup) {
                            encodingObj.put("isFold", true);
                        }
                        if (matchTurn.nextID > 0) {
                            encodingObj.put("nextID", matchTurn.nextID);
                            encodingObj.put("minBet", matchTurn.minBet);
                            encodingObj.put("maxBet", matchTurn.maxBet);
                        }
                        encodingObj.put("potMoney", matchTurn.potMoney);
                        if (matchTurn.isNewRound) {
                            encodingObj.put("poker", matchTurn.poker);

//                        encodingObj.put("idWin", matchEnded.idWin);
                            JSONArray players = new JSONArray();
                            for (PokerPlayer p : matchTurn.pokerPlayers) {
                                JSONObject jO = new JSONObject();
                                jO.put("uid", p.id);
                                jO.put("cash", p.cash);
                                if (p.cardsType > 52 && !p.isOutGame && !p.isFold && !p.isAllIn()) {
                                    jO.put("cardsType", p.cardsType);
                                    jO.put("focusCards", Utils.bytesToString(p.focusCards));
                                }
                                players.put(jO);

                            }
                            encodingObj.put("playerList", players);

                        } else {
                            encodingObj.put("currID", matchTurn.currID);
                            encodingObj.put("money", matchTurn.money);

//                            encodingObj.put("potMoney", matchTurn.potMoney);
                        }

                        break;
                    }
                    case ZoneID.XITO: {
                        System.out.println("turn json vao xito");
                        encodingObj.put("isShow", matchTurn.isShow);
                        if (matchTurn.isShow) {
                            encodingObj.put("currID", matchTurn.currID);
                            encodingObj.put("card", matchTurn.visibleCard);
                        } else {
                            encodingObj.put("betDes", matchTurn.betTypeDes);
                            encodingObj.put("isNewRound", matchTurn.isNewRound);
                            if (matchTurn.isGiveup) {
                                encodingObj.put("isFold", true);
                            }
                            if (matchTurn.nextID > 0) {
                                encodingObj.put("nextID", matchTurn.nextID);
                                encodingObj.put("minBet", matchTurn.minBet);
                                encodingObj.put("maxBet", matchTurn.maxBet);
                            }
                            encodingObj.put("potMoney", matchTurn.potMoney);
                            if (matchTurn.isNewRound) {
                                JSONArray players = new JSONArray();
                                for (PokerPlayer p : matchTurn.pokerPlayers) {
                                    JSONObject jO = new JSONObject();
                                    jO.put("uid", p.id);
                                    jO.put("cash", p.cash);
                                    if (p.cardsType > 52 && !p.isOutGame && !p.isFold && !p.isAllIn()) {
                                        jO.put("cardsType", p.cardsType);
                                        jO.put("focusCards", Utils.bytesToString(p.focusCards));
                                    }
                                    jO.put("card", p.currentCard);
                                    players.put(jO);

                                }
                                encodingObj.put("playerList", players);
                                encodingObj.put("isVisible", matchTurn.isVisible);

                            } else {
                                encodingObj.put("money", matchTurn.money);
                                encodingObj.put("currID", matchTurn.currID);
                            }
                        }
                        break;
                    }
                    case ZoneID.MAUBINH: {
                        break;
                    }
                    default:
                        break;
                }

            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
