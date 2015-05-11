package dreamgame.cotuong.data;

import java.util.Vector;

/**
 *
 * @author Dinhpv
 */
public final class chessBoard {

    private Vector blkPieces, redPieces;
    protected piece board[][] = new piece[9][10];
    private int movingCnt;
    final static int CARRIAGE = 0, HORSE = 1, PAO = 2, ELEPHANT = 3, GUARD = 4, KING = 5, SOLDIER = 6;

    public chessBoard(Vector blkPieces, Vector redPieces) {
        this.blkPieces = blkPieces;
        this.redPieces = redPieces;

        piece pc = null;

        for (int i = 0; i < blkPieces.size(); i++) {
            pc = (piece) blkPieces.elementAt(i);
            board[pc.col][pc.row] = pc;
        }
        for (int i = 0; i < redPieces.size(); i++) {
            pc = (piece) redPieces.elementAt(i);
            board[pc.col][pc.row] = pc;
        }
    }

    public void initAgain() {
        piece pc = null;
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 10; r++) {
                board[c][r] = null;
            }
        }

        for (int i = 0; i < blkPieces.size(); i++) {
            pc = (piece) blkPieces.elementAt(i);
            board[pc.col][pc.row] = pc;
        }
        for (int i = 0; i < redPieces.size(); i++) {
            pc = (piece) redPieces.elementAt(i);
            board[pc.col][pc.row] = pc;
        }
    }

    public boolean isValidMove(piece movingFrom, int toCol, int toRow) {
        if (toCol > 8 || toCol < 0 || toRow > 9 || toRow < 0) {
            return false;
        }
        piece tmp = board[toCol][toRow];
        if (toCol == movingFrom.col && toRow == movingFrom.row) {
            return false;   // the piece haven't moved at all
        }
        if (tmp != null && tmp.isBlk == movingFrom.isBlk) {
            return false;   // you can't catch yourself
        }
        switch (movingFrom.name) {
            case CARRIAGE:
                if (toCol == movingFrom.col) {
                    for (int row = Math.min(toRow, movingFrom.row) + 1;
                            row < Math.max(toRow, movingFrom.row); row++) {
                        if (board[toCol][row] != null) // clear?
                        {
                            return false;
                        }
                    }
                    return true;
                } else if (toRow == movingFrom.row) {
                    for (int col = Math.min(toCol, movingFrom.col) + 1;
                            col < Math.max(toCol, movingFrom.col); col++) {
                        if (board[col][toRow] != null) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }

            case HORSE: {
                int cols = toCol - movingFrom.col;
                int rows = toRow - movingFrom.row;
                return ((Math.abs(cols) == 2 && Math.abs(rows) == 1
                        && board[movingFrom.col + cols / 2][movingFrom.row] == null)
                        || (Math.abs(rows) == 2 && Math.abs(cols) == 1
                        && board[movingFrom.col][movingFrom.row + rows / 2] == null));
            }

            case PAO: {
                int cnt = 0;
                if (toCol != movingFrom.col && toRow != movingFrom.row) {
                    return false;
                }
                if (toCol == movingFrom.col) {
                    for (int row = Math.min(toRow, movingFrom.row) + 1;
                            row < Math.max(toRow, movingFrom.row); row++) {
                        if (board[toCol][row] != null) {
                            cnt++;
                        }
                    }
                } else {
                    for (int col = Math.min(toCol, movingFrom.col) + 1;
                            col < Math.max(toCol, movingFrom.col); col++) {
                        if (board[col][toRow] != null) {
                            cnt++;
                        }
                    }
                }
                return ((cnt == 0 && tmp == null)
                        || (cnt == 1 && tmp != null && tmp.isBlk != movingFrom.isBlk));
            }

            case ELEPHANT: {
                int cols = toCol - movingFrom.col;
                int rows = toRow - movingFrom.row;
                return (Math.abs(cols) == 2
                        && Math.abs(rows) == 2
                        && board[movingFrom.col + cols / 2][movingFrom.row + rows / 2] == null
                        && ((movingFrom.isBlk && toRow >= 5)
                        || (!movingFrom.isBlk && toRow <= 4)));
            }

            case GUARD:
                return (Math.abs(toCol - movingFrom.col) == 1
                        && Math.abs(toRow - movingFrom.row) == 1
                        && toCol >= 3
                        && toCol <= 5
                        && ((movingFrom.isBlk && toRow >= 7)
                        || (!movingFrom.isBlk && toRow <= 2)));

            case SOLDIER:
                if (movingFrom.isBlk) {
                    if (toCol == movingFrom.col && toRow == movingFrom.row - 1) {
                        return true;
                    }
                    if (toRow <= 4) {
                        return (toRow == movingFrom.row
                                && Math.abs(toCol - movingFrom.col) == 1);
                    }
                    return false;
                } else {
                    if (toCol == movingFrom.col && toRow == movingFrom.row + 1) {
                        return true;
                    }
                    if (toRow >= 5) {
                        return (toRow == movingFrom.row
                                && Math.abs(toCol - movingFrom.col) == 1);
                    }
                    return false;
                }

            default: //KING
                return (Math.abs(toCol - movingFrom.col)
                        + Math.abs(toRow - movingFrom.row) == 1
                        && toCol >= 3
                        && toCol <= 5
                        && ((movingFrom.isBlk && toRow >= 7)
                        || (!movingFrom.isBlk && toRow <= 2)));
        }
    }

    public Vector findPossibleMoves(piece pc) {
        Vector validMoves = new Vector();
        piece tmp = null;
        switch (pc.name) {
            case CARRIAGE:
                for (int col = pc.col + 1; col <= 8; col++) {
                    if ((tmp = board[col][pc.row]) != null) {
                        if (tmp.isBlk != pc.isBlk) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row, col, pc.row));
                        }
                        break;
                    }
                    validMoves.addElement(new moveFromTo(pc.col, pc.row, col, pc.row));
                }
                for (int col = pc.col - 1; col >= 0; col--) {
                    if ((tmp = board[col][pc.row]) != null) {
                        if (tmp.isBlk != pc.isBlk) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row, col, pc.row));
                        }
                        break;
                    }
                    validMoves.addElement(new moveFromTo(pc.col, pc.row, col, pc.row));
                }
                for (int row = pc.row + 1; row <= 9; row++) {
                    if ((tmp = board[pc.col][row]) != null) {
                        if (tmp.isBlk != pc.isBlk) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row, pc.col, row));
                        }
                        break;
                    }
                    validMoves.addElement(new moveFromTo(pc.col, pc.row, pc.col, row));
                }
                for (int row = pc.row - 1; row >= 0; row--) {
                    if ((tmp = board[pc.col][row]) != null) {
                        if (tmp.isBlk != pc.isBlk) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row, pc.col, row));
                        }
                        break;
                    }
                    validMoves.addElement(new moveFromTo(pc.col, pc.row, pc.col, row));
                }
                break;

            case PAO:
                for (int col = 0; col <= 8; col++) {
                    if (col == pc.col) {
                        continue;
                    }
                    if (isValidMove(pc, col, pc.row)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row, col, pc.row));
                    }
                }
                for (int row = 0; row <= 9; row++) {
                    if (row == pc.row) {
                        continue;
                    }
                    if (isValidMove(pc, pc.col, row)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row, pc.col, row));
                    }
                }
                break;

            case HORSE:
                for (int col = -2; col <= 2; col += 4) {
                    for (int row = -1; row <= 1; row += 2) {
                        if (isValidMove(pc, pc.col + col, pc.row + row)) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                    pc.col + col, pc.row + row));
                        }
                    }
                }
                for (int col = -1; col <= 1; col += 2) {
                    for (int row = -2; row <= 2; row += 4) {
                        if (isValidMove(pc, pc.col + col, pc.row + row)) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                    pc.col + col, pc.row + row));
                        }
                    }
                }
                break;

            case ELEPHANT:
                for (int col = -2; col <= 2; col += 4) {
                    for (int row = -2; row <= 2; row += 4) {
                        if (isValidMove(pc, pc.col + col, pc.row + row)) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                    pc.col + col, pc.row + row));
                        }
                    }
                }
                break;

            case GUARD:
                for (int col = -1; col <= 1; col += 2) {
                    for (int row = -1; row <= 1; row += 2) {
                        if (isValidMove(pc, pc.col + col, pc.row + row)) {
                            validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                    pc.col + col, pc.row + row));
                        }
                    }
                }
                break;

            case SOLDIER:
                if (pc.isBlk) {
                    if (isValidMove(pc, pc.col, pc.row - 1)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                pc.col, pc.row - 1));
                    } else if (isValidMove(pc, pc.col, pc.row + 1)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                pc.col, pc.row + 1));
                    }
                }
                for (int col = -1; col <= 1; col += 2) {
                    if (isValidMove(pc, pc.col + col, pc.row)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                pc.col + col, pc.row));
                    }
                }
                break;

            default: // KING
                for (int col = -1; col <= 1; col += 2) {
                    if (isValidMove(pc, pc.col + col, pc.row)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                pc.col + col, pc.row));
                    }
                }
                for (int row = -1; row <= 1; row += 2) {
                    if (isValidMove(pc, pc.col, pc.row + row)) {
                        validMoves.addElement(new moveFromTo(pc.col, pc.row,
                                pc.col, pc.row + row));
                    }
                }
        }
        return validMoves;
    }

    public boolean isKingFacingKing(Vector blkPcs, Vector redPcs) {
        piece blkKing = null, redKing = null;

//        for(myListEnumerator i=new myListEnumerator(blkPcs); i.hasMoreElements(); ) {
        for (int i = 0; i < blkPcs.size(); i++) {
            piece pc = (piece) blkPcs.elementAt(i);
            if (pc.name == KING) {
                blkKing = pc;
                break;
            }
        }

//        for(myListEnumerator i=new myListEnumerator(redPcs); i.hasMoreElements(); ) {
        System.out.println("redPcs" + redPcs.size());
        for (int i = 0; i < redPcs.size(); i++) {
            piece pc = (piece) redPcs.elementAt(i);
            if (pc.name == KING) {
                redKing = pc;
                break;
            }
        }

        if (blkKing.col == redKing.col) {
            for (int i = redKing.row + 1; i < blkKing.row; i++) {
                if (board[blkKing.col][i] != null) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean isTakingBlkKing(moveFromTo aMove) {
        piece pc = board[aMove.toCol][aMove.toRow];

        return (pc != null && pc.isBlk && pc.name == KING);
    }

    public boolean isTakingRedKing(moveFromTo aMove) {
        piece pc = board[aMove.toCol][aMove.toRow];

        return (pc != null && !pc.isBlk && pc.name == KING);
    }

    public boolean isCheckingRed() {
        for (int i = 0; i < blkPieces.size(); i++) {
            piece pc = (piece) blkPieces.elementAt(i);

            Vector possibleMove = findPossibleMoves(pc);

            for (int j = 0; j < possibleMove.size(); j++) {
                if (isTakingRedKing((moveFromTo) possibleMove.elementAt(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCheckingBlk() {
        for (int i = 0; i < redPieces.size(); i++) {
            piece pc = (piece) redPieces.elementAt(i);

            Vector possibleMove = findPossibleMoves(pc);

            for (int j = 0; j < possibleMove.size(); j++) {
                if (isTakingBlkKing((moveFromTo) possibleMove.elementAt(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void move(moveFromTo aMove) {
//        System.out.println("toCol:" + aMove.toCol + " fromCol " + aMove.toRow + " fromCol " + aMove.fromCol + "  " + aMove.fromRow);
        piece pc = board[aMove.toCol][aMove.toRow];
        piece pcO = board[aMove.fromCol][aMove.fromRow];

        if (pc != null) // remove an opposite piece and earn some mark
        {
            if (pc.isBlk) {
                blkPieces.removeElement(pc);
            } else {
                redPieces.removeElement(pc);
            }
        }

        board[aMove.fromCol][aMove.fromRow] = null;
        board[aMove.toCol][aMove.toRow] = pcO;
        pcO.col = aMove.toCol;
        pcO.row = aMove.toRow;
    }

    public piece getPieceRef(int col, int row) {
        return board[col][row];
    }

    public void incMovingCnt() {
        movingCnt++;
    }

    // Losing means no valid move
    public boolean isBlkLosing() {
        Vector possibleMoves;
        boolean isLost = true;
        for (int i = 0; i < blkPieces.size(); i++) {
            piece pc = (piece) blkPieces.elementAt(i);
            if (pc.name == KING) {
                isLost = false;
            }
        }
        if (isLost) {
            return isLost;
        }
        for (int i = 0; i < blkPieces.size(); i++) {
            piece pc = (piece) blkPieces.elementAt(i);
            possibleMoves = findPossibleMoves(pc);
            System.out.println("before removeInvalideBlkMove");
            removeInvalidBlkMove(possibleMoves);
            System.out.println("Blk: possibleMoves.size()=" + possibleMoves.size());
            if (possibleMoves.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isRedLosing() {
        Vector possibleMoves;
        boolean isLost = true;
        for (int i = 0; i < redPieces.size(); i++) {
            piece pc = (piece) redPieces.elementAt(i);
            if (pc.name == KING) {
                isLost = false;
            }
        }
        if (isLost) {
            return isLost;
        }
        for (int i = 0; i < redPieces.size(); i++) {
            piece pc = (piece) redPieces.elementAt(i);
            possibleMoves = findPossibleMoves(pc);
            System.out.println("before removeInvalideRedMove");
            removeInvalidRedMove(possibleMoves);
            System.out.println("Red: possibleMoves.size()=" + possibleMoves.size());
            if (possibleMoves.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isPeace() {
        for (int i = 0; i < redPieces.size(); i++) {
            piece pc = (piece) redPieces.elementAt(i);
            if (pc.name == CARRIAGE || pc.name == PAO || pc.name == HORSE || pc.name == SOLDIER) {
                return false;
            }
        }
        for (int i = 0; i < blkPieces.size(); i++) {
            piece pc = (piece) blkPieces.elementAt(i);
            if (pc.name == CARRIAGE || pc.name == PAO || pc.name == HORSE || pc.name == SOLDIER) {
                return false;
            }
        }
        return true;
    }

    public void removeInvalidRedMove(Vector redMoves) {
        // Invalid moves which is to be removed from redMoves
        Vector toBeRemoved = new Vector();

        for (int i = 0; i < redMoves.size(); i++) {
            moveFromTo mv = (moveFromTo) redMoves.elementAt(i);
            if (!isValidMove2(board[mv.fromCol][mv.fromRow], mv.toCol, mv.toRow)) {
                toBeRemoved.addElement(mv);
            }
        }

        // remove all invalid moves from redMoves
        for (int i = 0; i < toBeRemoved.size(); i++) {
            redMoves.removeElement((moveFromTo) toBeRemoved.elementAt(i));
        }
    }

    public void removeInvalidBlkMove(Vector blkMoves) {
        // Invalid moves which is to be removed from redMoves
        Vector toBeRemoved = new Vector();

        for (int i = 0; i < blkMoves.size(); i++) {
            moveFromTo mv = (moveFromTo) blkMoves.elementAt(i);
            if (!isValidMove2(board[mv.fromCol][mv.fromRow], mv.toCol, mv.toRow)) {
                toBeRemoved.addElement(mv);
            }
        }

        // remove all invalid moves from redMoves
        for (int i = 0; i < toBeRemoved.size(); i++) {
            blkMoves.removeElement((moveFromTo) toBeRemoved.elementAt(i));
        }
    }

    // is valid move based on one further move
    // it's regarding the cases: kings face each other and
    // the king is under checking
    public boolean isValidMove2(piece movingFrom, int toCol, int toRow) {
        Vector redPcs = new Vector();
        Vector blkPcs = new Vector();

        // make copies of pieces and chess board. All moves are done on
        // a virtural chess board
        for (int i = 0; i < blkPieces.size(); i++) {
            piece pc = new piece();
            pc.copyfrom((piece) blkPieces.elementAt(i));
            blkPcs.addElement(pc);
        }

        for (int i = 0; i < redPieces.size(); i++) {
            piece pc = new piece();
            pc.copyfrom((piece) redPieces.elementAt(i));
            redPcs.addElement(pc);
        }

        chessBoard bd = new chessBoard(blkPcs, redPcs);

        // make a virtural move
        moveFromTo aMove = new moveFromTo();
        aMove.fromCol = movingFrom.col;
        aMove.fromRow = movingFrom.row;
        aMove.toCol = toCol;
        aMove.toRow = toRow;
        bd.move(aMove);

        // if two kings face each other without any obstacle pieces
        //Thomc : đã chặn ở client (sinh lỗi khi 1 thằng tướng bị ăn)
        try {
            if (bd.isKingFacingKing(blkPcs, redPcs)) {
                return false;
            }
        } catch (Exception e) {
        }

        // if the move causes of a checking to itself?
        if (movingFrom.isBlk && bd.isCheckingBlk()) {
            return false;
        }

        if (!movingFrom.isBlk && bd.isCheckingRed()) {
            return false;
        }

        redPcs = null;
        blkPcs = null;
        return true;
    }
}
