package uClip;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;

import java.awt.image.RenderedImage;
import java.awt.*;
import java.io.*;

import com.google.firebase.*;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

  
public class clipboardChecker extends Thread implements ClipboardOwner {
  Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
  private FirebaseDatabase db;
  private DatabaseReference ref;
  
  
  public void run() {
	    db = FirebaseDatabase.getInstance();
	    ref = db.getReference("copy"); 
	    ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
	          	String str = dataSnapshot.getValue(String.class);
	          	
	          	URLConnection connection = null;
				
	          	try {
					connection = new URL(str).openConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	        	connection.setRequestProperty("User-Agent", "Mozilla/5.0");
	        	String contentType = connection.getHeaderField("Content-Type"); 
	          	
	        	if (contentType.startsWith("image")){
	        		Image image = null;
					try {
						image = ImageIO.read(connection.getInputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
	            	UrlImagetoPaste.write(image);
	                javax.swing.ImageIcon icon = new javax.swing.ImageIcon( UrlImagetoPaste.read() );
	                javax.swing.JLabel label = new javax.swing.JLabel( icon );
	        	}
			    
	        	else {
			        	Toolkit toolkit = Toolkit.getDefaultToolkit();
			      		Clipboard clipboard = toolkit.getSystemClipboard();
			      		StringSelection strSel = new StringSelection(str);
			      		clipboard.setContents(strSel, null);
			        }
            }
	    

			@Override
			public void onCancelled(DatabaseError arg0) {
				// TODO Auto-generated method stub
				
			}
        });
	
    Transferable trans = sysClip.getContents(this);
    regainOwnership(trans);
    System.out.println("Listening to board...");
	  
    while(true) {}
  }
 
  
  public void lostOwnership(Clipboard c, Transferable t) {
	  try {
	    this.sleep(10000);
	  } catch(Exception e) {
	    System.out.println("Exception: " + e);
	  }
	  Transferable contents = sysClip.getContents(this);
	  try {
		processContents(contents);
	} catch (UnsupportedFlavorException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  regainOwnership(contents);
	}
  

  
  void processContents(Transferable t) throws UnsupportedFlavorException, IOException {

	String data = (String) t.getTransferData(DataFlavor.stringFlavor);
    System.out.println("Processing: " + data);
    db = FirebaseDatabase.getInstance();
    ref = db.getReference("copy"); 
    ref.setValue(data);
    
  }
  
  void regainOwnership(Transferable t) {
    sysClip.setContents(t, this);
  }
  
  
  public static void main(String[] args) {
      try {
          FirebaseOptions options = new FirebaseOptions.Builder()
                  .setServiceAccount(new FileInputStream("uClip-ee4adee8d658.json"))
                  .setDatabaseUrl("https://uclip-e2537.firebaseio.com/")
                  .build();

          FirebaseApp.initializeApp(options);
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      }
    clipboardChecker u = new clipboardChecker();
    u.start();

  }
}