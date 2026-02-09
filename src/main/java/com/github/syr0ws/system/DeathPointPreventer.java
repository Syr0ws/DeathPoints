package com.github.syr0ws.system;

import com.github.syr0ws.component.ComponentManager;
import com.github.syr0ws.data.DeathPointSettings;
import com.github.syr0ws.model.DeathPointManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerDeathPositionData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * System that prevents death point creation.
 */
public class DeathPointPreventer extends DeathSystems.OnDeathSystem {

    private static final Query<EntityStore> QUERY = Query.and(Player.getComponentType(), DeathPointSettings.getComponentType());

    private final DeathPointManager manager;

    public DeathPointPreventer(@Nonnull DeathPointManager manager) {
        this.manager = manager;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref,
                                 @Nonnull DeathComponent deathComponent,
                                 @Nonnull Store<EntityStore> store,
                                 @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        DeathPointSettings settings = store.getComponent(ref, DeathPointSettings.getComponentType());

        if (settings == null || !settings.isDisableDeathPoints()) {
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());

        World world = player.getWorld();

        if (world == null) {
            return;
        }

        PlayerConfigData configData = player.getPlayerConfigData();
        PlayerWorldData worldData = configData.getPerWorldData(world.getName());

        PlayerDeathPositionData deathPositionData = worldData.getDeathPositions().getLast();

        this.manager.removeDeathPoint(player, deathPositionData.getMarkerId());
    }

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency<>(Order.AFTER, DeathSystems.PlayerDeathMarker.class));
    }
}
