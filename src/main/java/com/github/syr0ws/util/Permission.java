package com.github.syr0ws.util;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;

import javax.annotation.Nonnull;

public class Permission {

    private static final String BASE_PERMISSION = "com.github.syr0ws.deathpoints";

    public static final String DEATH_POINT_TELEPORT = register("teleport");

    private static String register(@Nonnull String permission) {
        PermissionsModule.registerPermission(BASE_PERMISSION + "." + permission);
        return permission;
    }
}
