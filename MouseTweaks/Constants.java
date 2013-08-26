package MouseTweaks;

public class Constants
{
    
    // Mod name
    public static final String  NAME                 = "Mouse Tweaks";
    
    // Mod version
    public static final String  VERSION              = "2.3.3 (for Minecraft 1.6.2)";
    
    // Obfuscated names
    public static final String  ISMOUSEOVERSLOT_NAME = "a";
    public static final String  FIELDE_NAME          = "G";
    public static final String  FIELDq_NAME          = "s";
    public static final String  MCPROFILER_NAME      = "C";
    
    // Inventory-related stuff
    public static final int     INVENTORY_SIZE       = 36;                           // Size of the player inventory
                                                                                      
    // Mod GUI container IDs
    public static final int     NOTASSIGNED          = -1;                           // When we haven't determined it yet.
    public static final int     NOTGUICONTAINER      = 0;                            // This is not a container GUI.
    public static final int     MINECRAFT            = 1;                            // Containers that should be compatible with vanilla Minecraft ones.
    public static final int     FORESTRY             = 2;                            // Forestry containers.
    public static final int     CODECHICKENCORE      = 3;                            // CodeChickenCore containers.
    public static final int     NEI                  = 4;                            // NotEnoughItems containers (like the crafting menu).
                                                                                      
    public static final boolean DEBUG                = false;                        // Debug mode (writing many stuff into STDOUT)
                                                                                      
}
