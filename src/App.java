import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 600;
        int boardHeight = 600;

        JFrame frame = new JFrame("Snake Game");
        Snakegame snakegame = new Snakegame(boardWidth, boardHeight);

        frame.add(snakegame);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        snakegame.requestFocusInWindow();
    }
}
