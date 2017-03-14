package edu.cornell.gdiac.physics.leveleditor;

import edu.cornell.gdiac.physics.obstacle.Obstacle;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class LevelModel {
    @Getter
    @Setter
    String title;
    @Getter
    @Setter
    int goalTimeGold;
    @Getter
    @Setter
    int goalTimeSilver;
    @Getter
    @Setter
    int goalTimeBronze;

    @Getter
    @Setter
    String background;
    @Getter
    @Setter
    String soundtrack;

    @Getter
    @Setter
    int height;

    @Getter
    @Setter
    int width;

    @Getter
    List<Obstacle> entities;

    public LevelModel() {
        this.entities = new ArrayList<Obstacle>();
        this.height = 768;
        this.width = 1024;
    }

    public void addEntity(Obstacle o) {
        entities.add(o);
    }
}
