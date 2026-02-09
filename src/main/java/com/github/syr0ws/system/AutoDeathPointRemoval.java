package com.github.syr0ws.system;

import com.github.syr0ws.component.ComponentManager;
import com.github.syr0ws.data.DeathPointSettings;
import com.github.syr0ws.model.DeathPointManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerDeathPositionData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * System that automatically removes a death point when a player has reached its position.
 */
public class AutoDeathPointRemoval extends EntityTickingSystem<EntityStore> {

    private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(),
            TransformComponent.getComponentType(), DeathPointSettings.getComponentType());

    private static final int DEATH_POINT_REMOVAL_DISTANCE_SQUARED = 25; // 5 blocks

    private final DeathPointManager manager;

    public AutoDeathPointRemoval(@Nonnull DeathPointManager manager) {
        this.manager = manager;
    }

    @Override
    public void tick(float dt, int index,
                     @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        DeathPointSettings settings = archetypeChunk.getComponent(index, DeathPointSettings.getComponentType());

        if (settings == null || !settings.isAutoDeleteDeathPoints()) {
            return;
        }

        DeathComponent deathComponent = archetypeChunk.getComponent(index, DeathComponent.getComponentType());

        // This code is called every tick. Therefore, when a player is dead, its position will be considered the same
        // as the death point and will be directly removed. Checking that the player is not dead (e.g. does not have
        // a death component) prevents that behavior.
        if (deathComponent != null) {
            return;
        }

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

        if (player == null || transform == null) {
            return;
        }

        // Not calling {@link DeathPointManager#getDeathPoints(Player)} in order to avoid too many object
        // creations as this code is called quite often.
        List<PlayerDeathPositionData> deathPositionDataList = this.getDeathPoints(player);

        for (PlayerDeathPositionData data : deathPositionDataList) {

            Vector3d deathPointPosition = data.getTransform().getPosition();

            // Distance squared is lighter to compute than the actual distance.
            double distanceSquared = deathPointPosition.distanceSquaredTo(transform.getPosition());

            if (distanceSquared <= DEATH_POINT_REMOVAL_DISTANCE_SQUARED) {

                this.manager.removeDeathPoint(player, data.getMarkerId());

                PlayerRef playerRef = store.getComponent(player.getReference(), PlayerRef.getComponentType());

                NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(),
                        Message.translation("deathpoints.notification.deathpoint.autoremoved"),
                        NotificationStyle.Success
                );
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    private List<PlayerDeathPositionData> getDeathPoints(@Nonnull Player player) {

        World world = player.getWorld();

        if (world == null) {
            return Collections.emptyList();
        }

        PlayerConfigData configData = player.getPlayerConfigData();
        PlayerWorldData worldData = configData.getPerWorldData(world.getName());

        return worldData.getDeathPositions();
    }
}
