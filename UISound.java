import java.util.ArrayList;

public class UISound {
    private GameEngine engine;
    private Flappy coreGameplay;
    private GameEngine.AudioClip helicopterSound;
    private GameEngine.AudioClip crashSound;
    private GameEngine.AudioClip coinSound;
    private GameEngine.AudioClip healthSound;
    private GameEngine.AudioClip backgroundMusic;
    private float backgroundMusic_volume = -20.0f;
    private float helicopterSound_volume = 0.0f;

    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    public UISound(GameEngine engine, Flappy coreGameplay) {
        this.engine = engine;
        this.coreGameplay = coreGameplay;

        // Load sounds
        this.helicopterSound = engine.loadAudio("src/sounds/helicopter.wav");
        this.crashSound = engine.loadAudio("src/sounds/crash.wav");
        this.coinSound = engine.loadAudio("src/sounds/coin.wav");
        this.healthSound = engine.loadAudio("src/sounds/health.wav");
        this.backgroundMusic = engine.loadAudio("src/sounds/background.wav");

        // Start continuous helicopter sound and background music
        this.engine.startAudioLoop(helicopterSound, helicopterSound_volume);
        this.engine.startAudioLoop(backgroundMusic, backgroundMusic_volume);
    }

    public void renderUI() {
        if (coreGameplay.getState() == Flappy.GameState.START) {
            engine.changeColor(engine.white);
            engine.drawBoldText(WIDTH / 2 - 100, HEIGHT / 2 - 50, "Press UP to Start", "Arial", 24);
        } else if (coreGameplay.getState() == Flappy.GameState.PLAYING) {
            engine.changeColor(engine.white);
            engine.drawText(20, 50, "Score: " + coreGameplay.getScore(), "Arial", 20);
            engine.drawText(20, 80, "Lives: " + coreGameplay.getLives(), "Arial", 20);
        } else if (coreGameplay.getState() == Flappy.GameState.GAME_OVER) {
            engine.changeColor(engine.white);
            engine.drawBoldText(WIDTH / 2 - 90, HEIGHT / 2 - 100, "Game Over", "Arial", 30);
            engine.drawText(WIDTH / 2 - 70, HEIGHT / 2, "Score: " + coreGameplay.getScore(), "Arial", 20);
            engine.drawText(WIDTH / 2 - 70, HEIGHT / 2 + 30, "Top Scores:", "Arial", 20);
            ArrayList<Integer> topScores = coreGameplay.getTopScores();
            for (int i = 0; i < topScores.size() && i < 3; i++) {
                engine.drawText(WIDTH / 2 - 70, HEIGHT / 2 + 60 + i * 30, (i + 1) + ": " + topScores.get(i), "Arial", 18);
            }
            engine.drawText(WIDTH / 2 - 100, HEIGHT / 2 + 200, "Press UP to Restart", "Arial", 20);
        }
    }

    public GameEngine.AudioClip getCrashSound() { return crashSound; }
    public GameEngine.AudioClip getCoinSound() { return coinSound; }
    public GameEngine.AudioClip getHealthSound() { return healthSound; }
}