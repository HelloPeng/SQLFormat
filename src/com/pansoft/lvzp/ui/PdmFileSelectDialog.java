package com.pansoft.lvzp.ui;

import com.google.gson.Gson;
import com.intellij.codeInsight.problems.DefaultProblemFileHighlightFilter;
import com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.pansoft.lvzp.entity.ConfigEntity;
import com.pansoft.lvzp.entity.ConfigNamespace;
import com.pansoft.lvzp.entity.TableEntity;
import com.pansoft.lvzp.utils.PdmXmlParserUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import javax.swing.plaf.ProgressBarUI;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PdmFileSelectDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private TextFieldWithBrowseButton pdmFileSelect;
    private TextFieldWithBrowseButton fileOutputDir;
    private JList list1;
    private JCheckBox allSelectCbx;
    private JProgressBar progressBar;
    private Project project;

    private JPanel pdmParserLoading;

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
        progressBar.setIndeterminate(true);//不确定进度条

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        FileChooserDescriptor pdmFileChooserDescriptor = new FileChooserDescriptor(true, true, false, false, false, true);
        pdmFileChooserDescriptor.setTitle("Pdm文件选择");
        //添加过滤器
        pdmFileChooserDescriptor.withFileFilter(virtualFile -> "pdm".equals(virtualFile.getExtension()));
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
                    try {
                        onSelectFilesCallback(virtualFiles);
                    } catch (Exception e1) {
                        e1.printStackTrace();
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

    private void onSelectFilesCallback(List<VirtualFile> virtualFiles) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        URL pdmThreeConfigUrl = getClass().getResource("/config/pdm_three.xml");
        Document pdmConfigDocument = builder.build(pdmThreeConfigUrl);
        ConfigEntity configEntity = new ConfigEntity();
        buildConfig(pdmConfigDocument.getRootElement(), configEntity);
        for (VirtualFile virtualFile : virtualFiles) {
            Document document = builder.build(virtualFile.getInputStream());
            Element tablesElement = getTablesElement(document, configEntity);
            long startTime = System.currentTimeMillis();
            List<TableEntity> tableEntities = PdmXmlParserUtils.buildTables(tablesElement, TableEntity.class);
            long endTime = System.currentTimeMillis();
            Gson gson = new Gson();
            System.out.println(gson.toJson(tableEntities));
            System.out.println("耗时：" + (endTime - startTime));
        }
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private Element getTablesElement(Document document, ConfigEntity configEntity) {
        Element rootElement = document.getRootElement();
        boolean hasNext = true;
        while (hasNext) {
            String namespace = configEntity.getNamespace();
            rootElement = rootElement.getChild(configEntity.getName(), ConfigNamespace.getNamespace(namespace));
            configEntity = configEntity.getChild();
            hasNext = configEntity != null;
        }
        return rootElement;
    }

    /**
     * 通过递归获取pdm数据的配置信息
     *
     * @param rootElement  需要解析的Object目录
     * @param configEntity 当前用来保存信息的实体类
     */
    private void buildConfig(Element rootElement, ConfigEntity configEntity) {
        Element object = rootElement.getChild("Object");
        if (object != null) {
            Element name = object.getChild("Name");
            Element namespace = object.getChild("Namespace");
            if (name != null) {
                configEntity.setName(name.getContent(0).getValue());
            }
            if (namespace != null) {
                configEntity.setNamespace(namespace.getContent(0).getValue());
            }
            Element child = object.getChild("Child");
            if (child != null) {
                ConfigEntity childConfig = new ConfigEntity();
                configEntity.setChild(childConfig);
                buildConfig(child, childConfig);
            }
        }

    }

}
