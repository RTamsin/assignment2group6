import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Flappy extends GameEngine{
    final int WINDOW_WIDTH = 400;
    final int WINDOW_HEIGHT = 600;
    final int G = 1;

    // Bird
    double birdX;
    double birdY;
    double birdVelY;

    // Pipes
    ArrayList<Rectangle> pipes = new ArrayList<>();
    int pipeX;
    int pipeY;
    int pipeWidth;
    int pipeHeight;
    double pipeVelX;
    int gap;



    public static void main(String[] args) {
        createGame(new Flappy(), 60);
    }
    @Override
    public void init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        birdX=100;
        birdY=300;
        birdVelY=0;
    }

    @Override
    public void update(double dt) {
        birdVelY += G;
        birdY+=birdVelY*dt*10;


    }

    @Override
    public void paintComponent() {
        changeBackgroundColor(Color.cyan);
        clearBackground(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Bird
        changeColor(yellow);
        drawSolidCircle(birdX,birdY,20);

        // Pipes


    }
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP){
            birdVelY = -15;
        }
    }
    public void keyReleased(KeyEvent e){

    }

}
