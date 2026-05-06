package org.example;

import me.felek.stricty.*;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.plugin.Plugin;

public class Stricty extends Plugin {

	public static ModuleCategory LEGIT_CATEGORY = ModuleCategory.register("Legit");
	
	@Override
	public void onLoad() {
		
		//logger
		this.getLogger().info("i do not allow cheating btw...");

		RusherHackAPI.getModuleManager().registerFeature(new TriggerPlus());
		RusherHackAPI.getModuleManager().registerFeature(new KillAuraStrict());
		RusherHackAPI.getModuleManager().registerFeature(new AutoBridge());
		RusherHackAPI.getModuleManager().registerFeature(new AutoHop());
		RusherHackAPI.getModuleManager().registerFeature(new ThrowPots());
		RusherHackAPI.getModuleManager().registerFeature(new ShieldTrigger());
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("Example plugin unloaded!");
	}
	
}