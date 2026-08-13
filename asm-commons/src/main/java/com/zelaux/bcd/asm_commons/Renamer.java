package com.zelaux.bcd.asm_commons;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Map;

public class Renamer extends MethodVisitor {
    public Renamer(int api) {
        super(api);
    }

    public Renamer(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    public Renamer(MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
    }

    public Map<CodeEntry, CodeEntry> all = new Object2ObjectOpenHashMap<>();
    public Map<String, String> classInternalMapper = new Object2ObjectOpenHashMap<>();

    public void addMapping(CodeEntry entry, CodeEntry replacement) {
        if(entry.entryType != replacement.entryType) {
            throw new RuntimeException("Entry type not equal: '%s' and '%s'".formatted(entry, replacement));
        }
        all.put(entry, replacement);
        if(entry.entryType == CodeEntry.EntryType.CLASS) {
            classInternalMapper.put(entry.owner.getInternalName(), replacement.owner.getInternalName());
        }
    }

    public void removeMapping(CodeEntry entry) {
        all.remove(entry);
        classInternalMapper.remove(entry.owner.getInternalName());
    }


    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, transformType(type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        CodeEntry codeEntry = CodeEntry.fieldEntry(Type.getObjectType(owner), Type.getType(descriptor), name);
        CodeEntry entry = all.get(codeEntry);
        if(entry!=null){
            super.visitFieldInsn(opcode, entry.owner().getInternalName(), entry.name, entry.type.getInternalName());
        }else{
            super.visitFieldInsn(opcode, transformType(owner), name, descriptor);
        }
    }

    private String transformType(String owner) {
        return classInternalMapper.getOrDefault(owner, owner);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        CodeEntry codeEntry = CodeEntry.methodEntry(Type.getObjectType(owner), Type.getType(descriptor), name);
        CodeEntry entry = all.get(codeEntry);
        if(entry!=null){
            super.visitMethodInsn(opcode, entry.owner().getInternalName(), entry.name, entry.type.getInternalName(),isInterface);
        }else{
            super.visitMethodInsn(opcode, transformType(owner), name, descriptor,isInterface);
        }
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        CodeEntry codeEntry = CodeEntry.classEntry(Type.getType(descriptor));
        super.visitLocalVariable(name, all.getOrDefault(codeEntry,codeEntry).type.getDescriptor(), signature, start, end, index);
    }
}
