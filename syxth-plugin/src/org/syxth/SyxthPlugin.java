package org.syxth;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SyxthPlugin extends AbstractUIPlugin {

	private static SyxthPlugin plugin;
	
	public SyxthPlugin() {
		super();
		plugin = this;
	}
	
	public static SyxthPlugin getDefault() {
		return plugin;
	}
}
