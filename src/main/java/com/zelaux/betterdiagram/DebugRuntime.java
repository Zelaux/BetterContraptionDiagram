package com.zelaux.betterdiagram;

import com.zelaux.bcd.asm_commons.ClassUtil;
import com.zelaux.bcd.asm_commons.MethodUtil;
import com.zelaux.betterdiagram.annotations.DebugOnly;
import com.zelaux.betterdiagram.annotations.DeleteInRelease;
import lombok.SneakyThrows;
import org.apache.commons.lang3.function.FailableConsumer;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;


@DebugOnly
@DeleteInRelease
public class DebugRuntime {

    public static final File FOLDER = new File(".bcd/stuff").getAbsoluteFile();
    public static final File THREAD_FOLDER = new File(FOLDER,"thread").getAbsoluteFile();
    public static final File FILE = new File(FOLDER, "common.txt");
    public static boolean disableAirflowScaling=false && isDebug();

    public static final Logger LOGGER = LoggerFactory.getLogger(BetterContraptionDiagram.class.getSimpleName()+"#"+DebugRuntime.class.getSimpleName());
    //public static final Logger LOGGER = BetterContraptionDiagram.LOGGER;
    static {
        System.out.println("Loaded : " + FOLDER.getAbsolutePath());
    }
    /**
     * The value will be <b>inlined</b> at the call-site.
     * */
    public static boolean isDebug(){
        return true;
    }

    @SneakyThrows
    public static boolean addThreadToFile(File file,String comment) {
        Thread thread = Thread.currentThread();
        file.getParentFile().mkdirs();
        try(FileOutputStream stream = new FileOutputStream(file, true)) {
            stream.write(("["+System.identityHashCode(thread)+"]"+thread+" | "+comment + "\n").getBytes(StandardCharsets.UTF_8));
        }
        return false;
    }
    @SneakyThrows
    public static void logFile(String fileName, String message){
        saveInFile(fileName, message.getBytes(StandardCharsets.UTF_8), true);
    }
    @SneakyThrows
    public static void saveInFile(String fileName, byte[] bytes, boolean append){
        saveInFile(fileName, append, stream -> stream.write(bytes));
    }

    public static void saveInFile(String fileName, boolean append, FailableConsumer<FileOutputStream, Exception> consumer) throws Exception {
        var file=new File(FOLDER, fileName);
        file.getParentFile().mkdirs();
        System.out.println(file.getAbsolutePath());
        try(FileOutputStream stream = new FileOutputStream(file, append)) {
            consumer.accept(stream);
        }
    }

    public static boolean removeAll(String message) {
        return addThreadToFile(FILE,"removeAll | "+message);
    }
    public static boolean add(String message) {
        return addThreadToFile(FILE, "    add   | " + message);
    }
    public static boolean isFullBlock() {
        return addThreadToFile(new File(THREAD_FOLDER, "fullBlock.txt"),"");
    }

    public static boolean isSolid() {
        return addThreadToFile(new File(THREAD_FOLDER, "isSolid.txt"),"");
    }

    public static boolean centerOfMass() {
        return addThreadToFile(new File(THREAD_FOLDER, "centerOfMass.txt"),"");
    }

    public static void saveMethodInFile(String className, MethodNode node) {
        saveInFile(ClassUtil.simpleName(className) + ".txt", MethodUtil.methodInsnToString(node), false);
    }
}
