package org.example.modules;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.example.ExamplePlugin;
import org.example.utils.PriorityMode;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.WorldUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KillAuraStrict extends ToggleableModule {
    private final NumberSetting<Float> tange = new NumberSetting<>("Range", 3.0f, 1.0f, 4.0f);
    private final NumberSetting<Float> rotationStep = new NumberSetting<>("Rotation Speed", 15.0f, 1.0f, 180.0f);
    private final NumberSetting<Integer> reactionTime = new NumberSetting<>("Reaction Time (ms)", 150, 0, 500);
    private final EnumSetting<PriorityMode> priority = new EnumSetting<PriorityMode>("Priority", PriorityMode.DISTANCE);

    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", true);
    private final BooleanSetting neutals = new BooleanSetting("Neutrals", false);

    private long targetTime = 0;
    private final Random random = new Random();
    private Entity lastTarget = null;

    public KillAuraStrict() {
        super("Killaura", "Attacks nearby enemies", ExamplePlugin.LEGIT_CATEGORY);
        registerSettings(tange, rotationStep, reactionTime, priority, players, animals, monsters, neutals);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;

        List<LivingEntity> targets = getTargets();
        if (targets.isEmpty()) return;

        LivingEntity target = targets.get(0);
        if (target != lastTarget) {
            targetTime = System.currentTimeMillis();
            lastTarget = target;
        }
        if (System.currentTimeMillis() - targetTime < reactionTime.getValue()) {
            return;
        }

        float randomStep = rotationStep.getValue() + (random.nextFloat() * 5.0f);
        RusherHackAPI.getRotationManager().updateRotation(target, randomStep);

        if (canAttack(target)) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private boolean canAttack(LivingEntity target) {
        if (!mc.player.hasLineOfSight(target)) return false;

        float cooldown = mc.player.getAttackStrengthScale(0.5f);
        return cooldown >= (0.90f + (random.nextFloat() * 0.05f));
    }

    private List<LivingEntity> getTargets() {
        Comparator<LivingEntity> comparator = priority.getValue() == PriorityMode.DISTANCE
                ? Comparator.comparingDouble(e -> mc.player.distanceTo(e))
                : Comparator.comparingDouble(LivingEntity::getHealth);

        return WorldUtils.getEntities().stream()
                .filter(e -> e instanceof LivingEntity && e != mc.player)
                .map(e -> (LivingEntity) e)
                .filter(this::isValid)
                .sorted(comparator)
                .toList();
    }

    private boolean isValid(LivingEntity entity) {
        if (!entity.isAlive() || mc.player.distanceTo(entity) > tange.getValue()) return false;

        if (players.getValue() && entity instanceof Player) return !RusherHackAPI.getRelationManager().isFriend(entity.getName().getString());
        if (monsters.getValue() && entity instanceof Monster) return true;
        if (animals.getValue() && entity instanceof Animal) return true;
        if (neutals.getValue() && entity instanceof net.minecraft.world.entity.NeutralMob) return true;

        return false;
    }
}
