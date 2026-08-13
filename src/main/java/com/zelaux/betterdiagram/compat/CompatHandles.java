package com.zelaux.betterdiagram.compat;

import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.mixin_plugin.postprocessors.GadgetsProcessorProvider;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

/**
 * @see GadgetsProcessorProvider#preMixin(String, ClassNode, String, IMixinInfo)
 * */
public class CompatHandles {
    public static DiagramScreen gadgetsScreen;
    //@SuppressWarnings("unused")
    public static void onPackageUpdate(Object screen){

        if(!(screen instanceof DiagramScreenAccessors sc)) return;
        sc.bcd$tryUpdateData();
    }
    //@SuppressWarnings("unused")

    public static void setGadgetsScreen(Object screen){
        if(screen==null){
            gadgetsScreen=null;
            return;
        }
        if(!(screen instanceof DiagramScreen sc)) return;
        gadgetsScreen=sc;
        ((DiagramScreenAccessors) sc).bcd$diagram().setUUID(sc.subLevel.getUniqueId());
    }
}
