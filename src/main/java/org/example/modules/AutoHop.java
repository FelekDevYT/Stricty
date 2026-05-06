package org.example.modules;

import net.minecraft.world.level.block.SlabBlock;
import org.example.ExamplePlugin;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;

public class AutoHop extends ToggleableModule {
    private final BooleanSetting noSlabs = new BooleanSetting("NoSlabs", true);

    public AutoHop() {
        super("AutoHop", "Automatically hops when on ground", ExamplePlugin.LEGIT_CATEGORY);
        registerSettings(noSlabs);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.onGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
            if (noSlabs.getValue() && mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() instanceof SlabBlock) {
                return;
            }

            mc.player.jumpFromGround();
        }
    }
}
