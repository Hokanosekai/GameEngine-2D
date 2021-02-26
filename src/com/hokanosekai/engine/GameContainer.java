package com.hokanosekai.engine;

import java.awt.event.KeyEvent;

public class GameContainer implements Runnable {

    private Thread thread;
    private Window window;
    private Renderer renderer;
    private Input input;
    private AbstractGame game;

    public boolean running = false;
    private final double UPDATE_CAP = 1.0/60.0;
    private int width = 320, height = width / 12 * 9;
    private float scale = 3f;
    private String title = "GameEngine v1.0";

    public GameContainer(AbstractGame game){
        this.game = game;
    }

    public void start(){
        window = new Window(this);
        renderer = new Renderer(this);
        input = new Input(this);

        thread = new Thread(this);
        thread.run();
    }

    public void stop(){
        running = false;
    }

    public void run() {
        running = true;

        boolean render = false;
        double firstTime = 0;
        double lastTime = System.nanoTime() / 1000000000D;
        double passedTime = 0;
        double unprocessedTime = 0;

        double frameTime = 0;
        int frames = 0;
        int fps = 0;

        while(running){
            render = true;

            firstTime = System.nanoTime() / 1000000000D;
            passedTime = firstTime - lastTime;
            lastTime = firstTime;

            unprocessedTime += passedTime;
            frameTime += passedTime;

            while (unprocessedTime >= UPDATE_CAP){
                unprocessedTime -= UPDATE_CAP;
                render = true;

                //TODO:  Update Game
                game.update(this, (float) UPDATE_CAP);

                input.update();

                if (frameTime >= 1D){
                    frameTime = 0;
                    fps = frames;
                    frames = 0;
                    System.out.println("FPS : "+fps);
                }
            }

            if (render){
                renderer.clear();
                game.render(this, renderer);
                renderer.process();
                renderer.drawText("FPS:"+fps,0,0,0xff00ffff);
                window.update();
                frames++;
            }
            else{
                try {
                    Thread.sleep(1);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        dispose();
    }

    private void dispose(){

    }

    public Input getInput() {
        return input;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Window getWindow() {
        return window;
    }
}