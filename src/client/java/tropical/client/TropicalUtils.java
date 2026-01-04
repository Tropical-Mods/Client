package tropical.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;

public class TropicalUtils {
    public static Dimension currentDimension = Dimension.OVERWORLD;

    public enum Dimension {
        NETHER {
            @Override
            public String toString() {
                return "minecraft.the_nether";
            }
        },
        OVERWORLD {
            @Override
            public String toString() {
                return "minecraft.overworld";
            }
        },
        END {
            @Override
            public String toString() {
                return "minecraft.the_end";
            }
        };

        public String simple() {
            switch(this) {
                case NETHER: return "Nether";
                case OVERWORLD: return "Overworld";
                case END: return "End";

                default: return "Overworld";
            }
        }

        public static Dimension getValue(String name) {
            for (var dimension : Dimension.values()) {
                if (name.equals(dimension.toString())) {
                    return dimension;
                }
            }

            return Dimension.OVERWORLD;
        }
    }

    // minecraft.overworld
    // minecraft.the_nether
    // minecraft.the_end
    @Nullable
    public static String getCurrentDimensionString() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) { return null; }

        return client.player.level().dimension().identifier().toLanguageKey();
    }

    public static void dimensionKeyToString() {
        System.out.println(Minecraft.getInstance().player.level().dimension().identifier().toLanguageKey());
    }

    @Nullable
    public static Dimension getCurrentDimension() {
        String cd = getCurrentDimensionString();

        return Dimension.getValue(cd);
    }
}
