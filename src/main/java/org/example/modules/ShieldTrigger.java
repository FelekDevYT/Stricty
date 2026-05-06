package org.example.modules;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.HitResult;
import org.example.ExamplePlugin;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

public class ShieldTrigger extends ToggleableModule {
    private NumberSetting<Integer> swapDelay = new NumberSetting<>("Swap Delay", 4, 0, 20);
    private BooleanSetting swapBack = new BooleanSetting("Swap Back", true);

    private int previousSlot = -1;
    private int timer = 0;

    public ShieldTrigger() {
        super("ShieldTrigger", "Swaps to axe when hitting a shielded player", ExamplePlugin.LEGIT_CATEGORY);
        registerSettings(swapDelay, swapBack);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;

        HitResult hit = mc.hitResult;
        if (hit instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity target) {

                if (target.isBlocking()) {
                    int axeSlot = findAxeSlot();
                    if (axeSlot != -1 && mc.player.getInventory().selected != axeSlot) {
                        previousSlot = mc.player.getInventory().selected;
                        mc.player.getInventory().selected = axeSlot;
                        timer = (int) swapDelay.getValue();
                    }
                }
                else if (swapBack.getValue() && previousSlot != -1) {
                    if (timer > 0) {
                        timer--;
                    } else {
                        mc.player.getInventory().selected = previousSlot;
                        previousSlot = -1;
                    }
                }
            }
        }
    }

    private int findAxeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }
}
