//import JavaPanes.JOptionSelectPane;
//import javazoom.jl.player.Player;
import javatrack.MusicTrack;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javatrack.PausablePlayer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author davidstevenrose
 */
class LoopMediaPlayer implements Runnable {

    private PausablePlayer pp;
    private String fp;

    public LoopMediaPlayer(String file) {
        this.fp = file;
    }    
    
    public void pausePausable(){
            this.pp.pause();
    }
    
    public void resumePausable(){
        this.pp.resume();
    }
    
    public void stopPausable(){
        this.pp.stop();
    }

    @Override
    public void run() {
        try {
            this.pp = new PausablePlayer(new FileInputStream(this.fp), PausablePlayer.NOTSTARTED_LOOPING);
            pp.play();            
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getMessage());
        }
    }
}

class MediaPlayer implements Runnable {
 
    private String fp;
    private PausablePlayer pp;

    public MediaPlayer(String file) {
        this.fp = file;
    }   

    //public boolean isPlaying() {
    //    return this.isPlaying;
    //}
    
    public void pausePausible(){
            this.pp.pause();
    }
    
    public void resumePausible(){
        this.pp.resume();
    }
    
    public void stopPausible(){
        this.pp.stop();
    }
    

    @Override
    public void run() {
        try {                            
            FileInputStream fileInputStream = new FileInputStream(this.fp);
            this.pp = new PausablePlayer(fileInputStream);
            this.pp.play();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getMessage());
        }
    }    
}

public class MusicGUI extends javax.swing.JFrame {

    private static Thread player = new Thread();
    private static Thread loopThread;
    private static FloatControl vc;
    private final HashMap<String, String> Tracks = new HashMap<>(20);//the value String is the file path for the mp3 file
    private final HashMap<Integer, Component> ButtonGroup = new HashMap<>(20);//components are JButtons
    private MediaPlayer mp;
    private LoopMediaPlayer lmp;
    final static Info SOURCE = Port.Info.SPEAKER;
    static Boolean looping = false;    

    public MusicGUI() {
        initComponents();
        fillTrackMap();
        fillButtonMap();
        
        Port outline;
        try {
            outline = (Port) AudioSystem.getLine(SOURCE);            
            outline.open();
            vc = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
            
        } catch (LineUnavailableException ex) {
            Logger.getLogger(MusicGUI.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    public void fillTrackMap() {
        for (int i = 1; i <= 20; i++) {
            Tracks.put("Track" + i, null);
        }
    }

    public void fillButtonMap() {
        Component[] cs = ButtonPanel.getComponents();
        for (Component v : cs) {
            if (!ButtonGroup.containsKey(Integer.parseInt(v.getName().substring(v.getName().length() - 2, v.getName().length())))) {
                ButtonGroup.put(Integer.parseInt(v.getName().substring(v.getName().length() - 2, v.getName().length())), v);
            }
        }
        for (Integer in : ButtonGroup.keySet()) {
            JButton jb = (JButton) ButtonGroup.get(in);
            jb.setText(in.toString());
        }
    }

    public void playMediaFromBind(int id) {
        try {            
            vc.setValue((float) VolumeSld.getValue());
            String relativeFilePath = Tracks.get("Track" + id);
            if (Tracks.get("Track" + id) != null) {
                if (!player.isAlive()) {//player is the new thread
                    mp = new MediaPlayer(relativeFilePath);
                    player = new Thread(mp);
                    player.start();
                } else{//previous clause was to prevent two files from playing at once                    
                    mp = new MediaPlayer(relativeFilePath);
                    player.run();
                }
            } else {
                JOptionPane.showMessageDialog(null, "There is no track binded to this button", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HeadlessException ex) {
            Logger.getLogger(MusicGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void setVolume(float f) {
        vc.setValue(f * 0.01f);        
    }

    public void startLoop(String tr) {
       if(tr!=null){
           lmp = new LoopMediaPlayer(Tracks.get(tr));
           loopThread = new Thread(lmp);
           loopThread.start();           
           looping = true;
       }
    }
    
    public void stopLoop(){
        if (looping){
            looping = false;
        }
    }
    
    public static void createSaveFile(HashMap tr){
        int index = 1;
        MusicTrack head = new MusicTrack(index, tr.get("Track"+(index)));
        MusicTrack n = head;
        while(index++<=tr.size()){
            n.setNext(new MusicTrack(index, tr.get("Track"+(index))));            
            n = n.getNext();
        }
        String name = JOptionPane.showInputDialog(null, "What is the name of your save file?", "Save", JOptionPane.QUESTION_MESSAGE);
        if(name!=null){
            MusicTrack.createFile(head,name);
        }       
    }
    
    public static void openSaveFile(HashMap tr, HashMap bg){
        MusicTrack n;
        MusicTrack head;
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter f = new FileNameExtensionFilter("BIN FILES", "bin");
        fc.setFileFilter(f);
        fc.showOpenDialog(null);
        File fp = fc.getSelectedFile();
        if(fp!=null){
            head = MusicTrack.openFile(fp.toString());
            if(head.getBind()==-1){
                JOptionPane.showMessageDialog(null, "The file you selected could not be read by this application because" +
                        "it was not created by this application.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }else{
                n = head;
            }
        }else return;        
        int i = 1;
        while (i <= 20) {
            String tfp = n.getTrack();
            tr.replace("Track" + i, tfp);
            JButton jb = (JButton) bg.get(i++);
            if(tfp!=null){
                jb.setFont(Font.decode(jb.getFont().toString() + "-bold-16"));
            }else{
                jb.setFont(Font.decode(jb.getFont().toString() + "-plain-11"));
            }
            n = n.getNext();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AddFrame = new javax.swing.JFrame();
        addCb = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        addChooseBtn = new javax.swing.JButton();
        addTxt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        Add_BindDtn = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        aboutFrame = new javax.swing.JFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        VolumeSld = new javax.swing.JSlider();
        jPanel2 = new javax.swing.JPanel();
        MuteTgl = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        PlayBtn = new javax.swing.JButton();
        PauseBtn = new javax.swing.JButton();
        StopBtn = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        LoopBtn = new javax.swing.JButton();
        AddBtn = new javax.swing.JButton();
        RemoveBtn = new javax.swing.JButton();
        BreakBtn = new javax.swing.JButton();
        ButtonPanel = new javax.swing.JPanel();
        Track01 = new javax.swing.JButton();
        Track02 = new javax.swing.JButton();
        Track03 = new javax.swing.JButton();
        Track04 = new javax.swing.JButton();
        Track05 = new javax.swing.JButton();
        Track06 = new javax.swing.JButton();
        Track07 = new javax.swing.JButton();
        Track08 = new javax.swing.JButton();
        Track09 = new javax.swing.JButton();
        Track10 = new javax.swing.JButton();
        Track11 = new javax.swing.JButton();
        Track12 = new javax.swing.JButton();
        Track13 = new javax.swing.JButton();
        Track14 = new javax.swing.JButton();
        Track15 = new javax.swing.JButton();
        Track16 = new javax.swing.JButton();
        Track17 = new javax.swing.JButton();
        Track18 = new javax.swing.JButton();
        Track19 = new javax.swing.JButton();
        Track20 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuItemSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuItemOpen = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        clearBoard = new javax.swing.JMenuItem();
        aboutItem = new javax.swing.JMenuItem();

        AddFrame.setTitle("Add A Track");
        AddFrame.setPreferredSize(new java.awt.Dimension(400, 250));
        AddFrame.setResizable(false);
        AddFrame.setSize(new java.awt.Dimension(400, 250));

        addCb.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }));

        jLabel1.setText("Which song do you want to add?");
        jLabel1.setMaximumSize(new java.awt.Dimension(171, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(171, 14));

        addChooseBtn.setText("Choose");
        addChooseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChooseBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("Which button do you want to bind your song to?");
        jLabel2.setPreferredSize(new java.awt.Dimension(244, 14));

        Add_BindDtn.setText("Bind");
        Add_BindDtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Add_BindDtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout AddFrameLayout = new javax.swing.GroupLayout(AddFrame.getContentPane());
        AddFrame.getContentPane().setLayout(AddFrameLayout);
        AddFrameLayout.setHorizontalGroup(
            AddFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddFrameLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(AddFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AddFrameLayout.createSequentialGroup()
                        .addComponent(addChooseBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addTxt))
                    .addGroup(AddFrameLayout.createSequentialGroup()
                        .addGroup(AddFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Add_BindDtn)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addCb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 22, Short.MAX_VALUE)))
                .addContainerGap())
        );
        AddFrameLayout.setVerticalGroup(
            AddFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AddFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addChooseBtn)
                    .addComponent(addTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addCb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(Add_BindDtn)
                .addContainerGap())
        );

        aboutFrame.setTitle("About");
        aboutFrame.setSize(new java.awt.Dimension(460, 300));

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(255, 255, 204));
        jTextPane1.setText("<html>\n<h1>About This GUI</h1>\n<p>This is a project I have begun working on in 2018. This is a project I have developed on my own free time outside of class. Currently, there are twenty bindable buttons to bind and play mp3 files. Right now, the 'loop' button is not working functionally, and will take up alot of memory, so I am working  on debugging this.</p>\n<p>\nHere is a list of information to navigate this GUI:\n</p>\n<ul>\n<li>Play - plays the selected media if the media is paused</li>\n<li>Pause - pauses the selected media if there is one playing</li>\n<li>Stop - stops the media playing and removes it from the media player</li>\n<li> Edit - Clear Board - removes all bindings on the board (Remember to save!)</li>\n</ul>\n</html>");
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout aboutFrameLayout = new javax.swing.GroupLayout(aboutFrame.getContentPane());
        aboutFrame.getContentPane().setLayout(aboutFrameLayout);
        aboutFrameLayout.setHorizontalGroup(
            aboutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addContainerGap())
        );
        aboutFrameLayout.setVerticalGroup(
            aboutFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MusicGUI");
        setBackground(new java.awt.Color(255, 255, 255));

        jSeparator2.setForeground(new java.awt.Color(51, 51, 51));
        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator3.setForeground(new java.awt.Color(51, 51, 51));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Volume"));

        VolumeSld.setMajorTickSpacing(10);
        VolumeSld.setMinorTickSpacing(1);
        VolumeSld.setPaintLabels(true);
        VolumeSld.setPaintTicks(true);
        VolumeSld.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                VolumeSldStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VolumeSld, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(VolumeSld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Mute"));

        MuteTgl.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                MuteTglStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(MuteTgl)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(MuteTgl)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Track"));

        PlayBtn.setText("Play");
        PlayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlayBtnActionPerformed(evt);
            }
        });

        PauseBtn.setText("Pause");
        PauseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PauseBtnActionPerformed(evt);
            }
        });

        StopBtn.setText("Stop");
        StopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StopBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PlayBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PauseBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StopBtn)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PlayBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PauseBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(StopBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "PlayList"));

        LoopBtn.setText("Loop");
        LoopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoopBtnActionPerformed(evt);
            }
        });

        AddBtn.setText("Add");
        AddBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddBtnActionPerformed(evt);
            }
        });

        RemoveBtn.setText("Remove");
        RemoveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveBtnActionPerformed(evt);
            }
        });

        BreakBtn.setText("Break");
        BreakBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BreakBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LoopBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AddBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BreakBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RemoveBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(AddBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(RemoveBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LoopBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(BreakBtn)
                .addContainerGap(98, Short.MAX_VALUE))
        );

        Track01.setText("One");
        Track01.setName("Track01"); // NOI18N
        Track01.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track01ActionPerformed(evt);
            }
        });

        Track02.setText("Two");
        Track02.setName("Track02"); // NOI18N
        Track02.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track02ActionPerformed(evt);
            }
        });

        Track03.setText("Three");
        Track03.setName("Track03"); // NOI18N
        Track03.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track03ActionPerformed(evt);
            }
        });

        Track04.setText("Four");
        Track04.setName("Track04"); // NOI18N
        Track04.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track04ActionPerformed(evt);
            }
        });

        Track05.setText("Five");
        Track05.setName("Track05"); // NOI18N
        Track05.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track05ActionPerformed(evt);
            }
        });

        Track06.setText("Six");
        Track06.setName("Track06"); // NOI18N
        Track06.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track06ActionPerformed(evt);
            }
        });

        Track07.setText("Seven");
        Track07.setName("Track07"); // NOI18N
        Track07.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track07ActionPerformed(evt);
            }
        });

        Track08.setText("Eight");
        Track08.setName("Track08"); // NOI18N
        Track08.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track08ActionPerformed(evt);
            }
        });

        Track09.setText("Nine");
        Track09.setName("Track09"); // NOI18N
        Track09.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track09ActionPerformed(evt);
            }
        });

        Track10.setText("Ten");
        Track10.setName("Track10"); // NOI18N
        Track10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track10ActionPerformed(evt);
            }
        });

        Track11.setText("Eleven");
        Track11.setName("Track11"); // NOI18N
        Track11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track11ActionPerformed(evt);
            }
        });

        Track12.setText("Twelve");
        Track12.setName("Track12"); // NOI18N
        Track12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track12ActionPerformed(evt);
            }
        });

        Track13.setText("Thirteen");
        Track13.setName("Track13"); // NOI18N
        Track13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track13ActionPerformed(evt);
            }
        });

        Track14.setText("Fourteen");
        Track14.setName("Track14"); // NOI18N
        Track14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track14ActionPerformed(evt);
            }
        });

        Track15.setText("Fifteen");
        Track15.setName("Track15"); // NOI18N
        Track15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track15ActionPerformed(evt);
            }
        });

        Track16.setText("Sixteen");
        Track16.setName("Track16"); // NOI18N
        Track16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track16ActionPerformed(evt);
            }
        });

        Track17.setText("Seventeen");
        Track17.setName("Track17"); // NOI18N
        Track17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track17ActionPerformed(evt);
            }
        });

        Track18.setText("Eighteen");
        Track18.setName("Track18"); // NOI18N
        Track18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track18ActionPerformed(evt);
            }
        });

        Track19.setText("Nineteen");
        Track19.setName("Track19"); // NOI18N
        Track19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track19ActionPerformed(evt);
            }
        });

        Track20.setText("Twenty");
        Track20.setName("Track20"); // NOI18N
        Track20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Track20ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ButtonPanelLayout = new javax.swing.GroupLayout(ButtonPanel);
        ButtonPanel.setLayout(ButtonPanelLayout);
        ButtonPanelLayout.setHorizontalGroup(
            ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ButtonPanelLayout.createSequentialGroup()
                        .addComponent(Track16, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track17, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track18, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track19, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ButtonPanelLayout.createSequentialGroup()
                        .addComponent(Track11, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track12, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track13, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track14, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ButtonPanelLayout.createSequentialGroup()
                        .addComponent(Track06, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track07, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track08, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track09, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ButtonPanelLayout.createSequentialGroup()
                        .addComponent(Track01, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track02, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track03, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(Track04, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Track20, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track15, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track10, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track05, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        ButtonPanelLayout.setVerticalGroup(
            ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ButtonPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Track02, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track01, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track04, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track03, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track05, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Track07, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track06, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track09, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track08, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track10, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Track12, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track11, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track14, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track13, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track15, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(ButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Track17, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track16, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track19, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track18, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Track20, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jMenu1.setText("File");

        menuItemSave.setText("Save");
        menuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveActionPerformed(evt);
            }
        });
        jMenu1.add(menuItemSave);
        jMenu1.add(jSeparator1);

        menuItemOpen.setText("Open");
        menuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenActionPerformed(evt);
            }
        });
        jMenu1.add(menuItemOpen);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        clearBoard.setText("Clear Board");
        clearBoard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBoardActionPerformed(evt);
            }
        });
        jMenu2.add(clearBoard);

        aboutItem.setText("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        jMenu2.add(aboutItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 757, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addComponent(ButtonPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(ButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void PlayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayBtnActionPerformed
        if(!looping){
            if(mp!=null){
                mp.resumePausible();
            }
        }else{
            if(lmp!=null){
                lmp.resumePausable();
            }
        }
    }//GEN-LAST:event_PlayBtnActionPerformed

    private void PauseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PauseBtnActionPerformed
        if(!looping){    
            if(mp!=null){
                mp.pausePausible();
            }
        }else{
            if(lmp!=null){
               lmp.resumePausable();
            }
        }
    }//GEN-LAST:event_PauseBtnActionPerformed

    private void StopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopBtnActionPerformed
        if(!looping){
            if(mp!=null){
                mp.stopPausible();
            }
        }else{
            if(lmp!=null){
                lmp.resumePausable();
            }
        }
    }//GEN-LAST:event_StopBtnActionPerformed

    private void AddBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddBtnActionPerformed
        if (!AddFrame.isVisible()) {
            AddFrame.setVisible(true);
        }
    }//GEN-LAST:event_AddBtnActionPerformed

    private void LoopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoopBtnActionPerformed
        try {
            if(!looping){
                String s = JOptionPane.showInputDialog(null, "Whitch Track do you wish to loop play?", "Loop a track", JOptionPane.QUESTION_MESSAGE);
                if(s==null)
                    return;
                int r = Integer.parseInt(s);
                if (Tracks.containsKey("Track" + Integer.toString(r)) && Tracks.get("Track" + Integer.toString(r)) != null) {
                    startLoop("Track" + Integer.toString(r));
                } else {
                    JOptionPane.showMessageDialog(null, "There is no track to play from this button.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "A track is already looping. Press \"Break\" to stop the looping track.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "You have entered bad input for your button ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_LoopBtnActionPerformed

    private void RemoveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveBtnActionPerformed
        try {
            String s = JOptionPane.showInputDialog(null, "Whitch Track do you wish to remove", "Remove a track", JOptionPane.QUESTION_MESSAGE);
            if(s==null)
                return;
            int r = Integer.parseInt(s);
            /*Object[] arr = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
            //JOptionSelectPane p = new JOptionSelectPane("Select a track to remove.", "Remove", arr);
            int r = JOptionSelectPane.showOptionSelectPanel("Select a track to remove.", "Remove", arr);*/
            if (Tracks.replace("Track" + Integer.toString(r), null) != null) {
                JButton jb = (JButton) ButtonGroup.get(r);
                jb.setFont(Font.decode(jb.getFont().toString() + "-plain-11"));
            } else {
                JOptionPane.showMessageDialog(null, "There is no track to remove from this button.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "You have entered bad input for your button ID.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_RemoveBtnActionPerformed

    private void addChooseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChooseBtnActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter f = new FileNameExtensionFilter("MP3 Files", "mp3");
        chooser.setFileFilter(f);
        chooser.showOpenDialog(null);
        File fp = chooser.getSelectedFile();
        try {
            String filePath = fp.toString();
            if (filePath.substring(filePath.length() - 3, filePath.length()).equalsIgnoreCase("mp3")) {
                addTxt.setText(filePath);
            } else {
                JOptionPane.showMessageDialog(null, "The selected file is not in correct format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NullPointerException e) {
            addTxt.setText("");
        }
    }//GEN-LAST:event_addChooseBtnActionPerformed

    private void Add_BindDtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Add_BindDtnActionPerformed
        int id = addCb.getSelectedIndex() + 1;
        String name = "Track" + id;
        if (Tracks.containsKey(name) && !addTxt.getText().equals("")) {
            System.out.println("Track" + id);
            Tracks.replace(name, addTxt.getText());
            JButton jb = (JButton) ButtonGroup.get(id);
            jb.setFont(Font.decode(jb.getFont().toString() + "-bold-16"));
        }
        AddFrame.setVisible(false);
    }//GEN-LAST:event_Add_BindDtnActionPerformed

    private void Track04ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track04ActionPerformed
        playMediaFromBind(4);
    }//GEN-LAST:event_Track04ActionPerformed

    private void Track11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track11ActionPerformed
        playMediaFromBind(11);
    }//GEN-LAST:event_Track11ActionPerformed

    private void Track01ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track01ActionPerformed
        playMediaFromBind(1);
    }//GEN-LAST:event_Track01ActionPerformed

    private void Track03ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track03ActionPerformed
        playMediaFromBind(3);
    }//GEN-LAST:event_Track03ActionPerformed

    private void Track06ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track06ActionPerformed
        playMediaFromBind(6);
    }//GEN-LAST:event_Track06ActionPerformed

    private void Track12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track12ActionPerformed
        playMediaFromBind(12);
    }//GEN-LAST:event_Track12ActionPerformed

    private void Track02ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track02ActionPerformed
        playMediaFromBind(2);
    }//GEN-LAST:event_Track02ActionPerformed

    private void Track20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track20ActionPerformed
        playMediaFromBind(20);
    }//GEN-LAST:event_Track20ActionPerformed

    private void Track09ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track09ActionPerformed
        playMediaFromBind(9);
    }//GEN-LAST:event_Track09ActionPerformed

    private void Track14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track14ActionPerformed
        playMediaFromBind(14);
    }//GEN-LAST:event_Track14ActionPerformed

    private void Track15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track15ActionPerformed
        playMediaFromBind(15);
    }//GEN-LAST:event_Track15ActionPerformed

    private void Track19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track19ActionPerformed
        playMediaFromBind(19);
    }//GEN-LAST:event_Track19ActionPerformed

    private void Track07ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track07ActionPerformed
        playMediaFromBind(7);
    }//GEN-LAST:event_Track07ActionPerformed

    private void Track18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track18ActionPerformed
        playMediaFromBind(18);
    }//GEN-LAST:event_Track18ActionPerformed

    private void Track10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track10ActionPerformed
        playMediaFromBind(10);
    }//GEN-LAST:event_Track10ActionPerformed

    private void Track08ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track08ActionPerformed
        playMediaFromBind(8);
    }//GEN-LAST:event_Track08ActionPerformed

    private void Track13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track13ActionPerformed
        playMediaFromBind(13);
    }//GEN-LAST:event_Track13ActionPerformed

    private void Track16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track16ActionPerformed
        playMediaFromBind(16);
    }//GEN-LAST:event_Track16ActionPerformed

    private void Track17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track17ActionPerformed
        playMediaFromBind(17);
    }//GEN-LAST:event_Track17ActionPerformed

    private void Track05ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Track05ActionPerformed
        playMediaFromBind(5);
    }//GEN-LAST:event_Track05ActionPerformed

    private void BreakBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BreakBtnActionPerformed
        stopLoop();
        JOptionPane.showMessageDialog(null, "Loop ended. Press OK to continue.", "Loop Broken", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_BreakBtnActionPerformed

    private void VolumeSldStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_VolumeSldStateChanged
        JSlider vs = (JSlider) evt.getSource();
        if(!vs.getValueIsAdjusting()){
            if (mp != null) {
               setVolume((float) VolumeSld.getValue());
            }
        }        
    }//GEN-LAST:event_VolumeSldStateChanged

    private void MuteTglStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_MuteTglStateChanged
         if(MuteTgl.isSelected()){
             setVolume(0f);
         }else{
             setVolume((float) VolumeSld.getValue());
         }
    }//GEN-LAST:event_MuteTglStateChanged

    private void menuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveActionPerformed
        createSaveFile(this.Tracks);
    }//GEN-LAST:event_menuItemSaveActionPerformed

    private void menuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenActionPerformed
        openSaveFile(this.Tracks, this.ButtonGroup);
    }//GEN-LAST:event_menuItemOpenActionPerformed

    private void clearBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBoardActionPerformed
        for(int i=0;i<Tracks.size();i++){
            if (Tracks.replace(Tracks.get("Track"+(i+1)), null) != null) {
                JButton jb = (JButton) ButtonGroup.get(i);
                jb.setFont(Font.decode(jb.getFont().toString() + "-plain-11"));
            }
        }
    }//GEN-LAST:event_clearBoardActionPerformed

    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        //show about panel
        aboutFrame.setVisible(true);
    }//GEN-LAST:event_aboutItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MusicGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MusicGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MusicGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MusicGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        

        // Create and display the form 
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MusicGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddBtn;
    private javax.swing.JFrame AddFrame;
    private javax.swing.JButton Add_BindDtn;
    private javax.swing.JButton BreakBtn;
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton LoopBtn;
    private javax.swing.JCheckBox MuteTgl;
    private javax.swing.JButton PauseBtn;
    private javax.swing.JButton PlayBtn;
    private javax.swing.JButton RemoveBtn;
    private javax.swing.JButton StopBtn;
    private javax.swing.JButton Track01;
    private javax.swing.JButton Track02;
    private javax.swing.JButton Track03;
    private javax.swing.JButton Track04;
    private javax.swing.JButton Track05;
    private javax.swing.JButton Track06;
    private javax.swing.JButton Track07;
    private javax.swing.JButton Track08;
    private javax.swing.JButton Track09;
    private javax.swing.JButton Track10;
    private javax.swing.JButton Track11;
    private javax.swing.JButton Track12;
    private javax.swing.JButton Track13;
    private javax.swing.JButton Track14;
    private javax.swing.JButton Track15;
    private javax.swing.JButton Track16;
    private javax.swing.JButton Track17;
    private javax.swing.JButton Track18;
    private javax.swing.JButton Track19;
    private javax.swing.JButton Track20;
    private javax.swing.JSlider VolumeSld;
    private javax.swing.JFrame aboutFrame;
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JComboBox<String> addCb;
    private javax.swing.JButton addChooseBtn;
    private javax.swing.JTextField addTxt;
    private javax.swing.JMenuItem clearBoard;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JMenuItem menuItemOpen;
    private javax.swing.JMenuItem menuItemSave;
    // End of variables declaration//GEN-END:variables
}
