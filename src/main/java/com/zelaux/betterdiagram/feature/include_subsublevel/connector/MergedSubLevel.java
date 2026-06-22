package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.google.common.collect.ImmutableList;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public abstract sealed class MergedSubLevel implements Iterable<SubLevel> {

    public static MergedSubLevel single(SubLevel level) {
        return new Single(level);
    }

    @Override
    public @NotNull Iterator<SubLevel> iterator() {
        return all().iterator();
    }

    public abstract List<SubLevel> all();

    public MergedSubLevel with(SubLevel b) {
        if(all().contains(b))return this;
        return new Many(
            ImmutableList.<SubLevel>builder()
                         .addAll(all())
                         .add(b)
                         .build()
        );
    }

    public boolean single() {
        return this instanceof Single;
    }

    @AllArgsConstructor
    public static non-sealed class Single extends MergedSubLevel {
        public SubLevel subLevel;

        @Override
        public List<SubLevel> all() {
            return List.of(subLevel);
        }

    }

    @AllArgsConstructor
    public static non-sealed class Many extends MergedSubLevel {
        public List<SubLevel> subLevels;

        @Override
        public List<SubLevel> all() {
            return subLevels;
        }
    }
}
