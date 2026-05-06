package me.felek.stricty;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import org.example.Stricty;
import org.example.utils.PvpMode;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.Random;

public class TriggerPlus extends ToggleableModule {
    private final EnumSetting<PvpMode> pvpMode = new EnumSetting<>("Mode", PvpMode.NEW);

    private final NumberSetting<Float> jitter = new NumberSetting<>("Jitter", 0.0f, 0.0f, 5.0f);
    private final BooleanSetting onlyWeapon = new BooleanSetting("OnlyWeapon", true);
    private final BooleanSetting onlyWhenHolding = new BooleanSetting("OnlyWhenHolding", true);
    private final BooleanSetting noShield = new BooleanSetting("NoShield", true);
    private final NumberSetting<Integer> cps = new NumberSetting<>("CPS", 10, 1, 20);
    private final NumberSetting<Integer> missChance = new NumberSetting<>("MissChance", 0, 0, 100);

    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", true);
    private final BooleanSetting neutals = new BooleanSetting("Neutrals", false);

    private long lastClickTime = 0;
    private final Random random = new Random();

    public TriggerPlus() {
        super("TriggerPlus", "TriggerBot for strict servers", Stricty.LEGIT_CATEGORY);
        this.registerSettings(pvpMode, jitter, onlyWeapon, onlyWhenHolding, noShield, cps, missChance, players, monsters, neutals, animals);
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;
        if (onlyWhenHolding.getValue() && !mc.options.keyAttack.isDown()) return;
        if (onlyWeapon.getValue() && !isHoldingWeapon()) return;
        if (noShield.getValue() && mc.player.isBlocking()) return;

        Entity target = getTargetUnderCrosshair();

        if (target != null && isTargetValid(target) && canAttack()) {
            performAttack(target);
        }
    }

    private boolean isHoldingWeapon() {
        var stack = mc.player.getMainHandItem().getItem();
        return stack instanceof SwordItem || stack instanceof AxeItem;
    }

    private Entity getTargetUnderCrosshair() {
        return mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY
                ? ((net.minecraft.world.phys.EntityHitResult) mc.hitResult).getEntity()
                : null;
    }

    private boolean isTargetValid(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (players.getValue() && entity instanceof net.minecraft.world.entity.player.Player) {
            if (RusherHackAPI.getRelationManager().isFriend(entity.getName().getString())) return false;
            return true;
        }
        if (monsters.getValue() && entity instanceof net.minecraft.world.entity.monster.Monster) {
            return true;
        }
        if (animals.getValue() && entity instanceof net.minecraft.world.entity.animal.Animal) {
            return true;
        }
        if (neutals.getValue() && entity instanceof net.minecraft.world.entity.NeutralMob) {
            return true;
        }

        return false;
    }

    private boolean canAttack() {
        if (missChance.getValue() > 0 && random.nextInt(100) < missChance.getValue()) {
            return false;
        }

        if (pvpMode.getValue() == PvpMode.LEGACY) {
            long delay = 1000 / cps.getValue();
            return (System.currentTimeMillis() - lastClickTime) > delay;
        } else {
            float cooldown = mc.player.getAttackStrengthScale(0.5f);
            return cooldown >= 0.95f;
        }
    }

    private void performAttack(Entity target) {
        if (jitter.getValue() > 0) {
            float yaw = mc.player.getYRot() + (random.nextFloat() - 0.5f) * jitter.getValue();
            mc.player.setYRot(yaw);
        }

        if (pvpMode.getValue() == PvpMode.LEGACY) {
        }

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        lastClickTime = System.currentTimeMillis();
    }
}
