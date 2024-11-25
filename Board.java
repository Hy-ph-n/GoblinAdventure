import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Board extends JPanel implements ActionListener, KeyListener {

    // controls the delay between each tick in ms
    private final int DELAY = 25;
    // controls the size of the board
    public static final int TILE_SIZE = 60;
    public static final int ROWS = 15;
    public static final int COLUMNS = 20;
    // controls how many damage tiles appear on the board
    public static final int NUM_BOMBS = 5;
    // controls how many walls appear on the board
    public static final int NUM_WALLS = 10;
    // controls how many coins appear on the board
    public static final int NUM_COINS = 5;
    // suppress serialization warning
    private static final long serialVersionUID = 490905409104883233L;
    
    // keep a reference to the timer object that triggers actionPerformed() in
    // case we need access to it in another method
    private final Timer timer;
    // objects that appear on the game board
    private final Player player;
    private ArrayList<Wall> walls;
    private ArrayList<Bomb> bombs;
    private ArrayList<Coin> coins;

    // Two boolean variables that determine win screen text
    private boolean win = false;
    private boolean lose = false;

    public Board() {
        // set the game board size
        setPreferredSize(new Dimension(TILE_SIZE * COLUMNS, TILE_SIZE * ROWS));
        // set the game board background color
        setBackground(new Color(232, 232, 232));

        // initialize the game state
        walls = createWalls();
        bombs = createBombs();
        player = new Player(walls, bombs);
        coins = populateCoins();

        // this timer will call the actionPerformed() method every DELAY ms
        timer = new Timer(DELAY, this);
        timer.start();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(newCoin, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // the game stops everything when the score hits 3000 or the player dies three times
        if (Player.score > 2999) {
            timer.stop();
            win = true;
            endScreen();
        } else if (Player.lives == 0) {
            timer.stop();
            lose = true;
            endScreen();
        } else {
            // this method is called by the timer every DELAY ms.
            // use this space to update the state of your game or animation
            // before the graphics are redrawn.

            // removes coins and moves the player if they encounter a bomb tile
            // or prevents the player from disappearing off the board/into a wall
            player.tick(walls, bombs);

            // give the player points for collecting coins
            collectCoins();

            // calling repaint() will trigger paintComponent() to run again,
            // which will refresh/redraw the graphics.
            repaint();
        }
    }

    Runnable newCoin = () -> {
        Random rand = new Random();
        int coinX = rand.nextInt(COLUMNS);
        int coinY = rand.nextInt(ROWS);
        coins.add(new Coin(coinX, coinY));
    };

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // when calling g.drawImage() we can use "this" for the ImageObserver 
        // because Component implements the ImageObserver interface, and JPanel 
        // extends from Component. So "this" Board instance, as a Component, can 
        // react to imageUpdate() events triggered by g.drawImage()

        // draw our graphics.
        drawBackground(g);
        if (!win && !lose) {
            drawScore(g);
            for (Coin coin : coins) {
                coin.draw(g, this);
            }
            player.draw(g, this);

            // draws walls
            for (Wall wall : walls) {
                wall.draw(g, this);
            }

            // draws bombs
            for (Bomb bomb : bombs) {
                bomb.draw(g, this);
            }
        } else {
            drawEndText(g);
        }

        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // this is not used but must be defined as part of the KeyListener interface
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // react to key down events
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP && player.canMove(0, -1, walls)) {
            player.keyPressed(e);
        }
        if (key == KeyEvent.VK_RIGHT && player.canMove(1, 0, walls)) {
            player.keyPressed(e);
        }
        if (key == KeyEvent.VK_DOWN && player.canMove(0, 1, walls)) {
            player.keyPressed(e);
        }
        if (key == KeyEvent.VK_LEFT && player.canMove(-1, 0, walls)) {
            player.keyPressed(e);
        }
        if (key == KeyEvent.VK_Y) {
            if (win || lose) {
                restart();
            }
        }
        if (key == KeyEvent.VK_N && player.canMove(-1, 0, walls)) {
            if (win || lose) {
                System.exit(0);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // react to key up events
    }

    private void drawBackground(Graphics g) {
        if (!win && !lose) {
            // draw a checkered background
            g.setColor(new Color(214, 214, 214));
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    // only color every other tile
                    if ((row + col) % 2 == 1) {
                        // draw a square tile at the current row/column position
                        g.fillRect(
                        col * TILE_SIZE, 
                        row * TILE_SIZE, 
                        TILE_SIZE, 
                        TILE_SIZE
                    );
                }
            }   
            } 
        } else {
            g.setColor(new Color(255, 255, 255));
        }
    }

    private void drawScore(Graphics g) {
        // set the text to be displayed
        String text = "Gold - " + player.getScore() + "   Lives - " + Player.lives;
        // we need to cast the Graphics to Graphics2D to draw nicer text
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        // set the text color and font
        g2d.setColor(new Color(30, 175, 155));
        g2d.setFont(new Font("Lato", Font.BOLD, 25));
        // draw the score in the bottom center of the screen
        // https://stackoverflow.com/a/27740330/4655368
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        // the text will be contained within this rectangle.
        // here I've sized it to be the entire bottom row of board tiles
        Rectangle rect = new Rectangle(0, TILE_SIZE * (ROWS - 1), TILE_SIZE * COLUMNS, TILE_SIZE);
        // determine the x coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // determine the y coordinate for the text
        // (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // draw the string
        g2d.drawString(text, x, y);
    }

    private void drawEndText(Graphics g) {
        String text;
        // set the text to be displayed
        if (win) {
            text = "  Congratulations, you won!\n"
            + "Would you like to play again?\n"
            + "        (Press Y or N)";
        } else {
            text = "  Sorry, you ran out of lives\n"
            + "Would you like to play again?\n"
            + "            (Press Y or N)";
        }
        // we need to cast the Graphics to Graphics2D to draw nicer text
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        // set the text color and font
        g2d.setColor(new Color(30, 175, 155));
        g2d.setFont(new Font("Lato", Font.BOLD, 42));
        // draw the score in the bottom center of the screen
        // https://stackoverflow.com/a/27740330/4655368
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        // the text will be contained within this rectangle.
        // here I've sized it to be the entire bottom row of board tiles
        Rectangle rect = new Rectangle(0, TILE_SIZE * (ROWS - 1), TILE_SIZE * COLUMNS, TILE_SIZE);
        // determine the x coordinate for the text
        int x = 300;
        // determine the y coordinate for the text
        // (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = 75;
        // draw the string
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            g2d.drawString(lines[i], x, y + i * metrics.getHeight());
        }
    }

    private ArrayList<Bomb> createBombs() {
        ArrayList<Bomb> bombList = new ArrayList<>();
        Random rand = new Random();

        // create the given number of bomb tiles in random positions on the board.
        // note that there is not check here to prevent two bomb tiles from occupying the same
        // spot, but there is one to prevent bomb tiles from spawning in the same spot as the player
        for (int i = 0; i < NUM_BOMBS; i++) {
            int bombX = rand.nextInt(COLUMNS);
            int bombY = rand.nextInt(ROWS);
            if (bombX == 0 && bombY == 0) {
                boolean bombInPlayerSpot = true;
                while (bombInPlayerSpot) {
                    bombX = rand.nextInt(COLUMNS);
                    bombY = rand.nextInt(ROWS);
                    if (bombX != 0 || bombY != 0) {
                        bombInPlayerSpot = false;
                    }
                }
            } else {
                bombList.add(new Bomb(bombX, bombY));
            }
        }

        return bombList;
    }

    private ArrayList<Wall> createWalls() {
        ArrayList<Wall> wallList = new ArrayList<>();
        Random rand = new Random();

        // create the given number of walls in random positions on the board.
        // note that there is not check here to prevent two walls from occupying the same
        // spot, nor to prevent walls from spawning in the same spot as the player
        for (int i = 0; i < NUM_WALLS; i++) {
            int wallX = rand.nextInt(COLUMNS);
            int wallY = rand.nextInt(ROWS);
            if (wallX == 0 && wallY == 0) {
                boolean wallInPlayerSpot = true;
                while (wallInPlayerSpot) {
                    wallX = rand.nextInt(COLUMNS);
                    wallY = rand.nextInt(ROWS);
                    if (wallX != 0 || wallY != 0) {
                        wallInPlayerSpot = false;
                    }
                }
            } else {
                wallList.add(new Wall(wallX, wallY));
            }
        }

        return wallList;
    }

    private ArrayList<Coin> populateCoins() {
        ArrayList<Coin> coinList = new ArrayList<>();
        Random rand = new Random();

        // create the given number of coins in random positions on the board.
        // note that there is not check here to prevent two coins from occupying the same
        // spot, nor to prevent coins from spawning in the same spot as the player
        for (int i = 0; i < NUM_COINS; i++) {
            int coinX = rand.nextInt(COLUMNS);
            int coinY = rand.nextInt(ROWS);
            coinList.add(new Coin(coinX, coinY));
        }

        return coinList;
    }

    private void collectCoins() {
        // allow player to pickup coins
        ArrayList<Coin> collectedCoins = new ArrayList<>();
        for (Coin coin : coins) {
            // if the player is on the same tile as a coin, collect it
            if (player.getPos().equals(coin.getPos())) {
                // give the player some points for picking this up
                if (coin.getSpecial()) {
                    player.addScore(300);
                } else {
                    player.addScore(50);
                }
                collectedCoins.add(coin);
            }
        }
        // remove collected coins from the board
        coins.removeAll(collectedCoins);
    }

    public void endScreen() {
        walls.clear();
        bombs.clear();
        coins.clear();
        repaint();
    }

    public void restart() {
        win = false;
        lose = false;
        Player.score = 0;
        Player.lives = 3;
        // set the game board background color
        setBackground(new Color(232, 232, 232));

        // initialize the game state
        walls = createWalls();
        bombs = createBombs();
        coins = populateCoins();

        // this timer will call the actionPerformed() method every DELAY ms
        timer.start();
    }

}
