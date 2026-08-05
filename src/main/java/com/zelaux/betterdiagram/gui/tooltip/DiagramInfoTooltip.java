package com.zelaux.betterdiagram.gui.tooltip;

import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip.BalloonModule;
import com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip.DiagramInfoModule;
import com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip.GravityModule;
import com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip.TerminalSpeedModule;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.Colors;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class DiagramInfoTooltip implements BDiagramButton.TooltipListProvider {

    private final int tooltipColor = Colors.TOOLTIP_COLOR.getRGB();


    public DiagramInfoTooltip(@NotNull DiagramScreen self, ClientSubLevel subLevel, DiagramDataPacket serverData) {
        DiagramScreenAccessors screenAccessors = (DiagramScreenAccessors) self;
        modules.addAll(List.of(
            new GravityModule(subLevel),
            new TerminalSpeedModule(self, subLevel),
            //new InertiaModule(screenAccessors),
            new BalloonModule(screenAccessors, self)
        ));
    }

    ObjectArrayList<Component> list = new ObjectArrayList<>();
    ObjectArrayList<DiagramInfoModule> modules = new ObjectArrayList<>();

    @Override
    public List<Component> get() {
        list.clear();
        list.add(Component.translatable("bcd.extra-info.diagram-button").withColor(tooltipColor));
        for(DiagramInfoModule module : modules) {
            module.appendInto(list, tooltipColor);
        }
        return list;

    }

    public void onClick() {
        for(DiagramInfoModule module : modules) {
            if(module.onClick()) break;
        }
    }

    public boolean canClick() {
        for(DiagramInfoModule module : modules) {
            if(module.canClick()) return true;
        }
        return false;
    }
}
