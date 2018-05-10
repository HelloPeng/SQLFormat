package com.pansoft.lvzp.ui;

import com.intellij.codeInsight.problems.DefaultProblemFileHighlightFilter;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class PdmFileSelectDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private TextFieldWithBrowseButton pdmFileSelect;
    private TextFieldWithBrowseButton fileOutputDir;
    private JList list1;
    private JCheckBox allSelectCbx;
    private Project project;

    private VirtualFile toSelectDir;
    private VirtualFile outputFileDir;

    public PdmFileSelectDialog(Project project) {
        this.project = project;
        setContentPane(contentPane);
        setTitle("pdm文件筛选");
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.setEnabled(false);
        allSelectCbx.setEnabled(false);
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        FileChooserDescriptor pdmFileChooserDescriptor = new FileChooserDescriptor(true, true, false, false, false, true);
        pdmFileChooserDescriptor.setTitle("Pdm文件选择");
        FileChooserDescriptor outputDirChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        outputDirChooserDescriptor.setTitle("实体类输出目录");
        pdmFileSelect.addBrowseFolderListener(new TextBrowseFolderListener(pdmFileChooserDescriptor) {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooser.chooseFiles(pdmFileChooserDescriptor, project, toSelectDir, virtualFiles -> {
                    for (VirtualFile selectedFile : virtualFiles) {
                        System.out.println(selectedFile.getPath());
                    }
                    if (virtualFiles.size() != 0) {
                        toSelectDir = virtualFiles.get(0);
                        pdmFileSelect.setText(toSelectDir.getPath());
                    }
                });

            }
        });
        fileOutputDir.addBrowseFolderListener(new TextBrowseFolderListener(outputDirChooserDescriptor) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputFileDir == null) {
                    outputFileDir = project.getBaseDir().findChild("src");
                }
                FileChooser.chooseFile(outputDirChooserDescriptor, project, outputFileDir, it -> {
                    outputFileDir = it;
                    fileOutputDir.setText(outputFileDir.getPath());
                });
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
