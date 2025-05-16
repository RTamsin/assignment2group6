import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;


public class Flappy extends GameEngine {
    final int WINDOW_WIDTH = 400;
    final int WINDOW_HEIGHT = 600;
    final int G = 1;
    final int COIN_HORIZONTAL_SPACING = 150;
    final int COIN_SIZE = 20;
    final int SKIP_DISTANCE = 30;


    enum GameState { START, PLAYING, GAME_OVER }
    GameState gameState = GameState.START;


    double birdX, birdY, birdVelY;
    int lives = 3;
    int score = 0;


    ArrayList<Rectangle> coins = new ArrayList<>();
    ArrayList<Rectangle> healthCoins = new ArrayList<>();
    ArrayList<Integer> topScores = new ArrayList<>();

    ArrayList<Obstacle> pipes;
    boolean pipePassed;

    int coinSpawnRate = 40;
    int healthCoinSpawnRate = 3500;
    int pipeSpeed = 30;
    int space = 200;

    double timer, elapsedTime;


    Random rand = new Random();

    Image background, helicopter;

    // Add UISound Instance - Peter
    private final UISound uiSound = new UISound(this, this);;


    public static void main(String[] args) {
        createGame(new Flappy(), 60);
    }


    @Override
    public void init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        background = loadImage("src/img/Background.png");
        helicopter = loadImage("src/img/heli.png");

        resetGame();

        pipes = new ArrayList<>();
        placeTopPipe();
        placeBottomPipe();
        pipePassed=false;

        timer = 0;
        elapsedTime = 10;
    }
    public void placeTopPipe(){
        int randomPipeHeight = rand(150)+80;
        pipes.add(new Obstacle(WINDOW_WIDTH, 0, 80, randomPipeHeight));
    }
    // Bottom Pipe
    public void placeBottomPipe(){
        int randomPipeHeight = rand(150)+80;
        int randX = rand(150)+100;
        pipes.add(new Obstacle(WINDOW_WIDTH+randX,randomPipeHeight+space, 80, WINDOW_HEIGHT-randomPipeHeight-space));
    }

    @Override
    public void update(double dt) {
        if (gameState != GameState.PLAYING) return;
        timer += dt;


        birdVelY += G;
        birdY += birdVelY * dt * 10;

        for (int i = 0; i < pipes.size(); i++) {
            pipes.get(i).x += -5*dt*pipeSpeed;
        }

        // Add difficulty progression every 10sec
        //1. The obstacles move faster
        //2. Space between obstacles get smaller
        if(timer>=elapsedTime){
            pipeSpeed+=5;
            space-=10;
            timer=0;
        }

        if(pipes.get(1).x + pipes.get(1).width < 0){
            pipes.remove(pipes.get(0));
            pipes.remove(pipes.get(0));
            placeTopPipe();
            placeBottomPipe();
        }

        for (Rectangle coin : coins) {
            coin.x -= 2;
        }


        coins.removeIf(coin -> coin.x + coin.width < 0);


        if (coins.isEmpty() || coins.get(coins.size() - 1).x < WINDOW_WIDTH - COIN_HORIZONTAL_SPACING) {
            if (rand.nextInt(coinSpawnRate) == 0) {
                spawnCoin();
            }
        }


        if (rand.nextInt(healthCoinSpawnRate) == 0) {
            healthCoins.add(randomItem(true));
        }


        for (Rectangle hcoin : healthCoins) {
            hcoin.x -= 2;
        }


        healthCoins.removeIf(hcoin -> hcoin.x + hcoin.width < 0);


        detectCollisions();


    }


    @Override
    public void paintComponent() {
        clearBackground(WINDOW_WIDTH, WINDOW_HEIGHT);
        drawBackground();


        drawImage(helicopter, birdX,birdY,100,60);

        changeColor(Color.black);
        for (Obstacle value : pipes) {
            drawSolidRectangle(value.x, value.y, value.width, value.height);
        }

        // Draw coins (gold)
        changeColor(Color.ORANGE);
        for (Rectangle coin : coins) {
            drawSolidCircle(coin.x + COIN_SIZE / 2.0, coin.y + COIN_SIZE / 2.0, COIN_SIZE / 2.0);
        }


        // Draw health coins (green)
        changeColor(Color.GREEN);
        for (Rectangle hcoin : healthCoins) {
            drawSolidCircle(hcoin.x + COIN_SIZE / 2.0, hcoin.y + COIN_SIZE / 2.0, COIN_SIZE / 2.0);
        }

        // Comment out the old UI and render new UI from UISound.java- Peter
        uiSound.renderUI();

//        // Display score and lives
//        changeColor(Color.BLACK);
//        drawText(20, 30, "Lives: " + lives, "Arial", 20);
//        drawText(20, 60, "Score: " + score, "Arial", 20);
//
//
//        // Display top scores on the right
//        drawText(WINDOW_WIDTH - 150, 30, "Top Scores:", "Arial", 20);
//        for (int i = 0; i < topScores.size(); i++) {
//            drawText(WINDOW_WIDTH - 150, 60 + i * 30, (i + 1) + ": " + topScores.get(i), "Arial", 18);
//        }
//
//
//        // Display game state messages
//        if (gameState == GameState.START) {
//            drawText(width() / 2 - 100, height() / 2, "PRESS UP TO START", "Arial", 24);
//        } else if (gameState == GameState.GAME_OVER) {
//            drawText(width() / 2 - 100, height() / 2 - 20, "GAME OVER", "Arial", 24);
//            drawText(width() / 2 - 130, height() / 2 + 20, "PRESS UP TO RESTART", "Arial", 24);
//        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
                init();
                gameState = GameState.PLAYING;
                birdVelY = -15;
            } else if (gameState == GameState.PLAYING) {
                birdVelY = -15;
            }
        }
    }


    private void detectCollisions() {
        Rectangle birdRect = new Rectangle((int) birdX - 20, (int) birdY - 20, 40, 40);
        boolean collisionDetected = false;

        coins.removeIf(coin -> {
            if (coin.intersects(birdRect)) {
                score += 10;
                // Coin collect sound - Peter
                playAudio(uiSound.getCoinSound());
                return true;
            }
            return false;
        });


        healthCoins.removeIf(hcoin -> {
            if (hcoin.intersects(birdRect)) {
                lives++;
                // Health coin collect sound - Peter
                playAudio(uiSound.getHealthSound());
                return true;
            }
            return false;
        });


        // If bird goes off screen, lose a life
        for (Obstacle pipe : pipes) {
            Rectangle pipeRect = new Rectangle(
                    (int) pipe.x,
                    (int) pipe.y,
                    (int) pipe.width,
                    (int) pipe.height
            );
            if (pipeRect.intersects(birdRect)) {
                lives--;
                birdY = 300;
                birdVelY = 0;
                // Crash sound on collision - Peter
                playAudio(uiSound.getCrashSound());
                if (lives <= 0) {
                    updateTopScores(score);
                    gameState = GameState.GAME_OVER;


                }
                return;
            }
        }

        // Boundary collision (top/bottom)
        if (birdY < 0 || birdY > WINDOW_HEIGHT) {
            lives--;
            birdY = 300;
            birdVelY = 0;
            // Crash sound on collision - Peter
            playAudio(uiSound.getCrashSound());
            if (lives <= 0) {
                updateTopScores(score);
                gameState = GameState.GAME_OVER;

            }
        }
    }



    private void resetGame() {
        lives = 3;
        score = 0;
        birdX = 100;
        birdY = 300;
        birdVelY = 0;
        coins.clear();
        healthCoins.clear();
        timer=0;
        space=200;

    }


    private void spawnCoin() {
        int x = WINDOW_WIDTH;
        int y = rand.nextInt(WINDOW_HEIGHT - COIN_SIZE - 20) + 20;
        coins.add(new Rectangle(x, y, COIN_SIZE, COIN_SIZE));
    }


    private Rectangle randomItem(boolean isHealth) {
        int border = 40;
        int x = WINDOW_WIDTH + border;
        int y = rand.nextInt(WINDOW_HEIGHT - border * 2 - COIN_SIZE) + border;
        return new Rectangle(x, y, COIN_SIZE, COIN_SIZE);
    }


    private void updateTopScores(int newScore) {
        topScores.add(newScore);
        topScores.sort((a, b) -> b - a);
        if (topScores.size() > 3) {
            topScores.remove(3);
        }
    }

    private void drawBackground(){
        drawImage(background,0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
    }

    // Create new methods for UISound - Peter
    public GameState getState() { return gameState; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public ArrayList<Integer> getTopScores() { return topScores; }
}
