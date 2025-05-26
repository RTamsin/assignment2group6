import java.awt.Image;

public class AnimatedSprite {
    private Image[] frames;
    private double frameDuration;
    private double timeSinceLastFrame = 0;
    private int currentFrame = 0;

    public AnimatedSprite(Image[] frames, double frameDuration) {
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    public void update(double dt) {
        timeSinceLastFrame += dt;
        if (timeSinceLastFrame >= frameDuration) {
            currentFrame = (currentFrame + 1) % frames.length;
            timeSinceLastFrame = 0;
        }
    }

    public Image getCurrentFrame() {
        return frames[currentFrame];
    }
}