/*
 * File:    Main.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import grbl.APIgrbl;
import gui.Gui;
import gui.interfaces.main.GcodeController;
import gui.interfaces.main.ModelController;
import gui.interfaces.main.RotationController;
import gui.interfaces.main.TraceController;
import javafx.application.Application;
import javafx.application.Platform;
import renderer.Renderer;
import tracer.Tracer;
import utils.CmdLine;
import utils.ConfigurationManager;
import utils.Constants;
import utils.MachineDetector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The launching class for our Application.
 */
public class Main
{
    
    //Constants
    
    /**
     * A flag indicating whether the application should disable certain checks for development purposes.
     */
    public static final boolean development = false;
    
    /**
     * The machine type to run when working in development mode.
     */
    public static final MachineDetector.Machine developmentMode = MachineDetector.Machine.CNC;
    
    /**
     * A flag indicating whether to use the demo mode of the application for an ideal case.
     */
    public static final boolean demoMode = false;
    
    /**
     * A flag indicating whether or not to produce development logging or not.
     */
    public static final boolean developmentLogging = false;
    
    /**
     * A flag indicating whether or not to bypass the Arduino for the Tracer.
     */
    public static final boolean bypassArduinoForTracer = false;
    
    
    //Static Fields
    
    /**
     * The singleton instance of the Main class.
     */
    public static Main main;
    
    /**
     * The start time of the printing process.
     */
    public static long startTime;
    
    /**
     * The current progress through the printing execution.
     */
    public static double progress = 0;
    
    /**
     * The total progress of the printing execution.
     */
    public static double totalProgressUnits = 0;
    
    
    //Fields
    
    /**
     * The name of the operating system.
     */
    public String operatingSystem;
    
    /**
     * The architecture level of the operating system; either "x64" or "x86".
     */
    public String architecture;
    
    /**
     * The Java runtime version.
     */
    public String javaVersion;
    
    /**
     * The Python runtime version.
     */
    public String pythonVersion;
    
    /**
     * The port connected to the Arduino UNO.
     */
    public String port;
    
    
    //Main Method
    
    /**
     * The Main method.
     *
     * @param args Arguments to the Main method.
     */
    public static void main(String[] args)
    {
        main = new Main();
        if (!main.init()) {
            return;
        }
        Application.launch(Gui.class, args);

    }
    
    
    //Methods
    
    /**
     * Initializes the system properties and ensures the system is valid to run the application.
     *
     * @return Whether the system setup is valid or not.
     */
    protected boolean init()
    {
        operatingSystem = System.getProperty("os.name").toUpperCase();
        
        if (!operatingSystem.contains("WIN")) {
            System.err.println("This application must be run on a Windows PC.");
            System.err.println("Please try again with a Windows PC.");
            return false;
        }
        
        architecture = (System.getenv("ProgramFiles(x86)") != null) ? Constants.ARCHITECTURE_x64 : Constants.ARCHITECTURE_x86;
        
        javaVersion = System.getProperty("java.runtime.version").toUpperCase(); // ie: 1.8.0_121-b13
        
        if (Double.parseDouble(javaVersion.substring(0, 3)) < 1.7) {
            System.err.println("This application must have a Java runtime environment of 1.7 or higher.");
            System.err.println("Please install a newer JRE and try again.");
            return false;
        }
        
//        try {
//            GraphicsConfigTemplate3D gconfigTemplate = new GraphicsConfigTemplate3D();
//            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getBestConfiguration(gconfigTemplate);
//        } catch (Error | Exception ignored) {
//            System.err.println("You must have Java3D installed on your system to use this application.");
//            System.out.println("Attempting to install Java3D...");
//
//            String installJava3D = Constants.JAVA3D_DIRECTORY + Constants.JAVA3D_FILENAME_BASE +
//                    (architecture.equals(Constants.ARCHITECTURE_x64) ? Constants.JAVA3D_FILENAME_SUFFIX_64 : Constants.JAVA3D_FILENAME_SUFFIX_86);
//
//            CmdLine.executeCmd(installJava3D, true);
//
//            try {
//                Class.forName("javax.media.j3d.J3DBuffer");
//            } catch (final ClassNotFoundException ignored2) {
//                System.out.println("Please install Java3D and try again.");
//                return false;
//            }
//        }
        
        String pythonCheck = CmdLine.executeCmd("py -V");
        boolean installPython = false;
        if (pythonCheck.isEmpty() || pythonCheck.matches("'python' is not recognized .*")) {
            installPython = true;
        } else {
            Pattern p = Pattern.compile("Python\\s(?<version>.+)\\r\\n");
            Matcher m = p.matcher(pythonCheck);
            if (m.matches()) {
                pythonVersion = m.group("version");
                if (!pythonVersion.startsWith("3")) {
                    installPython = true;
                }
            } else {
                installPython = true;
            }
        }
        if (installPython) {
            System.err.println("You must have Python 3 installed on your system to use this application.");
            System.out.println("Attempting to install Python 3...");
            
            String pythonInstallCmd = Constants.PYTHON_DIRECTORY + Constants.PYTHON_FILENAME;
            CmdLine.executeCmd(pythonInstallCmd, true);
            
            pythonCheck = CmdLine.executeCmd("py -V");
            if (pythonCheck.isEmpty() || pythonCheck.matches("'python' is not recognized .*")) {
                System.out.println("Please install Python 3 and try again.");
                return false;
            } else {
                Pattern p = Pattern.compile("Python\\s(?<version>.+)\\r\\n");
                Matcher m = p.matcher(pythonCheck);
                if (m.matches()) {
                    pythonVersion = m.group("version");
                    if (!pythonVersion.startsWith("3")) {
                        System.out.println("Please install Python 3 and try again.");
                        return false;
                    }
                } else {
                    System.out.println("Please install Python 3 and try again.");
                    return false;
                }
            }
        }
        CmdLine.executeCmd("pip install pyserial");
        CmdLine.executeCmd("pip install serial");
    
        ConfigurationManager.loadSettings();
        
        System.out.println("OS:           " + operatingSystem);
        System.out.println("Architecture: " + architecture);
        System.out.println("Java:         " + javaVersion);
        System.out.println("Python:       " + pythonVersion);
        System.out.println();
        
        return true;
    }
    
    
    //Static Methods
    
    /**
     * Resets the Application.
     */
    public static void resetApplication()
    {
        Renderer.reset();
        Tracer.reset();
        
        if (ModelController.controller != null) {
            ModelController.controller.reset();
        }
        if (TraceController.controller != null) {
            TraceController.controller.reset();
        }
        if (GcodeController.controller != null) {
            GcodeController.controller.reset();
        }
        if (RotationController.controller != null) {
            RotationController.controller.reset();
        }
        
        if (APIgrbl.grbl != null) {
            APIgrbl.grbl.reset();
        }
        startTime = 0;
    }
    
    /**
     * Kills the Application.
     */
    public static void killApplication()
    {
        Platform.exit();
        System.exit(0);
    }
    
}
