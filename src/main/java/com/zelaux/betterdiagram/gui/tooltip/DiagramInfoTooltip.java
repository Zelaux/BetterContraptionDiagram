package com.zelaux.betterdiagram.gui.tooltip;

import com.demonwav.mcdev.annotations.Translatable;
import com.zelaux.betterdiagram.DebugRuntime;
import com.zelaux.betterdiagram.extend.accessors.DiagramScreenAccessors;
import com.zelaux.betterdiagram.gui.tooltip.diagram_info_tooltip.*;
import com.zelaux.betterdiagram.gui.widget.BDiagramButton;
import com.zelaux.betterdiagram.index.Colors;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecFormat;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.physics.floating_block.FloatingBlockMaterial;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

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

}
