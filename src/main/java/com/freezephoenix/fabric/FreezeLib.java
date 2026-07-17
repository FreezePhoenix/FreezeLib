package com.freezephoenix.fabric;

import com.freezephoenix.fabric.api.block.BetterBlock;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class FreezeLib implements ModInitializer {
	public record BetterBlockEntity<B extends Block & BetterBlock<E>, E extends BlockEntity>(B block, BlockEntityType<E> entity) {}
	public record FreezeTab(Supplier<ItemLike> icon, Collection<ItemLike> items) {
		public FreezeTab(Supplier<ItemLike> icon) {
			this(icon, new ArrayList<>());
		}
	}
	public static final String MOD_ID = "freezelib";

	public static final Map<String, FreezeTab> CREATIVE_TABS = new HashMap<>();

	public static <B extends Block> B registerBlock(final BlockItemId ID, final Function<BlockBehaviour.Properties, B> factory, final Block template) {
		ResourceKey<Block> blockRegistryKey = ID.block();
		B block = Registry.register(
				BuiltInRegistries.BLOCK,
				blockRegistryKey,
				factory.apply(BlockBehaviour.Properties.ofFullCopy(template).setId(blockRegistryKey))
		);
		registerItem(ID, BlockItem::new, block);
		return block;
	}

	public static <B extends Block & BetterBlock<E>, E extends BlockEntity> BetterBlockEntity<B, E> registerBlockEntity(B block, Identifier ID, FabricBlockEntityTypeBuilder.Factory<E> factory) {
		return new BetterBlockEntity<>(block, Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ID,
				FabricBlockEntityTypeBuilder.create(factory, block).build()
		));
	}

	public static <B extends Block & BetterBlock<E>, E extends BlockEntity> BetterBlockEntity<B, E> registerBlockEntity(BlockItemId ID, Function<BlockBehaviour.Properties, B> bFactory, Block template, Identifier eID, FabricBlockEntityTypeBuilder.Factory<E> eFactory) {
		ResourceKey<Block> blockRegistryKey = ID.block();
		B block = Registry.register(
				BuiltInRegistries.BLOCK,
				blockRegistryKey,
				bFactory.apply(BlockBehaviour.Properties.ofFullCopy(template).setId(blockRegistryKey))
		);

		registerItem(ID, BlockItem::new, block);

		return registerBlockEntity(block, eID,eFactory);
	}

	public static <T extends Item, A> T registerItem(BlockItemId ID, BiFunction<A, Item.Properties, T> factory, A arg) {
		ResourceKey<Item> itemRegistryKey = ID.item();
		T new_item = Registry.register(
				BuiltInRegistries.ITEM,
				itemRegistryKey,
				factory.apply(arg, new Item.Properties().setId(itemRegistryKey))
		);
		var modId = ID.item().identifier().getNamespace();
		if(CREATIVE_TABS.containsKey(modId)) {
			CREATIVE_TABS.get(modId).items().add(new_item);
		}
		return new_item;
	}

	public static <T extends Item> T registerItem(Identifier ID, Function<Item.Properties, T> factory) {
		ResourceKey<Item> itemRegistryKey = ResourceKey.create(Registries.ITEM, ID);
		T new_item = Registry.register(
				BuiltInRegistries.ITEM,
				itemRegistryKey,
				factory.apply(new Item.Properties().setId(itemRegistryKey))
		);
		var modId = ID.getNamespace();
		if(CREATIVE_TABS.containsKey(modId)) {
			CREATIVE_TABS.get(modId).items().add(new_item);
		}
		return new_item;
	}

	public static @Nullable FreezeTab registerCreativeTab(String modId, Supplier<ItemLike> icon) {
		return CREATIVE_TABS.putIfAbsent(modId, new FreezeTab(icon));
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CREATIVE_TABS.forEach((modId, freezeTab) -> {
			Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
					Identifier.fromNamespaceAndPath(modId, "item_group"),
					FabricCreativeModeTab.builder()
										 .icon(() -> new ItemStack(
												 freezeTab.icon.get()))
										 .title(Component.translatable(modId + ".item_group"))
										 .displayItems((_, entries) -> {
											 for (ItemLike itemLike : freezeTab.items()) {
												 entries.accept(itemLike);
											 }
										 })
										 .build()
			);
		});
	}
}
