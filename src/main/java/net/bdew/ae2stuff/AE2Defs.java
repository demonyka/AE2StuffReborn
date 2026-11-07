package net.bdew.ae2stuff;

import appeng.api.IAppEngApi;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;

public final class AE2Defs {
    private AE2Defs() {
    }

    public static IAppEngApi api() {
        return Api.instance();
    }

    public static IMaterials materials() {
        return api().definitions().materials();
    }

    public static IItems items() {
        return api().definitions().items();
    }
}
