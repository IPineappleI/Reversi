/**
 * Прошу прощения за ужасный код (особенно за {@link model.MainMenuException} XD).
 * <p>Я искренне пытался в ООП, но в этой задачке странновато получилось. По крайней мере я смог полностью разделить
 * игровую логику и ввод-вывод.
 * <p>Надеюсь, что вы хотя бы посмеётесь :)
 */
public class Main {
    public static void main(String[] args) {
        ReversiConsole reversiConsole = new ReversiConsole();
        reversiConsole.play();
    }
}
