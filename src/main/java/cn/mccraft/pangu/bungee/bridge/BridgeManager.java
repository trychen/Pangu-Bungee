package cn.mccraft.pangu.bungee.bridge;

import cn.mccraft.pangu.bungee.Bridge;
import cn.mccraft.pangu.bungee.PanguBungee;
import cn.mccraft.pangu.bungee.data.ByteStreamPersistence;
import cn.mccraft.pangu.bungee.data.DataUtils;
import cn.mccraft.pangu.bungee.data.JsonPersistence;
import cn.mccraft.pangu.bungee.data.Persistence;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public enum BridgeManager implements Listener {
    INSTANCE;

    private Map<String, Solution> solutions = new HashMap<>();
    private Map<Class<? extends Persistence>, Persistence> persistences = new HashMap<Class<? extends Persistence>, Persistence>() {{
        put(JsonPersistence.class, JsonPersistence.INSTANCE);
        put(ByteStreamPersistence.class, ByteStreamPersistence.INSTANCE);
    }};

    public Persistence getPersistence(Class<? extends Persistence> clazz) {
        return persistences.get(clazz);
    }

    public Map<Class<? extends Persistence>, Persistence> getPersistences() {
        return persistences;
    }

    public void init() {
    }

    public void register(Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Bridge.class)) continue;
            Bridge bridge = method.getAnnotation(Bridge.class);

            try {
                solutions.put(bridge.value(), new Solution(object, method, persistences.get(bridge.persistence()), bridge.also()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPacket(PluginMessageEvent event) {
        if (!event.getTag().equals("pangu")) return;
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        if (!(event.getReceiver() instanceof Server)) return;
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(event.getData()));
        try {
            int id = input.readByte();
            byte[] keyBytes = new byte[DataUtils.readVarInt(input)];
            input.read(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            Solution solution = solutions.get(key);
            if (solution != null) {
                byte[] data = new byte[DataUtils.readVarInt(input)];
                input.read(data);

                solution.solve((ProxiedPlayer) event.getSender(), data);
                if (!solution.isAlso())event.setCancelled(true);
            }
        } catch (Exception e) {
            PanguBungee.getInstance().getLogger().log(Level.SEVERE, "error while solving @Bridge",e);
        }
    }

    public void send(Collection<ProxiedPlayer> players, String key, byte[] bytes) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeByte(0x01);

            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            DataUtils.writeVarInt(out, keyBytes.length);
            out.write(keyBytes);

            DataUtils.writeVarInt(out, bytes.length);
            out.write(bytes);

            for (byte aByte : bytes) {
                out.writeByte(aByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (ProxiedPlayer player : players) {
            player.sendData("pangu", b.toByteArray());
        }
    }

    public <T> T createProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(BridgeManager.class.getClassLoader(), new Class[]{clazz}, BridgeProxy.INSTANCE);
    }
}
