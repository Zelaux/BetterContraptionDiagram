package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.google.common.collect.ImmutableList;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public abstract sealed class SubLevelGroup implements Iterable<SubLevel> {

    public static SubLevelGroup single(SubLevel level) {
        return new Single(level);
    }

    @Override
    public @NotNull Iterator<SubLevel> iterator() {
        return all().iterator();
    }

    public abstract List<SubLevel> all();

    public SubLevelGroup with(SubLevel b) {
        if(all().contains(b)) return this;
        return new Many(
            ImmutableList.<SubLevel>builder()
                         .addAll(all())
                         .add(b)
                         .build()
        );
    }

    public SubLevelGroup with(Iterable<SubLevel> b) {
        ImmutableList.Builder<SubLevel> builder = null;
        List<SubLevel> all = all();
        for(SubLevel subLevel : b) {
            if(all.contains(subLevel)) continue;
            if(builder == null) builder = ImmutableList.<SubLevel>builder().addAll(all);
            builder.add(subLevel);
        }
        if(builder == null) return this;

        ImmutableList<SubLevel> build = builder.build();
        if(build.size()==1)return new Single(build.getFirst());
        return new Many(build);
    }

    public boolean single() {
        return amount()==1;
    }

    public SubLevel first() {
        return all().getFirst();
    }

    public abstract int amount();

    @AllArgsConstructor
    public static non-sealed class Single extends SubLevelGroup {
        public SubLevel subLevel;

        @Override
        public List<SubLevel> all() {
            return List.of(subLevel);
        }

        @Override
        public int amount() {
            return 1;
        }

    }

    @AllArgsConstructor
    public static non-sealed class Many extends SubLevelGroup {
        public List<SubLevel> subLevels;

        @Override
        public List<SubLevel> all() {
            return subLevels;
        }

        @Override
        public int amount() {
            return subLevels.size();
        }
    }
}
