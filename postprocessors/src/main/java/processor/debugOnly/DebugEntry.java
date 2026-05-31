package processor.debugOnly;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public final class DebugEntry {
    EntryType entryType;
    Type owner;
    Type type;
    String name;

    @Override
    public String toString() {
        return switch(entryType) {
            case CLASS -> "class '%s'".formatted(owner.getClassName());
            case FIELD -> "field '%s#%s'".formatted(owner.getClassName(),name);
            case METHOD -> "method '%s#%s %s'".formatted(owner.getClassName(),name,type.toString());
        };
    }

    public static DebugEntry classEntry(Type type) {
        return new DebugEntry(EntryType.CLASS, type, null, null);
    }

    public static DebugEntry fieldEntry(Type owner, FieldNode node) {
        return fieldEntry(owner, Type.getType(node.desc), node.name);
    }

    public static DebugEntry fieldEntry(Type owner, Type type, String name) {
        return new DebugEntry(EntryType.FIELD, owner, type, name);
    }

    public static DebugEntry methodEntry(Type owner, MethodNode node) {
        return methodEntry(owner, Type.getType(node.desc), node.name);
    }

    public static DebugEntry methodEntry(Type owner, Type type, String name) {
        return new DebugEntry(EntryType.METHOD, owner, type, name);
    }

    public enum EntryType {
        CLASS, FIELD, METHOD
    }
}
