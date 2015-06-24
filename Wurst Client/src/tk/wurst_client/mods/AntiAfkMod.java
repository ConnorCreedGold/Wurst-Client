/*
 * Copyright � 2014 - 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import tk.wurst_client.WurstClient;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.utils.BlockUtils;

@Info(name = "AntiAFK",
	description = "Walks around randomly to hide you from AFK detectors.\n"
		+ "Needs 3x3 blocks of free space.",
	category = Category.MISC)
public class AntiAfkMod extends Mod implements UpdateListener
{
	private BlockPos block;
	private Random random;
	private BlockPos nextBlock;
	
	@Override
	public void onEnable()
	{
		try
		{
			block = new BlockPos(Minecraft.getMinecraft().thePlayer);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		random = new Random();
		WurstClient.INSTANCE.eventManager.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		updateMS();
		if(hasTimePassedM(3000) || nextBlock == null)
		{
			if(block == null)
				onEnable();
			nextBlock =
				block.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
			updateLastMS();
		}
		BlockUtils.faceBlockClientHorizontally(nextBlock);
		if(BlockUtils.getHorizontalPlayerBlockDistance(nextBlock) > 0.75)
			Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = true;
		else
			Minecraft.getMinecraft().gameSettings.keyBindForward.pressed =
				false;
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.eventManager.remove(UpdateListener.class, this);
		Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = false;
	}
}
