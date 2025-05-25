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

    Image background, helicopter, heli;

    // Add UISound Instance - Peter
    private final UISound uiSound = new UISound(this, this);

    public static void main(String[] args) {
        createGame(new Flappy(), 60);
    }

    @Override
    public void init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        background = loadImage("src/img/background.png");
        helicopter = loadImage("src/img/heli.png");
        heli = loadImage("src/img/Fly2.png");

        resetGame();

        placeTopPipe();
        placeBottomPipe();
        pipePassed = false;

        timer = 0;
        elapsedTime = 10;
        initHelicopter();
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

        detectCollisions();
    }

    @Override
    public void paintComponent() {
        clearBackground(WINDOW_WIDTH, WINDOW_HEIGHT);
        drawBackground();

        // Only draw the helicopter if it's visible (flashing effect)
        if (heliVisible) {
            drawHeli();
        }

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

    private void drawBackground() {
        drawImage(background, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    Image[] heliImages = new Image[10];

    public void initHelicopter() {
        int n = 0;
        for (int y = 0; y < 1260; y += 420) {
            for (int x = 0; x < 772; x += 772) {
                heliImages[n] = subImage(heli, x, y, 772, 420);
                n++;
            }
        }
    }

    public void updateHeli(double dt) {
        // If the explosion is active
        if (gameState == GameState.PLAYING) {
            // Increment timer
            timer += dt * 100;
        }
    }

    public int getAnimationFrame(double timer, double duration, int numFrames) {
        // Get frame
        int i = (int) floor(((timer % duration) / duration) * numFrames);
        // Check range
        if (i >= numFrames) {
            i = numFrames - 1;
        }
        // Return
        return i;
    }

    public void drawHeli() {
        int i = getAnimationFrame(timer, 0.3, 3);
        drawImage(heliImages[i], birdX, birdY, 150, 100);
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
