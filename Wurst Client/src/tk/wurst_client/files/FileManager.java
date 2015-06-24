/*
 * Copyright � 2014 - 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

import org.darkstorm.minecraft.gui.component.Frame;
import org.darkstorm.minecraft.gui.component.basic.BasicSlider;

import tk.wurst_client.WurstClient;
import tk.wurst_client.alts.Alt;
import tk.wurst_client.alts.Encryption;
import tk.wurst_client.alts.gui.GuiAltList;
import tk.wurst_client.mods.*;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.options.Friends;
import tk.wurst_client.options.Options;
import tk.wurst_client.utils.XRayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileManager
{
	public final File wurstDir = new File(Minecraft.getMinecraft().mcDataDir,
		"wurst");
	public final File autobuildDir = new File(wurstDir, "autobuild");
	public final File skinDir = new File(wurstDir, "skins");
	public final File serverlistsDir = new File(wurstDir, "serverlists");
	public final File spamDir = new File(wurstDir, "spam");
	public final File scriptsDir = new File(spamDir, "autorun");
	
	public final File alts = new File(wurstDir, "alts.json");
	public final File friends = new File(wurstDir, "friends.json");
	public final File gui = new File(wurstDir, "gui.json");
	public final File modules = new File(wurstDir, "modules.json");
	public final File keybinds = new File(wurstDir, "keybinds.json");
	public final File sliders = new File(wurstDir, "sliders.json");
	public final File options = new File(wurstDir, "options.json");
	public final File autoMaximize = new File(
		Minecraft.getMinecraft().mcDataDir + "/wurst/automaximize.json");
	public final File xray = new File(wurstDir, "xray.json");
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public void init()
	{
		if(!wurstDir.exists())
			wurstDir.mkdir();
		if(!autobuildDir.exists())
			autobuildDir.mkdir();
		if(!spamDir.exists())
			spamDir.mkdir();
		if(!scriptsDir.exists())
			scriptsDir.mkdir();
		if(!skinDir.exists())
			skinDir.mkdir();
		if(!serverlistsDir.exists())
			serverlistsDir.mkdir();
		if(!options.exists())
			saveOptions();
		else
			loadOptions();
		if(!modules.exists())
			saveMods();
		else
			loadMods();
		if(!keybinds.exists())
			saveKeybinds();
		else
			loadKeybinds();
		if(!alts.exists())
			saveAlts();
		else
			loadAlts();
		if(!friends.exists())
			saveFriends();
		else
			loadFriends();
		if(!xray.exists())
		{
			XRayUtils.initXRayBlocks();
			saveXRayBlocks();
		}else
			loadXRayBlocks();
		File[] autobuildFiles = autobuildDir.listFiles();
		if(autobuildFiles != null && autobuildFiles.length == 0)
			createDefaultAutoBuildTemplates();
		loadAutoBuildTemplates();
		if(WurstClient.INSTANCE.options.autobuildMode >= AutoBuildMod.names
			.size())
		{
			WurstClient.INSTANCE.options.autobuildMode = 0;
			saveOptions();
		}
	}
	
	public void saveGUI(Frame[] frames)
	{
		try
		{
			JsonObject json = new JsonObject();
			for(Frame frame : frames)
				if(!frame.getTitle().equalsIgnoreCase("ArenaBrawl"))
				{
					JsonObject jsonFrame = new JsonObject();
					jsonFrame.addProperty("minimized", frame.isMinimized());
					jsonFrame.addProperty("pinned", frame.isPinned());
					jsonFrame.addProperty("posX", frame.getX());
					jsonFrame.addProperty("posY", frame.getY());
					json.add(frame.getTitle(), jsonFrame);
				}
			PrintWriter save = new PrintWriter(new FileWriter(gui));
			save.println(gson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadGUI(Frame[] frames)
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(gui));
			JsonObject json = (JsonObject)new JsonParser().parse(load);
			load.close();
			Iterator<Entry<String, JsonElement>> itr =
				json.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, JsonElement> entry = itr.next();
				for(Frame frame : frames)
					if(frame.getTitle().equals(entry.getKey()))
					{
						JsonObject jsonFrame = (JsonObject)entry.getValue();
						frame.setMinimized(jsonFrame.get("minimized")
							.getAsBoolean());
						frame.setPinned(jsonFrame.get("pinned").getAsBoolean());
						frame.setX(jsonFrame.get("posX").getAsInt());
						frame.setY(jsonFrame.get("posY").getAsInt());
					}
				
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveMods()
	{
		try
		{
			JsonObject json = new JsonObject();
			for(Mod mod : WurstClient.INSTANCE.modManager.getAllMods())
			{
				JsonObject jsonMod = new JsonObject();
				jsonMod.addProperty("enabled", mod.isEnabled());
				json.add(mod.getName(), jsonMod);
			}
			PrintWriter save = new PrintWriter(new FileWriter(modules));
			save.println(gson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String[] moduleBlacklist = {AntiAfkMod.class.getName(),
		ArenaBrawlMod.class.getName(), AutoBuildMod.class.getName(),
		AutoSignMod.class.getName(), FightBotMod.class.getName(),
		FollowMod.class.getName(), ForceOpMod.class.getName(),
		FreecamMod.class.getName(), InvisibilityMod.class.getName(),
		LsdMod.class.getName(), MassTpaMod.class.getName(),
		ProtectMod.class.getName(), RemoteViewMod.class.getName(),
		SpammerMod.class.getName(),};
	
	public void loadMods()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(modules));
			JsonObject json = (JsonObject)new JsonParser().parse(load);
			load.close();
			Iterator<Entry<String, JsonElement>> itr =
				json.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, JsonElement> entry = itr.next();
				Mod mod =
					WurstClient.INSTANCE.modManager
						.getModByName(entry.getKey());
				if(mod != null
					&& mod.getCategory() != Category.HIDDEN
					&& !Arrays.asList(moduleBlacklist).contains(
						mod.getClass().getName()))
				{
					JsonObject jsonModule = (JsonObject)entry.getValue();
					boolean enabled = jsonModule.get("enabled").getAsBoolean();
					if(enabled)
						mod.enableOnStartup();
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveKeybinds()
	{
		try
		{
			JsonObject json = new JsonObject();
			Iterator<Entry<String, String>> itr =
				WurstClient.INSTANCE.keybinds.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, String> entry = itr.next();
				json.addProperty(entry.getKey(), entry.getValue());
			}
			PrintWriter save = new PrintWriter(new FileWriter(keybinds));
			save.println(gson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadKeybinds()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(keybinds));
			JsonObject json = (JsonObject)new JsonParser().parse(load);
			load.close();
			WurstClient.INSTANCE.keybinds.clear();
			Iterator<Entry<String, JsonElement>> itr =
				json.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, JsonElement> entry = itr.next();
				WurstClient.INSTANCE.keybinds.put(entry.getKey(), entry
					.getValue().getAsString());
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveOptions()
	{
		try
		{
			PrintWriter save = new PrintWriter(new FileWriter(options));
			save.println(gson.toJson(WurstClient.INSTANCE.options));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadOptions()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(options));
			WurstClient.INSTANCE.options = gson.fromJson(load, Options.class);
			load.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean loadAutoMaximize()
	{
		boolean autoMaximizeEnabled = false;
		if(!autoMaximize.exists())
			saveAutoMaximize(true);
		try
		{
			BufferedReader load =
				new BufferedReader(new FileReader(autoMaximize));
			autoMaximizeEnabled =
				gson.fromJson(load, Boolean.class) && !Minecraft.isRunningOnMac;
			load.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return autoMaximizeEnabled;
	}
	
	public void saveAutoMaximize(boolean autoMaximizeEnabled)
	{
		try
		{
			if(!autoMaximize.getParentFile().exists())
				autoMaximize.getParentFile().mkdirs();
			PrintWriter save = new PrintWriter(new FileWriter(autoMaximize));
			save.println(gson.toJson(autoMaximizeEnabled));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveSliders()
	{
		try
		{
			JsonObject json = new JsonObject();
			for(Mod mod : WurstClient.INSTANCE.modManager.getAllMods())
			{
				if(mod.getSliders().isEmpty())
					continue;
				JsonObject jsonModule = new JsonObject();
				for(BasicSlider slider : mod.getSliders())
					jsonModule.addProperty(slider.getText(),
						(double)(Math.round(slider.getValue()
							/ slider.getIncrement()) * 1000000 * (long)(slider
							.getIncrement() * 1000000)) / 1000000 / 1000000);
				json.add(mod.getName(), jsonModule);
			}
			PrintWriter save = new PrintWriter(new FileWriter(sliders));
			save.println(gson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadSliders()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(sliders));
			JsonObject json = (JsonObject)new JsonParser().parse(load);
			load.close();
			Iterator<Entry<String, JsonElement>> itr =
				json.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, JsonElement> entry = itr.next();
				Mod mod =
					WurstClient.INSTANCE.modManager
						.getModByName(entry.getKey());
				if(mod != null)
				{
					JsonObject jsonModule = (JsonObject)entry.getValue();
					for(BasicSlider slider : mod.getSliders())
						try
						{
							slider.setValue(jsonModule.get(slider.getText())
								.getAsDouble());
						}catch(Exception e)
						{
							e.printStackTrace();
						}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveAlts()
	{
		try
		{
			JsonObject json = new JsonObject();
			for(Alt alt : GuiAltList.alts)
			{
				JsonObject jsonAlt = new JsonObject();
				jsonAlt.addProperty("name", alt.getName());
				jsonAlt.addProperty("password", alt.getPassword());
				jsonAlt.addProperty("cracked", alt.isCracked());
				jsonAlt.addProperty("starred", alt.isStarred());
				json.add(alt.getEmail(), jsonAlt);
			}
			Files.write(alts.toPath(), Encryption.encrypt(gson.toJson(json))
				.getBytes(Encryption.CHARSET));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadAlts()
	{
		try
		{
			JsonObject json =
				(JsonObject)new JsonParser().parse(Encryption
					.decrypt(new String(Files.readAllBytes(alts.toPath()),
						Encryption.CHARSET)));
			GuiAltList.alts.clear();
			Iterator<Entry<String, JsonElement>> itr =
				json.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, JsonElement> entry = itr.next();
				JsonObject jsonAlt = entry.getValue().getAsJsonObject();
				String email = entry.getKey();
				String name =
					jsonAlt.get("name") == null ? "" : jsonAlt.get("name")
						.getAsString();
				String password =
					jsonAlt.get("password") == null ? "" : jsonAlt.get(
						"password").getAsString();
				boolean cracked =
					jsonAlt.get("cracked") == null ? true : jsonAlt.get(
						"cracked").getAsBoolean();
				boolean starred =
					jsonAlt.get("starred") == null ? false : jsonAlt.get(
						"starred").getAsBoolean();
				if(cracked)
					GuiAltList.alts.add(new Alt(email, starred));
				else
					GuiAltList.alts
						.add(new Alt(email, name, password, starred));
			}
			GuiAltList.sortAlts();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveFriends()
	{
		try
		{
			PrintWriter save = new PrintWriter(new FileWriter(friends));
			save.println(gson.toJson(WurstClient.INSTANCE.friends));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadFriends()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(friends));
			WurstClient.INSTANCE.friends = gson.fromJson(load, Friends.class);
			load.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveXRayBlocks()
	{
		try
		{
			XRayUtils.sortBlocks();
			JsonArray json = new JsonArray();
			for(int i = 0; i < XRayMod.xrayBlocks.size(); i++)
				json.add(gson.toJsonTree(Block
					.getIdFromBlock(XRayMod.xrayBlocks.get(i))));
			PrintWriter save = new PrintWriter(new FileWriter(xray));
			save.println(gson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadXRayBlocks()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(xray));
			JsonArray json = new JsonParser().parse(load).getAsJsonArray();
			load.close();
			Iterator<JsonElement> itr = json.iterator();
			while(itr.hasNext())
				try
				{
					String jsonBlock = itr.next().getAsString();
					XRayMod.xrayBlocks.add(Block.getBlockFromName(jsonBlock));
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			XRayUtils.sortBlocks();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createDefaultAutoBuildTemplates()
	{
		try
		{
			String[] comment =
				{
					"Copyright � 2014 - 2015 | Alexander01998 | All rights reserved.",
					"This Source Code Form is subject to the terms of the Mozilla Public",
					"License, v. 2.0. If a copy of the MPL was not distributed with this",
					"file, You can obtain one at http://mozilla.org/MPL/2.0/."};
			Iterator<Entry<String, int[][]>> itr =
				new DefaultAutoBuildTemplates().entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, int[][]> entry = itr.next();
				JsonObject json = new JsonObject();
				json.add("__comment", gson.toJsonTree(comment, String[].class));
				json.add("blocks",
					gson.toJsonTree(entry.getValue(), int[][].class));
				PrintWriter save =
					new PrintWriter(new FileWriter(new File(autobuildDir,
						entry.getKey() + ".json")));
				save.println(gson.toJson(json));
				save.close();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadAutoBuildTemplates()
	{
		try
		{
			File[] files = autobuildDir.listFiles();
			if(files == null)
				return;
			for(File file : files)
			{
				BufferedReader load = new BufferedReader(new FileReader(file));
				JsonObject json = (JsonObject)new JsonParser().parse(load);
				load.close();
				AutoBuildMod.templates.add(gson.fromJson(json.get("blocks"),
					int[][].class));
				AutoBuildMod.names.add(file.getName().substring(0,
					file.getName().indexOf(".json")));
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
