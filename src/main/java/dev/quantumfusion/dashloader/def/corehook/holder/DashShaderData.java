package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.util.DashThreading;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.data.image.shader.DashShader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.Shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class DashShaderData {
	public final Map<String, DashShader> shaders;

	public DashShaderData(Map<String, DashShader> shaders) {
		this.shaders = shaders;
	}

	public DashShaderData(DashDataManager data) {
		this.shaders = new HashMap<>();
		data.shaders.getMinecraftData().forEach((s, shader) -> this.shaders.put(s, new DashShader(shader)));
	}

	public Map<String, Shader> export() {
		Map<String, Shader> out = new ConcurrentHashMap<>();
		List<Runnable> runnables = new ArrayList<>();
		shaders.forEach((key, value) -> runnables.add(() -> out.put(key, value.export())));
		DashThreading.run(runnables);
		return out;
	}
}