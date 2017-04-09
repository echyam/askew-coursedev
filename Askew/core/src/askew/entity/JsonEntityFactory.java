package askew.entity;

import askew.MantisAssetManager;
import askew.entity.owl.OwlModel;
import askew.entity.tree.StiffBranch;
import askew.entity.tree.Trunk;
import askew.entity.sloth.SlothModel;
import askew.entity.vine.Vine;
import askew.entity.wall.WallModel;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Since we cannot create a static method on Entity to create an instance of a particular entity from a JSON instance,
 * we use the factory pattern to create such entities
 */
public class JsonEntityFactory {

    public static Vine createVine(MantisAssetManager manager, JsonObject instance, Vector2 scale) {
        Vine vine;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        Texture managedTexture = manager.get(Vine.VINE_TEXTURE, Texture.class);
        TextureRegion vineTexture = new TextureRegion(managedTexture);
		vine = new Vine(x, y, numlinks, vineTexture.getRegionHeight() / scale.x, vineTexture.getRegionHeight() / scale.y, scale);
		vine.setDrawScale(scale.x, scale.y);
		vine.setTextures(manager);
        return vine;
    }

    public static Trunk createTrunk(MantisAssetManager manager, JsonObject instance, Vector2 scale) {
        Trunk trunk;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        float stiff = instance.get("stiffLen").getAsFloat();
        trunk = new Trunk(x, y, numlinks, 0.25f, 1.0f, stiff,scale);
        trunk.setDrawScale(scale.x, scale.y);
        trunk.setTextures(manager);
        return trunk;
    }

    public static SlothModel createSloth(MantisAssetManager manager, JsonObject instance, Vector2 scale) {
        SlothModel ret;
        ret = new SlothModel(instance.get("x").getAsFloat(), instance.get("y").getAsFloat());
        ret.setDrawScale(scale.x, scale.y);
        ret.setTextures(manager);
        return ret;
    }

    public static StiffBranch createStiffBranch(MantisAssetManager manager, JsonObject instance, Vector2 scale) {
        StiffBranch branch;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float stiff = instance.get("stiffLen").getAsFloat();
        branch = new StiffBranch(x, y, stiff, 0.25f, 0.1f,scale);
        branch.setDrawScale(scale.x, scale.y);
        branch.setTextures(manager);
        return branch;
    }

    public static OwlModel createOwl(MantisAssetManager manager, JsonObject instance, Vector2 scale) {
        OwlModel owl;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        owl = new OwlModel(x, y);
        owl.setDrawScale(scale.x, scale.y);
        owl.setTextures(manager);
        return owl;
    }

    public static WallModel createWall(MantisAssetManager manager, JsonObject instance, Vector2 scale) {
        WallModel wall;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        List<Float> points = new ArrayList<>();
        instance.get("points").getAsJsonArray().forEach(pt->points.add(pt.getAsFloat()));
        Float[] arrayPoints = points.toArray(new Float[points.size()]);
        float[] copy = new float[arrayPoints.length];
        for (int i = 0; i < arrayPoints.length; i++) {
            copy[i] = arrayPoints[i];
        }
        wall = new WallModel(x, y, copy);
        wall.setDrawScale(scale.x, scale.y);
        wall.setTextures(manager);
        return wall;
    }
}