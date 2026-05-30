package com.github.syr0ws.command;

import com.github.syr0ws.model.DeathPointManager;
import com.github.syr0ws.ui.DeathPointsPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Class for handling the /deathpoints command.
 */
public class DeathPointsCommand extends AbstractPlayerCommand {

    private static final String COMMAND_NAME = "deathpoints";
    private static final String[] COMMAND_ALIASES = {"deathpoint", "dp"};

    private final DeathPointManager manager;

    public DeathPointsCommand(@Nonnull DeathPointManager manager) {
        super(COMMAND_NAME, "deathpoints.command.description");
        super.addAliases(COMMAND_ALIASES);
        super.setAllowsExtraArguments(true);
        this.manager = manager;
    }

    @Override
    protected void execute(@Nonnull CommandContext context,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        Player player = store.getComponent(ref, Player.getComponentType());

        DeathPointsPage page = new DeathPointsPage(playerRef, this.manager);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
