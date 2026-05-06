package me.felek.stricty;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import org.example.Stricty;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.NumberSetting;

public class ThrowPots extends ToggleableModule {
    private NumberSetting<Integer> healthThreshold = new NumberSetting<>("Health Threshold", 10, 1, 20);
    private NumberSetting<Integer> delay = new NumberSetting<>("Delay", 5, 0, 20);

    private int tickCounter = 0;

    public ThrowPots() {
        super("ThrowPots", "Automatically throws health potions", Stricty.LEGIT_CATEGORY);
        registerSettings(healthThreshold, delay);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.getHealth() > healthThreshold.getValue()) return;

        int potSlot = -1;
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof PotionItem) {
                if (isBeneficial(stack)) {
                    potSlot = i;
                    break;
                }
            }
        }

        if (potSlot != -1) {
            if (tickCounter >= delay.getValue()) {
                int oldSlot = mc.player.getInventory().selected;
                mc.player.getInventory().selected = potSlot;
                mc.player.setXRot(90f);
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.getInventory().selected = oldSlot;
                tickCounter = 0;
            } else {
                tickCounter++;
            }
        }
    }

    private boolean isBeneficial(net.minecraft.world.item.ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        if (contents == null) return false;
        for (var effectInstance : contents.getAllEffects()) {
            if (effectInstance.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL) {
                return true;
            }
        }
        return false;
    }
}
