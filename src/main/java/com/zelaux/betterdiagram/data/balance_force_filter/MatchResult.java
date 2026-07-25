package com.zelaux.betterdiagram.data.balance_force_filter;

public record MatchResult(boolean matched) {
    public static final MatchResult NOT_MATCHED = new MatchResult(false);
    public static final MatchResult MATCHED = new MatchResult(true);

}