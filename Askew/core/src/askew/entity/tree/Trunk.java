/*
 * RopeBridge.java
 *
 * The class is a classic example of how to subclass ComplexPhysicsObject.
 * You have to implement the createJoints() method to stick in all of the
 * joints between objects.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package askew.entity.tree;

import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import askew.entity.obstacle.*;

/**
 * A tree trunk with planks connected by weld joints.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Trunk extends ComplexObstacle {

	private static final String VINE_NAME = "plank";			/** The debug name for the entire obstacle */
	private static final String PLANK_NAME = "driftwood";		/** The debug name for each plank */
	private static final String TRUNK_PIN_NAME = "pin";			/** The debug name for each anchor pin */
	private static final float TRUNK_PIN_RADIUS = 0.1f;			/** The radius of each anchor pin */
	private static final float BASIC_DENSITY = 13f;				/** The density of each plank in the bridge */

	private float x,y,stiffLen; 								/** starting coords of bottom anchor and length for branch */
	public transient Vector2 final_norm = null;					/** coords for starting branch off this trunk */

	// Invisible anchor objects
	private transient WheelObstacle start = null;				/** The bottom of the trunk */
	private transient WheelObstacle finish = null;				/** The top of the trunk */
	public transient static final float DAMPING_ROTATION = 5f;	/** Set damping constant for joint rotation in vines */

	// Dimension information
	protected transient Vector2 dimension;						/** The size of the entire bridge */
	protected transient Vector2 planksize;						/** The size of a single plank */
	protected transient float linksize = 1.0f;					/** The length of each link */
	// TODO: Fix this from being public (refactor artifact) ?
	private transient float spacing = 0.0f;						/** The spacing between each link */

	protected float numLinks;									// param for json constructor

	/**
	 * Creates a new tree trunk at the given position.
	 *
	 * This trunk is default vertical, but you can set an angle in degrees.
	 * The top planks designated by stiffLen will not be created as part of the trunk.
	 *
	 * @param x  		The x position of the left anchor
	 * @param y  		The y position of the left anchor
	 * @param length	The length of the trunk
	 * @param lwidth	The plank thickness
	 * @param lheight	The plank length
	 *
	 *
	 */
	public Trunk(float x, float y, float length, float lwidth, float lheight, float stiffLen, Vector2 scale) {
		this(x, y, x, y+length, lwidth*scale.y/32f, lheight*scale.y/32f, stiffLen, 0f);
		numLinks = length;
		this.x = x;
		this.y = y;
	}

	public Trunk(float x, float y, float length, float lwidth, float lheight, float stiffLen, Vector2 scale, float angle) {
		this(x, y, x, y+length, lwidth*scale.y/32f, lheight*scale.y/32f, stiffLen, angle);
		numLinks = length;
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new tree trunk with the given anchors and other params.
	 *
	 * @param x0  		The x position of the left anchor
	 * @param y0  		The y position of the left anchor
	 * @param x1  		The x position of the right anchor
	 * @param y1  		The y position of the right anchor
	 * @param lwidth	The plank thickness
	 * @param lheight	The plank length
	 */
	public Trunk(float x0, float y0, float x1, float y1, float lwidth, float lheight, float stiffLen, float angle) {
		super(x0,y0);
		this.x = x0;	this.y = y0;	this.stiffLen = stiffLen;
		setName(VINE_NAME);

		planksize = new Vector2(lwidth,lheight);
		linksize = planksize.y;

		// Compute the bridge length
		dimension = new Vector2(x1-x0,y1-y0);
		float length = dimension.len();
		Vector2 norm = new Vector2(dimension);
		norm.nor();
		norm.rotate(angle);

		// If too small, only make one plank.
		int nLinks = (int)(length / linksize);
		if (nLinks <= 1) {
			nLinks = 1;
			linksize = length;
			spacing = 0;
		} else {
			spacing = length - nLinks * linksize;
			spacing /= (nLinks-1);
		}

		// Create the planks
		planksize.y = linksize;
		Vector2 pos = new Vector2();
		for (int ii = 0; ii < nLinks-stiffLen; ii++) {
			float t = ii*(linksize+spacing) + linksize/2.0f;
			pos.set(norm);
			pos.scl(t);
			pos.add(x0,y0);
			BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
			plank.setName(PLANK_NAME+ii);
			plank.setDensity(BASIC_DENSITY);
			bodies.add(plank);
		}
		final_norm = new Vector2(pos);
		final_norm.add(0,linksize/2);
	}

	/**
	 * Creates the joints for this object.
	 *
	 * This method is executed as part of activePhysics. This is the primary method to
	 * override for custom physics objects.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world) {
		assert bodies.size > 0;

		Vector2 anchor1 = new Vector2();
		Vector2 anchor2 = new Vector2(0, -linksize / 2);

		// Create the bottom anchor
		// Normally, we would do this in constructor, but we have
		// reasons to not add the anchor to the bodies list.
		Vector2 pos = bodies.get(0).getPosition();
		pos.y -= linksize / 2;
		start = new WheelObstacle(pos.x,pos.y,TRUNK_PIN_RADIUS);
		start.setName(TRUNK_PIN_NAME+0);
		start.setDensity(BASIC_DENSITY);
		start.setBodyType(BodyDef.BodyType.StaticBody);
		start.activatePhysics(world);

		// Definition for a revolute joint
		WeldJointDef jointDef = new WeldJointDef();

		// Initial joint
		// uncomment section to stand up
		// comment section to fall over
		jointDef.bodyA = start.getBody();
		jointDef.bodyB = bodies.get(0).getBody();
		jointDef.localAnchorA.set(anchor1);
		jointDef.localAnchorB.set(anchor2);
		jointDef.collideConnected = false;
		Joint joint = world.createJoint(jointDef );
		joints.add(joint);

		// uncomment to fall over
		// comment to stand up
//		RevoluteJointDef flexJointDef = new RevoluteJointDef();
//		flexJointDef.bodyA = start.getBody();
//		flexJointDef.bodyB = bodies.get(0).getBody();
//		flexJointDef.localAnchorA.set(anchor1);
//		flexJointDef.localAnchorB.set(anchor2);
//		flexJointDef.collideConnected = false;
//		Joint joint = world.createJoint(flexJointDef);
//		joints.add(joint);

		//Joint joint;
		// Link the planks together
		anchor1.y = linksize / 2;
		for (int ii = 0; ii < bodies.size-1; ii++) {
			// join the planks
			jointDef = new WeldJointDef();
			jointDef.bodyA = bodies.get(ii).getBody();
			jointDef.bodyB = bodies.get(ii+1).getBody();
			jointDef.localAnchorA.set(anchor1);
			jointDef.localAnchorB.set(anchor2);
			jointDef.collideConnected = false;
			joint = world.createJoint(jointDef);
			joints.add(joint);
		}

		// Create the top anchor
		Obstacle last = bodies.get(bodies.size-1);

		pos = last.getPosition();
		pos.y += linksize / 2;
		finish = new WheelObstacle(pos.x,pos.y,TRUNK_PIN_RADIUS);
		finish.setName(TRUNK_PIN_NAME+1);
		finish.setDensity(BASIC_DENSITY);
		finish.setBodyType(BodyDef.BodyType.StaticBody);
		finish.activatePhysics(world);

		// Final joint
		anchor2.y = 0;
		jointDef.bodyA = last.getBody();
		jointDef.bodyB = finish.getBody();
		jointDef.localAnchorA.set(anchor1);
		jointDef.localAnchorB.set(anchor2);
		joint = world.createJoint(jointDef);
		joints.add(joint);

		return true;
	}

	public float getLinksize() {return linksize;}

	/**
	 * Destroys the physics Body(s) of this object if applicable,
	 * removing them from the world.
	 *
	 * @param world Box2D world that stores body
	 */
	public void deactivatePhysics(World world) {
		super.deactivatePhysics(world);
		if (start != null) {
			start.deactivatePhysics(world);
		}
	}

	/**
	 * Returns the texture for the individual planks
	 *
	 * @return the texture for the individual planks
	 */
	public TextureRegion getTexture() {
		if (bodies.size == 0) {
			return null;
		}
		return ((SimpleObstacle) bodies.get(0)).getTexture();
	}

	@Override
	public void setTextures(MantisAssetManager manager) {
		Texture managedTexture = manager.get("texture/branch/branch.png", Texture.class);
		TextureRegion regionTexture = new TextureRegion(managedTexture);
		for(Obstacle body : bodies) {
			((SimpleObstacle)body).setTexture(regionTexture);
		}
	}
}