/*
 * Copyright � 2014 - 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import tk.wurst_client.WurstClient;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.utils.BlockUtils;

@Info(category = Category.BLOCKS,
	description = "Places random blocks around you.",
	name = "BuildRandom")
public class BuildRandomMod extends Mod implements UpdateListener
{
	private float range = 6;
	
	@Override
	public void onEnable()
	{
		WurstClient.INSTANCE.eventManager.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(WurstClient.INSTANCE.modManager.getModByClass(FreecamMod.class)
			.isEnabled()
			|| WurstClient.INSTANCE.modManager.getModByClass(
				RemoteViewMod.class).isEnabled()
			|| Minecraft.getMinecraft().objectMouseOver == null
			|| Minecraft.getMinecraft().objectMouseOver.typeOfHit != MovingObjectType.BLOCK)
			return;
		if(Minecraft.getMinecraft().rightClickDelayTimer > 0
			&& !WurstClient.INSTANCE.modManager.getModByClass(
				FastPlaceMod.class).isEnabled())
			return;
		float xDiff = 0;
		float yDiff = 0;
		float zDiff = 0;
		float distance = range + 1;
		boolean hasBlocks = false;
		for(int y = (int)range; y >= -range; y--)
		{
			for(int x = (int)range; x >= -range - 1; x--)
			{
				for(int z = (int)range; z >= -range; z--)
					if(Block
						.getIdFromBlock(Minecraft.getMinecraft().theWorld
							.getBlockState(
								new BlockPos(
									(int)(x + Minecraft.getMinecraft().thePlayer.posX),
									(int)(y + Minecraft.getMinecraft().thePlayer.posY),
									(int)(z + Minecraft.getMinecraft().thePlayer.posZ)))
							.getBlock()) != 0
						&& BlockUtils.getBlockDistance(x, y, z) <= range)
					{
						hasBlocks = true;
						break;
					}
				if(hasBlocks)
					break;
			}
			if(hasBlocks)
				break;
		}
		if(!hasBlocks)
			return;
		BlockPos randomPos = null;
		while(distance > range
			|| distance < -range
			|| randomPos == null
			|| Block.getIdFromBlock(Minecraft.getMinecraft().theWorld
				.getBlockState(randomPos).getBlock()) == 0)
		{
			xDiff = (int)(Math.random() * range * 2 - range - 1);
			yDiff = (int)(Math.random() * range * 2 - range);
			zDiff = (int)(Math.random() * range * 2 - range);
			distance = BlockUtils.getBlockDistance(xDiff, yDiff, zDiff);
			int randomPosX =
				(int)(xDiff + Minecraft.getMinecraft().thePlayer.posX);
			int randomPosY =
				(int)(yDiff + Minecraft.getMinecraft().thePlayer.posY);
			int randomPosZ =
				(int)(zDiff + Minecraft.getMinecraft().thePlayer.posZ);
			randomPos = new BlockPos(randomPosX, randomPosY, randomPosZ);
		}
		MovingObjectPosition fakeObjectMouseOver =
			Minecraft.getMinecraft().objectMouseOver;
		if(fakeObjectMouseOver == null || randomPos == null)
			return;
		fakeObjectMouseOver.setBlockPos(randomPos);
		BlockUtils.faceBlockPacket(randomPos);
		Minecraft.getMinecraft().thePlayer.swingItem();
		Minecraft.getMinecraft().thePlayer.sendQueue
			.addToSendQueue(new C08PacketPlayerBlockPlacement(randomPos,
				fakeObjectMouseOver.sideHit.getIndex(), Minecraft
					.getMinecraft().thePlayer.inventory.getCurrentItem(),
				(float)fakeObjectMouseOver.hitVec.xCoord
					- fakeObjectMouseOver.getBlockPos().getX(),
				(float)fakeObjectMouseOver.hitVec.yCoord
					- fakeObjectMouseOver.getBlockPos().getY(),
				(float)fakeObjectMouseOver.hitVec.zCoord
					- fakeObjectMouseOver.getBlockPos().getZ()));
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.eventManager.remove(UpdateListener.class, this);
	}
}
