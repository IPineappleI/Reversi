package model;

import java.util.*;

/**
 * This class has all the logic needed to play Reversi. All classes that extend this class only need to provide the
 * user interface necessary to interact with the player.
 */
public abstract class Reversi {
    protected final char[][] board = new char[8][8];
    protected int scoreBlack, scoreWhite, highScoreEasy = 0, highScoreHard = 0, highScorePVP = 0;
    protected static final char BLACK = '◯', WHITE = '●', EMPTY = '-', POSSIBLE_TURN = '*';
    protected final Stack<Map.Entry<Point, ArrayList<Point>>> turnHistory = new Stack<>();

    /**
     * Prepares the {@link Reversi#board} for a new game, resets the scores and the {@link Reversi#turnHistory}.
     */
    protected void clearBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = EMPTY;
            }
        }
        board[3][3] = WHITE;
        board[3][4] = BLACK;
        board[4][3] = BLACK;
        board[4][4] = WHITE;
        scoreBlack = 2;
        scoreWhite = 2;
        turnHistory.clear();
    }

    /**
     * Removes {@link Reversi#POSSIBLE_TURN} marks from the {@link Reversi#board}.
     */
    protected void removePossibleTurns() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == POSSIBLE_TURN) {
                    board[i][j] = EMPTY;
                }
            }
        }
    }

    /**
     * Finds all the turns available to the player.
     *
     * @param color the player's color
     * @return a hash map of all possible turns
     */
    protected Map<Point, ArrayList<Point>> findPossibleTurns(char color) {
        removePossibleTurns();
        final char opponentColor = color == BLACK ? WHITE : BLACK;
        Map<Point, ArrayList<Point>> possibleTurns = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == color) {
                    for (int k = i - 1; k <= i + 1; k++) {
                        for (int l = j - 1; l <= j + 1; l++) {
                            if (k == i && l == j || k < 0 || l < 0 || k > 7 || l > 7) {
                                continue;
                            }
                            if (board[k][l] == opponentColor) {
                                for (int m = k - (i - k), n = l - (j - l);
                                     m >= 0 && m <= 7 && n >= 0 && n <= 7;
                                     m -= i - k, n -= j - l) {
                                    if (board[m][n] == EMPTY) {
                                        board[m][n] = POSSIBLE_TURN;
                                        Point to = new Point(m, n);
                                        possibleTurns.put(to, new ArrayList<>());
                                        possibleTurns.get(to).add(new Point(i, j));
                                        break;
                                    } else if (board[m][n] == POSSIBLE_TURN) {
                                        possibleTurns.get(new Point(m, n)).add(new Point(i, j));
                                        break;
                                    } else if (board[m][n] == color) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return possibleTurns;
    }

    /**
     * Swaps the color of the opponent's chips.
     *
     * @param toPoint    the {@link Point} on the {@link Reversi#board} to do the swap to
     * @param fromPoints the {@link Point}s that the swap will be done from
     * @param color      the player's color
     */
    protected void captureOpponentPoints(Point toPoint, ArrayList<Point> fromPoints, char color) {
        for (Point fromPoint : fromPoints) {
            final int directionX = Integer.compare(toPoint.x, fromPoint.x);
            final int directionY = Integer.compare(toPoint.y, fromPoint.y);
            for (int i = fromPoint.x + directionX, j = fromPoint.y + directionY;
                 i != toPoint.x || j != toPoint.y;
                 i += directionX, j += directionY) {
                board[i][j] = color;
                if (color == BLACK) {
                    scoreBlack++;
                    scoreWhite--;
                } else {
                    scoreWhite++;
                    scoreBlack--;
                }
            }
        }
    }

    /**
     * Does the specified turn of the specified color.
     *
     * @param turn  the key is the {@link Point} on the {@link Reversi#board} to do the turn to, the value is the
     *              {@link Point}s that the turn will be done from
     * @param color the player's color
     */
    protected void doTurn(Map.Entry<Point, ArrayList<Point>> turn, char color) {
        final Point toPoint = turn.getKey();
        captureOpponentPoints(toPoint, turn.getValue(), color);
        if (color == BLACK) {
            board[toPoint.x][toPoint.y] = BLACK;
            scoreBlack++;
        } else {
            board[toPoint.x][toPoint.y] = WHITE;
            scoreWhite++;
        }
        turnHistory.push(turn);
    }

    /**
     * Undoes the last turn.
     */
    protected void undoTurn() {
        Map.Entry<Point, ArrayList<Point>> turn = turnHistory.pop();
        final Point toPoint = turn.getKey();
        final char opponentColor = board[toPoint.x][toPoint.y] == BLACK ? WHITE : BLACK;
        captureOpponentPoints(toPoint, turn.getValue(), opponentColor);
        board[toPoint.x][toPoint.y] = EMPTY;
        if (opponentColor == BLACK) {
            scoreWhite--;
        } else {
            scoreBlack--;
        }
    }

    /**
     * Alerts the player that it's their turn and shows them the current state of the game.
     *
     * @param color the player's color
     */
    protected abstract void playerTurnAlert(char color);

    /**
     * Allows the player to choose one of the possible turns or to undo their last turn if possible.
     *
     * @param possibleTurns  the set of turns possible for the player to do
     * @param isUndoPossible {@code true} if the player can undo their last turn, {@code false} if there are no turns
     *                       for the player to undo
     * @return {@code -1} if the player has chosen to undo their last turn, {@code -2} if the player has chosen to
     * go back to the main menu, the position of the turn in {@code possibleTurns} (starting from zero) otherwise
     */
    protected abstract int getTurnOption(Set<Point> possibleTurns, boolean isUndoPossible);

    /**
     * Acts according to the turn option that the player has chosen.
     *
     * @param color the player's color
     * @return {@code true} if the player was able to do a turn, {@code false} otherwise
     * @throws MainMenuException thrown if the player has chosen to go back to the main menu
     */
    protected boolean playerTurn(char color) throws MainMenuException {
        Map<Point, ArrayList<Point>> possibleTurns = findPossibleTurns(color);
        if (possibleTurns.isEmpty()) {
            return false;
        }
        playerTurnAlert(color);
        int turnOption = getTurnOption(possibleTurns.keySet(), !turnHistory.isEmpty());
        if (turnOption == -2) {
            throw new MainMenuException();
        }
        if (turnOption == -1) {
            undoTurn();
            undoTurn();
            return playerTurn(color);
        }
        var it = possibleTurns.entrySet().iterator();
        for (int i = 0; i < turnOption; i++) {
            it.next();
        }
        doTurn(it.next(), color);
        return true;
    }

    /**
     * Announces the game result.
     *
     * @param color {@link Reversi#BLACK} if black won, {@link Reversi#WHITE} if white won, {@link Reversi#EMPTY}
     *              otherwise
     */
    protected abstract void announceResult(char color);

    /**
     * Shows that a new high score has been set.
     */
    protected abstract void newHighScoreAlert();

    /**
     * Starts the game in the Versus Player game mode.
     *
     * @throws MainMenuException thrown if the player has chosen to go back to the main menu
     */
    protected void playVersusPlayer() throws MainMenuException {
        clearBoard();
        int counter = 0;
        while (true) {
            if (!playerTurn(BLACK)) {
                if (++counter == 2) {
                    break;
                }
            } else {
                counter = 0;
            }
            if (!playerTurn(WHITE)) {
                if (++counter == 2) {
                    break;
                }
            } else {
                counter = 0;
            }
        }
        int scoreWinner = scoreBlack;
        if (scoreBlack > scoreWhite) {
            announceResult(BLACK);
        } else if (scoreWhite > scoreBlack) {
            announceResult(WHITE);
            scoreWinner = scoreWhite;
        } else {
            announceResult(EMPTY);
        }
        if (scoreWinner > highScorePVP) {
            newHighScoreAlert();
            highScorePVP = scoreWinner;
        }
    }

    /**
     * Assesses the specified turn's value according to the chosen difficulty.
     *
     * @param turn   the specified turn whose value needs to be assessed
     * @param isHard {@code true} if the chosen game mode is Versus Computer (Hard), {@code false} otherwise
     * @return the value of the specified turn represented by a floating-point number of double precision
     */
    protected double getTurnValue(Map.Entry<Point, ArrayList<Point>> turn, boolean isHard) {
        final Point toPoint = turn.getKey();
        double turnValue = toPoint.getValueTo();
        for (Point fromPoint : turn.getValue()) {
            final int directionX = Integer.compare(toPoint.x, fromPoint.x);
            final int directionY = Integer.compare(toPoint.y, fromPoint.y);
            for (int i = fromPoint.x + directionX, j = fromPoint.y + directionY;
                 i != toPoint.x || j != toPoint.y;
                 i += directionX, j += directionY) {
                turnValue += (new Point(i, j)).getValue();
            }
        }
        if (isHard) {
            doTurn(turn, WHITE);
            Map<Point, ArrayList<Point>> possibleOpponentTurns = findPossibleTurns(BLACK);
            undoTurn();
            double bestOpponentTurnValue = -999999999;
            for (Map.Entry<Point, ArrayList<Point>> opponentTurn : possibleOpponentTurns.entrySet()) {
                double opponentTurnValue = getTurnValue(opponentTurn, false);
                if (opponentTurnValue > bestOpponentTurnValue) {
                    bestOpponentTurnValue = opponentTurnValue;
                }
            }
            return turnValue - bestOpponentTurnValue;
        }
        return turnValue;
    }

    /**
     * Shows the turn chosen by the computer to the player.
     *
     * @param to the {@link Point} on the {@link Reversi#board} to which the turn was made
     */
    protected abstract void computerDoesTurnAlert(Point to);

    /**
     * Chooses the best turn for the computer to make according to the chosen difficulty.
     *
     * @param isHard {@code true} if the chosen game mode is Versus Computer (Hard), {@code false} otherwise
     * @return {@code true} if the computer was able to do a turn, {@code false} otherwise
     */
    protected boolean computerTurn(boolean isHard) {
        Map<Point, ArrayList<Point>> possibleTurns = findPossibleTurns(WHITE);
        if (possibleTurns.isEmpty()) {
            return false;
        }
        playerTurnAlert(WHITE);
        Map.Entry<Point, ArrayList<Point>> bestTurn = null;
        double bestTurnValue = -999999999;
        for (Map.Entry<Point, ArrayList<Point>> turn : possibleTurns.entrySet()) {
            double turnValue = getTurnValue(turn, isHard);
            if (turnValue > bestTurnValue) {
                bestTurn = turn;
                bestTurnValue = turnValue;
            }
        }
        assert bestTurn != null;
        computerDoesTurnAlert(bestTurn.getKey());
        doTurn(bestTurn, WHITE);
        return true;
    }

    /**
     * Starts the game in the Versus Computer game mode.
     *
     * @param isHard {@code true} if the chosen game mode is Versus Computer (Hard), {@code false} otherwise
     * @throws MainMenuException thrown if the player has chosen to go back to the main menu
     */
    protected void playVersusComputer(boolean isHard) throws MainMenuException {
        clearBoard();
        int counter = 0;
        while (true) {
            if (!playerTurn(BLACK)) {
                if (++counter == 2) {
                    break;
                }
            } else {
                counter = 0;
            }
            if (!computerTurn(isHard)) {
                if (++counter == 2) {
                    break;
                }
            } else {
                counter = 0;
            }
        }
        if (scoreBlack > scoreWhite) {
            announceResult(BLACK);
        } else if (scoreWhite > scoreBlack) {
            announceResult(WHITE);
        } else {
            announceResult(EMPTY);
        }
        if (isHard) {
            if (scoreBlack > highScoreHard) {
                newHighScoreAlert();
                highScoreHard = scoreBlack;
            }
        } else if (scoreBlack > highScoreEasy) {
            newHighScoreAlert();
            highScoreEasy = scoreBlack;
        }
    }

    /**
     * Starts the interaction with the player by allowing them to choose the game mode (Versus Computer (Easy),
     * Versus Computer (Hard) or Versus Player) or to quit.
     * <p>The game is started by invoking the method corresponding to the chosen game mode:
     * <p>vs Computer (Easy) - {@link Reversi#playVersusComputer}{@code (false)}
     * <p>vs Computer (Hard) - {@link Reversi#playVersusComputer}{@code (true)}
     * <p>vs Player - {@link Reversi#playVersusPlayer()}
     */
    public abstract void play();
}
