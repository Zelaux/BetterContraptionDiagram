package processor.debugOnly;

import asmlib.transform.Transformations;
import org.objectweb.asm.*;

import java.io.PrintStream;
import java.util.HashSet;

class ErrorMarker extends ClassVisitor {

    private final HashSet<DebugEntry> debugEntries;
    private final RemovedFromBuild removedFromBuild;
    private String className;
    private String source;

    protected ErrorMarker(HashSet<DebugEntry> debugEntries, RemovedFromBuild removedFromBuild) {
        super(Opcodes.ASM9);
        this.debugEntries = debugEntries;
        this.removedFromBuild = removedFromBuild;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
    }

    @Override
    public void visitSource(String source, String debug) {
        this.source = source;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        Type type = Type.getType(descriptor);
        DebugEntry debugEntry = DebugEntry.classEntry(type);
        if(debugEntries.contains(debugEntry)) {
            removedFromBuild.hasErrors = true;
            showError(
                new StackTraceElement(name, "<init>", source, 0), "Used @DebugOnly %s on non @DebugOnly classes"
                    .formatted(debugEntry)
            );
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(api) {
            private int line;

            @Override
            public void visitLineNumber(int line, Label start) {
                this.line = line;
                super.visitLineNumber(line, start);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                Type ownerType = Type.getObjectType(owner);
                Type descType = Type.getType(descriptor);
                DebugEntry debugEntry = DebugEntry.methodEntry(ownerType, descType, name);
                if(debugEntries.contains(debugEntry)) {
                    removedFromBuild.hasErrors = true;

                    showError(
                        new StackTraceElement(className, methodName, source, line), "Used @DebugOnly %s on non @DebugOnly classes"
                            .formatted(debugEntry)
                    );
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {

                Type ownerType = Type.getObjectType(owner);
                Type descType = Type.getType(descriptor);
                var debugEntry = DebugEntry.fieldEntry(ownerType, descType, name);
                if(debugEntries.contains(debugEntry)) {
                    removedFromBuild.hasErrors = true;

                    showError(
                        new StackTraceElement(className, methodName, source, line), "Used @DebugOnly %s on non @DebugOnly classes"
                            .formatted(debugEntry)
                    );
                }
                super.visitFieldInsn(opcode, owner, name, descriptor);
            }
        };
    }

    private void showError(StackTraceElement stackTraceElement, String formatted) {
        PrintStream err = System.err;
        err.println(formatted);
        err.println("\tat " + stackTraceElement);
        err.println();
    }
}
