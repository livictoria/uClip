package uClip;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.Log;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class Uclip extends JPanel /*implements ClipboardOwner*/ {

    private FirebaseDatabase db;
    private DatabaseReference ref;

    private static final String GOOD_GTLD_CHAR =
            "a-zA-Z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    public static final String GOOD_IRI_CHAR =
            "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    private static final String IRI
            = "[" + GOOD_IRI_CHAR + "]([" + GOOD_IRI_CHAR + "\\-]{0,61}[" + GOOD_IRI_CHAR + "]){0,1}";

    public static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    private static final String GTLD = "[" + GOOD_GTLD_CHAR + "]{2,63}";
    private static final String HOST_NAME = "(" + IRI + "\\.)+" + GTLD;

    public static final Pattern DOMAIN_NAME
            = Pattern.compile("(" + HOST_NAME + "|" + IP_ADDRESS + ")");

    public static final Pattern WEB_URL = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "(?:" + DOMAIN_NAME + ")"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)");

    private Clipboard sysClip;

    private CbChecker checkerThread;

    public Uclip() {
        super(new BorderLayout());

        setup();

        checkerThread = null;

        final JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("start pressed");
                if (checkerThread == null) {
                    checkerThread = new CbChecker();
                    checkerThread.execute();
                }
                startButton.setEnabled(false);
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("end pressed");
                checkerThread.cancel(true);
                checkerThread = null;
                startButton.setEnabled(true);
            }
        });

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

        sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getValue(String.class);

                if (WEB_URL.matcher(str).matches()) {
                    try {
                        URLConnection connection = new URL(str).openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                        String contentType = connection.getHeaderField("Content-Type");
                        System.out.println("content type: " + contentType);

                        if (contentType != null && contentType.startsWith("image")) {
                            UrlImagetoPaste.writeToClipboard(ImageIO.read(connection.getInputStream()));
                        } else {
                            Transferable strSel = new StringSelection(str);
                            sysClip.setContents(strSel, null);
                            System.out.println("copied to clipboard: " + strSel);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Transferable strSel = new StringSelection(str);
                    sysClip.setContents(strSel, null);
                    System.out.println("copied to clipboard: " + strSel);
                }
            }

            @Override
            public void onCancelled(DatabaseError arg0) {
                // TODO Auto-generated method stub
                System.out.println("onCancelled Firebase error: " + arg0);

            }
        });
    }

    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("uClip");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JComponent newContentPane = new Uclip();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /*
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            System.out.println("lostOwnership");

            String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
            System.out.println("Processing: " + data);
            db = FirebaseDatabase.getInstance();
            ref = db.getReference("copy");
            ref.setValue(data);
            clipboard.setContents(clipboard.getContents(this), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */


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

    private class CbChecker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            System.out.println("doInBackground");
            while (!isCancelled()) {
                Transferable content = sysClip.getContents(null);
                if (content != null) {
                    try {
                        if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            String data = (String) content.getTransferData(DataFlavor.stringFlavor);
                            db = FirebaseDatabase.getInstance();
                            ref = db.getReference("copy");
                            ref.setValue(data);
                        }
                    } catch (Exception e) {
                        System.out.println("wrong data flavor type");
                    }
                }
                //too short and there is a race condition?
                Thread.sleep(2500);
                System.out.println("post sleep");
            }
            return null;
        }
    }
}
