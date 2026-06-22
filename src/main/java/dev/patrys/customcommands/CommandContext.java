package dev.patrys.customcommands;

import dev.patrys.customcommands.platform.PlatformSender;

public class CommandContext {
    private final PlatformSender sender;
    private final String[] args;
    private final String label;

    public CommandContext(PlatformSender sender, String[] args, String label) {
        this.sender = sender;
        this.args = args;
        this.label = label;
    }

    public PlatformSender getSender() {
        return sender;
    }

    public String[] getArgs() {
        return args;
    }

    public String getLabel() {
        return label;
    }

    public String getArg(int index) {
        return args.length > index ? args[index] : null;
    }

    public int getArgCount() {
        return args.length;
    }
}