package com.hokanosekai.game;

import com.hokanosekai.engine.AbstractGame;
import com.hokanosekai.engine.GameContainer;
import com.hokanosekai.engine.Renderer;
import com.hokanosekai.engine.audio.SoundClip;
import com.hokanosekai.engine.gfx.Image;
import com.hokanosekai.engine.gfx.ImageTile;

import java.awt.event.KeyEvent;

public class GameManager extends AbstractGame {

    private Image image;
    private Image image2;
    private SoundClip clip;

    public GameManager(){
        image = new Image("/resources/SpriteSheet.png");
        image2 = new Image("/resources/test.png");
        image2.setAlpha(true);
        clip = new SoundClip("/resources/sounds/son1.wav");
    }

    public void reset(){

    }

    @Override
    public void update(GameContainer gc, float dt) {
        if(gc.getInput().isKeyDown(KeyEvent.VK_A)){
            clip.play();
        }
    }

    @Override
    public void render(GameContainer gc, Renderer r) {
        r.drawImage(image, 10,15);
        r.drawImage(image2, gc.getInput().getMouseX(),gc.getInput().getMouseY());
    }


    public static void main(String[] args) {
        GameContainer gc = new GameContainer(new GameManager());
        gc.start();
    }
}
