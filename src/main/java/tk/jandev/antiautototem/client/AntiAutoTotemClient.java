package tk.jandev.antiautototem.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import tk.jandev.antiautototem.tick.Tick;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class AntiAutoTotemClient implements ClientModInitializer {
    private final Map<UUID, Long> playerMap = new HashMap<>();
    @Override

    public void onInitializeClient() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (mc.player==null) return;
            Tick.currentTick++;

            for (PlayerEntity p : mc.world.getPlayers()) {

                if (!playerMap.containsKey(p.getUuid())) {
                    playerMap.put(p.getUuid(), getTotem(p));
                }

                if (hasTotem(p) && playerMap.get(p.getUuid()) != Tick.currentTick-1) {

                    if (Tick.currentTick-playerMap.get(p.getUuid()) <= Tick.tickCheck && Tick.enabled) {
                        mc.player.sendMessage(Text.of("§cFlagged "+p.getName().getString()+" for autototem! Ticks diff: "+Tick.currentTick-playerMap.get(p.getUuid())));
                    }

                }
                if (hasTotem(p)) {
                    playerMap.put(p.getUuid(), Tick.currentTick);
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("toggleflags").executes(context -> {
            if (Tick.enabled) {
                context.getSource().sendFeedback(Text.of("§cDisabled flag messages."));
            } else {
                context.getSource().sendFeedback(Text.of("§2Enabled flag messages."));
            }
            Tick.enabled = !Tick.enabled;
            return 1;
        })));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("totemchecker").then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer())).executes(context -> {
            Tick.tickCheck = IntegerArgumentType.getInteger(context, "ticks");
            context.getSource().sendFeedback(Text.of("§dUpdated totem checking to "+Tick.tickCheck));
            return 1;
        })));
    }

    private static long getTotem(PlayerEntity player) {
        if (hasTotem(player)) return Tick.currentTick;
        return -1;
    }

    private static boolean hasTotem(PlayerEntity player) {
        return (player.getOffHandStack().getItem()== Items.TOTEM_OF_UNDYING);
    }

}
