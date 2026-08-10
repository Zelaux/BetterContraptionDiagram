package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public abstract class AbstractForceMapper {

    private final boolean isMergable;

    {
        try {
            isMergable = getClass().getMethod("tryMergeWith", AbstractForceMapper.class) != AbstractForceMapper.class.getDeclaredMethod("tryMergeWith", AbstractForceMapper.class);
        } catch(NoSuchMethodException e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    public @NotNull SubLevelInfo sublevelInfo() {
        return new SubLevelInfo(
            this.originSubLevel,
            this.originGroup,
            this.toAttachSubLevel,
            this.toAttachGroup
        );
    }
    public static @NotNull SubLevelInfo sublevelInfo(SubLevel origin, SubLevel toAttach, Map<SubLevel, SubLevelGroup> subLevel2Group) {
        SubLevelGroup originGroup = subLevel2Group.get(origin);
        SubLevelGroup toAttachGroup = subLevel2Group.get(toAttach);
        return new SubLevelInfo(origin, originGroup, toAttach, toAttachGroup);
    }

    public AbstractForceMapper tryMergeWith(AbstractForceMapper other) {
        return null;
    }

    @SneakyThrows
    public boolean isMergable() {
        return isMergable;
    }

    @With
    public record SubLevelInfo(
        SubLevel sourceSubLevel,
        SubLevelGroup sourceGroup,
        SubLevel targetSubLevel,
        SubLevelGroup targetGroup
    ) {
        public SubLevelInfo withTarget(SubLevel subLevel, SubLevelGroup group){
            return new SubLevelInfo(
                sourceSubLevel,sourceGroup,subLevel,group
            );
        }
    }

    SubLevel originSubLevel;
    SubLevelGroup originGroup;
    SubLevel toAttachSubLevel;
    SubLevelGroup toAttachGroup;

    public AbstractForceMapper(SubLevel originSubLevel, SubLevelGroup originGroup, SubLevel toAttachSubLevel, SubLevelGroup toAttachGroup) {
        this.originSubLevel = originSubLevel;
        this.originGroup = originGroup;
        this.toAttachSubLevel = toAttachSubLevel;
        this.toAttachGroup = toAttachGroup;
    }

    public AbstractForceMapper(SubLevelInfo info) {
        this.originSubLevel = info.sourceSubLevel;
        this.originGroup = info.sourceGroup;
        this.toAttachSubLevel = info.targetSubLevel;
        this.toAttachGroup = info.targetGroup;
    }

    public abstract void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData);

    public boolean fixed() {
        return false;
    }
}
