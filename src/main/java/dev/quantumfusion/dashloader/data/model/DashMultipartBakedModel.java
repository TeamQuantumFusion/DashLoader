package dev.quantumfusion.dashloader.data.model;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.common.IntIntList;
import dev.quantumfusion.dashloader.data.common.IntObjectList;
import dev.quantumfusion.dashloader.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.data.model.predicates.DashPredicate;
import dev.quantumfusion.dashloader.mixin.accessor.MultipartBakedModelAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import dev.quantumfusion.dashloader.util.RegistryUtil;
import dev.quantumfusion.dashloader.util.UnsafeHelper;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.MultipartBakedModel;
import org.apache.commons.lang3.tuple.Pair;
import static dev.quantumfusion.dashloader.DashLoader.DL;

@DashObject(MultipartBakedModel.class)
@DashDependencies({DashWeightedBakedModel.class, DashPredicate.class, DashBlockState.class})
public class DashMultipartBakedModel implements DashModel {
	//identifier baked model
	public final IntIntList components;
	public final IntObjectList<byte[]> stateCache;
	transient MultipartBakedModel toApply;

	public DashMultipartBakedModel(IntIntList components, IntObjectList<byte[]> stateCache) {
		this.components = components;
		this.stateCache = stateCache;
	}

	public DashMultipartBakedModel(MultipartBakedModel model, RegistryWriter writer) {
		final DashDataManager.DashWriteContextData writeContextData = DL.getData().getWriteContextData();
		var selectors = writeContextData.multipartPredicates.get(model);
		var access = ((MultipartBakedModelAccessor) model);
		var accessComponents = access.getComponents();
		int size = accessComponents.size();

		this.components = new IntIntList(new ArrayList<>(size));
		for (int i = 0; i < size; i++) {
			var right = accessComponents.get(i).getRight();
			var selector = selectors.getKey().get(i);
			writeContextData.stateManagers.put(selector, selectors.getValue());
			this.components.put(writer.add(RegistryUtil.preparePredicate(selector)), writer.add(right));
		}
		this.stateCache = new IntObjectList<>();
		access.getStateCache().forEach((blockState, bitSet) -> this.stateCache.put(writer.add(blockState), bitSet.toByteArray()));
	}

	@Override
	public MultipartBakedModel export(RegistryReader reader) {
		MultipartBakedModel model = UnsafeHelper.allocateInstance(MultipartBakedModel.class);

		Map<BlockState, BitSet> stateCacheOut = new Reference2ObjectOpenHashMap<>();
		this.stateCache.forEach((blockstate, bitSet) -> stateCacheOut.put(reader.get(blockstate), BitSet.valueOf(bitSet)));
		((MultipartBakedModelAccessor) model).setStateCache(stateCacheOut);

		this.toApply = model;
		return model;
	}

	@Override
	public void postExport(RegistryReader reader) {
		var access = ((MultipartBakedModelAccessor) this.toApply);

		List<Pair<Predicate<BlockState>, BakedModel>> componentsOut = new ArrayList<>();
		this.components.forEach((key, value) -> componentsOut.add(Pair.of(reader.get(key), reader.get(value))));

		var bakedModel = componentsOut.iterator().next().getRight();
		access.setComponents(componentsOut);
		access.setAmbientOcclusion(bakedModel.useAmbientOcclusion());
		access.setDepthGui(bakedModel.hasDepth());
		access.setSideLit(bakedModel.isSideLit());
		access.setSprite(bakedModel.getParticleSprite());
		access.setTransformations(bakedModel.getTransformation());
		access.setItemPropertyOverrides(bakedModel.getOverrides());
	}
}

