package MouseTweaks;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Main extends DeobfuscationLayer
{
    
    private static GuiScreen oldGuiScreen                 = null;
    private static Object    container                    = null;
    private static Slot      oldSelectedSlot              = null;
    private static Slot      firstSlot                    = null;
    private static int       slotCount                    = 0;
    private static boolean   firstSlotClicked             = false;
    private static boolean   shouldClick                  = true;
    private static boolean   oldRMBDown                   = false;
    private static boolean   RMBStateChanged              = false;
    private static boolean   disableForThisContainer      = false;
    private static boolean   disableWheelForThisContainer = false;
    
    private static int       guiContainerID               = 0;
    
    private static boolean   readConfig                   = false;
    private static boolean   initialised                  = false;
    private static boolean   disabled                     = false;
    
    public static boolean    liteLoader                   = false;
    public static boolean    minecraftForge               = false;
    public static boolean    optifine                     = false;
    public static boolean    modLoader                    = false;
    public static boolean    useModLoader                 = false;
    
    public static boolean    DisableRMBTweak              = false;
    
    public static int        RMBTweak                     = 0;
    public static int        LMBTweakWithItem             = 0;
    public static int        LMBTweakWithoutItem          = 0;
    public static int        WheelTweak                   = 0;
    public static int        ForceModLoader               = 0;
    public static int        WheelSearchOrder             = 1;
    
    public static Config     mainConfig;
    
    public static boolean initialise()
    {
        if ( disabled )
            return false;
        
        if ( initialised )
            return true;
        
        initialised = true;
        
        mc = Minecraft.getMinecraft();
        
        mainConfig = new Config( mc.mcDataDir + File.separator + "config"
                + File.separator + "MouseTweaks.cfg" );
        readConfigFile();
        
        modLoader = Reflection.doesClassExist( "net.minecraft.src.ModLoader" );
        minecraftForge = Reflection
                .doesClassExist( "net.minecraftforge.client.MinecraftForgeClient" );
        
        if ( ForceModLoader == 1 )
        {
            if ( modLoader )
            {
                useModLoader = true;
                Logger.Log( "ForceModLoader is set to 1, using ModLoader's onTick for mod operation..." );
            }
            else
            {
                Logger.Log( "ForceModLoader is set to 1, but ModLoader's main class is not present, quitting..." );
                disabled = true;
                return false;
            }
        }
        else
        {
            liteLoader = Reflection.reflectLiteLoader();
            optifine = Reflection.reflectOptifine();
        }
        
        if ( minecraftForge )
        {
            Logger.Log( "Minecraft Forge is installed" );
        }
        else
        {
            Logger.Log( "Minecraft Forge is not installed" );
        }
        
        if ( !liteLoader && ( ForceModLoader == 0 ) )
        {
            useModLoader = !Reflection.reflectMinecraft();
            if ( !useModLoader )
            {
                if ( !Reflection.replaceProfiler() )
                {
                    if ( modLoader )
                    {
                        useModLoader = true;
                        Logger.Log( "Using ModLoader for mod operation" );
                    }
                    else
                    {
                        Logger.Log( "Failed to replace Minecraft profiler, quitting" );
                        
                        disabled = true;
                        return false;
                    }
                }
            }
            else
            {
                Logger.Log( "Using ModLoader for mod operation" );
            }
        }
        
        boolean returnValue = Reflection.reflectGuiContainer();
        
        if ( returnValue )
        {
            ModCompatibility.initialize();
            Logger.Log( Constants.NAME + " version " + Constants.VERSION
                    + " has been initialised" );
        }
        else
        {
            disabled = true;
        }
        
        return returnValue;
    }
    
    public static void readConfigFile()
    {
        if ( !mainConfig.readConfig() )
        {
            mainConfig.setPropertyValue( "RMBTweak", 1 );
            mainConfig.setPropertyValue( "LMBTweakWithItem", 1 );
            mainConfig.setPropertyValue( "LMBTweakWithoutItem", 1 );
            mainConfig.setPropertyValue( "WheelTweak", 1 );
            mainConfig.setPropertyValue( "WheelSearchOrder", 1 );
            mainConfig.setPropertyValue( "ForceModLoader", 0 );
            
            mainConfig.saveConfig();
            
            Logger.Log( "Mouse Tweaks config file was created." );
        }
        
        RMBTweak = mainConfig.getOrCreatePropertyValue( "RMBTweak", 1 );
        LMBTweakWithItem = mainConfig.getOrCreatePropertyValue( "LMBTweakWithItem", 1 );
        LMBTweakWithoutItem = mainConfig.getOrCreatePropertyValue( "LMBTweakWithoutItem", 1 );
        WheelTweak = mainConfig.getOrCreatePropertyValue( "WheelTweak", 1 );
        WheelSearchOrder = mainConfig.getOrCreatePropertyValue( "WheelSearchOrder", 1 );
        ForceModLoader = mainConfig.getOrCreatePropertyValue( "ForceModLoader", 0 );
        
        mainConfig.saveConfig();
    }
    
    public static void onUpdateInGame()
    {
        GuiScreen currentScreen = getCurrentScreen();
        if ( currentScreen == null )
        {
            // Reset stuff
            oldGuiScreen = null;
            container = null;
            oldSelectedSlot = null;
            firstSlot = null;
            slotCount = 0;
            firstSlotClicked = false;
            shouldClick = true;
            oldRMBDown = false;
            RMBStateChanged = false;
            disableForThisContainer = false;
            disableWheelForThisContainer = false;
            readConfig = true;
            
            guiContainerID = -1;
        }
        else
        {
            
            if ( readConfig )
            {
                readConfig = false;
                readConfigFile();
            }
            
            if ( guiContainerID == Constants.NOTASSIGNED )
            {
                guiContainerID = getGuiContainerID( currentScreen );
            }
            
            onUpdateInGui( currentScreen, guiContainerID );
            
        }
    }
    
    public static void onUpdateInGui( GuiScreen currentScreen, int guiContainerID )
    {
        
        if ( guiContainerID == Constants.NOTGUICONTAINER )
            return;
        
        if ( oldGuiScreen != currentScreen )
        {
            oldGuiScreen = currentScreen;
            
            container = getContainerWithID( currentScreen );
            slotCount = getSlotCountWithID( currentScreen );
            
            if ( Constants.DEBUG )
            {
                Logger.Log( new StringBuilder().append( "You have just opened a " ).append( getGuiContainerNameFromID() ).append( " container (" ).append( currentScreen.getClass().getSimpleName() ).append( ( container == null ) ? "" : "; " ).append( ( container == null ) ? "" : container.getClass().getSimpleName() ).append( "), which has " ).append( slotCount ).append( " slots!" ).toString() );
            }
            
            if ( slotCount == 0 )
            {
                disableForThisContainer = true;
                return;
            }
            
            disableWheelForThisContainer = isWheelDisabledForThisContainer( currentScreen );
        }
        
        if ( ( Main.DisableRMBTweak || ( Main.RMBTweak == 0 ) ) && ( Main.LMBTweakWithoutItem == 0 ) && ( Main.LMBTweakWithItem == 0 )
                && ( Main.WheelTweak == 0 ) )
            return;
        
        if ( disableForThisContainer )
            return;
        
        int wheel = ( ( Main.WheelTweak == 1 ) && !disableWheelForThisContainer ) ? Mouse.getDWheel() / 120 : 0;
        
        if ( !Mouse.isButtonDown( 1 ) )
        {
            firstSlotClicked = false;
            shouldClick = true;
        }
        
        if ( Mouse.isButtonDown( 1 ) != oldRMBDown )
        {
            RMBStateChanged = true;
        }
        else
        {
            RMBStateChanged = false;
        }
        
        Slot selectedSlot = getSelectedSlotWithID( currentScreen );
        
        // Copy the stacks, so that they don't change while we do our stuff.
        ItemStack stackOnMouse = copyItemStack( getStackOnMouse() );
        ItemStack targetStack = copyItemStack( getSlotStack( selectedSlot ) );
        
        if ( oldSelectedSlot != selectedSlot )
        {
            // To correctly determine, when the default RMB drag needs to be disabled, we need a bunch of conditions.
            if ( Mouse.isButtonDown( 1 ) && !firstSlotClicked && ( firstSlot == null ) && ( oldSelectedSlot != null ) )
            {
                if ( !areStacksCompatible( stackOnMouse, getSlotStack( oldSelectedSlot ) ) )
                {
                    shouldClick = false;
                }
                
                firstSlot = oldSelectedSlot;
            }
            
            if ( RMBStateChanged && ( oldSelectedSlot == null ) && !firstSlotClicked && ( firstSlot == null ) )
            {
                shouldClick = false;
            }
            
            if ( selectedSlot == null )
            {
                oldSelectedSlot = selectedSlot;
                
                if ( ( firstSlot != null ) && !firstSlotClicked )
                {
                    firstSlotClicked = true;
                    disableRMBDragWithID( currentScreen );
                    firstSlot = null;
                }
                
                return;
            }
            
            if ( Constants.DEBUG )
            {
                Logger.Log( new StringBuilder().append( "You have selected a new slot, it's slot number is " ).append( getSlotNumber( selectedSlot ) ).toString() );
            }
            
            boolean shiftIsDown = Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );
            
            if ( Mouse.isButtonDown( 1 ) )
            { // Right mouse button
                if ( ( Main.RMBTweak == 1 ) && !Main.DisableRMBTweak )
                {
                    
                    if ( ( stackOnMouse != null ) && areStacksCompatible( stackOnMouse, targetStack ) )
                    {
                        if ( ( firstSlot != null ) && !firstSlotClicked )
                        {
                            firstSlotClicked = true;
                            disableRMBDragWithID( currentScreen );
                            firstSlot = null;
                        }
                        
                        clickSlot( currentScreen, selectedSlot, 1, false );
                    }
                    
                }
            }
            else if ( Mouse.isButtonDown( 0 ) )
            { // Left mouse button
                if ( stackOnMouse != null )
                {
                    if ( Main.LMBTweakWithItem == 1 )
                    {
                        if ( ( targetStack != null ) && areStacksCompatible( stackOnMouse, targetStack ) )
                        {
                            
                            if ( shiftIsDown )
                            { // If shift is down, we just shift-click the slot and the item gets moved into another inventory.
                                clickSlot( currentScreen, selectedSlot, 0, true );
                            }
                            else
                            { // If shift is not down, we need to merge the item stack on the mouse with the one in the slot.
                                if ( ( getItemStackSize( stackOnMouse ) + getItemStackSize( targetStack ) ) <= getMaxItemStackSize( stackOnMouse ) )
                                {
                                    clickSlot( currentScreen, selectedSlot, 0, false ); // We need to click on the slot so that our item stack gets merged with
                                    clickSlot( currentScreen, selectedSlot, 0, false ); // it, and then click again to return the stack to the mouse.
                                }
                            }
                        }
                        
                    }
                }
                else if ( Main.LMBTweakWithoutItem == 1 )
                {
                    if ( targetStack != null )
                    {
                        if ( shiftIsDown )
                        {
                            clickSlot( currentScreen, selectedSlot, 0, true );
                        }
                    }
                }
            }
            
            oldSelectedSlot = selectedSlot;
        }
        
        if ( wheel != 0 )
        {
            int numItemsToMove = Math.abs( wheel );
            
            if ( slotCount > Constants.INVENTORY_SIZE )
            {
                ItemStack originalStack = getSlotStack( selectedSlot );
                boolean isCraftingOutput = isCraftingOutputSlot( currentScreen, selectedSlot );
                
                if ( ( originalStack != null )
                        && ( ( stackOnMouse == null ) || ( isCraftingOutput ? areStacksCompatible( originalStack, stackOnMouse ) : !areStacksCompatible( originalStack, stackOnMouse ) ) ) )
                {
                    do
                    {
                        Slot applicableSlot = null;
                        
                        int slotCounter = 0;
                        int countUntil = slotCount
                                - Constants.INVENTORY_SIZE;
                        if ( getSlotNumber( selectedSlot ) < countUntil )
                        {
                            slotCounter = countUntil;
                            countUntil = slotCount;
                        }
                        
                        if ( ( wheel < 0 ) || ( Main.WheelSearchOrder == 0 ) )
                        {
                            for ( int i = slotCounter; i < countUntil; i++ )
                            {
                                Slot sl = getSlotWithID( currentScreen, i );
                                ItemStack stackSl = getSlotStack( sl );
                                
                                if ( stackSl == null )
                                {
                                    if ( ( applicableSlot == null )
                                            && ( wheel < 0 )
                                            && sl.isItemValid( originalStack )
                                            && !isCraftingOutputSlot( currentScreen, sl ) )
                                    {
                                        applicableSlot = sl;
                                    }
                                }
                                else if ( areStacksCompatible( originalStack, stackSl ) )
                                {
                                    if ( ( wheel < 0 )
                                            && ( stackSl.stackSize < stackSl.getMaxStackSize() ) )
                                    {
                                        applicableSlot = sl;
                                        break;
                                    }
                                    else if ( wheel > 0 )
                                    {
                                        applicableSlot = sl;
                                        break;
                                    }
                                }
                            }
                        }
                        else
                        {
                            for ( int i = countUntil - 1; i >= slotCounter; i-- )
                            {
                                Slot sl = getSlotWithID( currentScreen, i );
                                ItemStack stackSl = getSlotStack( sl );
                                
                                if ( stackSl == null )
                                {
                                    if ( ( applicableSlot == null )
                                            && ( wheel < 0 )
                                            && sl.isItemValid( originalStack ) )
                                    {
                                        applicableSlot = sl;
                                    }
                                }
                                else if ( areStacksCompatible( originalStack, stackSl ) )
                                {
                                    if ( ( wheel < 0 )
                                            && ( stackSl.stackSize < stackSl.getMaxStackSize() ) )
                                    {
                                        applicableSlot = sl;
                                        break;
                                    }
                                    else if ( wheel > 0 )
                                    {
                                        applicableSlot = sl;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if ( isCraftingOutput )
                        {
                            if ( wheel < 0 )
                            {
                                boolean mouseWasEmpty = stackOnMouse == null;
                                
                                for ( int i = 0; i < numItemsToMove; i++ )
                                {
                                    clickSlot( currentScreen, selectedSlot, 0, false );
                                }
                                
                                if ( ( applicableSlot != null )
                                        && mouseWasEmpty )
                                {
                                    clickSlot( currentScreen, applicableSlot, 0, false );
                                }
                            }
                            
                            break;
                        }
                        
                        if ( applicableSlot != null )
                        {
                            Slot slotTo = ( wheel < 0 ) ? applicableSlot : selectedSlot;
                            Slot slotFrom = ( wheel < 0 ) ? selectedSlot : applicableSlot;
                            ItemStack stackTo = ( getSlotStack( slotTo ) != null ) ? copyItemStack( getSlotStack( slotTo ) ) : null;
                            ItemStack stackFrom = copyItemStack( getSlotStack( slotFrom ) );
                            
                            if ( wheel < 0 )
                            {
                                if ( ( stackTo != null )
                                        && ( ( getMaxItemStackSize( stackTo ) - getItemStackSize( stackTo ) ) <= numItemsToMove ) )
                                {
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                    clickSlot( currentScreen, slotTo, 0, false );
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                    
                                    numItemsToMove -= getMaxItemStackSize( stackTo )
                                            - getItemStackSize( stackTo );
                                }
                                else
                                {
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                    
                                    if ( getItemStackSize( stackFrom ) <= numItemsToMove )
                                    {
                                        clickSlot( currentScreen, slotTo, 0, false );
                                    }
                                    else
                                    {
                                        for ( int i = 0; i < numItemsToMove; i++ )
                                        {
                                            clickSlot( currentScreen, slotTo, 1, false );
                                        }
                                    }
                                    
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                    
                                    numItemsToMove = 0;
                                }
                            }
                            else
                            {
                                if ( ( getMaxItemStackSize( stackTo ) - getItemStackSize( stackTo ) ) <= numItemsToMove )
                                {
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                    clickSlot( currentScreen, slotTo, 0, false );
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                }
                                else
                                {
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                    
                                    if ( stackFrom.stackSize <= numItemsToMove )
                                    {
                                        clickSlot( currentScreen, slotTo, 0, false );
                                        numItemsToMove -= getMaxItemStackSize( stackFrom );
                                    }
                                    else
                                    {
                                        for ( int i = 0; i < numItemsToMove; i++ )
                                        {
                                            clickSlot( currentScreen, slotTo, 1, false );
                                        }
                                        
                                        numItemsToMove = 0;
                                    }
                                    
                                    clickSlot( currentScreen, slotFrom, 0, false );
                                }
                                
                                if ( getMaxItemStackSize( stackTo ) == getMaxItemStackSize( stackTo ) )
                                {
                                    numItemsToMove = 0;
                                }
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                    while ( numItemsToMove != 0 );
                }
            }
        }
    }
    
    public static int getGuiContainerID( GuiScreen currentScreen )
    {
        if ( isGuiContainer( currentScreen ) && isValidGuiContainer( currentScreen ) )
            return Constants.MINECRAFT;
        else
            return ModCompatibility.getModGuiContainerID( currentScreen );
    }
    
    public static Object getContainerWithID( GuiScreen currentScreen )
    {
        if ( guiContainerID == Constants.MINECRAFT )
            return getContainer( asGuiContainer( currentScreen ) );
        else
            return ModCompatibility.getModContainer( guiContainerID, currentScreen );
    }
    
    public static int getSlotCountWithID( GuiScreen currentScreen )
    {
        if ( guiContainerID == Constants.MINECRAFT )
            return getSlots( asContainer( container ) ).size();
        else
            return ModCompatibility.getModSlotCount( guiContainerID, currentScreen, container );
    }
    
    public static String getGuiContainerNameFromID()
    {
        switch ( guiContainerID )
        {
            case Constants.NOTASSIGNED:
                return "Unknown";
            case Constants.NOTGUICONTAINER:
                return "Wrong";
            case Constants.MINECRAFT:
                return "Vanilla Minecraft";
                
            default:
                return ModCompatibility.getModNameFromModGuiContainerID( guiContainerID );
        }
    }
    
    public static boolean isWheelDisabledForThisContainer( GuiScreen currentScreen )
    {
        if ( guiContainerID == Constants.MINECRAFT )
            return false;
        else
            return ModCompatibility.isWheelDisabledForThisModContainer( guiContainerID, currentScreen );
    }
    
    public static Slot getSelectedSlotWithID( GuiScreen currentScreen )
    {
        if ( guiContainerID == Constants.MINECRAFT )
            return getSelectedSlot( asGuiContainer( currentScreen ), asContainer( container ), slotCount );
        else
            return ModCompatibility.getModSelectedSlot( guiContainerID, currentScreen );
    }
    
    public static void clickSlot( GuiScreen currentScreen, Slot targetSlot, int mouseButton, boolean shiftPressed )
    {
        if ( guiContainerID == Constants.MINECRAFT )
        {
            windowClick( getWindowId( asContainer( container ) ), getSlotNumber( targetSlot ), mouseButton, shiftPressed ? 1 : 0 );
        }
        else
        {
            ModCompatibility.modClickSlot( guiContainerID, currentScreen, targetSlot, mouseButton, shiftPressed );
        }
    }
    
    public static boolean isCraftingOutputSlot( GuiScreen currentScreen, Slot targetSlot )
    {
        if ( guiContainerID == Constants.MINECRAFT )
            return isVanillaCraftingOutputSlot( asContainer( container ), targetSlot );
        else
            return ModCompatibility.modIsCraftingOutputSlot( guiContainerID, currentScreen, container, targetSlot );
    }
    
    public static Slot getSlotWithID( GuiScreen currentScreen, int slotNumber )
    {
        if ( guiContainerID == Constants.MINECRAFT )
            return getSlot( asContainer( container ), slotNumber );
        else
            return ModCompatibility.modGetSlot( guiContainerID, currentScreen, container, slotNumber );
    }
    
    public static void disableRMBDragWithID( GuiScreen currentScreen )
    {
        if ( guiContainerID == Constants.MINECRAFT )
        {
            disableVanillaRMBDrag( asGuiContainer( currentScreen ) );
            
            if ( shouldClick )
            {
                clickSlot( currentScreen, firstSlot, 1, false );
            }
        }
        else
        {
            ModCompatibility.disableRMBDragIfRequired( guiContainerID, currentScreen );
        }
    }
}
