package com.github.syr0ws.model;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerDeathPositionData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager class for Players' death points.
 */
public class DeathPointManager {

    /**
     * Removes a player's death point by id.
     *
     * @param player   the player for whom the death point is removed
     * @param markerId the marker id of the death point to remove
     */
    public void removeDeathPoint(@Nonnull Player player, @Nonnull String markerId) {

        World world = player.getWorld();

        if (world == null) {
            return;
        }

        PlayerConfigData configData = player.getPlayerConfigData();
        PlayerWorldData worldData = configData.getPerWorldData(world.getName());

        List<PlayerDeathPositionData> deathPositionDataList = worldData.getDeathPositions();
        deathPositionDataList.removeIf(deathPositionData -> deathPositionData.getMarkerId().equals(markerId));

        configData.markChanged();

        this.sendRemovedDeathPoints(player, markerId);
    }

    /**
     * Clears player's death points.
     *
     * @param player the player for whom death points are removed
     */
    public void clearDeathPoints(@Nonnull Player player) {

        World world = player.getWorld();

        if (world == null) {
            return;
        }

        PlayerConfigData configData = player.getPlayerConfigData();
        PlayerWorldData worldData = configData.getPerWorldData(world.getName());

        List<PlayerDeathPositionData> deathPositionDataList = worldData.getDeathPositions();

        // Retrieve marker ids to be able to remove them from the HUD.
        String[] deathMarkerIds = deathPositionDataList.stream()
                .map(PlayerDeathPositionData::getMarkerId)
                .toArray(String[]::new);

        // Remove death positions.
        deathPositionDataList.clear();
        configData.markChanged();

        // Remove markers from the compass HUD.
        this.sendRemovedDeathPoints(player, deathMarkerIds);
    }

    /**
     * Gets player's death points.
     *
     * @param player the player for whom death points are retrieved
     * @return a mutable list containing the player's death points
     */
    public List<DeathPoint> getDeathPoints(@Nonnull Player player) {

        World world = player.getWorld();

        if (world == null) {
            return Collections.emptyList();
        }

        PlayerConfigData configData = player.getPlayerConfigData();
        PlayerWorldData worldData = configData.getPerWorldData(world.getName());

        List<PlayerDeathPositionData> deathPositionDataList = worldData.getDeathPositions();

        return deathPositionDataList.stream()
                .map(this::getDeathPoint)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Update the player's compass / map to remove death points.
     *
     * @param player    the player for whom the UI is updated
     * @param markerIds the list of death point ids to remove
     */
    private void sendRemovedDeathPoints(@Nonnull Player player, @Nonnull String... markerIds) {

        Ref<EntityStore> ref = player.getReference();

        if (ref == null) {
            throw new IllegalStateException("Cannot retrieve Ref");
        }

        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null) {
            throw new IllegalStateException("Cannot retrieve PlayerRef");
        }

        PacketHandler packetHandler = playerRef.getPacketHandler();
        packetHandler.writeNoCache(new UpdateWorldMap(null, new MapMarker[0], markerIds));
    }

    /**
     * Instantiates a new {@link DeathPoint} from a {@link PlayerDeathPositionData}.
     *
     * @param data the player death position data
     * @return a {@link DeathPoint} object
     */
    private DeathPoint getDeathPoint(@Nonnull PlayerDeathPositionData data) {

        String markerId = data.getMarkerId();
        int day = data.getDay();

        Transform transform = data.getTransform();
        Vector3d position = transform.getPosition();

        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        return new DeathPoint(markerId, day, x, y, z);
    }
}
