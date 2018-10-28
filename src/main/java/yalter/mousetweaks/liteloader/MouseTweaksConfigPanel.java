package yalter.mousetweaks.liteloader;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import yalter.mousetweaks.Config;
import yalter.mousetweaks.Logger;
import yalter.mousetweaks.Main;

public class MouseTweaksConfigPanel extends AbstractConfigPanel {
	@Override
	public String getPanelTitle() {
		return "Mouse Tweaks Configuration";
	}

	@Override
	protected void addOptions(ConfigPanelHost host) {
		Logger.DebugLog("addOptions()");

		Main.config.read();

		int y = -16;

		this.addControl(new GuiCheckbox(0, 0, y += 16, "Enable RMB tweak"), new ConfigOptionListener<GuiCheckbox>() {
			@Override
			public void actionPerformed(GuiCheckbox control) {
				Main.config.rmbTweak = (control.checked = !control.checked);
			}
		}).checked = Main.config.rmbTweak;

		this.addControl(new GuiCheckbox(0, 0, y += 16, "Enable LMB tweak with item"),
		                new ConfigOptionListener<GuiCheckbox>() {
			                @Override
			                public void actionPerformed(GuiCheckbox control) {
				                Main.config.lmbTweakWithItem = (control.checked = !control.checked);
			                }
		                }).checked = Main.config.lmbTweakWithItem;

		this.addControl(new GuiCheckbox(0, 0, y += 16, "Enable LMB tweak without item"),
		                new ConfigOptionListener<GuiCheckbox>() {
			                @Override
			                public void actionPerformed(GuiCheckbox control) {
				                Main.config.lmbTweakWithoutItem = (control.checked = !control.checked);
			                }
		                }).checked = Main.config.lmbTweakWithoutItem;

		this.addControl(new GuiCheckbox(0, 0, y += 16, "Enable wheel tweak"), new ConfigOptionListener<GuiCheckbox>() {
			@Override
			public void actionPerformed(GuiCheckbox control) {
				Main.config.wheelTweak = (control.checked = !control.checked);
			}
		}).checked = Main.config.wheelTweak;

		this.addControl(new GuiCheckbox(0, 0, y += 16, "Enable debug mode"), new ConfigOptionListener<GuiCheckbox>() {
			@Override
			public void actionPerformed(GuiCheckbox control) {
				Config.debug = (control.checked = !control.checked);
			}
		}).checked = Config.debug;
	}

	@Override
	public void onPanelHidden() {
		Logger.DebugLog("onPanelHidden()");

		Main.config.save();
		Main.findOnTickMethod(true);
	}
}
