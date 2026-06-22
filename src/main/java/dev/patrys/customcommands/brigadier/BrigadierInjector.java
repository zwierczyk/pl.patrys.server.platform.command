package dev.patrys.customcommands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.patrys.customcommands.CommandRegistry;
import dev.patrys.customcommands.annotation.Arg;
import dev.patrys.customcommands.annotation.Join;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class BrigadierInjector {

    private Object minecraftServer;
    private CommandDispatcher<Object> dispatcher;
    private boolean isSupported;

    @SuppressWarnings("unchecked")
    public BrigadierInjector() {
        try {
            Field consoleField = Bukkit.getServer().getClass().getDeclaredField("console");
            consoleField.setAccessible(true);
            minecraftServer = consoleField.get(Bukkit.getServer());

            Method getDispatcherMethod = null;
            for (Method method : minecraftServer.getClass().getMethods()) {
                if (method.getReturnType().equals(CommandDispatcher.class)) {
                    getDispatcherMethod = method;
                    break;
                }
            }

            if (getDispatcherMethod == null) {
                for (Method method : minecraftServer.getClass().getMethods()) {
                    if (method.getName().equals("getCommandDispatcher") || method.getName().equals("vanillaCommandDispatcher")) {
                        Object dispatcherWrapper = method.invoke(minecraftServer);
                        for (Method wrapperMethod : dispatcherWrapper.getClass().getMethods()) {
                            if (wrapperMethod.getReturnType().equals(CommandDispatcher.class)) {
                                dispatcher = (CommandDispatcher<Object>) wrapperMethod.invoke(dispatcherWrapper);
                                break;
                            }
                        }
                    }
                }
            } else {
                dispatcher = (CommandDispatcher<Object>) getDispatcherMethod.invoke(minecraftServer);
            }

            if (dispatcher != null) {
                isSupported = true;
            }
        } catch (Exception e) {
            isSupported = false;
        }
    }

    public boolean isSupported() {
        return isSupported;
    }

    @SuppressWarnings("unchecked")
    public void registerCommand(String commandName, CommandRegistry.CommandData[] methods) {
        if (!isSupported) return;

        try {
            LiteralArgumentBuilder<Object> root = LiteralArgumentBuilder.literal(commandName);

            for (CommandRegistry.CommandData data : methods) {
                String[] routeParts = data.getRoute().isEmpty() ? new String[0] : data.getRoute().split(" ");
                LiteralArgumentBuilder<Object> currentLiteral = root;

                if (routeParts.length > 0) {
                    LiteralArgumentBuilder<Object> routeNode = LiteralArgumentBuilder.literal(routeParts[routeParts.length - 1]);

                    for (int i = routeParts.length - 2; i >= 0; i--) {
                        LiteralArgumentBuilder<Object> parent = LiteralArgumentBuilder.literal(routeParts[i]);
                        parent.then(routeNode);
                        routeNode = parent;
                    }
                    currentLiteral = LiteralArgumentBuilder.literal(routeParts[0]);
                }

                RequiredArgumentBuilder<Object, ?> lastArg = null;
                Parameter[] parameters = data.getMethod().getParameters();

                for (int i = parameters.length - 1; i >= 0; i--) {
                    Parameter param = parameters[i];
                    if (param.isAnnotationPresent(Arg.class)) {
                        Arg arg = param.getAnnotation(Arg.class);
                        boolean isJoin = param.isAnnotationPresent(Join.class);

                        RequiredArgumentBuilder<Object, ?> argNode = RequiredArgumentBuilder.argument(
                                arg.value(),
                                getArgumentType(param.getType(), isJoin)
                        );

                        argNode.executes(context -> 1);

                        if (lastArg != null) {
                            argNode.then(lastArg);
                        }
                        lastArg = argNode;
                    }
                }

                if (lastArg != null) {
                    if (routeParts.length > 0) {
                        LiteralArgumentBuilder<Object> deepestLiteral = findDeepest(currentLiteral, routeParts);
                        deepestLiteral.then(lastArg);
                    } else {
                        root.then(lastArg);
                    }
                } else if (routeParts.length > 0) {
                    LiteralArgumentBuilder<Object> deepestLiteral = findDeepest(currentLiteral, routeParts);
                    deepestLiteral.executes(context -> 1);
                } else {
                    root.executes(context -> 1);
                }

                if (routeParts.length > 0) {
                    root.then(currentLiteral);
                }
            }

            LiteralCommandNode<Object> node = dispatcher.register(root);

            syncCommands();

        } catch (Exception ignored) {
        }
    }

    private LiteralArgumentBuilder<Object> findDeepest(LiteralArgumentBuilder<Object> root, String[] routes) {
        return root;
    }

    private com.mojang.brigadier.arguments.ArgumentType<?> getArgumentType(Class<?> type, boolean isJoin) {
        if (type.equals(Integer.class) || type.equals(int.class)) return IntegerArgumentType.integer();
        if (type.equals(Double.class) || type.equals(double.class)) return DoubleArgumentType.doubleArg();
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return BoolArgumentType.bool();
        if (isJoin) return StringArgumentType.greedyString();
        return StringArgumentType.word();
    }

    private void syncCommands() {
        try {
            for (Method method : minecraftServer.getClass().getMethods()) {
                if (method.getName().equals("syncCommands")) {
                    method.invoke(minecraftServer);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
}