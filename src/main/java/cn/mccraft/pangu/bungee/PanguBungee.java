package cn.mccraft.pangu.bungee;

import cn.mccraft.pangu.bungee.bridge.BridgeManager;
import cn.mccraft.pangu.bungee.data.ByteStreamPersistence;
import cn.mccraft.pangu.bungee.data.JsonPersistence;
import cn.mccraft.pangu.bungee.data.Persistence;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PanguBungee extends Plugin {
    private static PanguBungee instance;

    private static Map<Class<? extends Persistence>, Persistence> persistences = new HashMap<Class<? extends Persistence>, Persistence>() {{
        put(JsonPersistence.class, JsonPersistence.INSTANCE);
        put(ByteStreamPersistence.class, ByteStreamPersistence.INSTANCE);
    }};

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getProxy().registerChannel("pangu");
        getProxy().getPluginManager().registerListener(this, BridgeManager.INSTANCE);
    }

    public static PanguBungee getInstance() {
        return instance;
    }

    public static Map<Class<? extends Persistence>, Persistence> getPersistences() {
        return persistences;
    }

    public static Persistence getPersistence(Class<? extends Persistence> clazz) {
        return persistences.get(clazz);
    }
}
