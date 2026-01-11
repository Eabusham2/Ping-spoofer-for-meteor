package eabusham2.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;

import eabusham2.addon.modules.misc.PingSpoofer;

public class PingSpoofAddon extends MeteorAddon {
    // Category for your module in Meteor's GUI
    public static final Category CATEGORY = new Category("Ping Spoof", Items.CLOCK.getDefaultStack());

    @Override
    public void onInitialize() {
        Modules.get().add(new PingSpoofer());
    }

    @Override
    public String getPackage() {
        return "eabusham2.addon";
    }
}
