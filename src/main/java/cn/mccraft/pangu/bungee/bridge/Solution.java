package cn.mccraft.pangu.bungee.bridge;

import cn.mccraft.pangu.bungee.data.Persistence;
import cn.mccraft.pangu.bungee.util.ArrayUtils;
import com.github.mouse0w0.fastreflection.FastReflection;
import com.github.mouse0w0.fastreflection.MethodAccessor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;

public class Solution {
    private Object instance;
    private Method method;
    private Persistence persistence;

    private boolean withPlayer;
    private Type[] actualParameterTypes;
    private String[] actualParameterNames;
    private MethodAccessor methodAccessor;
    private boolean also;

    public Solution(Object instance, Method method, Persistence persistence, boolean also) throws Exception {
        this.instance = instance;
        this.method = method;
        this.persistence = persistence;
        this.also = also;
        this.withPlayer = method.getParameterCount() > 0 && ProxiedPlayer.class.isAssignableFrom(method.getParameterTypes()[0]);
        this.actualParameterTypes = withPlayer?(Type[]) ArrayUtils.remove(method.getGenericParameterTypes(), 0):method.getGenericParameterTypes();
        this.actualParameterNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
        if (withPlayer) {
            this.actualParameterNames = (String[]) ArrayUtils.remove(actualParameterNames, 0);
        }

        this.methodAccessor = FastReflection.create(method);
    }

    public void solve(ProxiedPlayer player, byte[] bytes) throws Exception {
        Object[] objects = persistence.deserialize(actualParameterNames, bytes, actualParameterTypes);
        if (withPlayer) {
            objects = ArrayUtils.add(objects, 0, player);
        }
        methodAccessor.invoke(instance, objects);
    }

    public boolean isAlso() {
        return also;
    }
}
