package com.zelaux.betterdiagram.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.command.argument.SubLevelSelector;
import dev.ryanhcode.sable.command.argument.SubLevelSelectorModifierType;
import dev.ryanhcode.sable.command.argument.SubLevelSelectorType;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ClientSubLevelSelector {
    private final SubLevelSelectorType type;
    //private final List<Pair<SubLevelSelectorModifierType, SubLevelSelectorModifierType.Modifier>> modifiers;

    public ClientSubLevelSelector(SubLevelSelectorType type) {
        this.type = type;
        //this.modifiers = modifiers;
    }

    //static final MethodHandle MODIFIERS_GETTER;
    static {
    /*    try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Field declaredField = SubLevelSelector.class.getDeclaredField("modifiers");
            declaredField.setAccessible(true);
            MODIFIERS_GETTER = lookup.unreflectGetter(declaredField);
        } catch(NoSuchFieldException | IllegalAccessException e) {
            throw Lombok.sneakyThrow(e);
        }*/
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public ClientSubLevelSelector(SubLevelSelector selector) {
        this.type = selector.getSelectorType();
        //modifiers = (List<Pair<SubLevelSelectorModifierType, SubLevelSelectorModifierType.Modifier>>) MODIFIERS_GETTER.invoke(selector);

    }

    public Collection<ClientSubLevel> getSubLevels(final CommandSourceStack source) throws CommandSyntaxException {
        if(this.type == null) {
            return List.of();
        }

        final ClientLevel level = (ClientLevel) source.getUnsidedLevel();
        final ClientSubLevelContainer container = BCDCommandHelper.requireSubLevelContainer(source);

        final Iterable<ClientSubLevel> containerBodies = container.getAllSubLevels();
        final Collection<ClientSubLevel> bodies = new ObjectArrayList<>();

        for(final ClientSubLevel subLevel : containerBodies) {
            bodies.add(subLevel);
        }

        if(bodies.isEmpty()) {
            return Collections.emptySet();
        }

        final ActiveSableCompanion helper = Sable.HELPER;
        final Collection<ClientSubLevel> collectedSubLevels = switch(this.type) {
            case ALL -> new HashSet<>(bodies);
            case NEAREST -> {
                double closest = Double.MAX_VALUE;
                ClientSubLevel closestSubLevel = null;

                for(final ClientSubLevel body : bodies) {
                    final Vec3 sourcePosition = helper.projectOutOfSubLevel(source.getLevel(), source.getPosition());
                    final double distance = body.logicalPose().position().distance(sourcePosition.x, sourcePosition.y, sourcePosition.z);

                    if(distance < closest) {
                        closest = distance;
                        closestSubLevel = body;
                    }
                }

                yield Collections.singleton(closestSubLevel);
            }
            case RANDOM -> {
                final List<ClientSubLevel> list = new ArrayList<>(bodies);
                yield Collections.singleton(list.get(level.random.nextInt(list.size())));
            }
            case INSIDE -> {
                final ClientSubLevel subLevel = (ClientSubLevel) helper.getContaining(level, source.getPosition());
                if(subLevel != null) {
                    yield Collections.singleton(subLevel);
                } else {
                    yield Collections.emptySet();
                }
            }
            case TRACKING -> {
                if(source.getEntity() == null) {
                    yield Collections.emptySet();
                }

                final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getTrackingSubLevel(source.getEntity());

                if(subLevel != null) {
                    yield Collections.singleton(subLevel);
                } else {
                    yield Collections.emptySet();
                }
            }
            case VIEWED -> {
                if(source.getEntity() != null) {
                    final HitResult res = source.getEntity().pick(100.0, 1.0f, true);

                    if(res instanceof final BlockHitResult blockHitResult) {
                        final ClientSubLevel containing = (ClientSubLevel) helper.getContaining(level, blockHitResult.getBlockPos());
                        if(containing != null) {
                            yield Collections.singleton(containing);
                        } else {
                            yield Collections.emptySet();
                        }
                    } else {
                        yield Collections.emptySet();
                    }
                } else {
                    yield Collections.emptySet();
                }
            }
            case LATEST -> {
                final List<ClientSubLevel> subLevels = container.getAllSubLevels();
                if(subLevels.isEmpty()) {
                    yield Collections.emptySet();
                }
                yield Collections.singleton(subLevels.getLast());
            }
        };

        List<ClientSubLevel> modifiedSubLevels = new ObjectArrayList<>(collectedSubLevels);

        /*
        final Vector3d position = new Vector3d(source.getPosition().x, source.getPosition().y, source.getPosition().z);
        this.modifiers.sort(
            Comparator.comparingInt(a -> a.first().getFilterPriority().ordinal())
        );
        for(final Pair<SubLevelSelectorModifierType, SubLevelSelectorModifierType.Modifier> modifier : this.modifiers) {
            modifiedSubLevels = modifier.right().apply(modifiedSubLevels, position);
        }
        */

        return modifiedSubLevels;
    }
}
