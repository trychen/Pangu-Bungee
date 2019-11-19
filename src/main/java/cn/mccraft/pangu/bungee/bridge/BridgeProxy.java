package cn.mccraft.pangu.bungee.bridge;
import cn.mccraft.pangu.bungee.Bridge;
import cn.mccraft.pangu.bungee.PanguBungee;
import cn.mccraft.pangu.bungee.data.Persistence;
import cn.mccraft.pangu.bungee.util.ArrayUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public enum BridgeProxy implements InvocationHandler {
    INSTANCE;

    @SuppressWarnings("Duplicates")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.isDefault()) {
            PanguBungee.getInstance().getLogger().log(Level.SEVERE, "default method isn't support in this version", new IllegalAccessException());
            return null;
        }

        Bridge bridge = method.getAnnotation(Bridge.class);

        if (bridge == null) return null;

        Type[] types = method.getGenericParameterTypes();
        Collection<ProxiedPlayer> players = new HashSet<>();
        boolean addAllPlayers = true;

        if (args.length > 0) {
            if (args[0] instanceof ProxiedPlayer) {
                players.add((ProxiedPlayer) args[0]);
                addAllPlayers = false;
            } else if (args[0] instanceof Collection) {
                if (ProxiedPlayer.class.isAssignableFrom((Class<?>) ((ParameterizedType)types[0]).getActualTypeArguments()[0])) {
                    players.addAll((Collection<? extends ProxiedPlayer>) args[0]);
                    addAllPlayers = false;
                }
            } else if (args[0].getClass().isArray() && ProxiedPlayer.class.isAssignableFrom(args.getClass().getComponentType())) {
                Collections.addAll(players, (ProxiedPlayer[]) args[0]);
                addAllPlayers = false;
            }
        }
        String[] names = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);

        if (addAllPlayers) {
            players = PanguBungee.getInstance().getProxy().getPlayers();
        } else {
            args = ArrayUtils.remove(args, 0);
            types = (Type[]) ArrayUtils.remove(types, 0);
            names = (String[]) ArrayUtils.remove(names, 0);
        }

        Persistence persistence = PanguBungee.getPersistence(bridge.persistence());
        byte[] bytes = persistence.serialize(names, args, types, true);

        BridgeManager.INSTANCE.send(players, bridge.value(), bytes);
        return null;
    }
}
