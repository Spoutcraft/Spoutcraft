/*
 * This file is part of Spoutcraft (http://www.spout.org/).
 *
 * Spoutcraft is licensed under the SpoutDev License Version 1.
 *
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev license version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.client.gui.texturepacks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.pclewis.mcpatcher.mod.TextureUtils;

import org.getspout.commons.ChatColor;
import org.lwjgl.opengl.GL11;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.TexturePackBase;
import net.minecraft.src.TexturePackList;

import org.spoutcraft.spoutcraftapi.Spoutcraft;
import org.spoutcraft.spoutcraftapi.gui.ListWidget;
import org.spoutcraft.spoutcraftapi.gui.ListWidgetItem;
import org.spoutcraft.spoutcraftapi.gui.MinecraftTessellator;

import org.spoutcraft.client.SpoutClient;

public class TexturePackItem implements ListWidgetItem {
	protected final static Map<String, Integer> texturePackSize = new HashMap<String, Integer>();
	protected volatile static TexturePackSizeThread activeThread = null;
	private TexturePackBase pack;
	private ListWidget widget;
	private TexturePackList packList = SpoutClient.getHandle().texturePackList;
	int id = -1;
	private String title = null;
	protected volatile int tileSize = -1;

	public TexturePackItem(TexturePackBase pack) {
		this.setPack(pack);
		synchronized (texturePackSize) {
			if (!texturePackSize.containsKey(getName())) {
				calculateTexturePackSize(pack, this);
			} else {
				tileSize = texturePackSize.get(getName());
			}
		}
	}

	public void setListWidget(ListWidget widget) {
		this.widget = widget;
	}

	public ListWidget getListWidget() {
		return widget;
	}

	public int getHeight() {
		return 29;
	}

	public void render(int x, int y, int width, int height) {
		updateQueue();

		MinecraftTessellator tessellator = Spoutcraft.getTessellator();
		FontRenderer font = SpoutClient.getHandle().fontRenderer;

		font.drawStringWithShadow(getName(), x + 29, y + 2, 0xffffffff);
		font.drawStringWithShadow(pack.firstDescriptionLine, x + 29, y + 11, 0xffaaaaaa);
		font.drawStringWithShadow(pack.secondDescriptionLine, x + 29, y + 20, 0xffaaaaaa);

		String sTileSize;
		if (tileSize != -1) {
			sTileSize = tileSize + "x";
		} else {
			sTileSize = ChatColor.YELLOW + "Calculating...";
		}
		int w = font.getStringWidth(sTileSize);
		font.drawStringWithShadow(sTileSize, width - 5 - w, y + 2, 0xffaaaaaa);

		//TODO: Show database information (author/member who posted it)

		pack.bindThumbnailTexture(SpoutClient.getHandle());
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque(255, 255, 255);
		tessellator.addVertexWithUV(x + 2, y + 27, 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV(x + 27, y + 27, 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV(x + 27, y + 2, 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(x + 2, y + 2, 0.0D, 0.0D, 0.0D);
		tessellator.draw();
	}

	public void onClick(int x, int y, boolean doubleClick) {
		if (doubleClick) {
			select();
		}
	}

	public void setPack(TexturePackBase pack) {
		this.pack = pack;
	}

	public TexturePackBase getPack() {
		return pack;
	}

	public String getName() {
		if (title == null) {
			String name = pack.texturePackFileName;
			int suffix = name.lastIndexOf(".zip");
			if (suffix != -1) {
				name = name.substring(0, suffix);
			}
			int db = name.lastIndexOf(".id_");
			if (db != -1) {
				try {
					id = Integer.valueOf(name.substring(db + 4, name.length()));
				} catch (NumberFormatException e) {
				}
				name = name.substring(0, db);
			}
			name = name.replaceAll("_", " ");
			title = name;
		}
		return title;
	}

	public void select() {
		packList.setTexturePack(getPack());
		SpoutClient.getHandle().renderEngine.refreshTextures();
	}

	private static void updateQueue() {
		if (activeThread == null) {
			Thread thread = queued.poll();
			if (thread != null) {
				thread.start();
			}
		}
	}

	private static LinkedList<TexturePackSizeThread> queued = new LinkedList<TexturePackSizeThread>();

	private static void calculateTexturePackSize(TexturePackBase texturePack, TexturePackItem item) {
		if (activeThread == null) {
			activeThread = new TexturePackSizeThread(texturePack, item);
			activeThread.start();
		} else {
			queued.add(new TexturePackSizeThread(texturePack, item));
		}
	}
}

class TexturePackSizeThread extends Thread {
	TexturePackBase texturePack;
	TexturePackItem item;

	TexturePackSizeThread(TexturePackBase texturePack, TexturePackItem item) {
		this.texturePack = texturePack;
		this.item = item;
	}

	@Override
	public void run() {
		item.tileSize = TextureUtils.getTileSize(texturePack);
		synchronized (TexturePackItem.texturePackSize) {
			TexturePackItem.texturePackSize.put(getName(), item.tileSize);
		}
		texturePack.closeTexturePackFile();

		TexturePackItem.activeThread = null;
	}
}
