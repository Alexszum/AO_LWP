package com.iusail.ao.autumnowl;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class ParallaxSprite extends Sprite {

    private float mParallaxSpeed;
    private float mOffsetX = 0;
    private float mOffsetY = 0;
    private float baseOffsetX;

    public ParallaxSprite(float pX, float pY, float mParallaxSpeed, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
        this.mParallaxSpeed = mParallaxSpeed;
        this.mOffsetX = pX - (pX * mParallaxSpeed);
        this.mOffsetY = pY + (pX * mParallaxSpeed);
    }

    @Override
    public void onManagedUpdate(float pSecondsElapsed){
        super.onManagedUpdate(pSecondsElapsed);
        this.mOffsetY -= this.mParallaxSpeed * pSecondsElapsed;
    }

    @Override
    protected void onManagedDraw(GLState pGLState, Camera pCamera) {
        pGLState.pushModelViewGLMatrix();
        {
            final float shapeWidthScaled = this.getWidthScaled();//задаем область для рисования
            final float shapeHeightScaled = this.getHeightScaled();
            final float cameraWidth = pCamera.getWidth()*3;
            baseOffsetX = (this.mOffsetY * this.mParallaxSpeed)% shapeWidthScaled;

            while(baseOffsetX > 0) {
                baseOffsetX -= shapeWidthScaled;
            }
            pGLState.translateModelViewGLMatrixf( -baseOffsetX, -baseOffsetX, 0); // x .y. z

            float currentMaxX = baseOffsetX;

            do {

                this.preDraw(pGLState, pCamera);
                this.draw(pGLState, pCamera);
                this.postDraw(pGLState, pCamera);
                pGLState.translateModelViewGLMatrixf(shapeHeightScaled, shapeHeightScaled, 0); // x. y. z.
                currentMaxX += shapeWidthScaled;
            } while(currentMaxX < cameraWidth);
        }
        pGLState.popModelViewGLMatrix();
    }

    public void setSpeed(float pSpeed){
        this.mParallaxSpeed = pSpeed;
    }

    public float getSpeed(float pSpeed){
        return this.mParallaxSpeed;
    }

    public void setPosY(float pPosY){
        this.mOffsetY = pPosY;
    }

    public void setPosX(float pPosX){
        this.mOffsetX = pPosX;
    }

    public float getPosX(float pPosX){
        return this.mOffsetX;
    }
}
