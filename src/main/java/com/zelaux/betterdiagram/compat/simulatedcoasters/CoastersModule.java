package com.zelaux.betterdiagram.compat.simulatedcoasters;

import com.zelaux.betterdiagram.compat.CompatModule;
import com.zelaux.betterdiagram.compat.simulatedcoasters.actor.CoasterCartActor;
import com.zelaux.betterdiagram.compat.simulatedcoasters.actor.RivetsActor;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandler;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ActorHandlerPollution;
import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class CoastersModule extends CompatModule {
    @Override
    protected void load() {
        ActorHandlerPollution.register(subLevel -> {

            List<ActorHandler> list =null;
            if(RivetClientIndex.hasRivets(subLevel))list=add(list,new RivetsActor());
            if(CoasterCartResolve.isCartSubLevel(subLevel))list=add(list,new CoasterCartActor());
            return list;
        });
    }

    static <T> List<T> add(List<T> l, T handler) {
        if(l == null) l = new ObjectArrayList<>();
        l.add(handler);
        return l;
    }
}
