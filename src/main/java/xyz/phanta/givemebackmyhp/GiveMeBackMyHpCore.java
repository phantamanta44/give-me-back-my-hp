package xyz.phanta.givemebackmyhp;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(1540)
public class GiveMeBackMyHpCore implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "xyz.phanta.givemebackmyhp.GiveMeBackMyHpClassTransformer" };
    }

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
