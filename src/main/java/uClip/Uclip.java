package uClip;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class Uclip extends JPanel implements DatabaseReference.CompletionListener {

	    private FirebaseDatabase db;
	    private DatabaseReference ref;
	  
	    private ValueEventListener keyListener, mouseListener;

	    public Uclip() {
	        super(new BorderLayout());

	        setup();

	        JButton startButton = new JButton("Start");
	      

	        JButton stopButton = new JButton("not");
	      
	        JPanel testPanel = new JPanel();
	        testPanel.add(startButton);
	        testPanel.add(stopButton);

	        add(testPanel, BorderLayout.CENTER);
	        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	    }

	    private void setup() {
	        db = FirebaseDatabase.getInstance();
	        ref = db.getReference("copy"); 
	        //do other code here
	    }
/*
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        switch (e.getActionCommand()) {
	            case "Start":
	                clearQueue(ref);
	                break;
	            case "Stop":
	                ref.child("swipe").removeEventListener(mouseListener);
	                ref.removeEventListener(keyListener);
	                break;
	            default:
	                break;
	        }
	    }

	    private void clearQueue(DatabaseReference dr) {
	        System.out.println("clearQueue");
	        dr.setValue(null, this);
	    }
*/
	    @Override
	    public void onComplete(DatabaseError error, DatabaseReference ref) {
//	        ref.child("swipe").setValue(new Vector2D(0, 0));
	        //ref.child("swipe").addValueEventListener(mouseListener);
	        //ref.addValueEventListener(keyListener);
//	        ref.child("swipe").addValueEventListener(mouseListener);
//	        ref.child("button").addValueEventListener(keyListener);
	    }

	    private static void createAndShowGUI() {
	        try {
	            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        JFrame frame = new JFrame("Sit Back");
	        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	        JComponent newContentPane = new Uclip();
	        newContentPane.setOpaque(true);
	        frame.setContentPane(newContentPane);

	        frame.pack();
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
	    }

	    public static void main(String args[]) {
	        System.out.println("Start");
	        try {
	            FirebaseOptions options = new FirebaseOptions.Builder()
	                    .setServiceAccount(new FileInputStream("uClip-ee4adee8d658.json"))
	                    .setDatabaseUrl("https://uclip-e2537.firebaseio.com/")
	                    .build();

	            FirebaseApp.initializeApp(options);
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                createAndShowGUI();
	            }
	        });
	    }
}
