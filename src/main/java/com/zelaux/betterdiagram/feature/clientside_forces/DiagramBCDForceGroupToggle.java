package com.zelaux.betterdiagram.feature.clientside_forces;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.BAccessors;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramForceGroupToggle;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import net.minecraft.resources.ResourceLocation;

public class DiagramBCDForceGroupToggle extends DiagramForceGroupToggle {
    private final WithClientData clientData;
    private final ResourceLocation groupId;
    private final BCDForceGroup forceGroup;
    private final DiagramScreen diagramScreen;

    public DiagramBCDForceGroupToggle(DiagramScreen diagramScreen, BCDForceGroup forceGroup, int x, int y) {
        super(diagramScreen, forceGroup.group(), x, y);
        this.diagramScreen = diagramScreen;
        groupId = BCDForceGroups.getPath(forceGroup);
        this.forceGroup = forceGroup;
        this.clientData = (WithClientData) BAccessors.accessors(diagramScreen).bcd$diagram();

    }

    public boolean isEnabled() {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null) return forceGroup.group.defaultDisplayed();
        return data.enabledForces.display.contains(forceGroup);
    }


    public void toggleActive() {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null) return;
        BCDData newData;
        if(isEnabled()) {
            newData = data.withEnabledForces(data.enabledForces.removeDisplay(forceGroup));
        } else {
            newData = data.withEnabledForces(data.enabledForces.addDisplay(forceGroup));
        }
        if(data == newData) return;
        clientData.bcd$updateData(newData);
        this.diagramScreen.setConfigDirty();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        //super.onClick(mouseX, mouseY);
        this.toggleActive();
    }
}
