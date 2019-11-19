package cn.mccraft.pangu.bungee.util;

import cn.mccraft.pangu.bungee.data.DataUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface BufUtils {
    static String readString(DataInputStream input) throws IOException {
        byte[] keyBytes = new byte[DataUtils.readVarInt(input)];
        input.read(keyBytes);
        return new String(keyBytes, StandardCharsets.UTF_8);
    }
}
