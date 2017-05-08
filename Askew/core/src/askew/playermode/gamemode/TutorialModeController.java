/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package askew.playermode.gamemode;

import askew.GlobalConfiguration;
import askew.InputController;
import askew.InputControllerManager;
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.obstacle.Obstacle;
import askew.entity.tree.Trunk;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.util.ArrayList;
import java.util.Collections;

import static askew.entity.sloth.SlothModel.*;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not grabbedAll with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class TutorialModeController extends GameModeController {

	private int MAX_TUTORIAL;

	private final int STAGE_PINNED = 1;
	private final int STAGE_GRAB = 2;
	private final int STAGE_SHIMMY = 3;
	private final int STAGE_FLING = 4;
	private final int STAGE_VINE = 5;
	private int currentStage = STAGE_PINNED;
	private boolean next = false;

	private final float CONTROLLER_DEADZONE = 0.15f;

	private boolean pressedA = false;
	private boolean prevRightGrab;
	private boolean prevLeftGrab;
	private boolean grabbedAll;

	private float time = 0f;

	private Animation joystickAnimation;
	private Animation bumperLAnimation;
	private Animation bumperRAnimation;
	private float elapseTime;

	// selected animation textures to be drawn
	TextureRegion joystickNeutralTexture;
	TextureRegion joystickTexture;
	TextureRegion bumperLTexture;
	TextureRegion bumperRTexture;
	Texture container;

	// list of objects for stage of tutorial
	protected ArrayList<Trunk> trunkEntities = new ArrayList<Trunk>();
	private ArrayList<Boolean> trunkGrabbed = new ArrayList<Boolean>();

	// margin allowance for measuring distance from setpoints
	private float[] inRangeAllowance = {0.02f, 0.02f, 0.02f, ARMSPAN*3/4, 0.02f};
	// list of setpoints for drawing helplines & other vars
	private int inRangeSetPt = -1;			// step progression within tutorial level
	private final int MINUS30 = -1;			// constant as signal for drawing help lines -30 degrees from moving arm
	private final int NEUTRAL = 0;			// constant as signal for drawing help lines when moving arm close to target
	private final int PLUS30 = 1;			// constant as signal for drawing help lines +30 degrees from moving arm
	private int targetLine = NEUTRAL;		// variable to store decision on what type of help line to draw
	private float angleDiff = 0f; 			// keeps track of (arm angle minus target angle) for sloth to draw
	private boolean swing = false; 			// does sloth have enough angular velocity to fling?
	private float[] grabSetPoints = {14.019997f, 11.73999f, 9.399997f, 7.0199966f, 4.720001f};
	private Vector2[] shimmySetPoints = {
			new Vector2(12f,14f),
			new Vector2(12f,9f),
			new Vector2(17f,9f),
			new Vector2(17f,14f),
			new Vector2(22.5f,14f) };
	private Vector2[] flingSetPoints = {
			new Vector2(2f,14f),
			new Vector2(9f,16f),
			new Vector2(16f, 14f),
			new Vector2(24f, 14f)  };
	private Vector2[] flingLandPoints0 = {
			new Vector2(-2f, 12f),
			new Vector2(5f, 14f),
			new Vector2(13f, 12f),
			new Vector2(19f, 11.5f)
	};
	private Vector2[] flingLandPointsf = {
			new Vector2(6.5f, 16f),
			new Vector2(12.5f, 14f),
			new Vector2(21.5f, 14f),
			new Vector2(26f, 17f)
	};
	private Vector2[] vineSetPoints = {};
	// list of instructions
	private boolean[] shimmyGrabbed = {false, false, false, false, false};
	private int[] shimmyDir = {SHIMMY_S, SHIMMY_E, SHIMMY_N, SHIMMY_E, SHIMMY_SE};
	private boolean[] flingGrabbed = {false, false, false, false};
	private int[] flingDir = {SHIMMY_NE, SHIMMY_SE, SHIMMY_E, SHIMMY_SE};

	/**
	 * Load the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(MantisAssetManager manager) {
		super.loadContent(manager);
		container = manager.get("texture/tutorial/infoContainer.png");
		// reset animation frames
		if(joystickAnimation == null) {
			joystickAnimation = new Animation(0.15f, manager.getTextureAtlas().findRegions("joy"), Animation.PlayMode.LOOP);
			bumperLAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperL"), Animation.PlayMode.LOOP);
			bumperRAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperR"), Animation.PlayMode.LOOP);
		}
		DEFAULT_LEVEL = "tutorial0";
		loadLevel = DEFAULT_LEVEL;
	}

	// Physics objects for the game
	/** Reference to the character avatar */

	public TutorialModeController() {
		super();
		currentStage = 0;
		trunkEntities.clear();
		MAX_TUTORIAL = GlobalConfiguration.getInstance().getAsInt("maxTutorial");
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		loadLevel = "tutorial"+currentStage;
		trunkEntities.clear();
		trunkGrabbed.clear();
		super.reset();
		time = 0;
		inRangeSetPt = -1;
		targetLine = NEUTRAL;
		angleDiff = 0f;
		swing = false;
		for(int i = 0; i < shimmyGrabbed.length; i++)
			shimmyGrabbed[i] = false;
		for(int i = 0; i < flingGrabbed.length; i++)
			flingGrabbed[i] = false;

		joystickTexture = joystickAnimation.getKeyFrame(0);
		bumperLTexture = bumperLAnimation.getKeyFrame(0);
		bumperRTexture = bumperRAnimation.getKeyFrame(0);
	}

	/**
	 * Lays out the game geography.
	 */
	@Override
	protected void populateLevel() {
		super.populateLevel();
		for(Entity e: objects) {
			if(e instanceof Trunk) {
				trunkEntities.add((Trunk)e);
				trunkGrabbed.add(false);
			}
		}

		if(currentStage == STAGE_PINNED) {
			slothList.get(0).pin(world);
			slothList.get(0).setPinned();
		}

	}

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}


		InputController input = InputControllerManager.getInstance().getController(0);
		if(next) {
			currentStage++;
			next = false;
			if (currentStage < MAX_TUTORIAL) {
				System.out.println("moving on");
				listener.exitScreen(this, EXIT_TL_TL);
				return false;
			} else {
				System.out.println("tutorial completed");
				GlobalConfiguration.getInstance().setCurrentLevel(1);
				listener.exitScreen(this, EXIT_TL_GM);
				return false;
			}
		}
		pressedA = input.didBottomButtonPress();
		return true;
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class askew.playermode.WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		super.update(dt);
		if (!paused) {
			elapseTime += dt;
			time = time+dt ;
			// TODO: move sloth movement in slothmodel

			switch(currentStage) {
				case STAGE_PINNED:
					if( (int)(time/3) %2 == 0) {
						slothList.get(0).getRightArm().setAngle((float)Math.PI);
					} else {
						slothList.get(0).getLeftArm().setAngle((float)Math.PI);
					}
					break;
				case STAGE_GRAB:
					grabbedAll = trunkGrabbed.get(0);
					for (int i = 0; i < trunkGrabbed.size(); i++) {
						grabbedAll = trunkGrabbed.get(i) && grabbedAll;
					}
					if(inRangeSetPt+1 >= grabSetPoints.length) { break; }
					Vector2 setpt = new Vector2(slothList.get(0).getX(),grabSetPoints[inRangeSetPt+1]);
					if (inRange(setpt)) {
						inRangeSetPt++;
					}
					break;
				case STAGE_SHIMMY:
					if(inRangeSetPt+1 >= shimmySetPoints.length) { break; }
					if (inRange(shimmySetPoints[inRangeSetPt+1])) {
						inRangeSetPt++;
					}
					if (inRangeSetPt >= 0 && !shimmyGrabbed[inRangeSetPt]) {
						shimmyGrabbed[inRangeSetPt] = checkGrabbedPt(shimmySetPoints[inRangeSetPt], shimmyDir[inRangeSetPt]);
					}
					break;
				case STAGE_FLING:
					if(inRangeSetPt+1 >= flingSetPoints.length) {
						angleDiff = 0f;
						targetLine = NEUTRAL;
						break;
					}
					System.out.println("fling setpoint in range "+inRange(flingSetPoints[inRangeSetPt+1]));
					if (inRange(flingSetPoints[inRangeSetPt+1]) && flingGrabbed[inRangeSetPt+1]) {
						inRangeSetPt++;
					}
					System.out.println("progression "+inRangeSetPt);
					if (inRangeSetPt < flingGrabbed.length-1 && !flingGrabbed[inRangeSetPt+1]) {
						inRange(flingLandPoints0[inRangeSetPt+1]);
						System.out.println("targetLINE "+targetLine);
						if(!setUpToFling()) {
							swing = readyToFling();
						}
						if (!swing) {
							flingGrabbed[inRangeSetPt + 1] = inRange(flingLandPointsf[inRangeSetPt + 1]);
							if (flingGrabbed[inRangeSetPt + 1]) {
								swing = false;
							}
						}
//						System.out.println(flingGrabbed[inRangeSetPt+1]);
					}
					break;
				case STAGE_VINE:
					break;
				default:
					System.err.println(currentStage);
			}
			if(moveToNextStage())
				next = true;
			prevLeftGrab = slothList.get(0).isActualLeftGrab();
			prevRightGrab = slothList.get(0).isActualRightGrab();
		}
	}

	public boolean checkGrabbedPt(Vector2 setpt, int dir) {
//		System.out.print("setpt: ("+setpt.x+","+setpt.y+")   ");
		Body rTarget, lTarget, tTarget, bTarget;
		Vector2 rtPos, ltPos, ttPos, btPos;
		boolean xrange = false;
		boolean yrange = false;
		if (dir == SHIMMY_E || dir == SHIMMY_SE || dir == SHIMMY_NE) {
			rTarget = slothList.get(0).getRightmostTarget();
			if (rTarget == null) { return false; }
			rtPos = rTarget.getPosition();
//			System.out.println("E: ("+rtPos.x+","+rtPos.y+")");

			if (rtPos.x-0.05 >= setpt.x) { xrange = true; }
			if (dir == SHIMMY_E && Math.abs(setpt.y-rtPos.y) <= 0.05) { yrange = true; }

		} else if (dir == SHIMMY_W || dir == SHIMMY_SW || dir == SHIMMY_NW) {
			lTarget = slothList.get(0).getLeftmostTarget();
			if (lTarget == null) { return false; }
			ltPos = lTarget.getPosition();
//			System.out.println("W: ("+ltPos.x+","+ltPos.y+")");

			if (ltPos.x+0.05 <= setpt.x) { xrange = true;}
			if (dir == SHIMMY_W && Math.abs(setpt.y-ltPos.y) <= 0.05) { yrange = true; }

		} else if (dir == SHIMMY_S || dir == SHIMMY_SE || dir == SHIMMY_SW){
			bTarget = slothList.get(0).getBottomTarget();
			if (bTarget == null) { return false; }
			btPos = bTarget.getPosition();
//			System.out.println("S: ("+btPos.x+","+btPos.y+")");

			if (btPos.y+0.05 <= setpt.y) { yrange = true; }
			if (dir == SHIMMY_S && Math.abs(setpt.x - btPos.x) <= 0.05) { xrange = true; }

		} else if (dir == SHIMMY_N || dir == SHIMMY_NE || dir == SHIMMY_NW) {
			tTarget = slothList.get(0).getTopTarget();
			if (tTarget == null) { return false; }
			ttPos = tTarget.getPosition();
//			System.out.println("N: ("+ttPos.x+","+ttPos.y+")");

			if (ttPos.y-0.05 >= setpt.y) { yrange = true; }
			if (dir == SHIMMY_N && Math.abs(setpt.x - ttPos.x) <= 0.05) { xrange = true; }
		}
		return xrange && yrange;
	}

	// checks if next set point is in range for changing arm help
	public boolean inRange(Vector2 setpt) {
		Body lTarget = slothList.get(0).getLeftTarget();
		Body rTarget = slothList.get(0).getRightTarget();
		Body lHand = slothList.get(0).getLeftHand();
		Body rHand = slothList.get(0).getRightHand();

		Vector2 lhPos = lHand.getPosition();
		Vector2 rhPos = rHand.getPosition();
		Vector2 grabPos = new Vector2();

		if (lTarget == null && rTarget == null) {
			return false;
		}
		Vector2 lPos, rPos;
		boolean xrange = false;
		boolean yrange = false;
		float tAngle = 0f;
		float aAngle = 0f;
		float diff;
		if (lTarget != null && rTarget != null) {
			// if both hands grabbing
			lPos = lTarget.getPosition();
			rPos = rTarget.getPosition();
			if(lPos.x > rPos.x) {
				// move rh
				tAngle = (setpt.cpy().sub(rPos).angle()+360)%360;
				aAngle = (lhPos.cpy().sub(rhPos).angle()+360) %360;

				xrange = Math.abs(setpt.x - lPos.x) <= ARMSPAN+inRangeAllowance[currentStage];
				yrange = Math.abs(setpt.y - lPos.y) <= ARMSPAN+inRangeAllowance[currentStage];
				grabPos = lPos;
			} else {
				// move lh
				tAngle = (setpt.cpy().sub(lPos).angle()+360)%360;
				aAngle = (rhPos.cpy().sub(lhPos).angle()+360) %360;

				xrange = Math.abs(setpt.x - rPos.x) <= ARMSPAN+inRangeAllowance[currentStage];
				yrange = Math.abs(setpt.y - rPos.y) <= ARMSPAN+inRangeAllowance[currentStage];
				grabPos = rPos;
			}
		}
		if (lTarget != null) {
			// move lh
			lPos = lTarget.getPosition();
			tAngle = (setpt.cpy().sub(lPos).angle()+360)%360;
			aAngle = (rhPos.cpy().sub(lhPos).angle()+360) %360;

			xrange = Math.abs(setpt.x - lPos.x) <= ARMSPAN+inRangeAllowance[currentStage];
			yrange = Math.abs(setpt.y - lPos.y) <= ARMSPAN+inRangeAllowance[currentStage];
			grabPos = lPos;
		}
		if (rTarget != null) {
			// move rh
			rPos = rTarget.getPosition();
			tAngle = (setpt.cpy().sub(rPos).angle()+360)%360;
			aAngle = (lhPos.cpy().sub(rhPos).angle()+360) %360;

			xrange = Math.abs(setpt.x - rPos.x) <= ARMSPAN+inRangeAllowance[currentStage];
			yrange = Math.abs(setpt.y - rPos.y) <= ARMSPAN+inRangeAllowance[currentStage];
			grabPos = rPos;
		}

		diff = (((aAngle - tAngle)%360)+360)%360;
		if (30 < diff && diff < 180) {
			targetLine = MINUS30;
		} else if (180 <= diff && diff < 330) {
			targetLine = PLUS30;
		} else {
			targetLine = NEUTRAL;
		}
		angleDiff = diff;
		checkCloseToCorner(setpt,grabPos);
		return xrange && yrange;
	}

	public void checkCloseToCorner(Vector2 setpt, Vector2 grabpt) {
		float xrange = (float) Math.abs(setpt.x - grabpt.x);
		float yrange = (float) Math.abs(setpt.y - grabpt.y);
		if(xrange < 0.08 && yrange < 0.08) {
			shimmyGrabbed[inRangeSetPt+1] = true;
		}
	}

	public boolean setUpToFling() {
		System.out.println("anglediff: "+angleDiff);
		return angleDiff < 0.05f;
	}

	public boolean readyToFling() {
		System.out.println("RH omega "+slothList.get(0).getRightHand().getAngularVelocity());
		return slothList.get(0).getRightHand().getAngularVelocity() > 400;
	}

	public boolean moveToNextStage() {
		if(currentStage == STAGE_PINNED) {
			return (time > 1.5f && pressedA);
		} else if (currentStage == STAGE_GRAB) {
			return (grabbedAll && pressedA);
		}else if(currentStage > STAGE_PINNED) {
			return owl.isDoingVictory();
		}
		return false;
	}

	public void drawHelpLines() {
		switch (currentStage) {
			case STAGE_PINNED:
				break;
			case STAGE_GRAB:
				if(!grabbedAll)
					slothList.get(0).drawHelpLines(canvas, camTrans, SHIMMY_S, 0f);
				break;
			case STAGE_SHIMMY:
			case STAGE_FLING:
				switch(targetLine) {
					case PLUS30:
						slothList.get(0).drawHelpLines(canvas, camTrans, PLUS_30, 0f);
						break;
					case MINUS30:
						slothList.get(0).drawHelpLines(canvas, camTrans, MINUS_30, 0f);
						break;
					case NEUTRAL:
						slothList.get(0).drawHelpLines(canvas, camTrans, DEFAULT, angleDiff);
						break;
				}
				break;
			case STAGE_VINE:
				break;
		}

	}

	public void drawInstructions() {
		joystickNeutralTexture = joystickAnimation.getKeyFrame(0);
		joystickTexture = joystickAnimation.getKeyFrame(elapseTime, true);
		bumperLTexture = bumperLAnimation.getKeyFrame(elapseTime,true);
		bumperRTexture = bumperRAnimation.getKeyFrame(elapseTime, true);

		canvas.draw(container, Color.WHITE, container.getWidth() / 2, 0, 425, 300, 0, worldScale.x * 5 / container.getWidth(), worldScale.y * 5 / container.getHeight());
		if(currentStage == STAGE_PINNED) {
			if((int)(time/3) %2 == 0){
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
			} else{
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
			}
		} else if(currentStage == STAGE_GRAB) {
			if (!grabbedAll) {
				canvas.drawTextCentered("Try to grab all 5 branches", displayFont, 200f);
			}
		} else if (currentStage == STAGE_SHIMMY) {
			canvas.drawTextCentered("Try to shimmy across", displayFont, 200f);
		}
		if (currentStage >= STAGE_GRAB && currentStage < STAGE_VINE) {
			if(slothList.get(0).isActualRightGrab()) {
				canvas.draw(bumperRTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
			} else {
				canvas.draw(bumperLTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
			}
//		} else if (currentStage == MOVED_LEFT) {
//		} else if (currentStage == MOVED_RIGHT) {
//			canvas.draw(bumperLTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//		} else if (currentStage == GRABBED_LEFT) {
//			canvas.draw(bumperRTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//		} else if (currentStage >= GRABBED_RIGHT) {
		}
		if((currentStage == STAGE_PINNED && time > 6f) ||
				(currentStage == STAGE_GRAB && grabbedAll)) {
			canvas.drawTextCentered("Press A to continue", displayFont, 200f);
		}
	}

	public void draw(float delta){
		// GameMode draw with changes
		canvas.clear();

		canvas.begin();
		canvas.draw(background);
		canvas.end();

		System.out.println("stage " + currentStage);
		InputController input =  InputControllerManager.getInstance().getController(0);

		camTrans.setToTranslation(-1 * slothList.get(0).getBody().getPosition().x * worldScale.x
				, -1 * slothList.get(0).getBody().getPosition().y * worldScale.y);
		camTrans.translate(canvas.getWidth()/2,canvas.getHeight()/2);
		canvas.getCampos().set( slothList.get(0).getBody().getPosition().x * worldScale.x
				, slothList.get(0).getBody().getPosition().y * worldScale.y);

		canvas.begin(camTrans);
		Collections.sort(objects);
		slothList.get(0).setTutorial();
		for(Entity obj : objects) {
			obj.setDrawScale(worldScale);
			// if stage 2, tint trunks if already grabbed
			if(currentStage == STAGE_GRAB && obj instanceof Trunk) {
				Trunk trunk = (Trunk) obj;
				int ind = trunkEntities.indexOf(obj);

				for(Obstacle plank: trunk.getBodies()){
					if(plank.getBody().getUserData() instanceof Obstacle && ((Obstacle)plank.getBody().getUserData()).isGrabbed()) {
						trunkGrabbed.set(ind,true);
					}
				}
			} else {
				obj.draw(canvas);
			}
		}

		// trunk tinting done here
		for (int i = 0; i <trunkEntities.size(); i++) {
			if (trunkGrabbed.get(i)) {
				(trunkEntities.get(i)).draw(canvas, Color.GRAY);
			} else {
				trunkEntities.get(i).draw(canvas);
			}
		}

		if (!playerIsReady && !paused && coverOpacity <= 0)
			printHelp();
		canvas.end();
		slothList.get(0).drawGrab(canvas, camTrans);

		drawHelpLines();

		canvas.begin();
		canvas.drawTextStandard("current time:    "+currentTime, 10f, 70f);
		canvas.drawTextStandard("record time:     "+recordTime,10f,50f);

		//Draw control schemes
		canvas.drawTextStandard(typeMovement, 10f, 700f);
		canvas.drawTextStandard(typeControl,10f,680f);
		canvas.end();

//		sloth.drawHelpLines();

		if (debug) {
			canvas.beginDebug(camTrans);
			for(Entity obj : objects) {
				if( obj instanceof Obstacle){
					((Obstacle)obj).drawDebug(canvas);
				}
			}
			canvas.endDebug();
			canvas.begin();
			// text
			canvas.drawTextStandard("FPS: " + 1f/delta, 10.0f, 100.0f);
			canvas.end();
			slothList.get(0).drawForces(canvas, camTrans);
		}

		// draw instructional animations
//		canvas.begin();
//		drawInstructions();
//		canvas.end();

		if (coverOpacity > 0) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			displayFont.setColor(Color.WHITE);
			Color coverColor = new Color(0,0,0,coverOpacity);
			canvas.drawRectangle(coverColor,0,0,canvas.getWidth(), canvas
					.getHeight());
			coverOpacity -= (1/CYCLES_OF_INTRO);
			Gdx.gl.glDisable(GL20.GL_BLEND);
			canvas.begin();
			if (!playerIsReady && !paused)
				canvas.drawTextCentered(levelModel.getTitle(), displayFont, 0f);
			canvas.end();
		}

		// draw pause menu stuff over everything
		if (paused) {
			canvas.begin();
			canvas.draw(pauseTexture);
			canvas.draw(fern, Color.WHITE,fern.getWidth()/2, fern.getHeight()/2,
					pause_locs[pause_mode].x * canvas.getWidth(), pause_locs[pause_mode].y* canvas.getHeight(),
					0,2*worldScale.x/fern.getWidth(), 2*worldScale.y/fern.getHeight());
			canvas.end();
		}

	}

	public void restart() {
		//change back to 0
		currentStage = 1;
	}

}
