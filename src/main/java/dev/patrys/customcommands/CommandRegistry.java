package dev.patrys.customcommands;

import dev.patrys.customcommands.annotation.*;
import dev.patrys.customcommands.argument.ArgumentResolverRegistry;
import dev.patrys.customcommands.platform.Platform;
import dev.patrys.customcommands.platform.PlatformSender;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandRegistry {
    private final Platform platform;
    private final ArgumentResolverRegistry resolverRegistry;
    private final Map<String, List<CommandData>> commands = new HashMap<>();
    private final Map<String, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public CommandRegistry(Platform platform) {
        this.platform = platform;
        this.resolverRegistry = new ArgumentResolverRegistry();
    }

    public void register(Object commandInstance) {
        Class<?> clazz = commandInstance.getClass();

        if (!clazz.isAnnotationPresent(Command.class)) {
            throw new IllegalArgumentException("Klasa musi być oznaczona @Command!");
        }

        Command command = clazz.getAnnotation(Command.class);
        String basePermission = clazz.isAnnotationPresent(Permission.class)
                ? clazz.getAnnotation(Permission.class).value()
                : null;

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Execute.class)) {
                Execute execute = method.getAnnotation(Execute.class);
                CommandData data = new CommandData(
                        commandInstance,
                        method,
                        execute.route(),
                        basePermission
                );

                String commandName = command.name();
                commands.computeIfAbsent(commandName, k -> new ArrayList<>()).add(data);

                for (String alias : command.aliases()) {
                    commands.computeIfAbsent(alias, k -> new ArrayList<>()).add(data);
                }
            }
        }

        platform.registerCommand(command.name(), (sender, args) -> {
            executeCommand(sender, command.name(), args);
        });

        for (String alias : command.aliases()) {
            platform.registerCommand(alias, (sender, args) -> {
                executeCommand(sender, alias, args);
            });
        }
    }

    private void executeCommand(PlatformSender sender, String commandName, String[] args) {
        List<CommandData> commandDataList = commands.get(commandName);

        if (commandDataList == null) {
            sender.sendMessage("§cKomenda nie znaleziona!");
            return;
        }

        CommandData matchedCommand = findMatchingCommand(commandDataList, args);

        if (matchedCommand == null) {
            sender.sendMessage("§cNieprawidłowe użycie komendy!");
            return;
        }

        if (!checkPermission(sender, matchedCommand)) {
            return;
        }

        if (!checkCooldown(sender, matchedCommand)) {
            return;
        }

        Runnable task = () -> {
            try {
                Object[] methodArgs = resolveArguments(sender, matchedCommand, args);
                matchedCommand.method.invoke(matchedCommand.instance, methodArgs);
            } catch (Exception e) {
                sender.sendMessage("§cWystąpił błąd: " + e.getMessage());
                e.printStackTrace();
            }
        };

        if (matchedCommand.method.isAnnotationPresent(Async.class)) {
            platform.scheduleAsync(task);
        } else {
            task.run();
        }
    }

    private CommandData findMatchingCommand(List<CommandData> commands, String[] args) {
        return commands.stream()
                .filter(cmd -> matchesRoute(cmd.route, args))
                .max(Comparator.comparingInt(cmd -> cmd.route.split(" ").length))
                .orElse(commands.stream()
                        .filter(cmd -> cmd.route.isEmpty())
                        .findFirst()
                        .orElse(null));
    }

    private boolean matchesRoute(String route, String[] args) {
        if (route.isEmpty()) {
            return true;
        }

        String[] routeParts = route.split(" ");
        if (args.length < routeParts.length) {
            return false;
        }

        for (int i = 0; i < routeParts.length; i++) {
            if (!routeParts[i].equals(args[i])) {
                return false;
            }
        }

        return true;
    }

    private boolean checkPermission(PlatformSender sender, CommandData data) {
        Permission permission = data.method.getAnnotation(Permission.class);

        if (permission == null && data.basePermission != null) {
            if (!sender.hasPermission(data.basePermission)) {
                sender.sendMessage("§cNie masz uprawnień!");
                return false;
            }
        } else if (permission != null) {
            if (!sender.hasPermission(permission.value())) {
                sender.sendMessage(permission.message());
                return false;
            }
        }

        return true;
    }

    private boolean checkCooldown(PlatformSender sender, CommandData data) {
        Cooldown cooldown = data.method.getAnnotation(Cooldown.class);

        if (cooldown == null) {
            return true;
        }

        String key = data.method.toString();
        Map<String, Long> userCooldowns = cooldowns.computeIfAbsent(sender.getName(), k -> new ConcurrentHashMap<>());

        Long lastUsed = userCooldowns.get(key);
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = cooldown.unit().toMillis(cooldown.value());

        if (lastUsed != null && currentTime - lastUsed < cooldownMillis) {
            long remaining = (cooldownMillis - (currentTime - lastUsed)) / 1000;
            String message = cooldown.message().replace("{time}", String.valueOf(remaining));
            sender.sendMessage(message);
            return false;
        }

        userCooldowns.put(key, currentTime);
        return true;
    }

    private Object[] resolveArguments(PlatformSender sender, CommandData data, String[] args) {
        Parameter[] parameters = data.method.getParameters();
        Object[] resolvedArgs = new Object[parameters.length];

        int routeLength = data.route.isEmpty() ? 0 : data.route.split(" ").length;
        int argIndex = routeLength;

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            if (type.equals(PlatformSender.class)) {
                resolvedArgs[i] = sender;
                continue;
            }

            if (type.equals(CommandContext.class)) {
                resolvedArgs[i] = new CommandContext(sender, args, data.route);
                continue;
            }

            if (param.isAnnotationPresent(Arg.class)) {
                Arg arg = param.getAnnotation(Arg.class);

                if (argIndex >= args.length) {
                    if (arg.required()) {
                        throw new IllegalArgumentException("§cBrak wymaganego argumentu: " + arg.value());
                    }
                    resolvedArgs[i] = null;
                    continue;
                }

                var resolver = resolverRegistry.getResolver(type);
                if (resolver == null) {
                    throw new IllegalStateException("Brak resolvera dla typu: " + type.getName());
                }

                resolvedArgs[i] = resolver.resolve(sender, args[argIndex]);
                argIndex++;
            }
        }

        return resolvedArgs;
    }

    public ArgumentResolverRegistry getResolverRegistry() {
        return resolverRegistry;
    }

    private static class CommandData {
        final Object instance;
        final Method method;
        final String route;
        final String basePermission;

        CommandData(Object instance, Method method, String route, String basePermission) {
            this.instance = instance;
            this.method = method;
            this.route = route;
            this.basePermission = basePermission;
            method.setAccessible(true);
        }
    }
}