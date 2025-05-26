import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Image;

public class Flappy extends GameEngine {

    private AnimatedSprite helicopter;
    private AnimatedSprite coin;
    private AnimatedSprite heart;
    private Image background;

    ArrayList<Cloud> clouds = new ArrayList<>();
    Image[] cloudImages = new Image[8];



    class Cloud {
        int x, y;
        double speed;
        double scale; // New
        Image image;

        public Cloud(int x, int y, double speed, double scale, Image image) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.scale = scale;
            this.image = image;
        }
    }

    final int WINDOW_WIDTH = 400;
    final int WINDOW_HEIGHT = 600;
    final int G = 1;
    final int COIN_HORIZONTAL_SPACING = 150;
    final int COIN_SIZE = 20;
    final int SKIP_DISTANCE = 30;
    // Safe distance to ensure coins don't spawn too close to pipes - Peter
    private static final int SAFE_DISTANCE = 50;

    enum GameState {START, PLAYING, GAME_OVER}

    GameState gameState = GameState.START;

    double birdX, birdY, birdVelY;
    int lives = 3;
    int score = 0;

    ArrayList<Rectangle> coins = new ArrayList<>();
    ArrayList<Rectangle> healthCoins = new ArrayList<>();
    ArrayList<Integer> topScores = new ArrayList<>();

    ArrayList<Obstacle> pipes = new ArrayList<>();
    boolean pipePassed;

    int coinSpawnRate = 40;
    int healthCoinSpawnRate = 3500;
    int pipeSpeed = 30;
    int space = 200;

    double timer, elapsedTime;
    // Timer for invulnerability after collision - Peter
    double invulnerabilityTimer = 0;
    // Invulnerability duration in seconds - Peter
    final double INVULNERABILITY_DURATION = 2.5;
    double flashTimer = 0;
    // Flashing interval in seconds - Peter
    final double FLASH_INTERVAL = 0.2;
    // Toggle for flashing - Peter
    boolean heliVisible = true;

    Random rand = new Random();

    Image heli;

    // Add UISound Instance - Peter
    private final UISound uiSound = new UISound(this, this);

    public static void main(String[] args) {
        createGame(new Flappy(), 60);
    }

    @Override
    public void init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        for (int i = 0; i < 8; i++) {
            cloudImages[i] = loadImage("Resources/Cloud/clouds" + (i + 1) + ".png");
        }

        for (int i = 0; i < 4; i++) {
            int x = (int)(Math.random() * 400);
            int y = 10 + (int)(Math.random() * 50);  // stay near top
            double speed = 10 + Math.random() * 40;
            double scale = 0.2 + Math.random() * 0.2;  // scale between 0.4 and 0.7
            Image img = cloudImages[(int)(Math.random() * cloudImages.length)];
            clouds.add(new Cloud(x, y, speed, scale, img));
        }

        background = loadImage("Resources/Background/background3.png");

        Image[] helicopterFrames = {
                loadImage("Resources/Helicopter/helicopter_1.png"),
                loadImage("Resources/Helicopter/helicopter_2.png"),
                loadImage("Resources/Helicopter/helicopter_3.png"),
                loadImage("Resources/Helicopter/helicopter_4.png"),
                loadImage("Resources/Helicopter/helicopter_5.png"),
                loadImage("Resources/Helicopter/helicopter_6.png"),
                loadImage("Resources/Helicopter/helicopter_7.png"),
                loadImage("Resources/Helicopter/helicopter_8.png")
        };
        helicopter = new AnimatedSprite(helicopterFrames, 0.01); // 100ms per frame

        Image[] coinFrames = {
                loadImage("Resources/Coin/coins1.png"),
                loadImage("Resources/Coin/coins2.png"),
                loadImage("Resources/Coin/coins3.png"),
                loadImage("Resources/Coin/coins4.png"),
                loadImage("Resources/Coin/coins5.png"),
                loadImage("Resources/Coin/coins6.png"),
                loadImage("Resources/Coin/coins7.png")
        };
        coin = new AnimatedSprite(coinFrames, 0.10);

        Image[] heartFrames = {
                loadImage("Resources/Heart/heartCleaned.png"),
        };
        heart = new AnimatedSprite(heartFrames, 0.2);

        resetGame();

        placeTopPipe();
        placeBottomPipe();
        pipePassed = false;

        timer = 0;
        elapsedTime = 10;
    }

    public void placeTopPipe() {
        int randomPipeHeight = rand(150) + 80;
        pipes.add(new Obstacle(WINDOW_WIDTH, 0, 80, randomPipeHeight));
    }

    // Bottom Pipe
    public void placeBottomPipe() {
        int randomPipeHeight = rand(150) + 80;
        int randX = rand(150) + 100;
        pipes.add(new Obstacle(WINDOW_WIDTH + randX, randomPipeHeight + space, 80, WINDOW_HEIGHT - randomPipeHeight - space));
    }

    @Override
    public void update(double dt) {
        if (gameState != GameState.PLAYING) return;
        timer += dt;



        // Update invulnerability timer -Peter
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= dt;
            flashTimer += dt;
            if (flashTimer >= FLASH_INTERVAL) {
                heliVisible = !heliVisible;
                flashTimer = 0;
            }
        } else {
            heliVisible = true;
        }

        birdVelY += G;
        birdY += birdVelY * dt * 10;

        for (int i = 0; i < pipes.size(); i++) {
            pipes.get(i).x += -5 * dt * pipeSpeed;
        }

        // Add difficulty progression every 10sec
        // 1. The obstacles move faster
        // 2. Space between obstacles get smaller
        if (timer >= elapsedTime) {
            pipeSpeed += 5;
            space -= 10;
            timer = 0;
        }

        if (pipes.get(1).x + pipes.get(1).width < 0) {
            pipes.remove(0);
            pipes.remove(0);
            placeTopPipe();
            placeBottomPipe();
        }

        for (Rectangle coin : coins) {
            // Match pipe speed - Peter
            coin.x += -5 * dt * pipeSpeed;
        }

        coins.removeIf(coin -> coin.x + coin.width < 0);

        if (coins.isEmpty() || coins.get(coins.size() - 1).x < WINDOW_WIDTH - COIN_HORIZONTAL_SPACING) {
            if (rand.nextInt(coinSpawnRate) == 0) {
                spawnCoin();
            }
        }

        if (rand.nextInt(healthCoinSpawnRate) == 0) {
            // Spawn health coin only if there are no coins or the last coin is far enough -Peter
            Rectangle newHealthCoin = spawnHealthCoin();
            if (newHealthCoin != null) {
                healthCoins.add(newHealthCoin);
            }
        }

        for (Rectangle hcoin : healthCoins) {
            // Match pipe speed - Peter
            hcoin.x += -5 * dt * pipeSpeed;
        }

        healthCoins.removeIf(hcoin -> hcoin.x + hcoin.width < 0);

        for (Cloud cloud : clouds) {
            cloud.x -= cloud.speed * dt;

            int scaledWidth = (int)(cloud.image.getWidth(null) * cloud.scale);

            if (cloud.x + scaledWidth < 0) {
                cloud.x = 400 + scaledWidth; // move off-screen to the right
                cloud.y = 10 + (int)(Math.random() * 50);
                cloud.speed = Math.max(10, 10 + Math.random() * 40);
                cloud.scale = 0.2 + Math.random() * 0.2;
                cloud.image = cloudImages[(int)(Math.random() * cloudImages.length)];
            }
        }

        helicopter.update(dt);
        coin.update(dt);
        heart.update(dt);

        detectCollisions();
    }

    @Override
    public void paintComponent() {
        clearBackground(WINDOW_WIDTH, WINDOW_HEIGHT);
        drawImage(background, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        for (Cloud cloud : clouds) {
            int scaledWidth = (int)(cloud.image.getWidth(null) * cloud.scale);
            int scaledHeight = (int)(cloud.image.getHeight(null) * cloud.scale);
            drawImage(cloud.image, cloud.x, cloud.y, scaledWidth, scaledHeight);
        }

        // Only draw the helicopter if it's visible (flashing effect)
        if (heliVisible) {
            drawImage(helicopter.getCurrentFrame(), (int)birdX, (int)birdY, 100, 64);
        }

        changeColor(Color.black);
        for (Obstacle value : pipes) {
            drawSolidRectangle(value.x, value.y, value.width, value.height);
        }

        Image currentCoinFrame = coin.getCurrentFrame();
        for (Rectangle c : coins) {
            drawImage(currentCoinFrame, c.x, c.y, COIN_SIZE, COIN_SIZE);
        }


        // Draw health coins (green)
        for (Rectangle hcoin : healthCoins) {
            drawImage(heart.getCurrentFrame(), hcoin.x, hcoin.y, COIN_SIZE, COIN_SIZE);
        }


        // Comment out the old UI and render new UI from UISound.java - Peter
        uiSound.renderUI();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
                init();
                gameState = GameState.PLAYING;
                birdVelY = -20;
            } else if (gameState == GameState.PLAYING) {
                birdVelY = -20;
            }
        }
    }

    private void detectCollisions() {
        Rectangle birdRect = new Rectangle((int) birdX - 20, (int) birdY - 20, 40, 40);
        boolean collisionDetected = false;

        coins.removeIf(coin -> {
            if (birdX + 100 >= coin.x
                    && coin.x + coin.width >= birdX
                    && birdY < coin.y + coin.height
                    && birdY + 60 >= coin.y) {
                score += 10;
                // Coin collect sound - Peter
                playAudio(uiSound.getCoinSound());
                return true;
            }
            return false;
        });

        healthCoins.removeIf(hcoin -> {
            if (birdX + 100 >= hcoin.x
                    && hcoin.x + hcoin.width >= birdX
                    && birdY < hcoin.y + hcoin.height
                    && birdY + 60 >= hcoin.y) {
                lives++;
                playAudio(uiSound.getHealthSound());
                return true;
            }
            return false;
        });

        // Only check for pipe collisions if not invulnerable - Peter
        if (invulnerabilityTimer <= 0) {
            for (Obstacle pipe : pipes) {
                Rectangle pipeRect = new Rectangle(
                        (int) pipe.x,
                        (int) pipe.y,
                        (int) pipe.width,
                        (int) pipe.height
                );
                if (birdX + 100 >= pipe.x
                        && pipe.x + pipe.width >= birdX
                        && birdY < pipe.y + pipe.height
                        && birdY + 60 >= pipe.y) {
                    lives--;
                    birdY = 300;
                    birdVelY *= -1;
                    playAudio(uiSound.getCrashSound());
                    invulnerabilityTimer = INVULNERABILITY_DURATION;
                    if (lives <= 0) {
                        updateTopScores(score);
                        gameState = GameState.GAME_OVER;
                        timer = 0;
                        space = 200;
                    }
                    return;
                }
            }
        }

        // Boundary collision (top/bottom)
        if (birdY < 0 || birdY > WINDOW_HEIGHT) {
            lives--;
            birdY = 300;
            birdVelY = 0;
            // Crash sound on collision - Peter
            playAudio(uiSound.getCrashSound());
            invulnerabilityTimer = INVULNERABILITY_DURATION;
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
        timer = 0;
        space = 200;

        // reset other game parameters - Peter
        pipeSpeed = 30;
        pipes.clear();
        invulnerabilityTimer = 0;
        flashTimer = 0;
        heliVisible = true;
    }

    private void spawnCoin() {
        int x = WINDOW_WIDTH;

        // Ensure the coin spawns at a safe distance from the pipes - Peter
        int y;
        boolean validPosition = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        while (!validPosition && attempts < MAX_ATTEMPTS) {
            y = rand.nextInt(WINDOW_HEIGHT - COIN_SIZE - 20) + 20;
            Rectangle newCoin = new Rectangle(x, y, COIN_SIZE, COIN_SIZE);
            validPosition = true;

            for (Obstacle pipe : pipes) {
                Rectangle safeZone = new Rectangle(
                        (int) pipe.x - SAFE_DISTANCE,
                        (int) pipe.y - SAFE_DISTANCE,
                        (int) pipe.width + (2 * SAFE_DISTANCE),
                        (int) pipe.height + (2 * SAFE_DISTANCE)
                );
                if (safeZone.intersects(newCoin)) {
                    validPosition = false;
                    break;
                }
            }

            if (validPosition) {
                coins.add(newCoin);
                break;
            }
            attempts++;
        }

        // Fallback: If no valid position is found, spawn in a safe y-range
        if (!validPosition) {
            // Find the gap between the last pipe pair
            if (pipes.size() >= 2) {
                Obstacle topPipe = pipes.get(pipes.size() - 2);
                Obstacle bottomPipe = pipes.get(pipes.size() - 1);
                double gapStartY = topPipe.height;
                double gapEndY = bottomPipe.y;
                if (gapEndY - gapStartY >= COIN_SIZE + 20) {
                    y = rand.nextInt((int) (gapEndY - gapStartY - COIN_SIZE - 20)) + (int) gapStartY + 10;
                    coins.add(new Rectangle(x, y, COIN_SIZE, COIN_SIZE));
                }
            }
        }
    }

    private Rectangle spawnHealthCoin() {
        int border = 40;
        // Ensure the health coin spawns at a safe distance from the pipes - Peter
        int x = WINDOW_WIDTH + border;
        int y;
        boolean validPosition = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        while (!validPosition && attempts < MAX_ATTEMPTS) {
            y = rand.nextInt(WINDOW_HEIGHT - border * 2 - COIN_SIZE) + border;
            Rectangle newHealthCoin = new Rectangle(x, y, COIN_SIZE, COIN_SIZE);
            validPosition = true;

            for (Obstacle pipe : pipes) {
                Rectangle safeZone = new Rectangle(
                        (int) pipe.x - SAFE_DISTANCE,
                        (int) pipe.y - SAFE_DISTANCE,
                        (int) pipe.width + (2 * SAFE_DISTANCE),
                        (int) pipe.height + (2 * SAFE_DISTANCE)
                );
                if (safeZone.intersects(newHealthCoin)) {
                    validPosition = false;
                    break;
                }
            }

            if (validPosition) {
                return newHealthCoin;
            }
            attempts++;
        }

        // Fallback: If no valid position is found, spawn in a safe y-range
        if (pipes.size() >= 2) {
            Obstacle topPipe = pipes.get(pipes.size() - 2);
            Obstacle bottomPipe = pipes.get(pipes.size() - 1);
            double gapStartY = topPipe.height;
            double gapEndY = bottomPipe.y;
            if (gapEndY - gapStartY >= COIN_SIZE + 20) {
                y = rand.nextInt((int) (gapEndY - gapStartY - COIN_SIZE - 20)) + (int) gapStartY + 10;
                return new Rectangle(x, y, COIN_SIZE, COIN_SIZE);
            }
        }

        // If no safe position is found, return null (skip spawning)
        return null;
    }

    private void updateTopScores(int newScore) {
        topScores.add(newScore);
        topScores.sort((a, b) -> b - a);
        if (topScores.size() > 3) {
            topScores.remove(3);
        }
    }


    public void updateHeli(double dt) {
        // If the explosion is active
        if (gameState == GameState.PLAYING) {
            // Increment timer
            timer += dt * 100;
        }
    }





    // Create new methods for UISound - Peter
    public GameState getState() {
        return gameState;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public ArrayList<Integer> getTopScores() {
        return topScores;
    }
}