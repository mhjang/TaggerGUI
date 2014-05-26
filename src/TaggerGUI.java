/**
 * Created by mhjang on 4/24/14.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static java.util.Map.Entry;





class NoiseAction extends AbstractAction {
        TaggerGUI frame;
        public NoiseAction(TaggerGUI frame)
        {
            this.frame = frame;


        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            frame.getSaveButton().setEnabled(true);

            DefaultListModel lm = (DefaultListModel)frame.getJList().getModel();
            String line = (String)frame.getJList().getSelectedValue();
            int[] indices = frame.getJList().getSelectedIndices();
      /*      for(int i=0; i<indices.length; i++) {
                line = (String)lm.getElementAt(indices[i]);
                if(line.contains("<RELEVANT>")) {
                    line = line.replace("<RELEVANT>", "<NOISE-A>");
                    line = line.replace("</RELEVANT>", "</NOISE-A>");
                }
                else if(!line.contains("<NOISE-A>")) {
                    line = "<NOISE-A>" + line + "</NOISE-A>";
                }
                lm.setElementAt(line, indices[i]);
                System.out.println(indices[i]);

            }

            System.out.println("<NOISE-A>" + line + "</NOISE-A>");
         */
            frame.getJList().setSelectedIndex(indices[indices.length-1]+1);
            frame.getJScrollPane().revalidate();
            frame.getJScrollPane().repaint();


        }
}



class MyListModel extends DefaultListModel {
    int lastTaggedLine = 0;

    public MyListModel(String[] data) {
        strings = data;
    }
    public MyListModel() {

    }
    String[] strings = { "Load a new document to tag" };
    public int getSize() { return strings.length; }
    public Object getElementAt(int i) { return strings[i]; }
    public void setElementAt(Object obj, int i) {
        strings[i] = (String)obj;
        fireContentsChanged(this, i, i);
        if(i>lastTaggedLine) lastTaggedLine = i;
    }
    public void setData(String[] data) {
        strings = data;
    }
    public int getLastTaggedLine() {
        return lastTaggedLine;
    }
    public String[] getData() {
        return strings;
    }
}
/**
 *
 * @author mhjang
 */
public class TaggerGUI extends javax.swing.JFrame {
    BufferedWriter bw; // output file writer
    int currentLine;
    String filenameSaved;
    static String equationTag = "<EQUATION>";
    static String tableTag = "<TABLE>";
    static String codeTag = "<CODE>";
    static String miscTag = "<MISCELLANEOUS>";
    static String equationTagClose = "</EQUATION>";
    static String tableTagClose = "</TABLE>";
    static String codeTagClose = "</CODE>";
    static String miscTagClose = "</MISCELLANEOUS>";
    static String[] tags = {equationTag, tableTag, codeTag, miscTag};
    static String[] closeTags = {equationTagClose, tableTagClose, codeTagClose, miscTagClose};
    static HashMap<Integer, Integer> taggedZones = new HashMap<Integer, Integer>();
    MainListCellRenderer myCellRenderer = new MainListCellRenderer();
    SentenceListCellRenderer mySentenceCellRenderer  = new SentenceListCellRenderer();
    int initiatedTag = -1;
    static int tableTagIdx = 1;
    static int equTagIdx = 2;
    static int codeTagIdx = 3;
    static int miscTagIdx = 4;

    int lastTaggedLineIdx = -1;
    int lastTaggedTokenIdx = -1;

    // line number of the most recent tag, so that the end tag can't be above it
    static int initiatedLineNum = 0;

    /**
     * Creates new form NewJFrame
     */
    public TaggerGUI() {
        initComponents();
        mainList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        //      jButton1.addActionListener(na);
    }

    private static class SentenceListCellRenderer extends DefaultListCellRenderer {
        static boolean isTableInitiated = false;
        static boolean isEquationInitiated = false;
        static boolean isCodeInitiated = false;
        static boolean isMiscInitiated = false;

        static int initiatedIdx = 0;
        HashMap<Integer, Integer> tableCoverage = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> equCoverage = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> codeCoverage = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> miscCoverage = new HashMap<Integer, Integer>();


        static int endIdx = 0;

        public void closeUndo(String closeTag, int index) {
            Integer deleteIdx = -1;
            HashMap<Integer, Integer> releventMap = null;
            if(closeTag == equationTagClose) {
                releventMap = equCoverage;
            }
            else if(closeTag == tableTagClose) {
                releventMap = tableCoverage;
            }
            else if(closeTag == codeTagClose) {
                releventMap = codeCoverage;
            }
            else if(closeTag == miscTagClose) {
                releventMap = miscCoverage;
            }

            for(Integer key : releventMap.keySet()) {
                if(index == releventMap.get(key)) {
                    deleteIdx = key;
                    break;
                }
            }
            releventMap.remove(deleteIdx);
        }

        public void beginUndo(String tag, int index) {
            HashMap<Integer, Integer> releventMap = null;
            if(tag == equationTag) {
                releventMap = equCoverage;
            }
            else if(tag == tableTag) {
                releventMap = tableCoverage;
            }
            else if(tag == codeTag) {
                releventMap = codeCoverage;
            }
            else if(tag == miscTag) {
                releventMap = miscCoverage;
            }
            releventMap.remove(index);

        }
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            Component c = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            ListModel lm = list.getModel();
            String line = (String)lm.getElementAt(index);
            if(line.contains(tableTag)) {
                isTableInitiated = true;
                initiatedIdx = index;
                c.setBackground(Color.cyan);
                tableCoverage.put(initiatedIdx, lm.getSize());


            }
            else if(line.contains(equationTag)) {
                isEquationInitiated = true;
                initiatedIdx = index;
                c.setBackground(Color.pink);
                equCoverage.put(initiatedIdx, lm.getSize());


            }
            else if(line.contains(codeTag)) {
                isCodeInitiated = true;
                c.setBackground(Color.orange);
                initiatedIdx = index;
                codeCoverage.put(initiatedIdx, lm.getSize());

            }
            else if(line.contains(miscTag)) {
                isMiscInitiated = true;
                initiatedIdx = index;
                c.setBackground(Color.yellow);
                miscCoverage.put(initiatedIdx, lm.getSize());


            }

            if(line.contains(tableTagClose)) {
                endIdx = index;
                isTableInitiated = false;
                tableCoverage.remove(initiatedIdx);
                tableCoverage.put(initiatedIdx, endIdx);
            }
            else if(line.contains(equationTagClose)) {
                endIdx = index;
                isEquationInitiated = false;
                equCoverage.remove(initiatedIdx);
                equCoverage.put(initiatedIdx, endIdx);

            }
            else if(line.contains(codeTagClose)) {
                endIdx = index;
                isCodeInitiated = false;
                codeCoverage.remove(initiatedIdx);
                codeCoverage.put(initiatedIdx, endIdx);

            }
            else if(line.contains(miscTagClose)) {
                endIdx = index;
                isMiscInitiated = false;
                miscCoverage.remove(initiatedIdx);
                miscCoverage.put(initiatedIdx, endIdx);
            }


            for(Integer key: tableCoverage.keySet()) {
                if(index >= key && index<=tableCoverage.get(key)) {
                    c.setBackground(Color.blue);
                }
            }

            for(Integer key: codeCoverage.keySet()) {
                if(index >= key && index<=codeCoverage.get(key)) {
                    c.setBackground(Color.orange);
                }
            }

            for(Integer key: equCoverage.keySet()) {
                if(index >= key && index<=equCoverage.get(key)) {
                    c.setBackground(Color.pink);
                }
            }

            for(Integer key: miscCoverage.keySet()) {
                if(index >= key && index<=miscCoverage.get(key)) {
                    c.setBackground(Color.yellow);
                }
            }


            return c;
        }

    }

    private static class MainListCellRenderer extends DefaultListCellRenderer {
        static boolean isTableInitiated = false;
        static boolean isEquationInitiated = false;
        static boolean isCodeInitiated = false;
        static boolean isMiscInitiated = false;

        static int initiatedIdx = 0;
        HashMap<Integer, Integer> tableCoverage = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> equCoverage = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> codeCoverage = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> miscCoverage = new HashMap<Integer, Integer>();


        static int endIdx = 0;

        public void closeUndo(String closeTag, int index) {
            Integer deleteIdx = -1;
            HashMap<Integer, Integer> releventMap = null;
            if(closeTag == equationTagClose) {
                releventMap = equCoverage;
            }
            else if(closeTag == tableTagClose) {
                releventMap = tableCoverage;
            }
            else if(closeTag == codeTagClose) {
                releventMap = codeCoverage;
            }
            else if(closeTag == miscTagClose) {
                releventMap = miscCoverage;
            }

            for(Integer key : releventMap.keySet()) {
                 if(index == releventMap.get(key)) {
                     deleteIdx = key;
                     break;
                  }
            }
            releventMap.remove(deleteIdx);
        }

        public void beginUndo(String tag, int index) {
            HashMap<Integer, Integer> releventMap = null;
            if(tag == equationTag) {
                releventMap = equCoverage;
            }
            else if(tag == tableTag) {
                releventMap = tableCoverage;
            }
            else if(tag == codeTag) {
                releventMap = codeCoverage;
            }
            else if(tag == miscTag) {
                releventMap = miscCoverage;
            }
            releventMap.remove(index);

        }
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            Component c = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            ListModel lm = list.getModel();
            String line = (String)lm.getElementAt(index);
                if(line.contains(tableTag)) {
                    isTableInitiated = true;
                    initiatedIdx = index;
                    c.setBackground(Color.cyan);

                }
                else if(line.contains(equationTag)) {
                    isEquationInitiated = true;
                    initiatedIdx = index;
                    c.setBackground(Color.pink);

                }
                else if(line.contains(codeTag)) {
                    isCodeInitiated = true;
                    c.setBackground(Color.orange);

                    initiatedIdx = index;
                }
                else if(line.contains(miscTag)) {
                    isMiscInitiated = true;
                    initiatedIdx = index;
                    c.setBackground(Color.yellow);

                }

                if(line.contains(tableTagClose)) {
                    endIdx = index;
                    isTableInitiated = false;
                    if(!tableCoverage.containsKey(initiatedIdx))
                        tableCoverage.put(initiatedIdx, endIdx);
                }
                else if(line.contains(equationTagClose)) {
                    endIdx = index;
                    isEquationInitiated = false;
                    if(!equCoverage.containsKey(initiatedIdx))
                        equCoverage.put(initiatedIdx, endIdx);

                }
                else if(line.contains(codeTagClose)) {
                    endIdx = index;
                    isCodeInitiated = false;
                    if(!codeCoverage.containsKey(initiatedIdx))
                        codeCoverage.put(initiatedIdx, endIdx);

                }
                else if(line.contains(miscTagClose)) {
                    endIdx = index;
                    isMiscInitiated = false;
                    if(!miscCoverage.containsKey(initiatedIdx))
                        miscCoverage.put(initiatedIdx, endIdx);
                }


                for(Integer key: tableCoverage.keySet()) {
                    if(index >= key && index<=tableCoverage.get(key)) {
                        c.setBackground(Color.blue);
                    }
                }

                for(Integer key: codeCoverage.keySet()) {
                    if(index >= key && index<=codeCoverage.get(key)) {
                        c.setBackground(Color.orange);
                    }
                }

                for(Integer key: equCoverage.keySet()) {
                    if(index >= key && index<=equCoverage.get(key)) {
                        c.setBackground(Color.pink);
                    }
                }

                for(Integer key: miscCoverage.keySet()) {
                    if(index >= key && index<=miscCoverage.get(key)) {
                        c.setBackground(Color.yellow);
                    }
                }


            return c;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        equButton = new JButton();
        codeButton = new JButton();
        tableButton = new JButton();
        miscButton = new JButton();
        equButton2 = new JButton();
        codeButton2 = new JButton();
        tableButton2 = new JButton();
        miscButton2 = new JButton();
        jScrollPane1 = new JScrollPane();
        mainList = new JList();
        saveButton = new JButton();
        menuBar = new JMenuBar();
        jMenu1File = new JMenu();
        jMenu1ItemOpen = new JMenuItem();
        jMenu2Edit = new JMenu();
        jScrollPane3 =  new JScrollPane();
        sentenceList = new JList();
        revertButton = new JButton();
        jMenu3Tags = new JMenu();
        addTagMenuItem = new JMenuItem();
        jMenu1ItemSave = new JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);


        tableButton2.setEnabled(false);
        codeButton2.setEnabled(false);
        miscButton2.setEnabled(false);
        equButton2.setEnabled(false);

        equButton.setText("Equation <--(CTR+W)");
        equButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control W"), "NOISE_ACTION_A");
        equButton.getActionMap().put("NOISE_ACTION_A", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                equButton.doClick();
            }
        });
        equButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseButtonActionPerformed(evt);
            }
        });

        equButton2.setText("Equation -->(CTR+S)");
        equButton2.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "NOISE_ACTION_A");
        equButton2.getActionMap().put("NOISE_ACTION_A", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                equButton2.doClick();
            }
        });
        equButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseCloseButtonActionPerformed(evt);
            }
        });

        codeButton.setText("Code <--(CTR+E)");
        codeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control E"), "NOISE_ACTION_B");
        codeButton.getActionMap().put("NOISE_ACTION_B", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeButton.doClick();
            }
        });

        codeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseButtonActionPerformed(evt);
            }
        });
        codeButton2.setText("Code -->(CTR+D)");
        codeButton2.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control D"), "NOISE_ACTION_B");
        codeButton2.getActionMap().put("NOISE_ACTION_B", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                codeButton2.doClick();
            }
        });

        codeButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseCloseButtonActionPerformed(evt);
            }
        });

        tableButton.setText("Table <--(CTR+R)");
        tableButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control R"), "NOISE_ACTION_C");
        tableButton.getActionMap().put("NOISE_ACTION_C", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableButton.doClick();
            }
        });
        tableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //         semanticNoiseButtonActionPerformed(evt);
                noiseButtonActionPerformed(evt);
            }
        });

        tableButton2.setText("Table -->(CTR+F)");
        tableButton2.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "NOISE_ACTION_C");
        tableButton2.getActionMap().put("NOISE_ACTION_C", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableButton2.doClick();
            }
        });
        tableButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //         semanticNoiseButtonActionPerformed(evt);
                noiseCloseButtonActionPerformed(evt);
            }
        });

        miscButton.setText("MISCELLANEOUS <--(CTR+T)");
        miscButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control T"), "NOISE_ACTION_D");
        miscButton.getActionMap().put("NOISE_ACTION_D", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                miscButton.doClick();
            }
        });
        miscButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //         semanticNoiseButtonActionPerformed(evt);
                noiseButtonActionPerformed(evt);
            }
        });

        miscButton2.setText("MISCELLANEOUS -->(CTR+G)");
        miscButton2.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control G"), "NOISE_ACTION_D");
        miscButton2.getActionMap().put("NOISE_ACTION_D", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                miscButton2.doClick();
            }
        });
        miscButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //         semanticNoiseButtonActionPerformed(evt);
                noiseCloseButtonActionPerformed(evt);
            }
        });

        revertButton.setText("Undo");
        revertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(sentenceList);

        mainList.setModel(new MyListModel());
        mainList.setCellRenderer( myCellRenderer );
        sentenceList.setCellRenderer( mySentenceCellRenderer);

        sentenceList.setModel(new MyListModel());

        jScrollPane1.setViewportView(mainList);

    /*    saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
    */


        jMenu1File.setText("File");

        jMenu1ItemOpen.setText("Open");
        jMenu1ItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileOpenMenuClicked(evt);
            }
        });

        jMenu1ItemSave.setText("Save");
        jMenu1ItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        mainList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) throws NullPointerException {
                if (!mainList.isSelectionEmpty()) {
                    String line = (String) mainList.getSelectedValue();
                    String[] tokens;
                    if (line.contains(" "))
                        tokens = line.split(" ");
                    else
                        tokens = new String[]{""};
                    sentenceList.setModel(new MyListModel(tokens));
                    jScrollPane3.revalidate();
                    jScrollPane3.repaint();
                    sentenceList.repaint();

                } else {
                    String[] tokens = new String[]{""};

                    sentenceList.setModel(new MyListModel(tokens));
                    jScrollPane3.revalidate();
                    jScrollPane3.repaint();
                    sentenceList.repaint();
                }
            }
        });


        jMenu1File.add(jMenu1ItemOpen);
        menuBar.add(jMenu1File);
        jMenu2Edit.setText("Edit");
        menuBar.add(jMenu2Edit);

        jMenu3Tags.setText("Tags");
        addTagMenuItem.setText("Add Tags");
        jMenu3Tags.add(addTagMenuItem);
        menuBar.add(jMenu3Tags);
        setJMenuBar(menuBar);


        addTagMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagMenuClickedAction(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup()
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(equButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(codeButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tableButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(miscButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(revertButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        //           .addComponent(saveButton)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(equButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(codeButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tableButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(miscButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 314, javax.swing.GroupLayout.DEFAULT_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 212, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                                        .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 212, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(equButton)
                                        .addComponent(codeButton)
                                        .addComponent(tableButton)
                                        .addComponent(miscButton)
                                        .addComponent(revertButton))
                                        //            .addComponent(saveButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(equButton2)
                                        .addComponent(codeButton2)
                                        .addComponent(tableButton2)
                                        .addComponent(miscButton2)
                                        .addGap(22, 22, 22)))
        );

        pack();
    }// </editor-fold>


    private void tagMenuClickedAction(ActionEvent evt) {
        new AddTagFrame().setVisible(true);
    }

    private int findIndexOfNthToken(String line, int tokenIdx) {
        String[] tokens = line.split(" ");
        int curIdx = 0;
        for(int i=0; i<tokenIdx; i++) {
            curIdx += tokens[i].length();
            curIdx++; // for a blank space
        }
        return curIdx;

    }

    private void noiseButtonActionPerformed(ActionEvent evt) {

        String beginTag = null, closeTag = null;
        int index = mainList.getSelectedIndex();

        for(Integer startIdx : taggedZones.keySet()) {
            if(index >= startIdx && index <= taggedZones.get(startIdx)) {
                JOptionPane.showMessageDialog(this, "This area is already tagged!");
                return;
            }
        }

        tableButton.setEnabled(false);
        codeButton.setEnabled(false);
        miscButton.setEnabled(false);
        equButton.setEnabled(false);

        tableButton2.setEnabled(false);
        codeButton2.setEnabled(false);
        miscButton2.setEnabled(false);
        equButton2.setEnabled(false);
        initiatedLineNum = mainList.getSelectedIndex();




        if(evt.getSource() == equButton) {
            beginTag = equationTag;
            closeTag = equationTagClose;
            initiatedTag = equTagIdx;
            equButton2.setEnabled(true);

        }
        else if(evt.getSource() == tableButton) {
            beginTag = tableTag;
            closeTag = tableTagClose;
            initiatedTag = tableTagIdx;
            tableButton2.setEnabled(true);


        }
        else if(evt.getSource() == codeButton) {
            beginTag = codeTag;
            closeTag = codeTagClose;
            initiatedTag = codeTagIdx;
            codeButton2.setEnabled(true);
        }
        else if(evt.getSource() == miscButton) {
            beginTag = miscTag;
            closeTag = miscTagClose;
                initiatedTag = miscTagIdx;

            miscButton2.setEnabled(true);

        }
   //     saveButton.setEnabled(true);
        /**
         * Is token-based annotation list activated?
         */
        JList activeObject;
        boolean sentenceListActivated = false;
        if(sentenceList.isSelectionEmpty())
            activeObject = mainList;
        else {
            activeObject = sentenceList;
            sentenceListActivated = true;
        }
        MyListModel lm = (MyListModel)activeObject.getModel();
        String line = (String)activeObject.getSelectedValue();

        int[] indices = activeObject.getSelectedIndices();
        String originalLine;

        for(int i=0; i<indices.length; i++) {
            line = (String)lm.getElementAt(indices[i]);
            originalLine = line;
            line = replaceTagIfExists(line, beginTag, closeTag);
            line = beginTag + line;
            lm.setElementAt(line, indices[i]);
            lastTaggedLineIdx = indices[i];
            // merge consecutive tags
            if(sentenceListActivated) {
                String fullSentence = (String) mainList.getSelectedValue();
                int tokenIdx = findIndexOfNthToken(fullSentence, indices[i]);
                lastTaggedTokenIdx = tokenIdx;
                fullSentence = fullSentence.substring(0, tokenIdx) + fullSentence.substring(tokenIdx).replaceFirst(Pattern.quote(originalLine), line);
                MyListModel jlistModel = (MyListModel) mainList.getModel();
                jlistModel.setElementAt(fullSentence, mainList.getSelectedIndex());
            }

        }
     //    System.out.println("<NOISE-A>" + line + "</NOISE-A>");
        activeObject.setSelectedIndex(indices[indices.length-1]+1);
        jScrollPane1.revalidate();
        jScrollPane1.repaint();


    }



    private void noiseCloseButtonActionPerformed(ActionEvent evt) {
        // find out which button was clicked
        int mainIndex = mainList.getSelectedIndex();
        if(mainIndex < lastTaggedLineIdx) {
            JOptionPane.showMessageDialog(this, "An end tag should be located after the begin tag");
            return;
        }

        for(Integer startIdx : taggedZones.keySet()) {
            if(mainIndex >= startIdx && mainIndex <= taggedZones.get(startIdx)) {
                JOptionPane.showMessageDialog(this, "This area is already tagged!");
                return;
            }
        }

        int endIdx = mainList.getSelectedIndex();
        tableButton.setEnabled(true);
        codeButton.setEnabled(true);
        miscButton.setEnabled(true);
        equButton.setEnabled(true);

        tableButton2.setEnabled(false);
        codeButton2.setEnabled(false);
        miscButton2.setEnabled(false);
        equButton2.setEnabled(false);

        String beginTag = null, closeTag = null;

        if(evt.getSource() == equButton2) {
            beginTag = equationTag;
            closeTag = equationTagClose;
        }
        else if(evt.getSource() == tableButton2) {
            beginTag = tableTag;
            closeTag = tableTagClose;
        }
        else if(evt.getSource() == codeButton2) {
            beginTag = codeTag;
            closeTag = codeTagClose;

        }
        else if(evt.getSource() == miscButton2) {
            beginTag = miscTag;
            closeTag = miscTagClose;
        }
        if(initiatedTag == -1)
            JOptionPane.showMessageDialog(this, "Can't end a tag that was not initiated");
        else {
            /**
             * Is token-based annotation list activated?
             */
            JList activeObject;
            boolean sentenceListActivated = false;
            if(sentenceList.isSelectionEmpty())
                activeObject = mainList;
            else {
                activeObject = sentenceList;
                sentenceListActivated = true;
            }

            MyListModel lm = (MyListModel)activeObject.getModel();
            String line = (String)activeObject.getSelectedValue();
            int index = activeObject.getSelectedIndex();
            String originalLine = (String)lm.getElementAt(index);

            line = replaceTagIfExists(line, beginTag, closeTag);
            line =  line + closeTag;
            lm.setElementAt(line, index);
            initiatedTag = -1;

            if(sentenceListActivated) {
                String fullSentence = (String) mainList.getSelectedValue();
                int tokenIdx = findIndexOfNthToken(fullSentence, index);
                lastTaggedTokenIdx = tokenIdx;
                fullSentence = fullSentence.substring(0, tokenIdx) + fullSentence.substring(tokenIdx).replaceFirst(Pattern.quote(originalLine), line);
                MyListModel jlistModel = (MyListModel) mainList.getModel();
                jlistModel.setElementAt(fullSentence, mainList.getSelectedIndex());
            }


        }
        taggedZones.put(initiatedLineNum, endIdx);

        // trace back to the most recent opening tag
        //; if not matched alert
        // if matched, close it

    }

    private void fileOpenMenuClicked(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        saveButton.setEnabled(true);

        final JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File("./"));
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                //This is where a real application would open the file.
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                ArrayList<String> data = new ArrayList<String>();
                while((line = br.readLine())!= null) {
                    if(!line.isEmpty())
                        data.add(line);
                }
                String[] dataAsString = new String[data.size()];
                data.toArray(dataAsString);
                MyListModel lm = new MyListModel(dataAsString);
                mainList.setModel(lm);
                jScrollPane1.revalidate();
                jScrollPane1.repaint();
                mainList.revalidate();
                mainList.repaint();
                filenameSaved = file.getName();

            } catch (Exception ex) {
                Logger.getLogger(TaggerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        final JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File("./"));
        int returnVal = fc.showOpenDialog(this);
        String dir = "";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            dir = fc.getSelectedFile().getAbsolutePath();
        }
        try {
            bw = new BufferedWriter(new FileWriter(new File(dir+filenameSaved)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyListModel model = (MyListModel) mainList.getModel();
        String[] data = model.getData();
        try{
            for(int i=0; i<data.length; i++) {
                bw.write(data[i] + "<BR> \n");

            }
            bw.flush();
            JOptionPane.showMessageDialog(this, "Saved at" + filenameSaved);
            saveButton.setEnabled(false);
        }catch(Exception e) {
            e.printStackTrace();
        }

    }


    private void revertButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        JList activeObject;
        boolean sentenceListActivated = false;
        if(sentenceList.isSelectionEmpty())
            activeObject = mainList;
        else {
            activeObject = sentenceList;
            sentenceListActivated = true;
        }

         MyListModel lm = (MyListModel)activeObject.getModel();
         String line = (String)activeObject.getSelectedValue();
         String originalLine = line;
        int index = activeObject.getSelectedIndex();

        // If it is a close tag that we're trying to revert
        for(String closeTag : closeTags) {
            if (line.contains(closeTag)) {
                line = line.replace(closeTag, "");
                if(sentenceListActivated) {
                    String fullSentence = (String) mainList.getSelectedValue();
                    int tokenIdx = findIndexOfNthToken(fullSentence, index);
                    lastTaggedTokenIdx = tokenIdx;
                    fullSentence = fullSentence.substring(0, tokenIdx) + fullSentence.substring(tokenIdx).replaceFirst(Pattern.quote(originalLine), line);
                    MyListModel jlistModel = (MyListModel) mainList.getModel();
                    jlistModel.setElementAt(fullSentence, mainList.getSelectedIndex());
                }
                myCellRenderer.closeUndo(closeTag, mainList.getSelectedIndex());

                initiatedTag = findMatchingBeginTagIdx(closeTag);
                codeButton2.setEnabled(false);
                miscButton2.setEnabled(false);
                equButton2.setEnabled(false);
                tableButton2.setEnabled(false);

                equButton.setEnabled(false);
                tableButton.setEnabled(false);
                codeButton.setEnabled(false);
                miscButton.setEnabled(false);

                if(closeTag == tableTagClose)
                    tableButton2.setEnabled(true);

                if(closeTag == codeTagClose)
                    codeButton2.setEnabled(true);

                if(closeTag == equationTagClose)
                    equButton2.setEnabled(true);

                if(closeTag == miscTagClose)
                    miscButton2.setEnabled(true);

                int removeKey = -1;
                for(Integer key : taggedZones.keySet()) {
                    if(taggedZones.get(key) == mainList.getSelectedIndex()) {
                        removeKey = key;
                        break;
                    }
                }
                taggedZones.remove(removeKey);
            }
        }

        for(String tag: tags) {
            if (line.contains(tag)) {
                line = line.replace(tag, "");
                int lineIdx = mainList.getSelectedIndex();
                MyListModel jlistModel = (MyListModel) mainList.getModel();
                if(sentenceListActivated) {
                    String fullSentence = (String) mainList.getSelectedValue();
                    int tokenIdx = findIndexOfNthToken(fullSentence, index);
                    lastTaggedTokenIdx = tokenIdx;
                    fullSentence = fullSentence.substring(0, tokenIdx) + fullSentence.substring(tokenIdx).replaceFirst(Pattern.quote(originalLine), line);
                    jlistModel.setElementAt(fullSentence, lineIdx);
                    sentenceList.setModel(new MyListModel(fullSentence.split(" ")));

                }
                // also remove the matching end tag
                int endLineIdx = taggedZones.get(lineIdx);
                String endTagLine = (String) jlistModel.getElementAt(endLineIdx);
                endTagLine = endTagLine.replace(findMatchingEndTag(tag), "");
                jlistModel.setElementAt(endTagLine, endLineIdx);
                myCellRenderer.beginUndo(tag, mainList.getSelectedIndex());
                taggedZones.remove(mainList.getSelectedIndex());

                initiatedTag = -1;
                equButton.setEnabled(true);
                tableButton.setEnabled(true);
                codeButton.setEnabled(true);
                miscButton.setEnabled(true);


                equButton2.setEnabled(false);
                tableButton2.setEnabled(false);
                codeButton2.setEnabled(false);
                miscButton2.setEnabled(false);
                break;
            }


        }

        lm.setElementAt(line, index);

        jScrollPane1.revalidate();
        jScrollPane3.revalidate();
        sentenceList.repaint();

    }

    private String replaceTagIfExists(String line, String beginTag, String endTag) {
        for(String tag: tags) {
            if(line.contains(tag)) {
                line = line.replace(tag, beginTag);
                line = line.replace(findMatchingEndTag(tag), endTag);
                return line;
            }
        }
        return line;
    }

    private String findMatchingEndTag(String beginTag) {
        if(beginTag == tableTag) return tableTagClose;
        else if(beginTag == codeTag) return codeTagClose;
        else if(beginTag == equationTag) return equationTagClose;
        else return miscTagClose;
    }
    private int findMatchingBeginTagIdx(String closeTag) {
        if(closeTag == tableTagClose) return tableTagIdx;
        else if(closeTag == codeTagClose) return codeTagIdx;
        else if(closeTag == equationTagClose) return equTagIdx;
        else return miscTagIdx;
    }



    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TaggerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TaggerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TaggerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TaggerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TaggerGUI tg = new TaggerGUI();
                tg.setVisible(true);
                tg.setSize(1000, 700);
       //         tg.pack();



            }
        });
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JButton getNoiseButton() {
        return equButton;
    }

    public JButton getRelevantButton() {
        return codeButton;
    }

    public JList getJList() {
        return mainList;
    }

    public JScrollPane getJScrollPane() {
        return jScrollPane1;
    }
    // Variables declaration - do not modify
    private JButton saveButton;
    private javax.swing.JList mainList;
    private javax.swing.JList sentenceList;
    private javax.swing.JMenu jMenu1File;
    private javax.swing.JMenu jMenu3Tags;
    private javax.swing.JMenu jMenu2Edit;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem jMenu1ItemOpen;
    private javax.swing.JMenuItem jMenu1ItemSave;

    private JButton revertButton;
    private javax.swing.JMenuItem addTagMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private JButton tableButton;
    private JButton equButton;
    private JButton codeButton;
    private JButton miscButton;

    private JButton tableButton2;
    private JButton equButton2;
    private JButton codeButton2;
    private JButton miscButton2;


    // End of variables declaration
}
