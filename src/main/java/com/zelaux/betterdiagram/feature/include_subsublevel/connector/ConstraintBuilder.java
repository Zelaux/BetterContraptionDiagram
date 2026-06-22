package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ConstraintBuilder {

    public static ConstraintTree buildTree(final SubLevel origin, int maxDepth) {
        final var visited = new ObjectOpenHashSet<SubLevel>();
        var frontier = new ObjectOpenHashSet<ConstraintTree>();
        var frontier2 = new ObjectOpenHashSet<ConstraintTree>();

        ConstraintTree root = ConstraintTree.root(origin);
        frontier.add(root);
    int depth=0;
        while(!frontier.isEmpty() && depth<maxDepth) {
            depth++;
            for(var tree : frontier) {
                var currentMerged = tree.mergedSubLevel;
                for(SubLevel current : currentMerged) {
                    for(final BlockEntitySubLevelActor actor : current.getPlot().getBlockEntityActors()) {
                        Iterable<LocalConstraint> constraints = ActorMapper.findFor(actor).mapToConstraint(current);
                        if(constraints == null) continue;
                        for(final var constraint : constraints) {
                            if(constraint.a() != current) continue;
                            if(!tree.path.contains(constraint.b()) && !tree.hasChild(constraint)) {
                                if(constraint.kind() == LocalConstraint.Kind.Fixed) {
                                    tree.mergedSubLevel=tree.mergedSubLevel.with(constraint.b());
                                    frontier2.add(tree);
                                } else {
                                    frontier2.add(tree.child(constraint));
                                }
                            }
                        }
                    }
                }
            }
            frontier.clear();

            var tmp = frontier2;
            frontier2 = frontier;
            frontier = tmp;
        }

        return root;
    }
}
