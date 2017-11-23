package com.tencent.addchannel;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.tencent.util.ApkExternalInfoTool;

public class ApkChannel extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 576598524063504690L;

    JFileChooser fc = new JFileChooser();
    
    private JPanel AddChannel;
    private JTextField apkPathText;
    private JTextArea channelText;
    private JTextArea addChannelResultText;
    private JProgressBar progressBar;
    private JLabel processLabel;

    private JPanel CheckChannel;
    private JTextField checkApkPathText;
    private JTabbedPane tabbedPane;
    private JTable checkResultTable;
    private DefaultTableModel checkResultTableModel;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ApkChannel frame = new ApkChannel();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public ApkChannel() {
        setTitle("批量打包工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 657, 507);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(122, 5, 292, 101);
        setContentPane(tabbedPane);
        
        AddChannel = new JPanel();
        AddChannel.setBorder(new EmptyBorder(5, 5, 5, 5));
        tabbedPane.addTab("渠道打包", AddChannel);
        AddChannel.setLayout(null);
        
        JLabel chooseLabel = new JLabel(" 请选择APK文件");
        chooseLabel.setBounds(10, 10, 424, 23);
        AddChannel.add(chooseLabel);
        
        apkPathText = new JTextField();
        apkPathText.setEditable(false);
        apkPathText.setBounds(21, 38, 489, 30);
        AddChannel.add(apkPathText);
        apkPathText.setColumns(10);
        
        JButton openButton = new JButton("选择...");
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int intRetVal = fc.showOpenDialog(ApkChannel.this); 
                if( intRetVal == JFileChooser.APPROVE_OPTION){
                    apkPathText.setText(fc.getSelectedFile().getPath()); 
                } 
            }
        });
        openButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        openButton.setBounds(520, 38, 93, 32);
        AddChannel.add(openButton);
        
        JLabel channelLabel = new JLabel(" 请输入渠道号，以非数字字符隔开:");
        channelLabel.setBounds(10, 89, 301, 15);
        AddChannel.add(channelLabel);
        
        channelText = new JTextArea();
        channelText.setLineWrap(true);        //激活自动换行功能
        JScrollPane jspaneChannel = new JScrollPane(channelText);
        jspaneChannel.setBounds(21, 114, 211, 249);
        AddChannel.add(jspaneChannel);
        
        JLabel addChannelLabel = new JLabel("渠道打包结果：");
        addChannelLabel.setBounds(254, 89, 359, 15);
        AddChannel.add(addChannelLabel);
        
        addChannelResultText = new JTextArea();
        addChannelResultText.setBackground(SystemColor.control);
        addChannelResultText.setEditable(false);
        addChannelResultText.setLineWrap(true);        //激活自动换行功能
        JScrollPane jspaneChannelResult = new JScrollPane(addChannelResultText);
        jspaneChannelResult.setBounds(254, 114, 359, 249);
        AddChannel.add(jspaneChannelResult);
        
        progressBar = new JProgressBar();
        progressBar.setBackground(new Color(229, 231, 229));
        progressBar.setForeground(new Color(45, 218, 179));
        progressBar.setBounds(21, 373, 592, 14);
        AddChannel.add(progressBar);
        
        processLabel = new JLabel("点击[开始生成渠道包]即可开始打包...");
        processLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        processLabel.setForeground(Color.RED);
        processLabel.setBounds(21, 396, 433, 32);
        AddChannel.add(processLabel);
        
        JButton btnNewButton = new JButton("开始生成渠道包");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //开始生成渠道包
                startBuildChannelPkg();
            }
        });
        btnNewButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        btnNewButton.setBounds(464, 397, 149, 32);
        AddChannel.add(btnNewButton);
        
        CheckChannel = new JPanel();
        CheckChannel.setBorder(new EmptyBorder(5, 5, 5, 5));
        tabbedPane.addTab("检验渠道", CheckChannel);
        CheckChannel.setLayout(null);
        
        JLabel chooseApkLabel = new JLabel(" 请选择Apk文件目录");
        chooseApkLabel.setBounds(10, 10, 279, 15);
        CheckChannel.add(chooseApkLabel);
        
        checkApkPathText = new JTextField();
        checkApkPathText.setEditable(false);
        checkApkPathText.setBounds(20, 35, 485, 31);
        CheckChannel.add(checkApkPathText);
        checkApkPathText.setColumns(10);
        
        JButton chooseButton = new JButton("选择...");
        chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
                int intRetVal = fc.showOpenDialog(ApkChannel.this); 
                if( intRetVal == JFileChooser.APPROVE_OPTION){
                    checkApkPathText.setText(fc.getSelectedFile().getPath());
                    startCheckChannel();
                }
            }
        });
        chooseButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        chooseButton.setBounds(532, 34, 83, 31);
        CheckChannel.add(chooseButton);
        
        checkResultTable = new JTable();
        final Object[][] data = new Object[][] {};
        final String[] columnNames = new String[] {
            "APK", "ChannelID"
        };
        checkResultTableModel = new DefaultTableModel(data, columnNames) {
            private static final long serialVersionUID = 424085260520744933L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        checkResultTable.setModel(checkResultTableModel);
        checkResultTable.getColumnModel().getColumn(0).setPreferredWidth(450);
        checkResultTable.getColumnModel().getColumn(1).setPreferredWidth(145);
        checkResultTable.setRowHeight(20);
        
        JScrollPane jspane = new JScrollPane(checkResultTable);
        jspane.setBounds(20, 75, 600, 350);
        CheckChannel.add(jspane);
    }
    
    
    private void startBuildChannelPkg() {
        String apkPath = apkPathText.getText();
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            int beginIndex = apkPath.lastIndexOf("\\");
            int endIndex = apkPath.lastIndexOf(".");
            String apkName = apkPath.substring(beginIndex + 1, endIndex);
            
            String channelStr = channelText.getText();
            
            addChannelResultText.append("开始解析输入的渠道号...\n");
            
            String[] channelIds = channelStr.split("\\D+");
            String channelIdsValue = "[";
            for(int i=0; i<channelIds.length; i++) {
                String channelId = channelIds[i];
                if (null != channelId && !channelId.isEmpty())
                {
                    channelIdsValue += channelIds[i];
                    if (i<channelIds.length-1) channelIdsValue += ",";
                }             
            }
            channelIdsValue += "]";
            addChannelResultText.append("解析渠道号成功:" + channelIdsValue + "\n");

            if (null != channelIds) {
                int count = channelIds.length*2;
                int index = 0;
                
                for(String channelId : channelIds) {
                    channelId = channelId.replace("\n", "");
                    if (null != channelId && !channelId.isEmpty())
                    {
                        String apkSavePath = apkPath.substring(0, beginIndex) + "\\" + apkName + "-" + channelId + ".apk";
                        if(fileCopy(apkPath, apkSavePath, channelId)) {
                            File saveApk = new File(apkSavePath);
                            if (saveApk.exists()) {
                                //apk拷贝成功 更新进度条
                                progressBar.setValue(index*2+1/count);
                                
                                try {
                                    ApkExternalInfoTool.updateExternalInfo(saveApk, ApkExternalInfoTool.CHANNELID, channelId);
                                } catch (IOException e) {
                                    // TODO 自动生成的 catch 块
                                    e.printStackTrace();
                                    saveApk.delete();
                                    addChannelResultText.append("渠道号为" + channelId + "打包异常，删除复制的文件。e=" + e.getMessage() + "\n");
                                }
                            } else {
                                addChannelResultText.append("渠道号为" + channelId + "的文件不存在.\n");
                            }
                            
                            //apk写入渠道号成功 更新进度条
                            progressBar.setValue(index*2+2/count);
                            addChannelResultText.append("渠道号为" + channelId + "的渠道包打包成功。\n");
                        }
                    }
                    index++;
                }
                
                //所有文件处理完毕，更新进度条
                progressBar.setValue(100);
                addChannelResultText.append("批量渠道号打包全部成功。\n");
                processLabel.setText("批量渠道号打包全部成功。");
            }
        }
    }
    
    private boolean fileCopy(String source, String target, String channelId) {
        boolean success = false;
        
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            addChannelResultText.append("渠道号为" + channelId + "APK文件复制异常。e=" + e.getMessage() + "\n");
        } finally {
            try {
                inStream.close();
                in.close();
                outStream.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
    
    private void startCheckChannel() {
        String apkDir = checkApkPathText.getText();
        File apkDirFile = new File(apkDir);
        if(apkDirFile.exists()) {
            LinkedList<File> list = new LinkedList<File>();  
            File[] files = apkDirFile.listFiles();
            for (File file : files) {  
                String fileName = file.getName();
                if(fileName.endsWith(".apk")) {
                    String channelId = "";
                    try {
                        channelId = ApkExternalInfoTool.readChannelId(file);
                    } catch (IOException e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                        channelId = e.getMessage();
                    }
                    checkResultTableModel.addRow(new Object[]{fileName, channelId});
                }
            }
        }
    }
}
