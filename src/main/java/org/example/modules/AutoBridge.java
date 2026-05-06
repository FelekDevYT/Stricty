package org.example.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import org.example.ExamplePlugin;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;

public class AutoBridge extends ToggleableModule {
    private final BooleanSetting autoSneak = new BooleanSetting("AutoSneak", true);
    private final NumberSetting<Integer> sneakDelay = new NumberSetting<>("Sneak Delay", 50, 0, 500);
    private final BooleanSetting autoPlace = new BooleanSetting("AutoPlace", true);
    private final NumberSetting<Integer> placeDelay = new NumberSetting<>("Place Delay", 2, 0, 10);

    private int tickCounter = 0;
    private long lastSneakChange = 0;

    public AutoBridge() {
        super("AutoBridge", "Automatically builds a bridge", ExamplePlugin.LEGIT_CATEGORY);
        registerSettings(autoSneak, sneakDelay, autoPlace, placeDelay);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;
        BlockPos posUnder = mc.player.blockPosition().below();

        double reach = 6;
        var hitResult = mc.player.pick(reach, 1.0f, false);

        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult blockHit = (net.minecraft.world.phys.BlockHitResult) hitResult;
            BlockPos lookedAtPos = blockHit.getBlockPos();

            BlockPos targetPos = lookedAtPos.relative(blockHit.getDirection());

            boolean isAtEdge = lookedAtPos.getY() == posUnder.getY() &&
                    (lookedAtPos.equals(posUnder) ||
                            lookedAtPos.distManhattan(posUnder) <= 1);
            boolean isBelowPlayer = targetPos.getY() < mc.player.blockPosition().getY();
            boolean isTargetEmpty = mc.level.getBlockState(targetPos).isAir();
            if (autoPlace.getValue() && isAtEdge && isBelowPlayer && isTargetEmpty) {
                if (mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                    if (tickCounter >= placeDelay.getValue()) {
                        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        tickCounter = 0;
                    } else {
                        tickCounter++;
                    }
                }
            }
        }

        if (autoSneak.getValue()) {
            boolean isOverAir = mc.level.getBlockState(posUnder).isAir();
            if (mc.options.keyShift.isDown() != isOverAir) {
                mc.options.keyShift.setDown(isOverAir);
            }
        }
    }
}
