package dev.patrys.customcommands;

import dev.patrys.customcommands.annotation.*;
import dev.patrys.customcommands.argument.ArgumentResolver;
import dev.patrys.customcommands.argument.ArgumentResolverRegistry;
import dev.patrys.customcommands.exception.InvalidUsageException;
import dev.patrys.customcommands.exception.PlayerNotFoundException;
import dev.patrys.customcommands.handler.HandlerRegistry;
import dev.patrys.customcommands.platform.Platform;
import dev.patrys.customcommands.platform.PlatformSender;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandRegistry {
    private final Platform platform;
    private final ArgumentResolverRegistry resolverRegistry;
    private final HandlerRegistry handlerRegistry;
    private final Map<String, List<CommandData>> commands = new HashMap<>();
    private final Map<String, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public CommandRegistry(Platform platform) {
        this.platform = platform;
        this.resolverRegistry = new ArgumentResolverRegistry();
        this.handlerRegistry = new HandlerRegistry();
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

        platform.registerCommand(
                command.name(),
                (PlatformSender sender, String[] args) -> executeCommand(sender, command.name(), args),
                (PlatformSender sender, String[] args) -> getSuggestions(sender, command.name(), args)
        );

        for (String alias : command.aliases()) {
            platform.registerCommand(
                    alias,
                    (PlatformSender sender, String[] args) -> executeCommand(sender, alias, args),
                    (PlatformSender sender, String[] args) -> getSuggestions(sender, alias, args)
            );
        }
    }

    private List<String> getSuggestions(PlatformSender sender, String commandName, String[] args) {
        List<CommandData> commandDataList = commands.get(commandName);
        if (commandDataList == null || args.length == 0) return Collections.emptyList();

        Set<String> suggestions = new HashSet<>();
        String currentInput = args[args.length - 1];

        for (CommandData data : commandDataList) {
            if (!hasPermission(sender, data)) continue;

            String[] routeParts = data.route.isEmpty() ? new String[0] : data.route.split(" ");
            boolean matchesSoFar = true;

            for (int i = 0; i < args.length - 1; i++) {
                if (i < routeParts.length) {
                    if (!routeParts[i].equalsIgnoreCase(args[i])) {
                        matchesSoFar = false;
                        break;
                    }
                }
            }

            if (!matchesSoFar) continue;

            int currentIndex = args.length - 1;

            if (currentIndex < routeParts.length) {
                String expectedRoutePart = routeParts[currentIndex];
                if (expectedRoutePart.toLowerCase().startsWith(currentInput.toLowerCase())) {
                    suggestions.add(expectedRoutePart);
                }
            } else {
                int argIndex = currentIndex - routeParts.length;
                Parameter[] parameters = data.method.getParameters();

                int actualArgCount = 0;
                for (Parameter param : parameters) {
                    if (param.isAnnotationPresent(Arg.class)) {
                        boolean isJoin = param.isAnnotationPresent(Join.class);

                        if (actualArgCount == argIndex || (isJoin && argIndex >= actualArgCount)) {
                            ArgumentResolver<?> resolver = resolverRegistry.getResolver(param.getType());
                            if (resolver != null) {
                                suggestions.addAll(resolver.suggest(sender, currentInput));
                            }
                            break;
                        }
                        actualArgCount++;
                    }
                }
            }
        }

        List<String> sorted = new ArrayList<>(suggestions);
        Collections.sort(sorted);
        return sorted;
    }

    private void executeCommand(PlatformSender sender, String commandName, String[] args) {
        List<CommandData> commandDataList = commands.get(commandName);

        if (commandDataList == null) {
            sender.sendMessage("§cKomenda nie znaleziona!");
            return;
        }

        CommandData matchedCommand = findMatchingCommand(commandDataList, args);

        if (matchedCommand == null) {
            sendUsage(sender, commandName, commandDataList);
            return;
        }

        if (!checkPermissionAndNotify(sender, matchedCommand)) return;
        if (!checkCooldown(sender, matchedCommand)) return;

        Runnable task = () -> {
            try {
                Object[] methodArgs = resolveArguments(sender, matchedCommand, args);
                matchedCommand.method.invoke(matchedCommand.instance, methodArgs);
            } catch (InvalidUsageException e) {
                sendUsage(sender, commandName, Collections.singletonList(matchedCommand));
            } catch (PlayerNotFoundException e) {
                handlerRegistry.getPlayerNotFoundHandler().handle(sender, e.getTargetName());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(e.getMessage());
            } catch (Exception e) {
                // Jeśli błąd pochodzi ze środka metody komendy
                Throwable cause = e.getCause();
                if (cause instanceof InvalidUsageException) {
                    sendUsage(sender, commandName, Collections.singletonList(matchedCommand));
                } else if (cause instanceof PlayerNotFoundException) {
                    handlerRegistry.getPlayerNotFoundHandler().handle(sender, ((PlayerNotFoundException) cause).getTargetName());
                } else if (cause instanceof IllegalArgumentException) {
                    sender.sendMessage(cause.getMessage());
                } else {
                    sender.sendMessage("§cWystąpił błąd podczas wykonywania komendy!");
                    e.printStackTrace();
                }
            }
        };

        if (matchedCommand.method.isAnnotationPresent(Async.class)) {
            platform.scheduleAsync(task);
        } else {
            task.run();
        }
    }

    private CommandData findMatchingCommand(List<CommandData> commands, String[] args) {
        CommandData bestMatch = null;
        int bestRouteLength = -1;

        for (CommandData cmd : commands) {
            int routeLen = cmd.route.isEmpty() ? 0 : cmd.route.split(" ").length;

            if (matchesRoute(cmd.route, args)) {
                // Szukamy komendy z najdłuższą dopasowaną ścieżką
                if (routeLen > bestRouteLength) {
                    bestMatch = cmd;
                    bestRouteLength = routeLen;
                }
            }
        }

        return bestMatch;
    }

    private boolean matchesRoute(String route, String[] args) {
        if (route.isEmpty()) return true;

        String[] routeParts = route.split(" ");
        if (args.length < routeParts.length) return false;

        for (int i = 0; i < routeParts.length; i++) {
            if (!routeParts[i].equalsIgnoreCase(args[i])) return false;
        }

        return true;
    }

    private boolean hasPermission(PlatformSender sender, CommandData data) {
        Permission permission = data.method.getAnnotation(Permission.class);
        if (permission == null && data.basePermission != null) {
            return sender.hasPermission(data.basePermission);
        } else if (permission != null) {
            return sender.hasPermission(permission.value());
        }
        return true;
    }

    private boolean checkPermissionAndNotify(PlatformSender sender, CommandData data) {
        if (!hasPermission(sender, data)) {
            Permission permission = data.method.getAnnotation(Permission.class);
            String permString = permission != null ? permission.value() : (data.basePermission != null ? data.basePermission : "Brak informacji");
            handlerRegistry.getPermissionHandler().handle(sender, permString);
            return false;
        }
        return true;
    }

    private boolean checkCooldown(PlatformSender sender, CommandData data) {
        Cooldown cooldown = data.method.getAnnotation(Cooldown.class);
        if (cooldown == null) return true;

        String key = data.method.toString();
        Map<String, Long> userCooldowns = cooldowns.computeIfAbsent(sender.getName(), k -> new ConcurrentHashMap<>());

        Long lastUsed = userCooldowns.get(key);
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = cooldown.unit().toMillis(cooldown.value());

        if (lastUsed != null && currentTime - lastUsed < cooldownMillis) {
            long remaining = (cooldownMillis - (currentTime - lastUsed)) / 1000;
            handlerRegistry.getCooldownHandler().handle(sender, remaining);
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

        // Zliczanie oczekiwanych argumentów i sprawdzanie adnotacji Join
        int expectedArgs = routeLength;
        boolean hasJoin = false;
        for (Parameter p : parameters) {
            if (p.isAnnotationPresent(Arg.class)) {
                expectedArgs++;
                if (p.isAnnotationPresent(Join.class)) {
                    hasJoin = true;
                }
            }
        }

        // Jeśli gracz wpisał za dużo argumentów, rzucamy błąd (chyba że jest Join)
        if (args.length > expectedArgs && !hasJoin) {
            throw new InvalidUsageException();
        }

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
                boolean isJoin = param.isAnnotationPresent(Join.class);

                if (argIndex >= args.length) {
                    if (arg.required()) {
                        throw new InvalidUsageException();
                    }
                    resolvedArgs[i] = null;
                    continue;
                }

                String argumentToResolve;
                if (isJoin) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = argIndex; j < args.length; j++) {
                        if (j > argIndex) sb.append(" ");
                        sb.append(args[j]);
                    }
                    argumentToResolve = sb.toString();
                    argIndex = args.length;
                } else {
                    argumentToResolve = args[argIndex];
                    argIndex++;
                }

                ArgumentResolver<?> resolver = resolverRegistry.getResolver(type);
                if (resolver == null) {
                    throw new IllegalStateException("Brak resolvera dla typu: " + type.getName());
                }

                resolvedArgs[i] = resolver.resolve(sender, argumentToResolve);
            }
        }

        return resolvedArgs;
    }

    private void sendUsage(PlatformSender sender, String commandName, List<CommandData> commands) {
        List<CommandData> allowedCommands = new ArrayList<>();
        for (CommandData cmd : commands) {
            if (hasPermission(sender, cmd)) {
                allowedCommands.add(cmd);
            }
        }

        if (allowedCommands.isEmpty()) {
            handlerRegistry.getPermissionHandler().handle(sender, "Nieznane uprawnienie");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (allowedCommands.size() == 1) {
            sb.append("§7/").append(commandName).append(" ").append(buildCommandUsage(allowedCommands.get(0)));
        } else {
            for (CommandData cmd : allowedCommands) {
                sb.append("§8- §7/").append(commandName).append(" ").append(buildCommandUsage(cmd)).append("\n");
            }
        }

        handlerRegistry.getUsageHandler().handle(sender, sb.toString().trim());
    }

    private String buildCommandUsage(CommandData data) {
        StringBuilder sb = new StringBuilder();

        if (!data.route.isEmpty()) {
            sb.append(data.route).append(" ");
        }

        for (Parameter param : data.method.getParameters()) {
            if (param.isAnnotationPresent(Arg.class)) {
                Arg arg = param.getAnnotation(Arg.class);
                boolean isJoin = param.isAnnotationPresent(Join.class);

                String argName = arg.value() + (isJoin ? "..." : "");

                if (arg.required()) {
                    sb.append("<").append(argName).append("> ");
                } else {
                    sb.append("[").append(argName).append("] ");
                }
            }
        }

        return sb.toString().trim();
    }

    public ArgumentResolverRegistry getResolverRegistry() {
        return resolverRegistry;
    }

    public HandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
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