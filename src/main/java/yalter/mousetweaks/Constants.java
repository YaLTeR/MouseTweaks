package yalter.mousetweaks;

public class Constants
{
    
    // Mod name
    public static final String  NAME                 = "Mouse Tweaks";
    
    // Mod version
    public static final String  VERSION              = "2.4.1";
    
    // Obfuscated names
    public static final String  ISMOUSEOVERSLOT_NAME = "a";
    public static final String  FIELDE_NAME          = "H";
    public static final String  FIELDq_NAME          = "t";
    public static final String  MCPROFILER_NAME      = "A";

    // Forge obfuscated names
    public static final String  ISMOUSEOVERSLOT_FORGE_NAME = "func_146981_a";
    public static final String  FIELDE_FORGE_NAME          = "field_146995_H";
    public static final String  FIELDq_FORGE_NAME          = "field_147007_t";
    public static final String  MCPROFILER_FORGE_NAME      = "field_71424_I";
    
    // Inventory-related stuff
    public static final int     INVENTORY_SIZE       = 36;                           // Size of the player inventory
                                                                                      
    // Mod GUI container IDs
    public static final int     NOTASSIGNED          = -1;                           // When we haven't determined it yet.
    public static final int     NOTGUICONTAINER      = 0;                            // This is not a container GUI.
    public static final int     MINECRAFT            = 1;                            // Containers that should be compatible with vanilla Minecraft ones.
    public static final int		MTMODGUICONTAINER	 = 2;							 // Containers that implement the IMTModGuiContainer interface.
    public static final int     FORESTRY             = 3;                            // Forestry containers.
    public static final int     CODECHICKENCORE      = 4;                            // CodeChickenCore containers.
    public static final int     NEI                  = 5;                            // NotEnoughItems containers (like the crafting menu).
                                                                                      
}
