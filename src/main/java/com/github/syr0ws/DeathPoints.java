package com.github.syr0ws;

import com.github.syr0ws.command.DeathPointsCommand;
import com.github.syr0ws.component.ComponentManager;
import com.github.syr0ws.model.DeathPointManager;
import com.github.syr0ws.system.AutoDeathPointRemoval;
import com.github.syr0ws.system.DeathPointPreventer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DeathPoints extends JavaPlugin {

    private final DeathPointManager manager = new DeathPointManager();

    public DeathPoints(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.registerCommands();
        this.registerComponents();
        this.registerSystems();
        super.setup();
    }

    private void registerCommands() {
        CommandRegistry registry = super.getCommandRegistry();
        registry.registerCommand(new DeathPointsCommand(this.manager));
    }

    private void registerComponents() {
        ComponentRegistryProxy<EntityStore> registry = super.getEntityStoreRegistry();
        ComponentManager.get().registerComponents(registry);
    }

    private void registerSystems() {
        ComponentRegistryProxy<EntityStore> registry = super.getEntityStoreRegistry();
        registry.registerSystem(new DeathPointPreventer(this.manager));
        registry.registerSystem(new AutoDeathPointRemoval(this.manager));
    }
}
