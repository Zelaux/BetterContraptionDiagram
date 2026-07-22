package com.zelaux.betterdiagram.extend;

import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.extend.accessors.DiagramStickyNoteAccessors;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;

public interface BAccessors {
    static WithClientData accessors(DiagramEntity obj) {return (WithClientData) obj;}
    static DiagramScreenAccessors accessors(DiagramScreen screen) {return (DiagramScreenAccessors) screen;}

    static DiagramStickyNoteAccessors accessors(DiagramStickyNote screen) {return (DiagramStickyNoteAccessors) screen;}
}
