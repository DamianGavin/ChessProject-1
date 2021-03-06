package ie.gmit.sw.chess.board;

import ie.gmit.sw.chess.board.pieces.Colour;
import ie.gmit.sw.chess.board.pieces.King;
import ie.gmit.sw.chess.board.pieces.Piece;
import ie.gmit.sw.utilities.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class ChessBoard {

    public final static int BOARD_SIZE = 8;
    private final Piece[][] board;
    private final Stack<Move> moveHistory;

    private int turnNo;

    public ChessBoard() {
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];
        moveHistory = new Stack<>();
        this.turnNo = 1;
    }

    /**
     * Returns the Piece at a given location, null if empty.
     * It's best to check whether or not the position is occupied
     * or not via "posIsEmpty" to avoid NullPointerExceptions.
     *
     * @param pos the position in question
     * @return the Piece that occupies the position or null if empty.
     */
    public Piece getAt(Position pos) {
        // TODO handle index out of bounds errors here. IndexOutOfBoundsException possible
        return board[pos.x()][pos.y()];
    }

    public Piece getAt(String chessNotation) {
        return getAt(Util.stringToPosition(chessNotation));
    }

    /**
     * @param pos   the position the new piece will be inserted into
     * @param piece the piece that is to be inserted.
     */
    public void setAt(Position pos, Piece piece) {
        board[pos.x()][pos.y()] = piece;
        if (piece != null) {
            piece.setPosition(pos);
        }
    }

    public void setAt(String chessNotation, Piece piece) {
        setAt(Util.stringToPosition(chessNotation), piece);
    }

    /**
     * @param pos the position in question.
     * @return true or false for if the position is unoccupied.
     */
    public boolean posIsEmpty(Position pos) {
        return getAt(pos) == null;
    }

    public boolean posIsEmpty(String chessNotation) {
        return posIsEmpty(Util.stringToPosition(chessNotation));
    }

    /**
     * @return the current turn number.
     */
    public int getTurnNo() {
        return turnNo;
    }


    /**
     * @param move
     * @return true or false for if the provided move is valid with the current board state.
     */
    public boolean moveIsValid(Move move) {

        /*
        The move object represents 2 positions on the board. The piece at move.from()
        should be repositioned to move.to(), we need to confirm the move is valid before
        we mess with the board.
         */
        // invalid because the user tried to move from an empty piece to another piece. (not a legal move)
        if (posIsEmpty(move.from())) {
            return false; // it's not a valid move because it's not actually moving a piece.
        }
        // get the piece that we are going to move.
        Piece targetPiece = getAt(move.from());
        // it is a piece we want to move.
        Collection<Position> possibleMoves = targetPiece.getPossiblePositions(); // depends on the piece type what these are.
        // the move is valid if and only if the piece can actually move there. Otherwise it's invalid.
        boolean inPossibleMoves = possibleMoves.contains(move.to()); // true if the destination is in the piece's list of valid positions.


        if (!inPossibleMoves) {
            return false;
        }

        // then look for Check
        Colour allyTeam = targetPiece.getColour();

        // actually make the move.
        move.setFromPiece(targetPiece);
        move.setToPiece(getAt(move.to()));

        // 4. reposition it and the new position
        setAt(move.to(), targetPiece);

        // 5, ned to empty the original position so it's now free for other pieces.
        setAt(move.from(), null); // null means an empty spot
        moveHistory.push(move);

        if (isCheck(allyTeam)) {
            undoLastMove();
            if (isCheckMate(allyTeam)) {
                return false;
            }
            return false;
        }

        undoLastMove();
        return true;
    }



    /**
     * Making a move will update the piece at the from position and move it to the
     * to position. You should check if the move is valid before calling this method.
     * use "moveIsValid"
     *
     * @param move the move that will update the board state.
     */
    public void makeMove(Move move) {
        // 1. check if the move is valid
        if (!moveIsValid(move)) {
            // 2. if not, throw exception, otherwise, perform movement.
            throw new IllegalArgumentException("Provided an invalid move.");
        }

        applyMove(move);
    }

    public void undoLastMove() {
        Move lastMove = moveHistory.pop();
        Move undoMove = lastMove.reverse();
        setAt(undoMove.to(), undoMove.getToPiece());
        setAt(undoMove.from(), undoMove.getFromPiece());
    }


    /**
     * @return a list of all the non-null pieces in the game. Includes black and white pieces.
     */
    public List<Piece> getPieces() {
        List<Piece> allPieces = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = board[i][j];
                if (piece != null) {
                    allPieces.add(piece);
                }
            }
        }
        return allPieces;
    }


    /**
     * @param colour either BLACK or WHITE
     * @return all the non-null pieces of that colour.
     */
    public List<Piece> getPieces(Colour colour) {
        List<Piece> allPieces = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColour() == colour) {
                    allPieces.add(piece);
                }
            }
        }
        return allPieces;
    }

    public boolean isOnBoard(Position pos) {
        return !(pos.x() < 0
                || pos.y() < 0
                || pos.y() >= size()
                || pos.x() >= size());

    }

    public int size() {
        return BOARD_SIZE;
    }


    /**
     * @param colour
     * @return the King of the given colour. Assumes only one king of each colour on the board.
     */
    public King getKing(Colour colour) {
        for (Piece piece : getPieces(colour)) {
            if (piece instanceof King) {
                return (King) piece;
            }
        }
        return null;
    }

    public void emptyPosition(String chessNotion) {
        setAt(chessNotion, null);
    }

    public void emptyPosition(Position position) {
        setAt(position, null);
    }

    /**
     * @param colour
     * @return true/false for if the board is in check for a given colour.
     */
    public boolean isCheck(Colour colour) {

        King king = getKing(colour);
        // can't be in check if there's no king.
        if (king == null) {
            return false;
        }

        Colour otherColour;
        if (colour == Colour.WHITE) {
            otherColour = Colour.BLACK;
        } else {
            otherColour = Colour.WHITE;
        }

        List<Piece> opponentPieces = getPieces(otherColour);

        // get all possible other team moves
        Set<Position> positionsOtherColourCanMoveTo = new HashSet<>();
        for (Piece piece : opponentPieces) {
            positionsOtherColourCanMoveTo.addAll(piece.getPossiblePositions());
        }
        // if the king's position is in any of them he's in check
        return positionsOtherColourCanMoveTo.contains(king.getPosition());
    }

    private Collection<Move> getAllPossibleMoves(Colour colour) {
        Collection<Piece> allAllyPieces = getPieces(colour);
        Collection<Move> allPossibleMoves = new ArrayList<>();
        for (Piece piece : allAllyPieces) {
            allPossibleMoves.addAll(piece.getPossibleMoves());
        }
        return allPossibleMoves;
    }


    public boolean isCheckMate(Colour colour) {
        Collection<Move> allAllyMoves = getAllPossibleMoves(colour);

        for (Move move : allAllyMoves) {

            applyMove(move);

            boolean isCheck = isCheck(colour);
            undoLastMove();
            if (!isCheck) {
                return false;
            }
        }
        return true;
    }

    private void applyMove(Move move) {
        Piece piece = getAt(move.from());
        move.setFromPiece(piece);
        move.setToPiece(getAt(move.to()));

        // 4. reposition it and the new position
        setAt(move.to(), piece);

        // 5, ned to empty the original position so it's now free for other pieces.
        setAt(move.from(), null); // null means an empty spot
        moveHistory.push(move);
    }

}
