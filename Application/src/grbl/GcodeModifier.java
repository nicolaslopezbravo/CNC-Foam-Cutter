/*
 * File:    GcodeModifier.java
 * Package: grbl
 * Author:  Nicolas Lopez
 */

package grbl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Modifies gcode files to our Application's specifications.
 */
public class GcodeModifier
{
    
    //Constants
    
    /**
     * The list of acceptable gcode commands.
     */
    private static final List<String> ACCEPTABLE_GCODE = Arrays.asList(
            "G0", "G1", "G2", "G3", "G4", "G10L2", "G10L20", "G17", "G18",
            "G19", "G20", "G21", "G28", "G28.1", "G30", "G30.1", "G38.2",
            "G38.3", "G38.4", "G38.5", "G53", "G80", "G90", "G91", "G91.1",
            "G92", "G92.1", "G93", "G94", "G40", "G43.1", "G49", "G54", "G55",
            "G56", "G57", "G58", "G59", "G61", "M0", "M1", "M2", "M30*", "M7*",
            "M8", "M9", "M3", "M4", "M5", "F", "I", "J", "K", "L", "N", "P", "R",
            "S", "T", "X", "Y", "Z", "G00", "G01", "Z01"
    );
    
    /**
     * The list of unacceptable gcode parameters.
     */
    private static final List<Character> UNACCEPTABLE_PARAMETERS = Arrays.asList('E', 'A');
    
    
    //Fields
    
    /**
     * The list of gcode commands.
     */
    private List<String> commands = new ArrayList<>();
    
    /**
     * The gcode file to modify.
     */
    private String file;
    
    
    //Constructors
    
    /**
     * The constructor for a GcodeModifier.
     *
     * @param file The gcode file to modify.
     */
    public GcodeModifier(String file)
    {
        this.file = file;
    }
    
    
    //Methods
    
    /**
     * Modifies the specified gcode file.
     *
     * @return Whether the gcode file was successfully modified or not.
     */
    public boolean modify()
    {
        if (readGcodeFile()) {
            removeComments();
            removeWhitespace();
            removeBadCommands();
            convertBadParameters();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Reads the commands from the specified gcode file.
     *
     * @return Whether the gcode file was successfully read or not.
     */
    private boolean readGcodeFile()
    {
        //open the gcode file
        File gcode = new File(file);
        if (!gcode.exists()) {
            System.err.println("Cannot modify gcode file: " + gcode.getAbsolutePath() + ". File does not exist!");
            return false;
        }
    
        //read the gcode file
        try {
            commands = Files.readAllLines(Paths.get(gcode.getAbsolutePath()));
        } catch (IOException e) {
            System.err.println("Cannot modify gcode file: " + gcode.getAbsolutePath() + ". File cannot be read!");
            return false;
        }
        
        removeWhitespace();
        
        return true;
    }
    
    /**
     * Removes comments from the specified gcode file.
     */
    private void removeComments()
    {
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            if (command.contains(";")) {
                StringBuilder sb = new StringBuilder();
                char[] cbuf = command.toCharArray();
    
                for (char aCbuf : cbuf) {
                    if (aCbuf == ';') {
                        break;
                    }
                    sb.append(aCbuf);
                }
                
                String str = sb.toString();
                commands.set(i, str);
            }
        }
    }
    
    /**
     * Removes preceding and trailing whitespace form the specified gcode file.
     */
    private void removeWhitespace()
    {
        //remove unnecessary spaces in commands
        for (int i = 0; i < commands.size(); i++) {
            commands.set(i, commands.get(i).trim());
        }
    }
    
    /**
     * Removes bad commands from the specified gcode file.
     */
    private void removeBadCommands()
    {
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            
            // check the acceptable set
            if (command.isEmpty() || command.indexOf(' ') == -1 ||
                    !ACCEPTABLE_GCODE.contains(command.substring(0, command.indexOf(' ')))) {
                commands.remove(i); //continue or remove
                i--;
            }
        }
    }
    
    /**
     * Converts bad parameters in the specified gcode file.
     */
    private void convertBadParameters()
    {
        for (int index = 0; index < commands.size(); index++) {
            String command = commands.get(index);
    
            List<String> tokens = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(command);
            while (st.hasMoreTokens()) {
                tokens.add(st.nextToken());
            }
    
            if (tokens.size() > 0) {
                boolean hitF = false;
                int mockF = -1;
                for (int j = 0; j < tokens.size(); j++) {
                    String token = tokens.get(j);
                    if (UNACCEPTABLE_PARAMETERS.contains(token.charAt(0))) {
                        if (hitF) {
                            tokens.remove(j--);
                        } else {
                            tokens.set(j, 'F' + token.substring(1));
                            mockF = j;
                        }
                    } else if (token.charAt(0) == 'F') {
                        hitF = true;
                        if (mockF != -1) {
                            tokens.remove(mockF);
                            mockF = -1;
                            j--;
                        }
                    }
                }
        
                StringBuilder sb = new StringBuilder();
                for (String token : tokens) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(token);
                }
                commands.set(index, sb.toString());
        
            } else {
                commands.remove(index--);
            }
        }
    }
    
    /**
     * Prints the list of commands.
     */
    public void printCommands()
    {
        for (String s : commands) {
            System.out.println(s);
        }
    }
    
    
    //Getters
    
    /**
     * Returns the list of gcode commands.
     *
     * @return The list of gcode commands.
     */
    public List<String> getCommands()
    {
        return commands;
    }
    
}