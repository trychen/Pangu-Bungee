package cn.mccraft.pangu.bungee;

import cn.mccraft.pangu.bungee.bridge.BridgeManager;
import net.md_5.bungee.api.plugin.Plugin;

public class PanguBungee extends Plugin {
    private static PanguBungee instance;

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
}
