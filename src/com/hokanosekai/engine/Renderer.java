package com.hokanosekai.engine;

import com.hokanosekai.engine.gfx.Font;
import com.hokanosekai.engine.gfx.Image;
import com.hokanosekai.engine.gfx.ImageRequest;
import com.hokanosekai.engine.gfx.ImageTile;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;

public class Renderer {
    private int pW,pH;
    private int[] p;
    private int[] zBuffer;

    private int zDepth = 0;
    private boolean processing = false;

    private Font font = Font.STANDARD;
    private ArrayList<ImageRequest> imageRequest = new ArrayList<>();

    public Renderer(GameContainer gc){
        pW = gc.getWidth();
        pH = gc.getHeight();
        p = ((DataBufferInt) gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
        zBuffer = new int[p.length];
    }

    public void clear(){
        for (int i=0;i<p.length;i++){
            p[i] = 0;
            zBuffer[i] = 0;
        }
    }

    public void process(){
        processing = true;
        for (int i=0;i<imageRequest.size();i++){
            ImageRequest ir = imageRequest.get(i);
            setzDepth(ir.zDepth);
            ir.image.setAlpha(true);
            drawImage(ir.image, ir.offX, ir.offY);
        }

        imageRequest.clear();
        processing = false;
    }

    public void setPixel(int x, int y, int value){

        int alpha = ((value >> 24) & 0xff);

        if((x < 0 || x >= pW || y < 0 || y >= pH -1) || alpha == 0){
            return;
        }

        if (zBuffer[x + y * pW] > zDepth)
            return;

        if (alpha == 255){
            p[x + y * pW] = value;
        }
        else{
            int pixelColor = p[x + y + pW];

            int newRed = ((pixelColor >> 16) & 0xff) - (int)((((pixelColor >> 16) & 0xff) - ((value >> 16) & 0xff)) * (alpha / 255f));
            int newGreen = ((pixelColor >> 8) & 0xff) - (int)((((pixelColor >> 8) & 0xff) - ((value >> 8) & 0xff)) * (alpha / 255f));
            int newBleu = (pixelColor & 0xff) - (int)(((pixelColor & 0xff) - (value & 0xff)) * (alpha / 255f));

            p[x + y * pW] = (255 << 24 | newRed << 16 | newGreen << 8 | newBleu);
        }
    }

    public void drawImage(Image image, int offx, int offy){

        if (image.isAlpha() && !processing){
            imageRequest.add(new ImageRequest(image, zDepth, offx, offy));
            return;
        }

        //Don't render code
        if (offx < -image.getW()) return;
        if (offy < -image.getH()) return;
        if (offx > pW) return;
        if (offy > pH) return;

        int newX = 0;
        int newY = 0;
        int newWidth = image.getW();
        int newHeigt = image.getH();

        //Clipping code
        if (offx < 0){ newX -= offx; }
        if (offy < 0){ newY -= offy; }
        if (newWidth + offx > pW){ newWidth -= newWidth + offx - pW; }
        if (newHeigt + offy > pH){ newHeigt -= newHeigt + offy - pH; }

        for (int y=newY;y<newHeigt;y++){

            for (int x=newX;x<newWidth;x++){
                setPixel(x + offx,y + offy,image.getP()[x + y * image.getW()]);
            }
        }
    }

    public void drawImageTile(ImageTile image, int offx, int offy, int tileX, int tileY){

        //Don't render code
        if (offx < -image.getTileW()) return;
        if (offy < -image.getTileH()) return;
        if (offx > pW) return;
        if (offy > pH) return;

        int newX = 0;
        int newY = 0;
        int newWidth = image.getTileW();
        int newHeigt = image.getTileH();

        //Clipping code
        if (offx < 0){ newX -= offx; }
        if (offy < 0){ newY -= offy; }
        if (newWidth + offx > pW){ newWidth -= newWidth + offx - pW; }
        if (newHeigt + offy > pH){ newHeigt -= newHeigt + offy - pH; }

        for (int y=newY;y<newHeigt;y++){

            for (int x=newX;x<newWidth;x++){
                setPixel(x + offx,y + offy,image.getP()[(x + tileX * image.getTileW()) + (y + tileY * image.getTileH()) * image.getW()]);
            }
        }
    }

    public void drawText(String text, int offX, int offY, int color){
        text = text.toUpperCase();
        int offset = 0;

        for (int i=0;i<text.length();i++){
            int unicode = text.codePointAt(i) - 32;

            for (int y=0;y< font.getFontImage().getH();y++){
                for (int x=0;x< font.getWidths()[unicode];x++){
                    if (font.getFontImage().getP()[(x + font.getOffsets()[unicode]) + y * font.getFontImage().getW()] == 0xffffffff){
                        setPixel(x + offX + offset,y + offY ,color);
                    }
                }
            }

            offset += font.getWidths()[unicode];
        }
    }

    public void drawRect(int offX,int offY,int width, int height,int color){
        for (int y=0;y<=height;y++){
            setPixel(offX,y + offY,color);
            setPixel(offX + width,y + offY, color);
        }

        for (int x=0;x<=width;x++){
            setPixel(x + offX,offY,color);
            setPixel(x + offX,height + offY, color);
        }
    }

    public void drawFillRect(int offX, int offY, int width, int height, int color){

        //Don't render code
        if (offX < -width) return;
        if (offY < -height) return;
        if (offX >= pW) return;
        if (offY >= pH) return;

        int newX = 0;
        int newY = 0;
        int newWidth = width;
        int newHeight = height;

        if (offX < 0) {newX -= offX;}
        if (offY < 0) {newY -= offY;}
        if (newWidth + offX >= pW) {newWidth -= newWidth + offX - pW;}
        if (newHeight + offY >= pH) {newHeight -= newHeight + offY - pH;}

        for (int y=newY;y<=newHeight;y++){
            for (int x=newX;x<=newWidth;x++){
                setPixel(x + offX,y + offY,color);
            }
        }
    }

    public int getzDepth() {
        return zDepth;
    }

    public void setzDepth(int zDepth) {
        this.zDepth = zDepth;
    }
}
