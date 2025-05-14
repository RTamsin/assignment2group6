import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.channels.Pipe;
import java.util.ArrayList;

public class Flappy extends GameEngine{
    final int WINDOW_WIDTH = 400;
    final int WINDOW_HEIGHT = 600;
    final int G = 1;

    // Bird
    double birdX;
    double birdY;
    double birdVelocity;
    double birdSpeed;
    ArrayList<Obstacle> pipes;
    boolean pipePassed;

    Image background;
    Image helicopter;


    public static void main(String[] args) {
        createGame(new Flappy(), 60);
    }
    @Override
    public void init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        background = loadImage("Background.png");
        helicopter = loadImage("heli.png");
        birdX=100;
        birdY=300;
        birdVelocity=0;
        birdSpeed=8;

        pipes = new ArrayList<>();
        placeTopPipe();
        placeBottomPipe();
        pipePassed=false;

    }
    //Top Pipe
    public void placeTopPipe(){
        int randomPipeHeight = rand(150)+80;
        pipes.add(new Obstacle(WINDOW_WIDTH, 0, 80, randomPipeHeight));
    }
    // Bottom Pipe
    public void placeBottomPipe(){
        int randomPipeHeight = rand(150)+80;
        int randX = rand(150)+100;
        int space = 200;
        pipes.add(new Obstacle(WINDOW_WIDTH+randX,randomPipeHeight+space, 80, WINDOW_HEIGHT-randomPipeHeight-space));
    }
    public void drawBackground(){
        drawImage(background,0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
    }

    @Override
    public void update(double dt) {
        birdVelocity += G;
        birdY+=birdVelocity*dt*birdSpeed;

        // Update obstacles
        for (int i = 0; i < pipes.size(); i++) {
            pipes.get(i).x += -5*dt*30;
        }


        if(pipes.get(1).x + pipes.get(1).width < 0){
            pipes.remove(pipes.get(0));
            pipes.remove(pipes.get(0));
            placeTopPipe();
            placeBottomPipe();
        }
    }

    @Override
    public void paintComponent() {
        clearBackground(WINDOW_WIDTH, WINDOW_HEIGHT);
        drawBackground();

        // Bird
        drawImage(helicopter, birdX,birdY,100,60);

        // Pipes
        changeColor(Color.black);
        for (Obstacle value : pipes) {
            drawSolidRectangle(value.x, value.y, value.width, value.height);
        }

    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP){
            birdVelocity = -15;
        }
    }
    public void keyReleased(KeyEvent e){

    }

}
