package com.terraformersmc.terrestria.feature.tree.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.terraform.block.QuarterLogBlock;
import com.terraformersmc.terrestria.config.TerrestriaConfigManager;
import com.terraformersmc.terrestria.feature.tree.treeconfigs.QuarteredMegaTreeConfig;
import com.terraformersmc.terrestria.init.TerrestriaTrunkPlacerTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallSeagrassBlock;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class QuarteredMegaIncrementedStraightTrunkPlacer extends TrunkPlacer {

	public static final Codec<QuarteredMegaIncrementedStraightTrunkPlacer> CODEC = RecordCodecBuilder.create(quarteredMegaTreeTrunkPlacerInstance ->
			method_28904(quarteredMegaTreeTrunkPlacerInstance)
					.apply(quarteredMegaTreeTrunkPlacerInstance, QuarteredMegaIncrementedStraightTrunkPlacer::new));

	public QuarteredMegaIncrementedStraightTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight) {
		super(baseHeight, firstRandomHeight, secondRandomHeight);
	}

	@Override
	protected TrunkPlacerType<?> getType() {
		return TerrestriaTrunkPlacerTypes.QUARTERED_MEGA_TREE;
	}

	@Override
	public List<FoliagePlacer.TreeNode> generate(ModifiableTestableWorld world, Random random, int trunkHeight, BlockPos pos, Set<BlockPos> set, BlockBox blockBox, TreeFeatureConfig treeFeatureConfig) {
		//Determine the tree type
		//Terrestria Conifers can either be straight up or the layers can get smaller as it reaches the top
		boolean converge = random.nextBoolean();

		//Set the radius
		//If the tree converges give it a larger base radius but vary both
		int radius = (converge ? (5 + random.nextInt(2)) : (3 + random.nextInt(2)));

		//Determine the number of layers to place
		//This varies between the two types of conifer here
		int layers = converge ? radius : (6 + random.nextInt(4));

		//Check and set the block below to dirt
		method_27400(world, pos.down());
		method_27400(world, pos.down().east());
		method_27400(world, pos.down().south());
		method_27400(world, pos.down().south().east());

		//Create the Mutable version of our block position so that we can procedurally create the trunk
		BlockPos.Mutable currentPosition = pos.mutableCopy().move(Direction.DOWN);

		//Create the placer storage
		ArrayList<FoliagePlacer.TreeNode> foliageNodes = new ArrayList<>();

		//Place the trunk
		for (int i = 0; i < trunkHeight; i++) {
			placeLayer(world, random, currentPosition.move(Direction.UP).toImmutable(), set, blockBox, ((QuarteredMegaTreeConfig) treeFeatureConfig));
		}

		//Place the layers
		for (int i = 0; i < layers; i++) {
			//Place the layers
			for (int j = 0; j < (converge ? (radius - i + 1) : 4); j++) {
				placeLayer(world, random, currentPosition.move(Direction.UP).toImmutable(), set, blockBox, ((QuarteredMegaTreeConfig) treeFeatureConfig));
			}
			//Add locations for leaves to be placed at the top of the layer
			//The radius is determined by weather it converges or not, if it does subtract one from it every layer
			foliageNodes.add(new FoliagePlacer.TreeNode(currentPosition.toImmutable(), (converge ? (radius - i) : radius), true));
		}

		//Make sure the top is covered
		foliageNodes.add(new FoliagePlacer.TreeNode(currentPosition.toImmutable().up(), (converge ? 1 : radius), true));

		//Generate the roots
		growRoots(set, world, pos.mutableCopy(), random, blockBox, ((QuarteredMegaTreeConfig) treeFeatureConfig));

		//Return the nodes as an Immutable List to be placed later
		return ImmutableList.copyOf(foliageNodes);
	}

	private void placeLayer(ModifiableTestableWorld world, Random random, BlockPos pos, Set<BlockPos> set, BlockBox blockBox, QuarteredMegaTreeConfig treeFeatureConfig) {
		checkAndPlaceSpecificBlockState(world, random, pos.south(), set, blockBox, TerrestriaConfigManager.getGeneralConfig().areQuarterLogsEnabled() ? treeFeatureConfig.quarterLogBlock.with(QuarterLogBlock.BARK_SIDE, QuarterLogBlock.BarkSide.SOUTHWEST) : treeFeatureConfig.logBlock);
		checkAndPlaceSpecificBlockState(world, random, pos.east(), set, blockBox, TerrestriaConfigManager.getGeneralConfig().areQuarterLogsEnabled() ? treeFeatureConfig.quarterLogBlock.with(QuarterLogBlock.BARK_SIDE, QuarterLogBlock.BarkSide.NORTHEAST): treeFeatureConfig.logBlock);
		checkAndPlaceSpecificBlockState(world, random, pos.south().east(), set, blockBox, TerrestriaConfigManager.getGeneralConfig().areQuarterLogsEnabled() ? treeFeatureConfig.quarterLogBlock.with(QuarterLogBlock.BARK_SIDE, QuarterLogBlock.BarkSide.SOUTHEAST): treeFeatureConfig.logBlock);
		checkAndPlaceSpecificBlockState(world, random, pos, set, blockBox, TerrestriaConfigManager.getGeneralConfig().areQuarterLogsEnabled() ? treeFeatureConfig.quarterLogBlock.with(QuarterLogBlock.BARK_SIDE, QuarterLogBlock.BarkSide.NORTHWEST): treeFeatureConfig.logBlock);
	}

	private static void checkAndPlaceSpecificBlockState(ModifiableTestableWorld modifiableTestableWorld, Random random, BlockPos blockPos, Set<BlockPos> set, BlockBox blockBox, BlockState blockState) {
		if (TreeFeature.canReplace(modifiableTestableWorld, blockPos)) {
			method_27404(modifiableTestableWorld, blockPos, blockState, blockBox);
			set.add(blockPos.toImmutable());
		}
	}

	public void growRoots(Set<BlockPos> logs, ModifiableTestableWorld world, BlockPos.Mutable pos, Random random, BlockBox box, QuarteredMegaTreeConfig treeFeatureConfig) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		tryGrowRoot(logs, world, pos.set(x - 1, y, z + random.nextInt(2)), random, box, treeFeatureConfig);
		tryGrowRoot(logs, world, pos.set(x + 2, y, z + random.nextInt(2)), random, box, treeFeatureConfig);
		tryGrowRoot(logs, world, pos.set(x + random.nextInt(2), y, z - 1), random, box, treeFeatureConfig);
		tryGrowRoot(logs, world, pos.set(x + random.nextInt(2), y, z + 2), random, box, treeFeatureConfig);
	}

	public void tryGrowRoot(Set<BlockPos> logs, ModifiableTestableWorld world, BlockPos.Mutable bottom, Random random, BlockBox box, QuarteredMegaTreeConfig treeFeatureConfig) {
		if (random.nextInt(5) == 0) {
			return;
		}

		int height = random.nextInt(4) + 1;

		for (int i = 0; i < height; i++) {
			if (TreeFeature.canTreeReplace(world, bottom) || TreeFeature.canReplace(world, bottom) || world.testBlockState(bottom, state -> state.getBlock() instanceof TallSeagrassBlock)) {
				checkAndPlaceSpecificBlockState(world, random, bottom, logs, box, treeFeatureConfig.woodBlock);
			}

			bottom.move(Direction.UP);
		}
	}
}