import model.MainMenuException;
import model.Point;
import model.Reversi;

import java.util.Scanner;
import java.util.Set;

/**
 * This is an extension of the {@link Reversi} abstract class with console input and output.
 */
public class ReversiConsole extends Reversi {
    private final Scanner in = new Scanner(System.in);

    /**
     * Shows the current state of the game to the player.
     *
     * @param message a {@link String} to show on top of the current state
     */
    private void printCurrentState(String message) {
        StringBuilder currentState =
                new StringBuilder(String.format("%s\nЧёрные: %d  Белые: %d", message, scoreBlack, scoreWhite));
        for (int i = 0; i < 8; i++) {
            currentState.append('\n').append(8 - i);
            for (int j = 0; j < 8; j++) {
                currentState.append(' ').append(board[i][j]);
            }
        }
        currentState.append("\n ");
        for (int i = 0; i < 8; i++) {
            currentState.append(' ').append((char) ('a' + i));
        }
        System.out.println(currentState);
    }

    @Override
    protected void playerTurnAlert(char color) {
        printCurrentState("Ход " + (color == BLACK ? "чёрных" : "белых"));
    }

    @Override
    protected int getTurnOption(Set<Point> possibleTurns, boolean isUndoPossible) {
        int turnOption, minimum = isUndoPossible ? 0 : 1;
        while (true) {
            System.out.println("Введите номер желаемого хода:");
            turnOption = 1;
            for (Point to : possibleTurns) {
                System.out.println(turnOption++ + ". " + to);
            }
            if (isUndoPossible) {
                System.out.println("0. Отменить предыдущий ход");
            }
            System.out.println("-1. Вернуться в главное меню");
            turnOption = in.nextInt();
            if (turnOption == -1 || turnOption >= minimum && turnOption <= possibleTurns.size()) {
                break;
            }
            System.out.println("Ошибка! Такой вариант отсутствует");
        }
        return turnOption - 1;
    }

    @Override
    protected void announceResult(char color) {
        if (color == BLACK) {
            printCurrentState("Победа чёрных!");
        } else if (color == WHITE) {
            printCurrentState("Победа белых!");
        } else {
            printCurrentState("Ничья!");
        }
    }

    @Override
    protected void newHighScoreAlert() {
        System.out.println("Новый рекорд!");
    }

    @Override
    protected void computerDoesTurnAlert(Point to) {
        System.out.println("Компьютер делает ход " + to);
    }

    @Override
    public void play() {
        while (true) {
            System.out.printf("""
                    Введите номер желаемого режима игры:
                    1. Против компьютера (лёгкий)		Рекорд: %d
                    2. Против компьютера (продвинутый)	Рекорд: %d
                    3. Игрок против игрока				Рекорд: %d
                    0. Выйти%n""", highScoreEasy, highScoreHard, highScorePVP);
            int gameModeOption = in.nextInt();
            try {
                if (gameModeOption == 1) {
                    playVersusComputer(false);
                } else if (gameModeOption == 2) {
                    playVersusComputer(true);
                } else if (gameModeOption == 3) {
                    playVersusPlayer();
                } else if (gameModeOption == 0) {
                    return;
                } else {
                    System.out.println("Ошибка! Такой вариант отсутствует");
                }
            } catch (MainMenuException ignored) {
            }
        }
    }
}
