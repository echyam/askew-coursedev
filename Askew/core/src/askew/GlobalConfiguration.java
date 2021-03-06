package askew;

import askew.util.json.JSONLoaderSaver;
import com.google.gson.JsonObject;

/**
 * A model class containing variables which we set as configurable for faster prototyping and modding.
 * Uses the singleton pattern.
 */
@SuppressWarnings("SameParameterValue")
public class GlobalConfiguration {

    private static final String CONFIG_PATH = "data/config.json";
    private static GlobalConfiguration instance;

    private JsonObject dataBlob;

    private GlobalConfiguration() {
        dataBlob = new JsonObject();
    }

    public static GlobalConfiguration getInstance() {
        if (instance == null) {
            update();
        }

        return instance;
    }

    /**
     * Creates a new instance populated with the current values of the config.json.
     */
    public static void update() {
        JsonObject newBlob = JSONLoaderSaver.loadArbitrary(CONFIG_PATH).orElseThrow(RuntimeException::new);
        instance = new GlobalConfiguration();
        instance.dataBlob = newBlob;
    }

    /**
     * Returns the boolean represented by a key.
     *
     * @param key The unique key at the top level of the config.json
     * @return The object or null if it does not exist.
     */
    public boolean getAsBoolean(String key) {
        return dataBlob.get(key).getAsBoolean();
    }

    /**
     * Similar to getAsBoolean
     */
    public String getAsString(String key) {
        return dataBlob.get(key).getAsString();
    }

    /**
     * Similar to getAsBoolean
     */
    public int getAsInt(String key) {
        return dataBlob.get(key).getAsInt();
    }

    /**
     * Similar to getAsBoolean
     */
    public float getAsFloat(String key) {
        return dataBlob.get(key).getAsFloat();
    }

    public int getCurrentLevel() {
        return dataBlob.get("currentLevel").getAsInt();
    }

    public int getCurrentMultiLevel() {
        return dataBlob.get("currentMultiLevel").getAsInt();
    }

    public void setCurrentLevel(int lvl) {
        dataBlob.remove("currentLevel");
        dataBlob.addProperty("currentLevel", lvl);
    }

    public void setFlowControlMode(boolean mode) {
        dataBlob.remove("flowControlMode");
        if (mode)
            dataBlob.addProperty("flowControlMode", 0);
        else
            dataBlob.addProperty("flowControlMode", 1);
    }

    public void setFlowMovementMode(boolean mode) {
        dataBlob.remove("flowMovementMode");
        if (mode)
            dataBlob.addProperty("flowMovementMode", 0);
        else
            dataBlob.addProperty("flowMovementMode", 1);
    }

    public void setGraphicsQuality(boolean mode) {
        dataBlob.remove("graphics");
        if (mode)
            dataBlob.addProperty("graphics", 0);
        else
            dataBlob.addProperty("graphics", 1);
    }

    public void setMusic(boolean mode) {
            dataBlob.addProperty("enableMusic", mode);
    }

    public void setMultiplayer(boolean multiplayer) {
        dataBlob.addProperty("multiplayer", multiplayer);
    }

    public void setCurrentMultiLevel(int currentMultiLevel) {
        dataBlob.remove("currentMultiLevel");
        dataBlob.addProperty("currentMultiLevel", currentMultiLevel);
    }
}
