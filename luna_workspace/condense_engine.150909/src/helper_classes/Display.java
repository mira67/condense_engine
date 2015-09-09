package helper_classes;

/*
 * Display
 * 
 * Display a buffered image on the screen.
 * 
 * @author Glenn Grant, NSIDC
 * 2014.09.19
 */

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;


public class Display
{
    Display(final BufferedImage image, final String name) throws Exception
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame editorFrame = new JFrame(name);
                editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
                ImageIcon imageIcon = new ImageIcon(image);
                JLabel jLabel = new JLabel();
                jLabel.setIcon(imageIcon);
                editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

                editorFrame.pack();
                editorFrame.setLocationRelativeTo(null);
                editorFrame.setVisible(true);
            }
        });
    }
}
