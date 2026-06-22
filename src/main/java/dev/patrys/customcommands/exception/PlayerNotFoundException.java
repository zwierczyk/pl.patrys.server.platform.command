package dev.patrys.customcommands.exception;

public class PlayerNotFoundException extends RuntimeException {
    private final String targetName;

    public PlayerNotFoundException(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetName() {
        return targetName;
    }
}