import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class UltimateBrickBreaker extends JPanel implements KeyListener, ActionListener {
    private boolean play = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean gameWon = false;

    private int score = 0;
    private int totalBricks = 28;

    private Timer timer;
    private int delay = 6;

    private int playerX = 310;
    private int ballposX = 350;
    private int ballposY = 530;
    private int ballXdir = -2;
    private int ballYdir = -3;

    private UltimateMapGenerator map;

    public UltimateBrickBreaker() {
        map = new UltimateMapGenerator(4, 7);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing for smooth visuals
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient background
        GradientPaint bg = new GradientPaint(0, 0, Color.decode("#141E30"), 700, 600, Color.decode("#243B55"));
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, 692, 592);

        if (!gameStarted) {
            drawStartScreen(g2d);
            return;
        }

        // Draw bricks
        map.draw(g2d);

        // Borders
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(0, 0, 692, 592);

        // Score
        g2d.setFont(new Font("Orbitron", Font.BOLD, 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString("SCORE: " + score, 560, 25);

        // Paddle
        GradientPaint paddlePaint = new GradientPaint(playerX, 550, Color.CYAN, playerX + 100, 550, Color.MAGENTA);
        g2d.setPaint(paddlePaint);
        g2d.fillRoundRect(playerX, 550, 120, 12, 20, 20);

        // Ball
        RadialGradientPaint ballGlow = new RadialGradientPaint(
                new Point(ballposX + 10, ballposY + 10),
                25,
                new float[]{0f, 1f},
                new Color[]{Color.YELLOW, new Color(255, 255, 0, 0)}
        );
        g2d.setPaint(ballGlow);
        g2d.fill(new Ellipse2D.Double(ballposX - 10, ballposY - 10, 40, 40));

        g2d.setColor(Color.YELLOW);
        g2d.fillOval(ballposX, ballposY, 20, 20);

        if (gameWon) {
            drawEndScreen(g2d, "🏆 YOU WON! 🏆", Color.GREEN);
        } else if (gameOver) {
            drawEndScreen(g2d, "💀 GAME OVER 💀", Color.RED);
        }

        g2d.dispose();
    }

    private void drawStartScreen(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Orbitron", Font.BOLD, 38));
        drawCenteredText(g2d, "⚡ Ultimate Brick Breaker ⚡", 200);

        g2d.setFont(new Font("Serif", Font.BOLD, 22));
        drawCenteredText(g2d, "Press ENTER to Start", 300);
        g2d.setFont(new Font("Serif", Font.PLAIN, 18));
        drawCenteredText(g2d, "Use ← and → keys to move the paddle", 350);
    }

    private void drawEndScreen(Graphics2D g2d, String message, Color color) {
        g2d.setColor(color);
        g2d.setFont(new Font("Orbitron", Font.BOLD, 36));
        drawCenteredText(g2d, message, 250);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Serif", Font.BOLD, 20));
        drawCenteredText(g2d, "Final Score: " + score, 310);
        drawCenteredText(g2d, "Press ENTER to Restart", 360);
    }

    private void drawCenteredText(Graphics2D g2d, String text, int y) {
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (700 - textWidth) / 2, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();

        if (play) {
            if (new Rectangle(ballposX, ballposY, 20, 20)
                    .intersects(new Rectangle(playerX, 550, 120, 10))) {
                ballYdir = -ballYdir;
            }

            // Brick collisions
            A:
            for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle rect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);

                        if (ballRect.intersects(rect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += 10;

                            if (ballposX + 19 <= rect.x || ballposX + 1 >= rect.x + rect.width) {
                                ballXdir = -ballXdir;
                            } else {
                                ballYdir = -ballYdir;
                            }

                            break A;
                        }
                    }
                }
            }

            ballposX += ballXdir;
            ballposY += ballYdir;

            if (ballposX < 0) ballXdir = -ballXdir;
            if (ballposY < 0) ballYdir = -ballYdir;
            if (ballposX > 670) ballXdir = -ballXdir;

            if (ballposY > 570) {
                play = false;
                gameOver = true;
            }

            if (totalBricks <= 0) {
                play = false;
                gameWon = true;
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_RIGHT) {
            if (playerX >= 580) playerX = 580;
            else moveRight();
        }

        if (code == KeyEvent.VK_LEFT) {
            if (playerX <= 10) playerX = 10;
            else moveLeft();
        }

        if (code == KeyEvent.VK_ENTER) {
            if (!gameStarted) {
                gameStarted = true;
                repaint();
                return;
            }

            if (gameOver || gameWon) {
                resetGame();
                repaint();
            }
        }
    }

    private void resetGame() {
        play = true;
        gameOver = false;
        gameWon = false;
        score = 0;
        totalBricks = 28;
        playerX = 310;
        ballposX = 350;
        ballposY = 530;
        ballXdir = -2;
        ballYdir = -3;
        map = new UltimateMapGenerator(4, 7);
    }

    public void moveRight() {
        play = true;
        playerX += 30;
    }

    public void moveLeft() {
        play = true;
        playerX -= 30;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame obj = new JFrame();
        UltimateBrickBreaker gamePlay = new UltimateBrickBreaker();
        obj.setBounds(10, 10, 700, 600);
        obj.setTitle("⚡ Ultimate Neon Brick Breaker ⚡");
        obj.setResizable(false);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(gamePlay);
        obj.setVisible(true);
    }
}

class UltimateMapGenerator {
    public int map[][];
    public int brickWidth;
    public int brickHeight;
    private final Color[] neonColors = {
        Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GREEN, Color.PINK
    };

    public UltimateMapGenerator(int row, int col) {
        map = new int[row][col];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = 1;
            }
        }
        brickWidth = 540 / col;
        brickHeight = 150 / row;
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    Color color = neonColors[(i + j) % neonColors.length];
                    g.setColor(color);
                    g.fillRoundRect(j * brickWidth + 80, i * brickHeight + 50,
                            brickWidth - 5, brickHeight - 5, 15, 15);

                    g.setStroke(new BasicStroke(2));
                    g.setColor(new Color(255, 255, 255, 180));
                    g.drawRoundRect(j * brickWidth + 80, i * brickHeight + 50,
                            brickWidth - 5, brickHeight - 5, 15, 15);
                }
            }
        }
    }

    public void setBrickValue(int value, int row, int col) {
        map[row][col] = value;
    }
}
