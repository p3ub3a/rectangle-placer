package appearance;

import javax.swing.*;
import java.awt.*;

import static utils.Constants.FRAME_HEIGHT;
import static utils.Constants.FRAME_WIDTH;

public class RectangleCanvas extends JFrame {
    public RectangleCanvas(){
        setTitle("Rectangles");
        setBounds(100,100, FRAME_WIDTH, FRAME_HEIGHT);
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(BorderLayout.NORTH, new SettingsPanel(this));
        setVisible(true);
    }
}
