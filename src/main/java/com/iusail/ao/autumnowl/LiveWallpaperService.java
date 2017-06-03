package com.iusail.ao.autumnowl;


import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.particle.BatchedPseudoSpriteParticleSystem;
import org.andengine.entity.particle.emitter.RectangleParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.ScaleParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ColorParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.RotationParticleModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.view.ConfigChooser;
import org.andengine.opengl.view.EngineRenderer;
import org.andengine.opengl.view.IRendererListener;

import javax.microedition.khronos.opengles.GL10;

public class LiveWallpaperService extends BaseLiveWallpaperService implements
		 IOffsetsChanged, SharedPreferences.OnSharedPreferenceChangeListener {
	private static  int CAMERA_WIDTH ;
	private static  int CAMERA_HEIGHT ;
	//private static  int width;
	//private static  int height;
	//private static  float height_tmp;
	private static  float pos_ = 0;
	//private static  float width_tmp;
	//private static  int width_sprite;
	private static int MODE = 0;

	//private static int MODE_TMP = 0;
	private SharedPreferences mSharedPreferences;
	public final static String PREFERENCES = "LiveWallpaperSettings;";
	public static float ratio;
	public static float ratio2;
	private static boolean render = true;
	private IOffsetsChanged mOffsetsChangedListener = null;
	private Camera mCamera;
	private Scene mScene;


	//флаги для настроек пользователя
	private boolean tuman1_visible;
	private boolean tuman2_visible;

	Sprite backgroundpre;
	BitmapTextureAtlas backgroundpreTexture;
	ITextureRegion backgroundpreRegion;

	BitmapTextureAtlas mRayTextureAtlas;
	ITextureRegion mRayRegion;

	Sprite background;
	BitmapTextureAtlas backgroundTexture;
	ITextureRegion backgroundRegion;

	Sprite owl;
	BitmapTextureAtlas owlTexture;
	ITextureRegion owlRegion;

    BitmapTextureAtlas mLeaf1Particle;
    ITextureRegion mLeaf1ParticleRegion;

	BitmapTextureAtlas mLeaf2Particle;
	ITextureRegion mLeaf2ParticleRegion;

	BitmapTextureAtlas mLeaf3Particle;
	ITextureRegion mLeaf3ParticleRegion;

	BitmapTextureAtlas mLeaf4Particle;
	ITextureRegion mLeaf4ParticleRegion;

	BitmapTextureAtlas mGlowParticle;
	ITextureRegion mGlowParticleRegion;

	ParallaxSpriteGorizontal spriteRay;

    private VelocityParticleInitializer<Sprite> mVelocityParticleInitializer;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pSharedPrefs,
			String pKey) {;
		if (pSharedPrefs.getBoolean(LiveWallpaperSettings.H_ENABLED_CHECKBOX_KEY, true)){
			tuman1_visible = true;
		} else {
			tuman1_visible = false;
		}
		if (pSharedPrefs.getBoolean(LiveWallpaperSettings.SF_ENABLED_CHECKBOX_KEY, true)){
			tuman2_visible = true;
		} else {
			tuman2_visible = false;
		}

	}

	public void initializePreferences() {
		mSharedPreferences = LiveWallpaperService.this.getSharedPreferences(
				PREFERENCES, 0);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(mSharedPreferences, null);
	}

    //эффект параллакса
	@Override
	public void offsetsChanged(float xOffset, float yOffset, float xOffsetStep,
			float yOffsetStep, int xPixelOffset, int yPixelOffset) {
				pos_ = xOffset;
	}

	//Обрабатываем переориентацию экрана
	@Override
	public void onSurfaceChanged(final GLState pGLState, final int pWidth, final int pHeight) {
		super.onSurfaceChanged(pGLState, pWidth, pHeight);
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		wm.getDefaultDisplay().getRotation();
		CAMERA_WIDTH = pWidth;
		CAMERA_HEIGHT = pHeight;
        // весь предыдущий код нужен для того что бы узнать новые размеры окна
		if (pWidth>pHeight){
			MODE = 1;
		}
		else{
			MODE = 0;
		}
		this.mEngine.getCamera().set(0, 0, pWidth, pHeight); // это и есть та самая строчка
	}

	// Вычисление пропорции для изображений
	public float GetTrueProportion(int width, int height, float imgWidth, float imgHeight) {
		// Вычисление пропорций для правильного отображения картинок
		float ratio = 0;
		if (imgWidth < imgHeight) {
			ratio = (float) width / imgWidth;
		} else{
			ratio = (float) height / imgHeight;
		}
		return ratio;
	}

	@Override
	public EngineOptions onCreateEngineOptions() {

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		wm.getDefaultDisplay().getRotation();
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;


		if (CAMERA_WIDTH>CAMERA_HEIGHT){

    //Mode =1 = landscape mode
    MODE = 1;
    //MODE_TMP = 1;
    this.mCamera = new Camera(0, 0, CAMERA_HEIGHT, CAMERA_WIDTH);
            return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_HEIGHT, CAMERA_WIDTH), mCamera);
    }
    else

            //Mode =0 = portrait mode
    MODE = 0;
	//MODE_TMP = 0;
    this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
            return new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);

		}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		initializePreferences();
		//откуда выгружаем картинки...
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		backgroundpreTexture = new BitmapTextureAtlas(this.getTextureManager(), 2000, 1125, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		backgroundpreRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundpreTexture, this, "1prebackground.jpg", 0,0);
		backgroundpreTexture.load();
		this.getEngine().getTextureManager().loadTexture(this.backgroundpreTexture);

		mRayTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 2000, 1125, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mRayRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mRayTextureAtlas, this, "2parallaxray.png" , 0, 0);
		mRayTextureAtlas.load();

		backgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 2000, 1125, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		backgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundTexture, this, "3background.png", 0,0);
		backgroundTexture.load();
		this.getEngine().getTextureManager().loadTexture(this.backgroundTexture);

		owlTexture = new BitmapTextureAtlas(this.getTextureManager(), 2000, 1125, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		owlRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.owlTexture, this, "5owl.png", 0,0);
		owlTexture.load();
		this.getEngine().getTextureManager().loadTexture(this.owlTexture);

		mLeaf1Particle = new BitmapTextureAtlas(this.getTextureManager(), 198, 216, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mLeaf1ParticleRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mLeaf1Particle, this, "4leaf.png", 0, 0);
		mEngine.getTextureManager().loadTexture(this.mLeaf1Particle);

		mLeaf3Particle = new BitmapTextureAtlas(this.getTextureManager(), 198, 216, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mLeaf3ParticleRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mLeaf3Particle, this, "4leaf2.png", 0, 0);
		mEngine.getTextureManager().loadTexture(this.mLeaf3Particle);

		mLeaf4Particle = new BitmapTextureAtlas(this.getTextureManager(), 198, 216, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mLeaf4ParticleRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mLeaf4Particle, this, "4leaf3.png", 0, 0);
		mEngine.getTextureManager().loadTexture(this.mLeaf4Particle);

		mLeaf2Particle = new BitmapTextureAtlas(this.getTextureManager(), 230, 230, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mLeaf2ParticleRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mLeaf2Particle, this, "6leafblur.png", 0, 0);
		mEngine.getTextureManager().loadTexture(this.mLeaf2Particle);

		mGlowParticle = new BitmapTextureAtlas(this.getTextureManager(), 25, 25, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mGlowParticleRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mGlowParticle, this, "glow1.png", 0, 0);
		mEngine.getTextureManager().loadTexture(this.mGlowParticle);

		//завершили загрузку
		pOnCreateResourcesCallback.onCreateResourcesFinished();

	}
	//создание сцены, здесь создаются спрайты и выводятся по координатам на сцену
	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		this.mEngine.registerUpdateHandler(new FPSLogger() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				//проверяем настройки пользователя
				//скрываем листья или делаем их видимыми
//				mScene.getChildByIndex(4).setVisible(leaves_visible);
				if (MODE == 1) {
                    backgroundpre.setPosition(-(pos_) * (backgroundpre.getWidth() - CAMERA_WIDTH)*2/3 ,(CAMERA_HEIGHT - backgroundpre.getHeight())/2);
					spriteRay.setPosition(-(pos_) * (spriteRay.getWidth() - CAMERA_WIDTH)*2/3 ,(CAMERA_HEIGHT - spriteRay.getHeight())/2);
					background.setPosition(-(pos_) * (background.getWidth() - CAMERA_WIDTH)*2/3 ,(CAMERA_HEIGHT - background.getHeight())/2);
					owl.setPosition(-(pos_) * (owl.getWidth() - CAMERA_WIDTH) ,(CAMERA_HEIGHT - owl.getHeight())/2);
				} else if (MODE == 0) {
                    backgroundpre.setPosition(-(pos_) * (backgroundpre.getWidth() - CAMERA_WIDTH)*2/3 ,(CAMERA_HEIGHT - backgroundpre.getHeight()));
					spriteRay.setPosition(-(pos_) * (spriteRay.getWidth() - CAMERA_WIDTH)*2/3 ,(CAMERA_HEIGHT - spriteRay.getHeight()));
					background.setPosition(-(pos_) * (background.getWidth() - CAMERA_WIDTH)*2/3 ,(CAMERA_HEIGHT - background.getHeight()));
					owl.setPosition(-(pos_) * (owl.getWidth() - CAMERA_WIDTH) ,(CAMERA_HEIGHT - owl.getHeight()));
				}
			}

			@Override
            public void reset() {
			}
		});
		mScene = new Scene();

		backgroundpre = new Sprite(0,0,this.backgroundpreRegion,getVertexBufferObjectManager())
		{
			@Override
			protected void draw(GLState pGLState, Camera pCamera) {
				super.draw(pGLState, pCamera);
				pGLState.enableDither();

			}
		}

		;

		if (MODE == 0){
			ratio = GetTrueProportion(this.CAMERA_WIDTH, this.CAMERA_HEIGHT, backgroundpre.getWidth(), backgroundpre.getHeight());
		}
		else if (MODE == 1){
			ratio = GetTrueProportion(this.CAMERA_HEIGHT, this.CAMERA_WIDTH, backgroundpre.getWidth(), backgroundpre.getHeight());
		}

		backgroundpre.setSize(backgroundpre.getWidth() * ratio, backgroundpre.getHeight() * ratio*12f/11f);
		backgroundpre.setPosition((this.CAMERA_WIDTH - backgroundpre.getWidth()) / 0, 0);
		backgroundpre.setPosition(0, this.CAMERA_HEIGHT - backgroundpre.getHeight());
		mScene.attachChild(backgroundpre);

		spriteRay = new ParallaxSpriteGorizontal(0, 0, 8f, mRayRegion, getVertexBufferObjectManager());
		spriteRay.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		//	spriteRay.registerEntityModifier(new AlphaModifier(245, 245, 245));

		if (MODE==0){
			ratio = GetTrueProportion(this.CAMERA_WIDTH, this.CAMERA_HEIGHT, spriteRay.getWidth(), spriteRay.getHeight());
		}
		else if (MODE==1){
			ratio = GetTrueProportion(this.CAMERA_HEIGHT, this.CAMERA_WIDTH, spriteRay.getWidth(), spriteRay.getHeight());
		}
		spriteRay.setSize(spriteRay.getWidth() * ratio, spriteRay.getHeight() * ratio);
		spriteRay.setPosition((this.CAMERA_WIDTH - spriteRay.getWidth()) / 0, 0);
		spriteRay.setPosition(0, this.CAMERA_HEIGHT - spriteRay.getHeight());
		mScene.attachChild(spriteRay);

		background = new Sprite(0,0,this.backgroundRegion,getVertexBufferObjectManager())
		{
			 @Override
			protected void draw(GLState pGLState, Camera pCamera) {
				super.draw(pGLState, pCamera);
				pGLState.enableDither();

             }
		}

        ;

		if (MODE == 0){
			ratio = GetTrueProportion(this.CAMERA_WIDTH, this.CAMERA_HEIGHT, background.getWidth(), background.getHeight());
		}
		else if (MODE == 1){
			ratio = GetTrueProportion(this.CAMERA_HEIGHT, this.CAMERA_WIDTH, background.getWidth(), background.getHeight());
        }
		background.setSize(background.getWidth() * ratio, background.getHeight() * ratio*12f/11f);
		// Перемещаем фон на правильную позицию
		background.setPosition((this.CAMERA_WIDTH - background.getWidth()) / 0, 0);
		background.setPosition(0, this.CAMERA_HEIGHT - background.getHeight());
		mScene.attachChild(background);

		//////////////////////////////////////////////////////////////////////
		owl = new Sprite(0,0,this.owlRegion,getVertexBufferObjectManager())
		{
			@Override
			protected void draw(GLState pGLState, Camera pCamera) {
				super.draw(pGLState, pCamera);
				pGLState.enableDither();

			}
		}

		;

		if (MODE == 0){
			ratio = GetTrueProportion(this.CAMERA_WIDTH, this.CAMERA_HEIGHT, owl.getWidth(), owl.getHeight());
			ratio2 = GetTrueProportion(this.CAMERA_WIDTH, this.CAMERA_HEIGHT, owl.getWidth(), owl.getHeight()*10);
		}
		else if (MODE == 1){
			ratio = GetTrueProportion(this.CAMERA_HEIGHT, this.CAMERA_WIDTH, owl.getWidth(), owl.getHeight());
			ratio2 = GetTrueProportion(this.CAMERA_HEIGHT, this.CAMERA_WIDTH, owl.getWidth(), owl.getHeight()*4/3);
		}

		if (MODE == 0){
			owl.setSize(owl.getWidth() * ratio, owl.getHeight() * ratio*12f/11);
		}
		else if (MODE == 1){
			owl.setSize(owl.getWidth() * ratio2, owl.getHeight() * ratio2*12f/11);
		}
		owl.setPosition((this.CAMERA_WIDTH - owl.getWidth()) / 0, 0);
		owl.setPosition(0, this.CAMERA_HEIGHT - owl.getHeight());
		mScene.attachChild(owl);

		final BatchedPseudoSpriteParticleSystem particleSystem = new BatchedPseudoSpriteParticleSystem(
				new RectangleParticleEmitter(background.getWidth()/ 2, -200, background.getWidth(), 50), 1/2, 1, 25, this.mLeaf1ParticleRegion, this.getVertexBufferObjectManager());
		particleSystem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		particleSystem.addParticleInitializer(new VelocityParticleInitializer<Entity>(-2, -1, 1, 3));
		particleSystem.addParticleInitializer(new AccelerationParticleInitializer<Entity>(1f, 3f, 3f, 7f));
		particleSystem.addParticleInitializer(new RotationParticleInitializer<Entity>(20, 1000));
		particleSystem.addParticleInitializer(new ScaleParticleInitializer<Entity>(0.3f, (float) CAMERA_WIDTH/1500));
		particleSystem.addParticleInitializer(new ScaleParticleInitializer<Entity>((float) CAMERA_WIDTH/3500, (float) CAMERA_WIDTH/2000));
		particleSystem.addParticleModifier(new AlphaParticleModifier<Entity>(0f, 200f, 1.0f, 0.7f));
		particleSystem.addParticleInitializer(new ExpireParticleInitializer<Entity>(40f));
		owl.attachChild(particleSystem);

		final BatchedPseudoSpriteParticleSystem particle5System = new BatchedPseudoSpriteParticleSystem(
				new RectangleParticleEmitter(background.getWidth()/ 2, -200, background.getWidth(), 50), 1/4, 1, 25, this.mLeaf3ParticleRegion, this.getVertexBufferObjectManager());
		particle5System.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		particle5System.addParticleInitializer(new VelocityParticleInitializer<Entity>(-2, -1, 1, 3));
		particle5System.addParticleInitializer(new AccelerationParticleInitializer<Entity>(1f, 3f, 3f, 7f));
		particle5System.addParticleInitializer(new RotationParticleInitializer<Entity>(20, 1000));
		particle5System.addParticleInitializer(new ScaleParticleInitializer<Entity>(0.3f, (float) CAMERA_WIDTH/1500));
		particle5System.addParticleInitializer(new ScaleParticleInitializer<Entity>((float) CAMERA_WIDTH/3500, (float) CAMERA_WIDTH/2000));
		particle5System.addParticleModifier(new AlphaParticleModifier<Entity>(0f, 200f, 1.0f, 0.7f));
		particle5System.addParticleInitializer(new ExpireParticleInitializer<Entity>(40f));
		owl.attachChild(particle5System);

		final BatchedPseudoSpriteParticleSystem particle4System = new BatchedPseudoSpriteParticleSystem(
				new RectangleParticleEmitter(background.getWidth()/ 2, -200, background.getWidth(), 50), 1/4, 1, 25, this.mLeaf4ParticleRegion, this.getVertexBufferObjectManager());
		particle4System.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		particle4System.addParticleInitializer(new VelocityParticleInitializer<Entity>(-2, -1, 1, 3));
		particle4System.addParticleInitializer(new AccelerationParticleInitializer<Entity>(1f, 3f, 3f, 7f));
		particle4System.addParticleInitializer(new RotationParticleInitializer<Entity>(20, 1000));
		particle4System.addParticleInitializer(new ScaleParticleInitializer<Entity>(0.3f, (float) CAMERA_WIDTH/1500));
		particle4System.addParticleInitializer(new ScaleParticleInitializer<Entity>((float) CAMERA_WIDTH/3500, (float) CAMERA_WIDTH/2000));
		particle4System.addParticleModifier(new AlphaParticleModifier<Entity>(0f, 200f, 1.0f, 0.7f));
		particle4System.addParticleInitializer(new ExpireParticleInitializer<Entity>(40f));
		owl.attachChild(particle4System);

		final BatchedPseudoSpriteParticleSystem particle2System = new BatchedPseudoSpriteParticleSystem(
				new RectangleParticleEmitter(background.getWidth()/ 2,-150, background.getWidth(), 50), 1/4, 1, 10, this.mLeaf2ParticleRegion, this.getVertexBufferObjectManager());
		particle2System.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		particle2System.addParticleInitializer(new VelocityParticleInitializer<Entity>(-3, -2, 1, 2));
		particle2System.addParticleInitializer(new AccelerationParticleInitializer<Entity>(0f, 3f, 3f, 7f));
		particle2System.addParticleInitializer(new RotationParticleInitializer<Entity>(20, 1000));
		particle2System.addParticleInitializer(new ScaleParticleInitializer<Entity>(0.4f, (float) CAMERA_WIDTH/3000));
		particle2System.addParticleInitializer(new ScaleParticleInitializer<Entity>((float) CAMERA_WIDTH/6000, (float) CAMERA_WIDTH/2500));
		particle2System.addParticleModifier(new AlphaParticleModifier<Entity>(10f, 10f, 0.7f, 0.7f));
		particle2System.addParticleInitializer(new ExpireParticleInitializer<Entity>(40f));
		background.attachChild(particle2System);

		final BatchedPseudoSpriteParticleSystem particle3System = new BatchedPseudoSpriteParticleSystem(
				new RectangleParticleEmitter(background.getWidth()/ 2, background.getHeight()+20, background.getWidth(), 50), 1, 2, 400, this.mGlowParticleRegion, this.getVertexBufferObjectManager());
		particle3System.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
		particle3System.addParticleInitializer(new AccelerationParticleInitializer<Entity>(-1f, -1f, -1f, -2f));
		particle3System.addParticleInitializer(new RotationParticleInitializer<Entity>(20, 1000));
		particle3System.addParticleInitializer(new ScaleParticleInitializer<Entity>((float) CAMERA_WIDTH/1800, (float) CAMERA_WIDTH/2300));
		particle3System.addParticleInitializer(new ScaleParticleInitializer<Entity>((float) CAMERA_WIDTH/2400, (float) CAMERA_WIDTH/1900));
		particle3System.addParticleModifier(new ColorParticleModifier<Entity>(1, 2500, 95f, 75f, 210f, 80f, 50f, 105f));
		particle3System.addParticleModifier(new AlphaParticleModifier<Entity>(1f, 2f, 0.002f, 0.001f));
		particle3System.addParticleInitializer(new RegisterXSwingEntityModifierInitializer<Entity>(50f, 100f, (float) Math.PI * 16 , 100f, 200f, true));
		particle3System.addParticleInitializer(new ExpireParticleInitializer<Entity>(60f));
		background.attachChild(particle3System);

		/////////////////////////////////////////////////////////////////////

		//завершаем загрузку на сцену
		pOnCreateSceneCallback.onCreateSceneFinished(mScene);
	}

    @Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public WallpaperService.Engine onCreateEngine() {
		return new MyBaseWallpaperGLEngine(this);
	}

	@Override
	public void onOffsetsChanged(float xOffset, float yOffset,
			float xOffsetStep, float yOffsetStep, int xPixelOffset,
			int yPixelOffset) {

		super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
				xPixelOffset, yPixelOffset);

		if (this.mOffsetsChangedListener != null)
			this.mOffsetsChangedListener.offsetsChanged(xOffset, yOffset, xOffsetStep,
					yOffsetStep, xPixelOffset, yPixelOffset);


	}

	protected class MyBaseWallpaperGLEngine extends GLEngine {

		private EngineRenderer mEngineRenderer;
		private ConfigChooser mConfigChooser;


		public MyBaseWallpaperGLEngine(final IRendererListener pRendererListener) {

			if (this.mConfigChooser == null) {
				LiveWallpaperService.this.mEngine.getEngineOptions()
						.getRenderOptions().setMultiSampling(false);
				this.mConfigChooser = new ConfigChooser(
						LiveWallpaperService.this.mEngine.getEngineOptions()
								.getRenderOptions().isMultiSampling());
			}
			this.setEGLConfigChooser(this.mConfigChooser);

			this.mEngineRenderer = new EngineRenderer(
					LiveWallpaperService.this.mEngine, this.mConfigChooser,
					pRendererListener);
			this.setRenderer(this.mEngineRenderer);
			this.setRenderMode(GLEngine.RENDERMODE_CONTINUOUSLY);
		}

		@Override
		public Bundle onCommand(final String pAction, final int pX,
				final int pY, final int pZ, final Bundle pExtras,
				final boolean pResultRequested) {
			if (pAction.equals(WallpaperManager.COMMAND_TAP)) {
				// LiveWallpaperService.this.onTap(pX, pY);
			} else if (pAction.equals(WallpaperManager.COMMAND_DROP)) {
				// LiveWallpaperService.this.onDrop(pX, pY);
			}

			return super.onCommand(pAction, pX, pY, pZ, pExtras,
					pResultRequested);
		}

		@Override
		public void onOffsetsChanged(final float pXOffset,
				final float pYOffset, final float pXOffsetStep,
				final float pYOffsetStep, final int pXPixelOffset,
				final int pYPixelOffset) {
			LiveWallpaperService.this.offsetsChanged(pXOffset, pYOffset,
					pXOffsetStep, pYOffsetStep, pXPixelOffset, pYPixelOffset);
		}

		@Override
		public void onResume() {
			super.onResume();
			LiveWallpaperService.this.onResume();
		}

		@Override
		public void onPause() {
			super.onPause();
			LiveWallpaperService.this.onPause();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			this.mEngineRenderer = null;
		}
	}

	protected class WallpaperEngine extends BaseWallpaperGLEngine {

        public WallpaperEngine(IRendererListener pRendererListener) {
            super(pRendererListener);
            this.setRenderMode(GLEngine.RENDERMODE_WHEN_DIRTY);
            startRenderThread();

        }

        private void startRenderThread() {
            render = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while (render) {
                        requestRender();
                    }
                }
            };
            new Thread(runnable).start();
        }




             @Override
        public void onVisibilityChanged(boolean pVisibility) {
            if (!pVisibility)
            {
                render = false;
            }

           else {
                startRenderThread();
            }
        }

    }
}
