package com.freezephoenix.fabric.client;

import com.freezephoenix.fabric.client.gui.BetterScreen;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;

public class FreezeLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {}

	public static <T extends SyncedGuiDescription> void registerMenu(MenuType<T> screenHandlerType) {
		MenuScreens.register(screenHandlerType, BetterScreen<T>::new);
	}
}
