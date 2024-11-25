import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Player {

    // image that represents the player's position on the board
    private BufferedImage image;
    // current position of the player on the board grid
    private final Point pos;
    // keep track of the player's score
    static int score;
    // keep track of player lives
    static int lives = 3;

    public Player(ArrayList<Wall> walls, ArrayList<Bomb> bomb) {
        // load the assets
        loadImage();

        // initialize the state
        pos = new Point(0, 0);
        score = 0;
    }

    private void loadImage() {
        try {
            // you can use just the filename if the image file is in your
            // project folder, otherwise you need to provide the file path.
            image = ImageIO.read(new File("images/goblin.png"));
        } catch (IOException exc) {
            System.out.println("Error opening image file: " + exc.getMessage());
        }
    }

    public void draw(Graphics g, ImageObserver observer) {
        // with the Point class, note that pos.getX() returns a double, but 
        // pos.x reliably returns an int. https://stackoverflow.com/a/30220114/4655368
        // this is also where we translate board grid position into a canvas pixel
        // position by multiplying by the tile size.
        g.drawImage(
            image, 
            pos.x * Board.TILE_SIZE, 
            pos.y * Board.TILE_SIZE, 
            observer
        );
    }
    
    public void keyPressed(KeyEvent e) {
        // every keyboard get has a certain code. get the value of that code from the
        // keyboard event so that we can compare it to KeyEvent constants
        int key = e.getKeyCode();
        
        // depending on which arrow key was pressed, we're going to move the player by
        // one whole tile for this input
        if (key == KeyEvent.VK_UP) {
            pos.translate(0, -1);
        }
        if (key == KeyEvent.VK_RIGHT) {
            pos.translate(1, 0);
        }
        if (key == KeyEvent.VK_DOWN) {
            pos.translate(0, 1);
        }
        if (key == KeyEvent.VK_LEFT) {
            pos.translate(-1, 0);
        }
    }

    public void tick(ArrayList<Wall> walls, ArrayList<Bomb> bomb) {
        // this gets called once every tick, before the repainting process happens.
        // so we can do anything needed in here to update the state of the player.

        int currentX = pos.x;
        int currentY = pos.y;

        // prevents the player from moving off the edge of the board sideways
        if (pos.x < 0) {
            currentX = 0;
            pos.x = Board.COLUMNS - 1;
        } else if (pos.x >= Board.COLUMNS) {
            currentX = Board.COLUMNS - 1;
            pos.x = 0;
        }
        // prevents the player from moving off the edge of the board vertically
        if (pos.y < 0) {
            currentY = 0;
            pos.y = Board.ROWS - 1;
        } else if (pos.y >= Board.ROWS) {
            currentY = Board.ROWS - 1;
            pos.y = 0;
        }

        if (isOverlappingWithBomb(bomb)) {
            lives--;
            pos.x = 0;
            pos.y = 0;
        }// checks if the player's position overlaps with walls
        else if (isOverlappingWithWalls(walls)) {
            pos.x = currentX;
            pos.y = currentY;
        }
    }

    private boolean isOverlappingWithBomb(ArrayList<Bomb> bomb) {
        for (Bomb bombTile : bomb) {
            if (pos.equals(bombTile.getPos())) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlappingWithWalls(ArrayList<Wall> walls) {
        // Check if the player's new position overlaps with any wall's position
        for (Wall wall : walls) {
            if (pos.equals(wall.getPos())) {
                return true;
            }
        }
        return false;
    }

    public String getScore() {
        return String.valueOf(score);
    }

    public void addScore(int amount) {
        score += amount;
    }

    public Point getPos() {
        return pos;
    }

    public boolean canMove(int dx, int dy, ArrayList<Wall> walls) {
    Point nextPos = new Point(pos.x + dx, pos.y + dy);
    for (Wall wall : walls) {
        if (wall.getPos().equals(nextPos)) {
            return false;
        }
    }
    return true;
}

}
