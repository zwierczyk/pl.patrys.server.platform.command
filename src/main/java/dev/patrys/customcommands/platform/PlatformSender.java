package dev.patrys.customcommands.platform;

public interface PlatformSender {
    void sendMessage(String message);
    String getName();
    boolean hasPermission(String permission);
    Object getHandle();
}