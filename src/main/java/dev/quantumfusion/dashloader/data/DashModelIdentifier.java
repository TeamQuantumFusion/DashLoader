package dev.quantumfusion.dashloader.data;


import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.mixin.accessor.ModelIdentifierAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.hyphen.scan.annotations.DataFixedArraySize;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@DashObject(ModelIdentifier.class)
public final class DashModelIdentifier implements DashIdentifierInterface {
	public final String @DataFixedArraySize(3) [] strings;

	public DashModelIdentifier(String[] strings) {
		this.strings = strings;
	}

	public DashModelIdentifier(ModelIdentifier identifier) {
		this.strings = new String[3];
		this.strings[0] = identifier.getNamespace();
		this.strings[1] = identifier.getPath();
		this.strings[2] = identifier.getVariant();
	}

	@Override
	public Identifier export(RegistryReader exportHandler) {
		return ModelIdentifierAccessor.init(this.strings);
	}
}
