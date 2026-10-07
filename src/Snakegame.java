import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class Snakegame extends JPanel implements ActionListener, KeyListener {
    private static class Tile {
        int x, y;
        Tile(int x, int y) { this.x = x; this.y = y; }
    }

    int boardWidth, boardHeight;
    int tileSize = 25;
    int cols, rows;

    Tile snakeHead;
    ArrayList<Tile> snakeBody = new ArrayList<>();
    Tile food;
    Random random = new Random();

    Timer gameLoop;
    int velocityX = 0, velocityY = 0;
    int lastVelocityX = 0, lastVelocityY = 0;
    boolean gameover = false;
    boolean isPaused = false;
    boolean gameStarted = false;

    int score = 0;
    int highScore = 0;
    final String HIGH_SCORE_FILE = "highscore.txt";

    // Difficulty Levels: Easy, Medium, Hard
    String[] levelNames = {"Easy", "Medium", "Hard"};
    int[] levelDelays = {180,120,90}; // Delay in milliseconds (lower = faster)
    int currentLevel = 1; // Default: Medium

    public Snakegame(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        this.cols = width / tileSize;
        this.rows = height / tileSize;

        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(20, 24, 30));
        addKeyListener(this);
        setFocusable(true);

        loadHighScore();
        food = new Tile(10, 10);
        resetGame();

        gameLoop = new Timer(levelDelays[currentLevel], this);
        gameLoop.start();
    }

    // Sound generation without external files
    private void playTone(int freq, int durationMs) {
        new Thread(() -> {
            try {
                byte[] buf = new byte[durationMs * 8]; // 8000 Hz sample rate
                for (int i = 0; i < buf.length; i++) {
                    buf[i] = (byte) (Math.sin(i / (8000.0 / freq) * 2 * Math.PI) * 90);
                }
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private void playEatSound() {
        playTone(700, 70); // High pleasant beep
    }

    private void playGameOverSound() {
        playTone(220, 300); // Low game over tone
    }

    // Change difficulty level (0=Easy, 1=Medium, 2=Hard)
    public void setLevel(int level) {
        currentLevel = level % levelNames.length;
        if (gameLoop != null) {
            gameLoop.setDelay(levelDelays[currentLevel]);
        }
        repaint();
    }

    private void resetGame() {
        snakeHead = new Tile(cols / 4, rows / 2);
        snakeBody.clear();
        score = 0;
        velocityX = 0;
        velocityY = 0;
        lastVelocityX = 0;
        lastVelocityY = 0;
        gameover = false;
        isPaused = false;
        gameStarted = false;
        placeFood();
        if (gameLoop != null) {
            gameLoop.setDelay(levelDelays[currentLevel]);
            gameLoop.start();
        }
    }

    public void placeFood() {
        boolean onSnake;
        do {
            onSnake = false;
            food.x = random.nextInt(cols);
            food.y = 1 + random.nextInt(rows - 1); // Keep top row free for header info

            if (snakeHead != null && food.x == snakeHead.x && food.y == snakeHead.y) {
                onSnake = true;
                continue;
            }
            for (Tile part : snakeBody) {
                if (food.x == part.x && food.y == part.y) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);
    }

    public void move() {
        if (!gameStarted || isPaused || gameover) return;

        lastVelocityX = velocityX;
        lastVelocityY = velocityY;

        // Eat food check
        if (snakeHead.x == food.x && snakeHead.y == food.y) {
            snakeBody.add(new Tile(food.x, food.y));
            score = snakeBody.size();
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
            playEatSound();
            placeFood();
        }

        // Move body segments
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile part = snakeBody.get(i);
            if (i == 0) {
                part.x = snakeHead.x;
                part.y = snakeHead.y;
            } else {
                Tile prev = snakeBody.get(i - 1);
                part.x = prev.x;
                part.y = prev.y;
            }
        }

        // Move head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // Self-collision
        for (Tile part : snakeBody) {
            if (snakeHead.x == part.x && snakeHead.y == part.y) {
                gameOver();
                return;
            }
        }

        // Wall collision
        if (snakeHead.x < 0 || snakeHead.x >= cols || snakeHead.y < 0 || snakeHead.y >= rows) {
            gameOver();
        }
    }

    private void gameOver() {
        gameover = true;
        gameLoop.stop();
        playGameOverSound();
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine().trim());
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (Exception ignored) {}
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw food (apple)
        g2.setColor(new Color(230, 60, 60));
        g2.fillOval(food.x * tileSize + 2, food.y * tileSize + 2, tileSize - 4, tileSize - 4);
        g2.setColor(new Color(40, 180, 80));
        g2.fillRect(food.x * tileSize + tileSize / 2 - 1, food.y * tileSize, 2, 4);

        // Draw snake body
        g2.setColor(new Color(46, 204, 113));
        for (Tile part : snakeBody) {
            g2.fillRoundRect(part.x * tileSize + 1, part.y * tileSize + 1, tileSize - 2, tileSize - 2, 6, 6);
        }

        // Draw snake head
        g2.setColor(new Color(39, 174, 96));
        g2.fillRoundRect(snakeHead.x * tileSize + 1, snakeHead.y * tileSize + 1, tileSize - 2, tileSize - 2, 8, 8);

        // Snake eyes
        g2.setColor(Color.WHITE);
        g2.fillOval(snakeHead.x * tileSize + 4, snakeHead.y * tileSize + 4, 5, 5);
        g2.fillOval(snakeHead.x * tileSize + tileSize - 9, snakeHead.y * tileSize + 4, 5, 5);
        g2.setColor(Color.BLACK);
        g2.fillOval(snakeHead.x * tileSize + 5, snakeHead.y * tileSize + 5, 3, 3);
        g2.fillOval(snakeHead.x * tileSize + tileSize - 8, snakeHead.y * tileSize + 5, 3, 3);

        // Top Header HUD
        g2.setColor(new Color(10, 14, 20, 220));
        g2.fillRect(0, 0, boardWidth, 30);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Score: " + score, 15, 20);

        g2.setColor(new Color(241, 196, 15));
        g2.drawString("Best: " + highScore, boardWidth / 2 - 40, 20);

        g2.setColor(new Color(52, 152, 219));
        g2.drawString("Level: " + levelNames[currentLevel] + " [L]", boardWidth - 145, 20);

        // Overlays
        if (!gameStarted && !gameover) {
            drawCenteredText(g2, "Press Arrow Keys to Start", "Change Level: Press L or 1/2/3");
        } else if (isPaused) {
            drawCenteredText(g2, "PAUSED", "Press P or Space to Resume");
        } else if (gameover) {
            drawCenteredText(g2, "GAME OVER! (Score: " + score + ")", "Press SPACE or R to Restart");
        }
    }

    private void drawCenteredText(Graphics2D g2, String title, String subtitle) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, boardWidth, boardHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (boardWidth - fm.stringWidth(title)) / 2, boardHeight / 2 - 10);

        g2.setColor(new Color(180, 200, 220));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, (boardWidth - fm.stringWidth(subtitle)) / 2, boardHeight / 2 + 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Level change shortcuts (L cycles through, 1=Easy, 2=Medium, 3=Hard)
        if (key == KeyEvent.VK_L) {
            setLevel((currentLevel + 1) % levelNames.length);
            return;
        } else if (key == KeyEvent.VK_1) {
            setLevel(0);
            return;
        } else if (key == KeyEvent.VK_2) {
            setLevel(1);
            return;
        } else if (key == KeyEvent.VK_3) {
            setLevel(2);
            return;
        }

        // Restart on Game Over
        if (gameover) {
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_R) {
                resetGame();
                repaint();
            }
            return;
        }

        // Pause toggle
        if (key == KeyEvent.VK_P || (key == KeyEvent.VK_SPACE && gameStarted)) {
            isPaused = !isPaused;
            if (isPaused) gameLoop.stop();
            else gameLoop.start();
            repaint();
            return;
        }

        if (isPaused) return;

        // Direction controls with 180-degree turn prevention
        if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && lastVelocityY != 1) {
            velocityX = 0; velocityY = -1; gameStarted = true;
        } else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && lastVelocityY != -1) {
            velocityX = 0; velocityY = 1; gameStarted = true;
        } else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && lastVelocityX != 1) {
            velocityX = -1; velocityY = 0; gameStarted = true;
        } else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && lastVelocityX != -1) {
            velocityX = 1; velocityY = 0; gameStarted = true;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}
