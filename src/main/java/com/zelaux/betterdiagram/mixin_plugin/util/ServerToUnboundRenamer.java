package com.zelaux.betterdiagram.mixin_plugin.util;

import com.zelaux.bcd.asm_commons.CodeEntry;
import com.zelaux.bcd.asm_commons.Renamer;
import com.zelaux.betterdiagram.mixin_plugin.TypeIndex;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

public class ServerToUnboundRenamer {
    public static Map<CodeEntry, CodeEntry> entries = Map.of(
        CodeEntry.classEntry(TypeIndex.ServerSubLevel), CodeEntry.classEntry(TypeIndex.SubLevel)
    );
public static Renamer make(MethodVisitor visitor){
    Renamer renamer = new Renamer(visitor);
    register(renamer);
    return renamer;
}
    public static void register(Renamer renamer) {
        entries.forEach(renamer::addMapping);
    }
}
