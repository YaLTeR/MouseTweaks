package yalter.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;

public class ProfilerCustom extends Profiler {
	private Minecraft minecraft;

	public ProfilerCustom() {
		minecraft = Minecraft.getMinecraft();
	}

	@Override
	public void startSection(String sectionName) {
		// The useProfiler check is redundant here since the OnTick method cannot change in the runtime (at the time of writing this comment).
		if (Main.useProfiler && "gameRenderer".equals(sectionName)) {
			Main.onUpdateInGame();
		}

		super.startSection(sectionName);

		if (Main.optifine) {
			if (!Reflection.gameSettings.setFieldValue(minecraft.gameSettings, "ofProfiler", true)) {
				Main.optifine = false;
			}
		}
	}
}
