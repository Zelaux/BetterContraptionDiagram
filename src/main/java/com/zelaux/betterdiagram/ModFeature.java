package com.zelaux.betterdiagram;

public enum ModFeature {
    Buoyancy

    ;
    public final int id=ordinal();
    public final static ModFeature[] all=values();
    public final static ModFeature[] none=new ModFeature[0];
}
