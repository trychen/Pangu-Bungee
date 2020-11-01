package cn.mccraft.pangu.bungee.bridge;

import cn.mccraft.pangu.bungee.Bridge;
import cn.mccraft.pangu.bungee.PanguBungee;
import cn.mccraft.pangu.bungee.Side;
import cn.mccraft.pangu.bungee.data.DataUtils;
import cn.mccraft.pangu.bungee.util.BufUtils;
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
    public void register(Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (!method.isAnnotationPresent(Bridge.class)) continue;
            Bridge bridge = method.getAnnotation(Bridge.class);

            try {
                solutions.put(bridge.value(), new Solution(object, method, PanguBungee.getPersistence(bridge.persistence()), bridge));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPacket(PluginMessageEvent event) {
        if (event.getData()[0] >= 2) return;
        if (event.getSender() instanceof Server) return;
        if (!event.getTag().equals("pangu")) return;
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(event.getData()));
        try {
            int id = input.readByte();
            String key = BufUtils.readString(input);

            Solution solution = solutions.get(key);
            if (solution != null) {
                if (solution.getSide() == Side.SERVER && !(event.getSender() instanceof Server)) return;
                if (solution.getSide() == Side.PLAYER && !(event.getSender() instanceof ProxiedPlayer)) return;

                byte[] data = new byte[DataUtils.readVarInt(input)];
                input.read(data);

                if (!solution.solve((ProxiedPlayer) event.getSender(), data) || !solution.isAlso()) event.setCancelled(true);
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
