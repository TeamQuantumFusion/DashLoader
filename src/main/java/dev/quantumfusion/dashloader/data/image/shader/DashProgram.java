package dev.quantumfusion.dashloader.data.image.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.quantumfusion.dashloader.mixin.accessor.EffectProgramAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.ProgramAccessor;
import dev.quantumfusion.dashloader.util.MissingDataException;
import java.util.List;
import net.minecraft.client.gl.EffectProgram;
import net.minecraft.client.gl.Program;
import org.apache.commons.lang3.StringUtils;
import static dev.quantumfusion.dashloader.DashLoader.DL;

public final class DashProgram {
	public final Program.Type shaderType;
	public final String name;
	public final List<String> shader;

	public DashProgram(Program.Type shaderType, String name, List<String> shader) {
		this.shaderType = shaderType;
		this.name = name;
		this.shader = shader;
	}

	public DashProgram(Program program) throws MissingDataException {
		ProgramAccessor access = (ProgramAccessor) program;
		this.shaderType = access.getShaderType();
		this.name = program.getName();
		List<String> shader = DL.getData().getWriteContextData().programData.get(access.getShaderRef());
		if (shader == null) {
			throw new MissingDataException();
		}
		this.shader = shader;
	}

	public int createProgram(Program.Type type) {
		//noinspection ConstantConditions (MixinAccessor shit)
		int i = GlStateManager.glCreateShader(((ProgramAccessor.TypeAccessor) (Object) type).getGlType());
		GlStateManager.glShaderSource(i, this.shader);
		GlStateManager.glCompileShader(i);
		if (GlStateManager.glGetShaderi(i, 35713) == 0) {
			String string2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
			throw new RuntimeException("Couldn't compile " + type.getName() + " : " + string2);
		} else {
			return i;
		}
	}

	public Program exportProgram() {
		final Program program = ProgramAccessor.create(this.shaderType, this.createProgram(this.shaderType), this.name);
		this.shaderType.getProgramCache().put(this.name, program);
		return program;
	}

	public EffectProgram exportEffectProgram() {
		return EffectProgramAccessor.create(this.shaderType, this.createProgram(this.shaderType), this.name);
	}
}
