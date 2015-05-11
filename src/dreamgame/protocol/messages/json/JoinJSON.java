/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.JoinRequest;
import dreamgame.protocol.messages.JoinResponse;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;

//import dreamgame.caro.data.TableCell;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.piece;

//import dreamgame.oantuti.data.OTTPlayer;
import dreamgame.poker.data.PokerPlayer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import phom.data.PhomPlayer;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.Utils;
import com.mysql.jdbc.Util;

/**
 *
 * @author binh_lethanh
 */
public class JoinJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(JoinJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // request messsage
            JoinRequest matchJoin = (JoinRequest) aDecodingObj;
            // parsing
            matchJoin.mMatchId = jsonData.getLong("match_id");
            matchJoin.uid = jsonData.getLong("uid");
            if (jsonData.has("zone_id")) {
                matchJoin.zone_id = jsonData.getInt("zone_id");
            }

            if (jsonData.has("quickplay")) {
                matchJoin.quickplay = jsonData.getBoolean("quickplay");
            }

            try {
                matchJoin.password = jsonData.getString("password");
            } catch (Exception ee) {
            }
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public void addPhomData(JoinResponse matchJoin, JSONObject jCell, PhomPlayer player) {
        try {
            jCell.put("id", player.id);
            jCell.put("username", player.username);
            jCell.put("level", player.level);
            jCell.put("avatar", player.avatarID);
            jCell.put("money", player.cash);
            jCell.put("isReady", player.isReady);
            jCell.put("isAuto", player.isAutoPlay);

            if ((matchJoin.isResume || matchJoin.isObserve) && !player.isObserve) {
                jCell.put("playedCards", player.cardToString(player.frontCards));
                jCell.put("eatCards", player.cardToString(player.eatingCards));
                jCell.put("doneBocBai", player.doneBocBai);

                if (player.haPhom) {
                    jCell.put("phoms", player.phomToString(player.phoms));
                } else {
                    jCell.put("phoms", "0");
                }
            }
        } catch (Exception e) {
        }

    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            JoinResponse matchJoin = (JoinResponse) aResponseMessage;
            encodingObj.put("code", matchJoin.mCode);
            if (matchJoin.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchJoin.mErrorMsg);
            } else if (matchJoin.mCode == ResponseCode.SUCCESS) {
                encodingObj.put("match_id", matchJoin.mMatchId);
                encodingObj.put("minBet", matchJoin.minBet);
                encodingObj.put("roomname", matchJoin.roomName);
                encodingObj.put("capacity", matchJoin.capacity);

                encodingObj.put("roomOwner_id", matchJoin.roomOwner.id);
                encodingObj.put("roomOwner_username", matchJoin.roomOwner.username);
                encodingObj.put("roomOwner_level", matchJoin.roomOwner.level);
                encodingObj.put("roomOwner_avatar", matchJoin.roomOwner.avatarID);
                encodingObj.put("roomOwner_money", matchJoin.roomOwner.cash);
                encodingObj.put("joinZone", matchJoin.zoneID);
                encodingObj.put("isInvite", matchJoin.isInvite);
                switch (matchJoin.zoneID) {
                    case ZoneID.BACAY: {
                        JSONArray arrValues = new JSONArray();
                        for (BacayPlayer player : matchJoin.mPlayerBacay) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", player.id);
                            jCell.put("username", player.username);
                            jCell.put("level", player.level);
                            jCell.put("avatar", player.avatarID);
                            jCell.put("money", player.cash);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);
                        break;
                    }
                    case ZoneID.OTT: {
			//deleted
                        break;
                    }
                    case ZoneID.GAME_CHAN: {
                        break;
                    }
                    case ZoneID.PHOM: {
                        encodingObj.put("isAn", matchJoin.isAn);
                        encodingObj.put("isTaiGui", matchJoin.isTaiGui);
                        encodingObj.put("isPlaying", matchJoin.isPlaying);

                        if (matchJoin.isResume || matchJoin.isObserve) {
                            if (matchJoin.isResume) {
                                encodingObj.put("cards", matchJoin.cards);
                            }

                            encodingObj.put("turn", matchJoin.turn);
                            encodingObj.put("deck", matchJoin.deck);
                        }

                        JSONArray arrValues = new JSONArray();

                        for (PhomPlayer player : matchJoin.mPlayerPhom) {
                            JSONObject jCell = new JSONObject();
                            addPhomData(matchJoin, jCell, player);
                            jCell.put("isObserve", false);
                            arrValues.put(jCell);
                        }

                        for (PhomPlayer player : matchJoin.mWaitingPlayerPhom) {
                            JSONObject jCell = new JSONObject();
                            addPhomData(matchJoin, jCell, player);
                            jCell.put("isObserve", true);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);

                        break;
                    }
                    case ZoneID.CARO: {
			//deleted
                        break;
                    }
                    case ZoneID.COTUONG: {
                        JSONArray arrValues = new JSONArray();
                        encodingObj.put("uid", matchJoin.uid);
                        for (CoTuongPlayer player : matchJoin.mPlayerCoTuong) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", player.id);
                            jCell.put("username", player.username);
                            jCell.put("level", player.level);
                            jCell.put("avatar", player.avatarID);
                            jCell.put("money", player.cash);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);
                        if (matchJoin.mPlayerList != null) {
                            JSONArray player_list = new JSONArray();
//                            for (CoTuongPlayer player : matchJoin.mPlayerList) {
//                                JSONObject jCell = new JSONObject();
//                                jCell.put("id", player.id);
//                                jCell.put("username", player.username);
//                                jCell.put("level", player.level);
//                                jCell.put("avatar", player.avatarID);
//                                jCell.put("money", player.cash);
//                                player_list.put(jCell);
//                            }
                            encodingObj.put("player_list", player_list);
                        }
                        encodingObj.put("isJoinAfterPlaying", matchJoin.isJoinAfterPlaying);
                        encodingObj.put("available", matchJoin.available);
                        encodingObj.put("totalTime", matchJoin.totalTime);
                        if (matchJoin.isJoinAfterPlaying) {
                            JSONArray arrPieces = new JSONArray();
                            piece[][] board = matchJoin.board;
                            for (int i = 0; i < board.length; i++) {
                                for (int j = 0; j < 10; j++) {
                                    if (board[i][j] != null) {
                                        JSONObject jCell = new JSONObject();
                                        piece p = (piece) board[i][j];
                                        jCell.put("col", p.col);
                                        jCell.put("row", p.row);
                                        jCell.put("name", p.name);
                                        jCell.put("isBlk", p.isBlk);
                                        jCell.put("focusOn", p.focusOn);
                                        jCell.put("visible", p.visible);
                                        arrPieces.put(jCell);
                                    }
                                }
                            }
                            encodingObj.put("chessboard", arrPieces);
                            encodingObj.put("currentID", matchJoin.currentID);
                        }
                        break;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        if (matchJoin.isObserve) {
                            if (!matchJoin.cards.equals("")) {
                                encodingObj.put("lastCards", matchJoin.cards);

                            }
                            encodingObj.put("turn", matchJoin.turn);
//                            encodingObj.put("deck", matchJoin.deck);
                        }
                        JSONArray arrValues = new JSONArray();
                        for (TienLenPlayer player : matchJoin.mTienLenPlayer) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", player.id);
                            jCell.put("username", player.username);
                            jCell.put("level", player.level);
                            jCell.put("avatar", player.avatarID);
                            jCell.put("money", player.cash);
                            if (matchJoin.isObserve) {
                                jCell.put("isReady", true);
                            }
                            jCell.put("isReady", player.isReady);
                            jCell.put("isOutGame", player.isOutGame);
                            jCell.put("isObserve", false);
                            if (matchJoin.isObserve) {
                                jCell.put("numHand", player.numHand);
                            }

                            arrValues.put(jCell);
                        }


                        for (TienLenPlayer viewer : matchJoin.mWaitingPlayerTienlen) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", viewer.id);
                            jCell.put("username", viewer.username);
                            jCell.put("level", viewer.level);
                            jCell.put("avatar", viewer.avatarID);
                            jCell.put("money", viewer.cash);
                            jCell.put("isReady", viewer.isReady);
                            jCell.put("isOutGame", viewer.isOutGame);
                            jCell.put("isObserve", true);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);

                        break;
                    }
                    case ZoneID.BAUCUA: {
//                        if (matchJoin.isObserve) {
                        encodingObj.put("isObserve", matchJoin.isObserve);
                        if (matchJoin.isObserve) {
                            encodingObj.put("time", matchJoin.time);
                        }
//                             encodingObj.put("turn", matchJoin.turn);
//                            if (!matchJoin.cards.equals("")) {
//                                encodingObj.put("lastCards", matchJoin.cards);
//
//                            }
//                            encodingObj.put("turn", matchJoin.turn);
////                            encodingObj.put("deck", matchJoin.deck);
//                        }
                        JSONArray arrValues = new JSONArray();
                        for (BauCuaPlayer player : matchJoin.mBauCuaPlayer) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", player.id);
                            jCell.put("username", player.username);
                            jCell.put("level", player.level);
                            jCell.put("avatar", player.avatarID);
                            jCell.put("money", player.cash);
                            if (matchJoin.isObserve) {
                                jCell.put("isReady", true);
                            }
                            jCell.put("isReady", player.isReady);
                            jCell.put("isOutGame", player.isOutGame);
                            jCell.put("isObserve", false);
//                            if (matchJoin.isObserve) {
////                                jCell.put("numHand", player.numHand);
//                            }

                            arrValues.put(jCell);
                        }


//                        for (BauCuaPlayer viewer : matchJoin.mWaitingBauCuaPlayer) {
//                            JSONObject jCell = new JSONObject();
//                            jCell.put("id", viewer.id);
//                            jCell.put("username", viewer.username);
//                            jCell.put("level", viewer.level);
//                            jCell.put("avatar", viewer.avatarID);
//                            jCell.put("money", viewer.cash);
//                            jCell.put("isReady", viewer.isReady);
//                            jCell.put("isOutGame", viewer.isOutGame);
//                            jCell.put("isObserve", true);
//                            arrValues.put(jCell);
//                        }
                        encodingObj.put("table_values", arrValues);

                        break;
                    }
                    case ZoneID.POKER: {
                        System.out.println("came here!");
                        if (matchJoin.isObserve) {
                            if (!matchJoin.cards.equals("")) {
                                encodingObj.put("cards", matchJoin.cards);
                                encodingObj.put("potMoney", matchJoin.minBet);
                            }
                            encodingObj.put("turn", matchJoin.turn);
//                            encodingObj.put("deck", matchJoin.deck);
                        }
                        JSONArray arrValues = new JSONArray();
                        for (PokerPlayer player : matchJoin.mPokerPlayer) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", player.id);
                            jCell.put("username", player.username);
                            jCell.put("level", player.level);
                            jCell.put("avatar", player.avatarID);
                            jCell.put("money", player.cash);
                            if (matchJoin.isObserve) {
                                jCell.put("isReady", true);
                            }
                            jCell.put("isReady", player.isReady);
                            jCell.put("isOutGame", player.isOutGame);
                            jCell.put("isObserve", false);
                            if (matchJoin.isObserve) {
                                jCell.put("numHand", player.numHand);
                            }

                            arrValues.put(jCell);
                        }
                        for (PokerPlayer viewer : matchJoin.mWaitingPokerPlayer) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", viewer.id);
                            jCell.put("username", viewer.username);
                            jCell.put("level", viewer.level);
                            jCell.put("avatar", viewer.avatarID);
                            jCell.put("money", viewer.cash);
                            jCell.put("isReady", viewer.isReady);
                            jCell.put("isOutGame", viewer.isOutGame);
                            jCell.put("isObserve", true);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);

                        break;
                    }

                    case ZoneID.XITO: {
                        System.out.println("came here!");
                        if (matchJoin.isObserve) {
//                            if (!matchJoin.cards.equals("")) {
//                                encodingObj.put("cards", matchJoin.cards);
                            encodingObj.put("potMoney", matchJoin.minBet);
//                            }
                            encodingObj.put("turn", matchJoin.turn);
//                            encodingObj.put("deck", matchJoin.deck);
                        }
                        JSONArray arrValues = new JSONArray();
                        for (PokerPlayer player : matchJoin.mPokerPlayer) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", player.id);
                            jCell.put("username", player.username);
                            jCell.put("level", player.level);
                            jCell.put("avatar", player.avatarID);
                            jCell.put("money", player.cash);
                            if (matchJoin.isObserve) {
                                jCell.put("isReady", true);
                                jCell.put("numHand", player.getNumHand(matchJoin.mType));
                                jCell.put("visibleCards", dreamgame.poker.data.Utils.bytesToString(player.getMyHandForOther(matchJoin.mType)));
                            }
                            jCell.put("isReady", player.isReady);
                            jCell.put("isOutGame", player.isOutGame);
                            jCell.put("isObserve", false);
                            arrValues.put(jCell);
                        }
                        for (PokerPlayer viewer : matchJoin.mWaitingPokerPlayer) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", viewer.id);
                            jCell.put("username", viewer.username);
                            jCell.put("level", viewer.level);
                            jCell.put("avatar", viewer.avatarID);
                            jCell.put("money", viewer.cash);
                            jCell.put("isReady", viewer.isReady);
                            jCell.put("isOutGame", viewer.isOutGame);
                            jCell.put("isObserve", true);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);

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
