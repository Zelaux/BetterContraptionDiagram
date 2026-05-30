package com.zelaux.betterdiagram;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
/**
 *
 * */
public class DebugRuntime {

    public static final File FOLDER = new File("hello_world.log").getAbsoluteFile();

    static {
        System.out.println("Loaded : " + FOLDER.getAbsolutePath());
    }
    @SneakyThrows
    public static boolean addThreadToFile(File file){
        Thread thread = Thread.currentThread();
        file.getParentFile().mkdirs();
        try(FileOutputStream stream = new FileOutputStream(file, true)) {
            stream.write((thread.toString()+"\n").getBytes(StandardCharsets.UTF_8));
        }
        return false;
    }

    public static boolean isFullBlock() {
        return addThreadToFile(new File(FOLDER,"fullBlock.txt"));
    }
    public static boolean isSolid() {
        return addThreadToFile(new File(FOLDER,"isSolid.txt"));
    }
    public static boolean centerOfMass() {
        return addThreadToFile(new File(FOLDER,"centerOfMass.txt"));
    }
}
