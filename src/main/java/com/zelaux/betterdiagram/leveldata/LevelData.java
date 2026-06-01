package com.zelaux.betterdiagram.leveldata;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public class LevelData {
    public static final String CURRENT_TICK = "currentTick";
    public long currentTick;
    Path path;
    private File infoFile;

    public LevelData(Path path) {
        this.currentTick = 0;
        this.path = path;
        infoFile = path.resolve("info.txt").toFile();
        File file = infoFile;
        if(!file.exists()) return;
        try(var stream = new Scanner(new FileInputStream(file))) {
            while(stream.hasNext()) {
                String line = stream.nextLine();
                String[] split = line.split("=");
                if(split.length != 2) continue;
                if(split[0].trim().equals(CURRENT_TICK)) {
                    currentTick = Integer.parseInt(split[1]);
                }
            }
        } catch(Exception ignored) {}
    }


    public Path locateLevelDirectory(Level level) {
        Path storageFolder = DimensionType.getStorageFolder(level.dimension(), path);
        if(storageFolder == path) {//overworld moment
            storageFolder = storageFolder.resolve("DIM0");
        }
        DiagramEntityData.CACHE.invalidateAll();

        return storageFolder;
    }

    public void disconnect() {

        File file = infoFile;
        infoFile.getParentFile().mkdirs();
        try(var stream = new FileOutputStream(file)) {
            stream.write((CURRENT_TICK + "=" + currentTick+"\n").getBytes(StandardCharsets.UTF_8));
        } catch(Exception ignore) {

        }
    }

    public void currentTick() {
        currentTick++;
    }
}
