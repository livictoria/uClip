package uClip;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
  
public class clipboardChecker extends Thread implements ClipboardOwner {
  Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
  private FirebaseDatabase db;
  private DatabaseReference ref;
  
  public void run() {
    Transferable trans = sysClip.getContents(this);
    regainOwnership(trans);
    System.out.println("Listening to board...");
    while(true) {}
  }
  
  public void lostOwnership(Clipboard c, Transferable t) {
	  try {
	    this.sleep(2000);
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