package com.zelaux.betterdiagram.debug;

import com.zelaux.betterdiagram.annotations.DebugOnly;
import com.zelaux.betterdiagram.annotations.DeleteInRelease;
import lombok.AllArgsConstructor;
@DeleteInRelease
@DebugOnly
public class ServerTestWithoutTwoInstances {
    public static ServerTestWithoutTwoInstances.TestKind kind = ServerTestWithoutTwoInstances.TestKind.HasClient_HasServer;

    @AllArgsConstructor
    @DebugOnly
    @DeleteInRelease
    public enum TestKind {
        HasClient_HasServer(true, true),
        NoClient_HasServer(false, true),
        HasClient_NoServer(true, false),
        ;
        public final boolean hasClient, hasServer;

        public boolean both() {
            return hasServer && hasClient;
        }
    }

}
