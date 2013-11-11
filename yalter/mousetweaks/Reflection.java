package yalter.mousetweaks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.minecraft.src.Minecraft;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.Slot;
import net.minecraft.src.Tessellator;

public class Reflection
{
    public static ReflectionCache guiContainerClass;
    public static ReflectionCache liteLoaderClass;
    public static ReflectionCache liteModMouseTweaks;
    public static ReflectionCache gameSettings;
    public static ReflectionCache minecraft;
    
    public static ReflectionCache forestry;
    public static ReflectionCache codechickencore;
    public static ReflectionCache NEI;
    
    public static boolean reflectGuiContainer()
    {
        guiContainerClass = new ReflectionCache();
        guiContainerClass.storeClass( "GuiContainer", GuiContainer.class );
        
        Method isMouseOverSlot = getMethod( GuiContainer.class,
                getObfuscatedName( "isMouseOverSlot", "func_74186_a", Constants.ISMOUSEOVERSLOT_NAME ), Slot.class, int.class,
                int.class );
        
        if ( isMouseOverSlot == null )
        {
            Logger.Log( "Failed to get isMouseOverSlot method, quitting" );
            return false;
        }
        
        guiContainerClass.storeMethod( "isMouseOverSlot", isMouseOverSlot );
        
        Field field;
        field = getField( GuiContainer.class,
                getObfuscatedName( "field_94068_E", "field_94068_E", Constants.FIELDE_NAME ) );
        
        if ( field == null )
        {
            Logger.Log( "Failed to retrieve field_94068_E, disabling RMBTweak" );
            Main.DisableRMBTweak = true;
        }
        else
        {
            guiContainerClass.storeField( "field_94068_E", field );
            
            field = getField( GuiContainer.class,
                    getObfuscatedName( "field_94076_q", "field_94076_q", Constants.FIELDq_NAME ) );
            
            if ( field == null )
            {
                Logger.Log( "Failed to retreive field_94076_q, disabling RMBTweak" );
                Main.DisableRMBTweak = true;
            }
            else
            {
                guiContainerClass.storeField( "field_94076_q", field );
            }
        }
        
        return true;
    }
    
    public static boolean reflectLiteLoader()
    {
        liteLoaderClass = new ReflectionCache();
        
        Class lL = getClass( "com.mumfrey.liteloader.core.LiteLoader" );
        if ( lL != null )
        {
            liteLoaderClass.storeClass( "LiteLoader", lL );
            
            liteLoaderClass.available = true;
            
            Class rL = getClass( "com.mumfrey.liteloader.RenderListener" );
            if ( rL != null )
            {
                Method addRenderListener = getMethod( lL, "addRenderListener", rL );
                if ( addRenderListener != null )
                {
                    liteLoaderClass.storeMethod( "addRenderListener", addRenderListener );
                    
                    Method getInstance = getMethod( lL, "getInstance" );
                    if ( getInstance != null )
                    {
                        liteLoaderClass.storeMethod( "getInstance", getInstance );
                        
                        Object liteLoaderInstance = liteLoaderClass.invokeStaticMethod( "LiteLoader", "getInstance" );
                        if ( liteLoaderInstance != null )
                        {
                            Class mouseTweaks = getClass( "MouseTweaks.LiteModMouseTweaks" );
                            if ( mouseTweaks != null )
                            {
                                liteModMouseTweaks = new ReflectionCache();
                                liteModMouseTweaks.storeClass( "LiteModMouseTweaks", mouseTweaks );
                                
                                Method getLMMTInstance = getMethod( mouseTweaks, "getInstance" );
                                if ( getLMMTInstance != null )
                                {
                                    liteModMouseTweaks.storeMethod( "getInstance", getLMMTInstance );
                                    
                                    Object LMMTInstance = liteModMouseTweaks.invokeStaticMethod( "LiteModMouseTweaks", "getInstance" );
                                    if ( LMMTInstance != null )
                                    {
                                        liteLoaderClass.invokeMethod( liteLoaderInstance, "addRenderListener", LMMTInstance );
                                        
                                        liteLoaderClass.compatible = true;
                                        Logger.Log( "LiteLoader is installed and compatible" );
                                        
                                        return true;
                                    }
                                    else
                                    {
                                        Logger.Log( "Could not get instance from getInstance method of LiteModMouseTweaks class! Have you modified the class?" );
                                    }
                                }
                                else
                                {
                                    Logger.Log( "Could not get getInstance method from LiteModMouseTweaks class! Have you modified the class?" );
                                }
                            }
                            else
                            {
                                Logger.Log( "Could not get LiteModMouseTweaks class! Have you deleted it accidentally?" );
                            }
                        }
                        else
                        {
                            Logger.Log( "Failed to retrieve LiteLoader instance!" );
                        }
                    }
                }
            }
        }
        
        Logger.Log( ( !liteLoaderClass.available ) ? "LiteLoader is not installed" : "LiteLoader is installed, but not compatible" );
        return false;
    }
    
    public static boolean reflectOptifine()
    {
        gameSettings = new ReflectionCache();
        gameSettings.storeClass( "GameSetting", GameSettings.class );
        
        Field optifineProfilerEnabled = getField( GameSettings.class, "ofProfiler" );
        if ( optifineProfilerEnabled != null )
        {
            gameSettings.storeField( "ofProfiler", optifineProfilerEnabled );
            
            Logger.Log( "Optifine is installed" );
            return true;
        }
        
        Logger.Log( "Optifine is not installed" );
        return false;
    }
    
    public static boolean reflectMinecraft()
    {
        minecraft = new ReflectionCache();
        minecraft.storeClass( "Minecraft", Minecraft.class );
        
        Field profilerField = getFinalField( Minecraft.class, getObfuscatedName( "mcProfiler", "field_71424_I", Constants.MCPROFILER_NAME ) );
        if ( profilerField != null )
        {
            minecraft.storeField( "mcProfiler", profilerField );
            return true;
        }
        
        Logger.Log( "Failed to get the Minecraft profiler field!" );
        return false;
    }
    
    public static boolean replaceProfiler()
    {
        return minecraft.setFieldValue( Minecraft.getMinecraft(), "mcProfiler", new ProfilerCustom() );
    }
    
    public static boolean reflectForestry()
    {
        forestry = new ReflectionCache();
        
        Class guiForestryClass = getClass( "forestry.core.gui.GuiForestry" );
        if ( guiForestryClass != null )
        {
            forestry.storeClass( "GuiForestry", guiForestryClass );
            forestry.available = true;
            
            Field inventorySlotsField = getField( guiForestryClass, "inventorySlots" );
            if ( inventorySlotsField != null )
            {
                forestry.storeField( "inventorySlots", inventorySlotsField );
                
                Method getSlotAtPositionMethod = getMethod( guiForestryClass, "getSlotAtPosition", int.class, int.class );
                if ( getSlotAtPositionMethod != null )
                {
                    forestry.storeMethod( "getSlotAtPosition", getSlotAtPositionMethod );
                    
                    Method handleMouseClickMethod = getMethod( guiForestryClass, "handleMouseClick", Slot.class, int.class, int.class, int.class );
                    if ( handleMouseClickMethod != null )
                    {
                        forestry.storeMethod( "handleMouseClick", handleMouseClickMethod );
                        
                        forestry.compatible = true;
                        
                        // Class containerForestryClass = getClass( "forestry.core.gui.ContainerForestry" );
                        // if ( containerForestryClass != null ) {
                        // forestry.storeClass( "ContainerForestry", containerForestryClass );
                        //
                        // Field slotCountField = getField( containerForestryClass, "slotCount" );
                        // if ( slotCountField != null ) {
                        // forestry.storeField( "slotCount", slotCountField );
                        //
                        // forestry.compatible = true;
                        // }
                        // }
                    }
                }
            }
        }
        
        return forestry.compatible;
    }
    
    public static boolean reflectCodeChickenCore()
    {
        codechickencore = new ReflectionCache();
        
        Class guiContainerWidgetClass = getClass( "codechicken.core.inventory.GuiContainerWidget" );
        if ( guiContainerWidgetClass != null )
        {
            codechickencore.storeClass( "GuiContainerWidget", guiContainerWidgetClass );
            
            codechickencore.available = true;
            codechickencore.compatible = true;
        }
        
        return codechickencore.compatible;
    }
    
    public static boolean reflectNEI()
    {
        NEI = new ReflectionCache();
        
        Class guiRecipeClass = getClass( "codechicken.nei.recipe.GuiRecipe" );
        if ( guiRecipeClass != null )
        {
            NEI.storeClass( "GuiRecipe", guiRecipeClass );
            NEI.available = true;
            
            Class guiEnchantmentModifierClass = getClass( "codechicken.nei.GuiEnchantmentModifier" );
            if ( guiEnchantmentModifierClass != null )
            {
                NEI.storeClass( "GuiEnchantmentModifier", guiEnchantmentModifierClass );
                
                NEI.compatible = true;
            }
        }
        
        return NEI.compatible;
    }
    
    public static boolean is( Object object, String name )
    {
        return object.getClass().getSimpleName() == name;
    }
    
    public static boolean doesClassExist( String name )
    {
        Class clazz = getClass( name );
        return clazz != null;
    }
    
    public static Class getClass( String name )
    {
        try
        {
            return Class.forName( name );
        }
        catch ( ClassNotFoundException e )
        {
            ;
        }
        
        return null;
    }
    
    public static Field getField( Class clazz, String name )
    {
        try
        {
            Field field = null;
            
            try
            {
                field = clazz.getField( name );
            }
            catch ( Exception e )
            {
                field = null;
            }
            
            if ( field == null )
            {
                field = clazz.getDeclaredField( name );
            }
            
            field.setAccessible( true );
            return field;
        }
        catch ( Exception e )
        {
            if ( name != "ofProfiler" )
            {
                Logger.Log( "Could not retrieve field \"" + name + "\" from class \"" + clazz.getName()
                        + "\"" );
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    public static Field getFinalField( Class clazz, String name )
    {
        try
        {
            Field field = null;
            
            try
            {
                field = clazz.getField( name );
            }
            catch ( Exception e )
            {
                field = null;
            }
            
            if ( field == null )
            {
                field = clazz.getDeclaredField( name );
            }
            
            Field modifiers = Field.class.getDeclaredField( "modifiers" );
            modifiers.setAccessible( true );
            
            modifiers.set( field, field.getModifiers() & ~Modifier.FINAL );
            
            field.setAccessible( true );
            return field;
        }
        catch ( Exception e )
        {
            Logger.Log( "Could not retrieve field \"" + name + "\" from class \"" + clazz.getName()
                    + "\"" );
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static Method getMethod( Class clazz, String name, Class... args )
    {
        try
        {
            Method method = null;
            
            try
            {
                method = clazz.getMethod( name, args );
            }
            catch ( Exception e )
            {
                method = null;
            }
            
            if ( method == null )
            {
                if ( ( args != null ) & ( args.length != 0 ) )
                {
                    method = clazz.getDeclaredMethod( name, args );
                }
                else
                {
                    method = clazz.getDeclaredMethod( name, new Class[0] );
                }
            }
            
            method.setAccessible( true );
            return method;
        }
        catch ( Exception e )
        {
            Logger.Log( "Could not retrieve method \"" + name + "\" from class \"" + clazz.getName()
                    + "\"" );
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String methodToString( Method method )
    {
        return ( Modifier.toString( method.getModifiers() ) + " " + method.getReturnType() ) != null ? method
                .getReturnType().getName() : "void" + " " + method.getName();
    }
    
    public static String getObfuscatedName( String mcpName, String forgeName, String originalName )
    {
        return Main.minecraftForge ? forgeName : Tessellator.instance.getClass().getSimpleName()
                .equals( "Tessellator" ) ? mcpName : originalName;
    }
}
