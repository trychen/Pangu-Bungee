package cn.mccraft.pangu.bungee;

import cn.mccraft.pangu.bungee.data.JsonPersistence;
import cn.mccraft.pangu.bungee.data.Persistence;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bridge {
    /**
     * message key
     */
    String value();

    boolean also() default false;

    Class<? extends Persistence> persistence() default JsonPersistence.class;
}