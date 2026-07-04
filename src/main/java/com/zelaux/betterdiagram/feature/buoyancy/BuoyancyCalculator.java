package com.zelaux.betterdiagram.feature.buoyancy;

import com.tterrag.registrate.AbstractRegistrate;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BuoyancyCalculator {
    private static final String NATIVE_DIR = ".bcd_sable_ext/natives";
    private static final String LIB_ZIP_NAME = "bcd_sable_ext_binaries.zip.l4z";
    private static final String LIB_NAME = "bcd_sable_ext";

    public static final String NATIVE_NAME = getNativeName();

    private static int countingObjectID = 0;
    private static boolean hasNative;

    static {
        loadLibrary();
    }

    private static String getNativeName() {
        final String arch;
        if(System.getProperty("os.arch").equals("arm") || System.getProperty("os.arch").startsWith("aarch64")) {
            arch = "aarch64";
        } else {
            arch = "x86_64";
        }

        final Util.OS os = Util.getPlatform();
        if(os == Util.OS.WINDOWS) {
            return LIB_NAME + "_" + arch + "_windows.dll";
        } else if(os == Util.OS.OSX) {
            return LIB_NAME + "_" + arch + "_macos.dylib";
        } else {
            if(os != Util.OS.LINUX) {
                BetterContraptionDiagram.LOGGER.error("Unknown platform '{}' detected, sable will attempt to use linux natives, this may or may not work.", System.getProperty("os.name"));
            }
            return LIB_NAME + "_" + arch + "_linux.so";
        }
    }

    private static void loadLibrary() {
        try(final InputStream is = BuoyancyCalculator.class.getResourceAsStream("/natives/" + LIB_NAME + "/" + LIB_ZIP_NAME)) {
            if(is == null) {
                throw new FileNotFoundException(LIB_ZIP_NAME);
            }

            final Path dir = Paths.get(NATIVE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            try (final LZ4FrameInputStream is2 = new LZ4FrameInputStream(is);
                 final ZipInputStream ti = new ZipInputStream(is2)) {

                ZipEntry entry;
                while ((entry = ti.getNextEntry()) != null) {
                	System.out.println("Entry name: "+entry.getName());
                    if (entry.getName().equals(NATIVE_NAME)) {
                        final Path tempFile = dir.resolve(NATIVE_NAME);
                        if (!Files.exists(tempFile)) {
                            Files.createFile(tempFile);
                        }
                        Files.copy(ti, tempFile, StandardCopyOption.REPLACE_EXISTING);
                        System.load(tempFile.toAbsolutePath().toString());
                        hasNative = true;
                        return;
                    }
                }

                throw new FileNotFoundException(NATIVE_NAME);
            }
        } catch(final Throwable t) {
            if(!AbstractRegistrate.isDevEnvironment()) {
                hasNative = false;
                return;
            }
            /*BetterContraptionDiagram.LOGGER.error(
                "Sable has failed to load the natives needed for its Rapier pipeline. Please report with system details and logs to {}",
                BetterContraptionDiagram.ISSUE_TRACKER_URL,
                t);*/
            final CrashReport crashReport = CrashReport.forThrowable(t instanceof UnsatisfiedLinkError ? t.getCause() : t, "BetterContraptionDiagram linking with Rapier-ext natives");
            final CrashReportCategory category = crashReport.addCategory("Natives");
            category.setDetail("Name", NATIVE_NAME);
            throw new ReportedException(crashReport);
        }

    }

    /**
     * @return float.length = 0 | 12
     */
    private static native float[] calculateForces0(long scenePtr, int bodyId);
    /**
     * @return true if succeed
     *
     * */
    public static boolean calculateForces(ServerSubLevel sublevel,
                                          Vector3d buoyancyPoint, Vector3d buoyancyForce,
                                          Vector3d dragPoint, Vector3d dragForce
    ){
        if(!hasNative)return false;
        long sceneHandle = Rapier3D.getSceneHandle(sublevel.getLevel());
        float[] floats = calculateForces0(sceneHandle, sublevel.getRuntimeId());
        if(floats.length==0)return false;
        if(buoyancyPoint==null)return true;
        int i=0;
        buoyancyPoint.set(floats[i++],floats[i++],floats[i++]);
        buoyancyForce.set(floats[i++],floats[i++],floats[i++]);
        dragPoint.set(floats[i++],floats[i++],floats[i++]);
        dragForce.set(floats[i++],floats[i++],floats[i++]);
        return true;

    }

    public static native String helloWorld(String name);


}
