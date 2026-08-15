package com.zelaux.bcd.asm_commons;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LabelCompleter extends MethodVisitor {
    public LabelCompleter(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    public final ObjectOpenHashSet<Label> visitedLabel = new ObjectOpenHashSet<>();
    public final ObjectOpenHashSet<Label> labelQueue = new ObjectOpenHashSet<>();

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        visitedLabel.add(label);
        labelQueue.remove(label);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        if(visitedLabel.contains(label)) return;
        labelQueue.add(label);
    }

    public boolean completeLabels() {
        labelQueue.forEach(super::visitLabel);
        int size = labelQueue.size();
        labelQueue.clear();
        return size!=0;
    }
}
