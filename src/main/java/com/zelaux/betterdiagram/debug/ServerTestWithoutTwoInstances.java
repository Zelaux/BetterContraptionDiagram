package com.zelaux.betterdiagram.debug;

import lombok.AllArgsConstructor;

public class ServerTestWithoutTwoInstances {
    public static ServerTestWithoutTwoInstances.TestKind kind = ServerTestWithoutTwoInstances.TestKind.HasClient_HasServer;

    @AllArgsConstructor
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
