package appearance;

import entities.Rectangle;
import utils.RectangleService;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.Instant;

import static utils.Constants.*;

public class SettingsPanel extends JPanel {
    private JFrame canvas;
    private JLabel threadNrLabel;
    private JTextField threadNr;
    private JLabel rectangleNrLabel;
    private JTextField rectangleNr;
    private JButton generateBtn;
    private JLabel timeElapsedLabel;
    private JLabel timeElapsedValue;
    private JLabel timeElapsedValueUnits;

    private long timeElapsed;

    private JComponent rectanglesComponent;

    public SettingsPanel(JFrame canvas){
        this.canvas = canvas;
        threadNrLabel = new JLabel(THREAD_NR_TEXT);
        threadNr = new JTextField(3);

        rectangleNrLabel = new JLabel(RECTANGLE_NR_TEXT);
        rectangleNr = new JTextField(3);

        generateBtn = new JButton(GENERATE_BTN_TEXT);
        generateBtn.addActionListener(new SettingsListener(this));

        timeElapsedLabel = new JLabel(RUNNING_TIME_TEXT);
        timeElapsedValueUnits = new JLabel(RUNNING_TIME_VALUE_UNITS_TEXT);
        timeElapsedValue = new JLabel();
        setTimeElapsed(timeElapsed);

        this.add(BorderLayout.NORTH,threadNrLabel);
        this.add(BorderLayout.NORTH,threadNr);
        this.add(BorderLayout.NORTH,rectangleNrLabel);
        this.add(BorderLayout.NORTH,rectangleNr);
        this.add(BorderLayout.SOUTH, generateBtn);
        this.add(BorderLayout.SOUTH, timeElapsedLabel);
        this.add(BorderLayout.SOUTH, timeElapsedValue);
        this.add(BorderLayout.SOUTH, timeElapsedValueUnits);
    }

    public JTextField getThreadNr() {
        return threadNr;
    }

    public JTextField getRectangleNr() {
        return rectangleNr;
    }

    private class SettingsListener implements ActionListener {
        private SettingsPanel settingsPanel;

        public SettingsListener(SettingsPanel settingsPanel){
            this.settingsPanel = settingsPanel;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(threadNr.getText() != null && rectangleNr.getText() != null){

                Instant start = Instant.now();

                java.util.List<Rectangle> sortedRectangles = RectangleService.runRectanglePlacement(Integer.parseInt(threadNr.getText()), Integer.parseInt(rectangleNr.getText()));

                if(rectanglesComponent != null){
                    canvas.remove(rectanglesComponent);
                }

                rectanglesComponent = new JComponent() {
                    @Override
                    public void paint(Graphics g) {
                        for(Rectangle rectangle:sortedRectangles){
                            int midX = rectangle.getX() + rectangle.getWidth()/2 - 5;
                            int midY = rectangle.getY() + rectangle.getHeight()/2 + 5;
                            g.setColor(Color.getHSBColor((float)Math.random(),(float)Math.random(),(float)Math.random()));
                            g.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

                            g.setColor(Color.BLACK);
                            g.drawString(rectangle.getId() + "", midX, midY);
                        }
                    }
                };

                canvas.getContentPane().add(rectanglesComponent);

                Instant finish = Instant.now();
                timeElapsed = Duration.between(start, finish).toMillis();
                setTimeElapsed(timeElapsed);


                settingsPanel.revalidate();
                settingsPanel.repaint();
                canvas.revalidate();
                canvas.repaint();
            }
        }
    }

    private void setTimeElapsed(long timeElapsed){
        timeElapsedValue.setText("");
        timeElapsedValue.setText(Long.toString(timeElapsed));
    }


}
