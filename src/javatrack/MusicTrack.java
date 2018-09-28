package javatrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *Class MusicTrack will create a MusicTrack Object that stores
 * a key int "bind" and the String file path "track" the track is associated
 * with as the head pointer. 
 * 
 * There is a third field "next" that stores another MusicTrack Object,
 * which should not be null until the data structure has reached the end
 * of a list of key Integers (represented by the "bind" field in this class).
 * 
 * An Object, whether it be the head pointer or some pointer within the head,
 * with the field "track" set to null means that there is no file path
 * associated with the key.
 * 
 * @author davidstevenrose 
 */
public class MusicTrack implements Serializable {
    
    private int bind;
    private String track;
    private MusicTrack next;
    
    /**
     * 
     * @param b the ID of the bind
     * @param fp the String file path of the track
     */
    public MusicTrack(int b, Object fp){
        this.bind = b;
        if(fp!=null){
            this.track = fp.toString();
        }else{
            this.track = null;
        }
        this.next = null;
    }

    /**
     * @return the bind ID
     */
    public int getBind() {
        return bind;
    }
    /**
     * @param bind the bind to set
     */
    public void setBind(int bind) {
        this.bind = bind;
    }
    /**
     * @return the string of the file path of the track
     */
    public String getTrack() {
        return track;
    }
    /**
     * @param track the file path of the track to set
     */
    public void setTrack(String track) {
        this.track = track;
    }    
    /**
     * 
     * @return the next MusicTrack object.
     * This returns null if there is no track associated with its bind.
     */
    public MusicTrack getNext(){
        return this.next;
    }
    /**
     * 
     * @param mt the next MusicTrack type in this linear data structure
     */
    public void setNext(MusicTrack mt){
        this.next = mt;
    }
    /**
     * Creates a binary file for the current passed head pointer.
     * @param head the head pointer to be written
     * @param name the name of the save file
     */
    public static void createFile(MusicTrack head, String name) {
        String fileName = "C:\\Users\\Public\\"+name+".bin";
        File f = new File(fileName);        
        try {
            if(!f.createNewFile()){
                JOptionPane.showMessageDialog(null, "That file already exists!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName));
            os.writeObject(head);
            os.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MusicTrack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MusicTrack.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * Opens the binary file that stores the MusicTrack head pointer.
     * @param fp the file path to be opened
     * @return the head pointer is the file contains a MusicTrack Object.
     * If the file does not contain a MusicTrack object as its head pointer,
     * method returns a new MusicTrack Object with the bind set to -1.
     * If some other exception is thrown, this method returns null.
     */
    public static MusicTrack openFile(String fp){
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(fp));
            MusicTrack head = (MusicTrack) is.readObject();
            return head;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MusicTrack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MusicTrack.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MusicTrack.class.getName()).log(Level.SEVERE, null, ex);
            return new MusicTrack(-1,null);
        }
        return null;
    }
    
}
