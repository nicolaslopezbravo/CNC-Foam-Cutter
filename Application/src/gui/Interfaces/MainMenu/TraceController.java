package gui.Interfaces.MainMenu;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.Label;
import tracer.Tracer;

public class TraceController {

    //FXML
    public Label grblX;
    public Label grblY;
    public Label grblZ;
    public Label grblA;
    public SwingNode swingNodeTrace;
    
    public static TraceController controller;
    
    
    public void initialize()
    {
        controller = this;
        Tracer.setup(swingNodeTrace);
        
        // These can be generated by grbl
        //TODO these are just test values
        setGrblX(1.0);
        setGrblY(8.15465422);
        setGrblZ(1.57);
        setGrblA(55.0);
    }
    
    
    public static void addTrace(double x, double y, double z)
    {
        Tracer.addTrace(x, y, z);
    }
    
    
    public static void setGrblX(double x) {
        controller.grblX.setText(String.format("X Position: %.2f", x));
    }
    
    public static void setGrblY(double y) {
        controller.grblY.setText(String.format("Y Position: %.2f", y));
    }
    
    public static void setGrblZ(double z) {
        controller.grblZ.setText(String.format("Z Position: %.2f", z));
    }
    
    public static void setGrblA(double a) {
        controller.grblA.setText(String.format("Acceleration: %.2f", a));
    }
    
    
}
