package com.github.syr0ws.ui;

import com.github.syr0ws.data.DeathPointSettings;
import com.github.syr0ws.model.DeathPoint;
import com.github.syr0ws.model.DeathPointManager;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Class for handling the DeathPointsPage UI.
 */
public class DeathPointsPage extends InteractiveCustomUIPage<DeathPointsPage.DeathPointsPageEventData> {

    private static final String DEATH_POINTS_PAGE_UI = "DeathPoints/DeathPointsPage.ui";
    private static final String DEATH_POINT_ENTRY_UI = "DeathPoints/DeathPointEntry.ui";

    private static final Value<String> TAB_STYLE_ACTIVE = Value.ref("DeathPoints/DeathPointsPage.ui", "ActiveTabButtonStyle");
    private static final Value<String> TAB_STYLE_INACTIVE = Value.ref("Common.ui", "SecondaryButtonStyle");

    private final DeathPointManager manager;

    public DeathPointsPage(@Nonnull PlayerRef playerRef, @Nonnull DeathPointManager manager) {
        super(playerRef, CustomPageLifetime.CanDismiss, DeathPointsPageEventData.CODEC);
        this.manager = manager;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmdBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        cmdBuilder.append(DEATH_POINTS_PAGE_UI);

        this.buildDeathPointListTab(ref, cmdBuilder, eventBuilder, store);
        this.buildDeathPointSettingsTab(ref, cmdBuilder, store);
        this.bindEvents(eventBuilder);
    }

    private void bindEvents(@Nonnull UIEventBuilder eventBuilder) {

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabDeathPointListButton",
                EventData.of("Action", Action.OPEN_TAB_DEATH_POINT_LIST.name())
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabSettingsButton",
                EventData.of("Action", Action.OPEN_TAB_SETTINGS.name())
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#DeathPointListTab #ClearButton",
                EventData.of("Action", Action.CLEAR_DEATH_POINTS.name())
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#DeathPointSettingsTab #SaveButton",
                new EventData().append("Action", Action.SAVE_SETTINGS.name())
                        .append("@DisableDeathPoint", "#DisableDeathPointSetting #CheckBox.Value")
                        .append("@AutoDeleteDeathPoint", "#AutoDeleteDeathPointSetting #CheckBox.Value")
        );
    }

    private void buildDeathPointListTab(@Nonnull Ref<EntityStore> ref,
                                        @Nonnull UICommandBuilder cmdBuilder,
                                        @Nonnull UIEventBuilder eventBuilder,
                                        @Nonnull Store<EntityStore> store) {

        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            throw new IllegalStateException("Cannot retrieve Player from Store");
        }

        List<DeathPoint> deathPointList = this.manager.getDeathPoints(player);
        this.handleDeathPointListTabComponentsVisibility(deathPointList, cmdBuilder);

        if (!deathPointList.isEmpty()) {
            this.addDeathPointEntries(deathPointList, cmdBuilder, eventBuilder);
        }
    }

    private void handleDeathPointListTabComponentsVisibility(@Nonnull List<DeathPoint> deathPointList, @Nonnull UICommandBuilder cmdBuilder) {
        cmdBuilder.set("#DeathPointListTab #NoDeathPointLabel.Visible", deathPointList.isEmpty());
        cmdBuilder.set("#DeathPointListTab #DeathPointList.Visible", !deathPointList.isEmpty());
        cmdBuilder.set("#DeathPointListTab #ClearButton.Visible", !deathPointList.isEmpty());
    }

    private void buildDeathPointSettingsTab(@Nonnull Ref<EntityStore> ref,
                                            @Nonnull UICommandBuilder cmdBuilder,
                                            @Nonnull Store<EntityStore> store) {

        DeathPointSettings settings = store.ensureAndGetComponent(ref, DeathPointSettings.getComponentType());

        cmdBuilder.set("#DisableDeathPointSetting #CheckBox.Value", settings.isDisableDeathPoints());
        cmdBuilder.set("#AutoDeleteDeathPointSetting #CheckBox.Value", settings.isAutoDeleteDeathPoints());
    }

    private void addDeathPointEntries(@Nonnull List<DeathPoint> deathPointList, @Nonnull UICommandBuilder cmdBuilder, @Nonnull UIEventBuilder eventBuilder) {
        cmdBuilder.clear("#DeathPointList");

        for (int i = 0; i < deathPointList.size(); i++) {
            DeathPoint deathPoint = deathPointList.get(i);

            cmdBuilder.append("#DeathPointList", DEATH_POINT_ENTRY_UI);

            String selector = "#DeathPointList[%d]".formatted(i);

            cmdBuilder.set(selector + " #DeathPointContent #DeathPointTitle.Text",
                    Message.translation("deathpoints.ui.menu.entry.title").param("day", deathPoint.day()));

            cmdBuilder.set(selector + " #DeathPointCoordinates #CoordX.Text",
                    Message.translation("deathpoints.ui.menu.entry.content.coordinate.x").param("x", (int) deathPoint.x()));

            cmdBuilder.set(selector + " #DeathPointCoordinates #CoordY.Text",
                    Message.translation("deathpoints.ui.menu.entry.content.coordinate.y").param("y", (int) deathPoint.y()));

            cmdBuilder.set(selector + " #DeathPointCoordinates #CoordZ.Text",
                    Message.translation("deathpoints.ui.menu.entry.content.coordinate.z").param("z", (int) deathPoint.z()));

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #RemoveButton",
                    new EventData().append("Action", Action.REMOVE_DEATH_POINT.name())
                            .append("DeathPointIndex", String.valueOf(i))
            );
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull DeathPointsPageEventData data) {

        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            throw new IllegalStateException("Cannot retrieve Player from Store");
        }

        String action = data.action();

        if (action != null) {

            if (action.equals(Action.CLEAR_DEATH_POINTS.name())) {
                this.handleClear(player);

            } else if (action.equals(Action.SAVE_SETTINGS.name())) {
                this.handleSettingsSave(ref, store, data);

            } else if (action.equals(Action.REMOVE_DEATH_POINT.name())) {
                this.handleRemove(player, data);

            } else if (action.equals(Action.OPEN_TAB_DEATH_POINT_LIST.name()) || action.equals(Action.OPEN_TAB_SETTINGS.name())) {
                this.handleTabUpdate(action);
            }
        }
    }

    private void handleClear(@Nonnull Player player) {

        this.manager.clearDeathPoints(player);

        super.close();

        NotificationUtil.sendNotification(
                super.playerRef.getPacketHandler(),
                Message.translation("deathpoints.notification.deathpoints.cleared"),
                NotificationStyle.Success
        );
    }

    private void handleRemove(@Nonnull Player player, @Nonnull DeathPointsPageEventData data) {

        int index = data.deathPointIndex();

        List<DeathPoint> deathPointList = this.manager.getDeathPoints(player);
        DeathPoint deathPoint = deathPointList.remove(index);

        this.manager.removeDeathPoint(player, deathPoint.markerId());

        UICommandBuilder cmdBuilder = new UICommandBuilder();
        cmdBuilder.remove("#DeathPointList[%d]".formatted(index));

        this.handleDeathPointListTabComponentsVisibility(deathPointList, cmdBuilder);

        sendUpdate(cmdBuilder);

        NotificationUtil.sendNotification(
                super.playerRef.getPacketHandler(),
                Message.translation("deathpoints.notification.deathpoint.removed"),
                NotificationStyle.Success
        );
    }

    private void handleTabUpdate(@Nonnull String action) {

        UICommandBuilder cmdBuilder = new UICommandBuilder();

        cmdBuilder.set("#TabDeathPointListButton.Style", action.equals(Action.OPEN_TAB_DEATH_POINT_LIST.name()) ? TAB_STYLE_ACTIVE : TAB_STYLE_INACTIVE);
        cmdBuilder.set("#TabSettingsButton.Style", action.equals(Action.OPEN_TAB_SETTINGS.name()) ? TAB_STYLE_ACTIVE : TAB_STYLE_INACTIVE);

        cmdBuilder.set("#DeathPointListTab.Visible", action.equals(Action.OPEN_TAB_DEATH_POINT_LIST.name()));
        cmdBuilder.set("#DeathPointSettingsTab.Visible", action.equals(Action.OPEN_TAB_SETTINGS.name()));

        sendUpdate(cmdBuilder);
    }

    private void handleSettingsSave(@Nonnull Ref<EntityStore> ref,
                                    @Nonnull Store<EntityStore> store,
                                    @Nonnull DeathPointsPageEventData data) {

        DeathPointSettings settings = store.ensureAndGetComponent(ref, DeathPointSettings.getComponentType());
        settings.setDisableDeathPoints(data.disableDeathPoint());
        settings.setAutoDeleteDeathPoints(data.autoDeleteDeathPoint());

        super.close();

        NotificationUtil.sendNotification(
                super.playerRef.getPacketHandler(),
                Message.translation("deathpoints.notification.setting.save"),
                NotificationStyle.Success
        );
    }

    private enum Action {

        REMOVE_DEATH_POINT, CLEAR_DEATH_POINTS, SAVE_SETTINGS, OPEN_TAB_DEATH_POINT_LIST, OPEN_TAB_SETTINGS
    }

    public static class DeathPointsPageEventData {

        public static final BuilderCodec<DeathPointsPageEventData> CODEC = BuilderCodec.builder(
                        DeathPointsPageEventData.class, DeathPointsPageEventData::new)
                .append(new KeyedCodec<>("Action", BuilderCodec.STRING), (data, value) -> data.action = value, data -> data.action)
                .add()
                .append(new KeyedCodec<>("DeathPointIndex", BuilderCodec.STRING), (data, value) -> data.deathPointIndex = Integer.parseInt(value), data -> String.valueOf(data.deathPointIndex))
                .add()
                .append(new KeyedCodec<>("@DisableDeathPoint", BuilderCodec.BOOLEAN), (data, value) -> data.disableDeathPoint = value, data -> data.disableDeathPoint)
                .add()
                .append(new KeyedCodec<>("@AutoDeleteDeathPoint", BuilderCodec.BOOLEAN), (data, value) -> data.autoDeleteDeathPoint = value, data -> data.autoDeleteDeathPoint)
                .add()
                .build();
        private String action;
        private int deathPointIndex;
        private boolean disableDeathPoint;
        private boolean autoDeleteDeathPoint;

        public DeathPointsPageEventData() {
        }

        public String action() {
            return this.action;
        }

        public int deathPointIndex() {
            return this.deathPointIndex;
        }

        public boolean disableDeathPoint() {
            return this.disableDeathPoint;
        }

        public boolean autoDeleteDeathPoint() {
            return this.autoDeleteDeathPoint;
        }
    }
}
