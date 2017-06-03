package com.iusail.ao.autumnowl;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class ParallaxSpriteGorizontal extends Sprite {
	 
    private float mParallaxSpeed;
    private float mOffsetX = 0;
    private float mOffsetY = 0;
    private float baseOffsetX;
    
    public ParallaxSpriteGorizontal(float pX, float pY, float mParallaxSpeed, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
        this.mParallaxSpeed = mParallaxSpeed;
        this.mOffsetX = pX - (pX * mParallaxSpeed);
        this.mOffsetY = pY;
    }
 
    @Override
    public void onManagedUpdate(float pSecondsElapsed){
     super.onManagedUpdate(pSecondsElapsed);
     this.mOffsetX -= this.mParallaxSpeed * pSecondsElapsed; 
    }
     
    @Override
    protected void onManagedDraw(GLState pGLState, Camera pCamera) {
     pGLState.pushModelViewGLMatrix();
        {
         final float shapeWidthScaled = this.getWidthScaled();//задаем область для рисования
         final float cameraWidth = pCamera.getWidth()*3;
            baseOffsetX = (this.mOffsetX * this.mParallaxSpeed)% shapeWidthScaled;
 
            while(baseOffsetX > 0) {
             baseOffsetX -= shapeWidthScaled;
   }
            pGLState.translateModelViewGLMatrixf(baseOffsetX, this.mOffsetY, 0);
             
            float currentMaxX = baseOffsetX;
    
   do {
     
    this.preDraw(pGLState, pCamera);
             this.draw(pGLState, pCamera);
             this.postDraw(pGLState, pCamera);
    pGLState.translateModelViewGLMatrixf(shapeWidthScaled, 0, 0);
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
