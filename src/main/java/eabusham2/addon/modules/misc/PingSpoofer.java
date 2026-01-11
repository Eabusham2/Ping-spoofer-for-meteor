package eabusham2.addon.modules.misc;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;

public class PingSpoofer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> pingMs = sgGeneral.add(new IntSetting.Builder()
        .name("ping")
        .description("Extra ping (ms) added by delaying KeepAlive packets.")
        .defaultValue(250)
        .min(1)
        .sliderMin(0)
        .sliderMax(1000)
        .build()
    );

    // keepAliveId -> time queued (ms)
    private final Long2LongMap queuedAt = new Long2LongOpenHashMap();
    // keepAliveId -> packet instance
    private final Long2ObjectMap<KeepAliveC2SPacket> queuedPackets = new Long2ObjectOpenHashMap<>();

    public PingSpoofer() {
        super(Categories.Misc, "ping-spoofer",
            "Artificially increases measured ping by delaying KeepAlive packets.");
    }

    @Override
    public void onActivate() {
        queuedAt.clear();
        queuedPackets.clear();
    }

    @Override
    public void onDeactivate() {
        flushAll();
    }

    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (!(event.packet instanceof KeepAliveC2SPacket packet)) return;

        long id = packet.getId();

        queuedAt.put(id, System.currentTimeMillis());
        queuedPackets.put(id, packet);
        event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        flushDue();
    }

    private void flushDue() {
        if (mc.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();
        long delay = pingMs.get();

        var it = queuedAt.long2LongEntrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            long id = e.getLongKey();
            long t0 = e.getLongValue();

            if (t0 + delay <= now) {
                KeepAliveC2SPacket pkt = queuedPackets.remove(id);
                it.remove();
                if (pkt != null) mc.getNetworkHandler().sendPacket(pkt);
            }
        }
    }

    private void flushAll() {
        if (mc.getNetworkHandler() == null) return;

        for (var entry : queuedPackets.long2ObjectEntrySet()) {
            mc.getNetworkHandler().sendPacket(entry.getValue());
        }

        queuedAt.clear();
        queuedPackets.clear();
    }
}