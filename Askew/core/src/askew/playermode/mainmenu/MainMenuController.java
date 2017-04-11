package askew.playermode.mainmenu;

import askew.InputController;
import askew.MantisAssetManager;
import askew.playermode.WorldController;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;
import lombok.Setter;

public class MainMenuController extends WorldController {
    @Getter @Setter
    private int selected = 0;
    @Getter @Setter
    private int minLevel = 0;
    @Getter @Setter
    private int maxLevel = 10;

    // main menu modes
    private final int HOME_SCREEN = 0;
    private final int LEVEL_SELECT = 1;
    private int mode;
    private int prevMode;

    // home mode options
    private final int PLAY_BUTTON = 0;
    private final int LEVEL_SELECT_BUTTON = 1;
    private final int QUIT_BUTTON = 2;
    private int home_button = PLAY_BUTTON;
    private Vector2[] home_button_locs = {new Vector2(620f,257f), new Vector2(620f,204f), new Vector2(620f,155f)};

    // level select mode options
    private final int CHOOSE_LEVEL = 0;
    private final int RETURN_HOME = 1;
    private int select_button = CHOOSE_LEVEL;
    private Vector2[] select_button_locs = {new Vector2(630f, 230f), new Vector2(625f, 170f)};

    private static final String FERN_TEXTURE = "texture/background/fern.png";
    private static final String MENU_BACKGROUND1_TEXTURE = "texture/background/menu1.png";
    private static final String MENU_BACKGROUND2_TEXTURE = "texture/background/menu2.png";

    private Texture fern, menu1, menu2;

    // player selected another mode
    private String nextCon = "";

    @Override
    public void loadContent(MantisAssetManager manager) {
        super.loadContent(manager);
        fern = manager.get(FERN_TEXTURE);
        menu1 = manager.get(MENU_BACKGROUND1_TEXTURE);
        menu2 = manager.get(MENU_BACKGROUND2_TEXTURE);
    }

    public MainMenuController() {
        mode = PLAY_BUTTON;
    }

    @Override
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        InputController input = InputController.getInstance();

        if (input.didLeftButtonPress() || nextCon.equals("GM")) {
            System.out.println("GM");
            listener.exitScreen(this, EXIT_MM_GM);
            return false;
        } else if (input.didTopButtonPress()) {
            System.out.println("LE");
            listener.exitScreen(this, EXIT_MM_LE);
            return false;
        }
//        else if (input.didBottomButtonPress()) {
//            System.out.println("GM_OLD");
//            listener.exitScreen(this, EXIT_MM_GM_OLD);
//            return false;
//        }
//        System.out.println("selected "+selected);
//        System.out.println("mode "+mode);
//        System.out.println("home "+home_button);
//        System.out.println("select "+select_button);
        prevMode = mode;
        return true;
    }

    @Override
    public void draw(float delta) {
        if(mode == HOME_SCREEN) {
            canvas.clear(menu1);
        }
        else if (mode == LEVEL_SELECT) {
            canvas.clear(menu2);
            displayFont.setColor(Color.GREEN);
        }

        canvas.begin(); // DO NOT SCALE
        if(mode == HOME_SCREEN) {
            canvas.draw(menu1,0,0);
            canvas.draw(fern, home_button_locs[home_button].x, home_button_locs[home_button].y);
        }
        else if(mode == LEVEL_SELECT) {
            canvas.draw(menu2,0,0);
            canvas.drawText("         " + selected, displayFont, 300f, 280f);
            canvas.draw(fern, select_button_locs[select_button].x, select_button_locs[select_button].y);
        }
        canvas.end();
    }

    @Override
    public void reset() {
        nextCon = "";
    }

    @Override
    public void update(float dt) {
        InputController input = InputController.getInstance();
        if(mode == HOME_SCREEN) {
            if(mode!=prevMode)
                return;
            if (input.didTopDPadPress() && home_button > 0) {
                home_button--;
            }
            else if (input.didBottomDPadPress() && home_button < home_button_locs.length - 1) {
                home_button++;
            }

            if(input.didBottomButtonPress() && home_button == PLAY_BUTTON) {
                selected = 1;
                nextCon = "GM";
                return;
            }
            else if(input.didBottomButtonPress() && home_button == LEVEL_SELECT_BUTTON) {
                mode = LEVEL_SELECT;
                select_button = CHOOSE_LEVEL;
                selected = 0;
            }
            else if(input.didBottomButtonPress() && home_button == QUIT_BUTTON) {
                listener.exitScreen(this, EXIT_QUIT);
            }
        }
        if(mode == LEVEL_SELECT) {
            if(mode!=prevMode)
                return;
            if(input.didLeftDPadPress() && selected < maxLevel && select_button == CHOOSE_LEVEL) {
                selected++;
            } else if(input.didRightDPadPress() && selected > minLevel && select_button == CHOOSE_LEVEL) {
                selected--;
            }

            if(input.didTopDPadPress()) {
                select_button = CHOOSE_LEVEL;
            }
            else if(input.didBottomDPadPress()){
                select_button = RETURN_HOME;
            }
            if(input.didBottomButtonPress() && select_button == RETURN_HOME) {
                    System.out.println("return home");
                    mode = HOME_SCREEN;
                    home_button = PLAY_BUTTON;
            }
            else if(input.didBottomButtonPress() && select_button == CHOOSE_LEVEL){
                    System.out.println("selected level");
                    nextCon = "GM";
                    return;
            }
        }

    }

    public String getLevel() {
        String lvl;
        if(mode == HOME_SCREEN)
            selected = 0;
        lvl = "level"+selected;
        return lvl;
    }
}