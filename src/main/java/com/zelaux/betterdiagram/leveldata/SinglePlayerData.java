package com.zelaux.betterdiagram.leveldata;

import java.nio.file.Path;

public class SinglePlayerData extends LevelData {
    public SinglePlayerData(Path worldFolder) {
        super(worldFolder.resolve(".bcdiagram"));
    }
}
