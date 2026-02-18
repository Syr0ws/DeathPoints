package com.github.syr0ws.data;

import com.github.syr0ws.component.ComponentManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Component to store player's death point settings.
 */
public class DeathPointSettings implements Component<EntityStore> {

    /**
     * Codec to for serialization / deserialization.
     */
    public static final BuilderCodec<DeathPointSettings> CODEC = BuilderCodec.builder(
                    DeathPointSettings.class,
                    DeathPointSettings::new
            )
            .append(new KeyedCodec<>("DisableDeathPoints", Codec.BOOLEAN),
                    DeathPointSettings::setDisableDeathPoints,
                    data -> data.disableDeathPoints)
            .add()
            .append(new KeyedCodec<>("AutoDeleteDeathPoints", Codec.BOOLEAN),
                    DeathPointSettings::setAutoDeleteDeathPoints,
                    data -> data.autoDeleteDeathPoints)
            .add()
            .build();

    private boolean disableDeathPoints = false;
    private boolean autoDeleteDeathPoints = false;

    /**
     * Gets the {@link ComponentType} associated with {@link DeathPointSettings}.
     *
     * @return a {@code ComponentType<EntityStore, DeathPointSettings>}.
     */
    public static ComponentType<EntityStore, DeathPointSettings> getComponentType() {
        return ComponentManager.get().getDeathPointSettingsComponentType();
    }

    /**
     * Returns whether death points creation is disabled.
     *
     * @return {@code true} if death points creation is disabled, {@code false} otherwise
     */
    public boolean isDisableDeathPoints() {
        return this.disableDeathPoints;
    }

    /**
     * Sets whether death points creation is disabled.
     *
     * @param disableDeathPoints {@code true} to disable death points creation, {@code false} otherwise
     */
    public void setDisableDeathPoints(boolean disableDeathPoints) {
        this.disableDeathPoints = disableDeathPoints;
    }

    /**
     * Returns whether automatic death point removal when a player reaches a death point is enabled.
     *
     * @return {@code true} if automatic death point removal is enabled, {@code false} otherwise
     */
    public boolean isAutoDeleteDeathPoints() {
        return this.autoDeleteDeathPoints;
    }

    /**
     * Sets whether automatic death point removal when a player reaches a death point is enabled.
     *
     * @param autoDeleteDeathPoints {@code true} to enable automatic death point removal, {@code false} otherwise
     */
    public void setAutoDeleteDeathPoints(boolean autoDeleteDeathPoints) {
        this.autoDeleteDeathPoints = autoDeleteDeathPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component<EntityStore> clone() {
        DeathPointSettings settings = new DeathPointSettings();
        settings.setDisableDeathPoints(this.disableDeathPoints);
        settings.setAutoDeleteDeathPoints(this.autoDeleteDeathPoints);
        return settings;
    }
}
