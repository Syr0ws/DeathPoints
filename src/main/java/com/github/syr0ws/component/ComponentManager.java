package com.github.syr0ws.component;

import com.github.syr0ws.data.DeathPointSettings;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Class for registering and retrieving custom ECS components.
 */
public class ComponentManager {

    private static ComponentManager instance;

    private ComponentType<EntityStore, DeathPointSettings> deathPointSettingsComponentType;

    /**
     * Register custom ECS components.
     *
     * @param registry the registry in which registering components.
     */
    public void registerComponents(ComponentRegistryProxy<EntityStore> registry) {
        this.deathPointSettingsComponentType = registry.registerComponent(
                DeathPointSettings.class, "DeathPointSettings", DeathPointSettings.CODEC);
    }

    /**
     * Gets the {@link ComponentType} associated with {@link DeathPointSettings}.
     *
     * @return a {@code ComponentType<EntityStore, DeathPointSettings>}.
     */
    public ComponentType<EntityStore, DeathPointSettings> getDeathPointSettingsComponentType() {
        return this.deathPointSettingsComponentType;
    }

    /**
     * Gets the {@link ComponentManager} instance.
     *
     * @return an instance of {@link ComponentManager}
     */
    public static ComponentManager get() {

        if (instance == null) {
            instance = new ComponentManager();
        }

        return instance;
    }
}
