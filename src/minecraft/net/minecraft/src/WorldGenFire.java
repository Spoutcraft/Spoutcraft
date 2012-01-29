package net.minecraft.src;

import java.util.Random;

public class WorldGenFire extends WorldGenerator {
	public WorldGenFire() {
	}

	public boolean generate(World world, Random random, int i, int j, int k) {
		for (int l = 0; l < 64; l++) {
			int i1 = (i + random.nextInt(8)) - random.nextInt(8);
			int j1 = (j + random.nextInt(4)) - random.nextInt(4);
			int k1 = (k + random.nextInt(8)) - random.nextInt(8);
			if (world.isAirBlock(i1, j1, k1) && world.getBlockId(i1, j1 - 1, k1) == Block.netherrack.blockID) {
				world.setBlockWithNotify(i1, j1, k1, Block.fire.blockID);
			}
		}

		return true;
	}
}
