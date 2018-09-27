package com.pansoft.lvzp.ui;

import static com.pansoft.lvzp.utils.PDMUtils.buildConfig;
import static com.pansoft.lvzp.utils.PDMUtils.getTablesElement;

import com.google.gson.Gson;
import com.intellij.ide.projectView.impl.nodes.PsiFieldNode;
import com.intellij.ide.projectWizard.ModuleTypeCategory.Java;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.compiler.CompilationException.Message;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.LightPsiClassBuilder;
import com.intellij.psi.impl.source.PsiJavaFileBaseImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.pansoft.lvzp.entity.PDMConfigEntity;
import com.pansoft.lvzp.entity.ConfigNamespace;
import com.pansoft.lvzp.entity.PansoftFieldEntify;
import com.pansoft.lvzp.entity.TableEntity;
import com.pansoft.lvzp.utils.PDMUtils;
import com.pansoft.lvzp.utils.PdmXmlParserUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;

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

  private List<List<TableEntity>> pdmFileTableEntityList = new ArrayList<>();

  public PdmFileSelectDialog(Project project) {
    this.project = project;

    setContentPane(contentPane);
    setTitle("pdm文件筛选");
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    allSelectCbx.setEnabled(false);
    buttonOK.setEnabled(true);
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
    FileChooserDescriptor pdmFileChooserDescriptor = new FileChooserDescriptor(true, true, false,
        false, false, true);
    pdmFileChooserDescriptor.setTitle("Pdm文件选择");
    //添加过滤器
    pdmFileChooserDescriptor
        .withFileFilter(virtualFile -> "pdm".equalsIgnoreCase(virtualFile.getExtension()));
    FileChooserDescriptor outputDirChooserDescriptor = new FileChooserDescriptor(false, true, false,
        false, false, false);
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
    contentPane
        .registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onSelectFilesCallback(List<VirtualFile> virtualFiles) throws Exception {
    pdmFileTableEntityList.clear();
    SAXBuilder builder = new SAXBuilder();
    URL pdmThreeConfigUrl = getClass().getResource("/config/pdm_three.xml");
    Document pdmConfigDocument = builder.build(pdmThreeConfigUrl);
    PDMConfigEntity PDMConfigEntity = new PDMConfigEntity();
    buildConfig(pdmConfigDocument.getRootElement(), PDMConfigEntity);
    for (VirtualFile virtualFile : virtualFiles) {
      Document document = builder.build(virtualFile.getInputStream());
      Element tablesElement = getTablesElement(document, PDMConfigEntity);
      long startTime = System.currentTimeMillis();
      List<TableEntity> tableEntities = PdmXmlParserUtils
          .buildTables(tablesElement, TableEntity.class);
      long endTime = System.currentTimeMillis();
      Gson gson = new Gson();
      System.out.println(gson.toJson(tableEntities));
      System.out.println("耗时：" + (endTime - startTime));
      pdmFileTableEntityList.add(tableEntities);
    }
  }

  private void onOK() {
    WriteCommandAction.runWriteCommandAction(project, () -> {

      if (pdmFileTableEntityList.isEmpty()) {
        Messages.showErrorDialog(
            "请先选择要解析的pdm文件",
            "未找到要解析的文件信息");
        return;
      }

      if (StringUtil.isEmpty(fileOutputDir.getText())) {
        Messages.showErrorDialog(
            "请先选择输出目录后操作",
            "目录未找到");
        return;
      }
      SAXBuilder builder = new SAXBuilder();
      URL pdmThreeConfigUrl = getClass().getResource("/config/pansoft_type.xml");
      List<PansoftFieldEntify> pansoftFieldEntifies = null;
      try {
        Document pdmConfigDocument = builder.build(pdmThreeConfigUrl);
        pansoftFieldEntifies = PDMUtils
            .buildFileTypes(pdmConfigDocument.getRootElement());
      } catch (JDOMException | IOException e) {
        e.printStackTrace();
      }
      if (pansoftFieldEntifies == null || pansoftFieldEntifies.isEmpty()) {
        Messages.showErrorDialog(
            "字符串类型数据解析出错",
            "错误警告");
        return;
      }
      VirtualFile outPutFilePath = LocalFileSystem.getInstance()
          .findFileByIoFile(new File(fileOutputDir.getText()));
      if (outPutFilePath == null) {
        outPutFilePath = outputFileDir;
      }
      System.out.println(outPutFilePath.getPath());
      PsiDirectory directory = PsiManager.getInstance(project).findDirectory(outPutFilePath);
      // 通过获取到PsiElementFactory来创建相应的Element，包括字段，方法，注解，类，内部类等等
      PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
      List<PansoftFieldEntify> finalPansoftFieldEntifies = pansoftFieldEntifies;
      pdmFileTableEntityList.forEach(it -> it.forEach(clsIt -> {
        String tableName = clsIt.getCode();
        //表名被存在code中，表名的创建规则为“简称_名称”,创建实体类时，需要把简称去除
        String clsName = tableName.replace("_", "");
        System.out.println("当前需要创建的文件为 = " + clsName);
        if (directory != null && directory.findFile(clsName + ".java") != null) {
          return;
        }
        // 创建类
        PsiClass aClass = elementFactory
            .createClass(clsName);
        PsiAnnotation entityAnn = elementFactory
            .createAnnotationFromText("@Entity", aClass);
        PsiAnnotation tableAnn = elementFactory
            .createAnnotationFromText("@Table(name = \"" + tableName.toLowerCase() + "\")", aClass);
        PsiAnnotation dataAnn = elementFactory.createAnnotationFromText("@Data", aClass);
        PsiModifierList clsModifierList = aClass.getModifierList();
        if (null != clsModifierList) {
          clsModifierList.addAfter(entityAnn, null);
          clsModifierList.addAfter(tableAnn, null);
          clsModifierList.addAfter(dataAnn, null);
        }
        String ref = clsIt.getKeys().get(0).getKeyColumnsEntryList().get(0).getRef();
        clsIt.getColumns().forEach(columnEntity -> {
          String columnName = columnEntity.getCode();
          String fieldTypeValue = null;
          for (PansoftFieldEntify finalPansoftFieldEntify : finalPansoftFieldEntifies) {
            if (columnEntity.getDataType().contains(finalPansoftFieldEntify.getName())) {
              fieldTypeValue = finalPansoftFieldEntify.getValue();
              break;
            }
          }
          String fieldValue = columnName.split("_")[1];
          boolean isId = ref.equals(columnEntity.getId());
          if (fieldValue.equalsIgnoreCase("id") && !isId) {
            fieldValue = columnName.split("_")[0] + fieldValue;
          }
          PsiField field = elementFactory
              .createFieldFromText("/**\n* " + columnEntity.getName() + "\n */\n"
                  + "private " + fieldTypeValue + " " + fieldValue + ";", aClass);
          // 创建字段 所有的PsiElement创建后都可以获得其ModifierList，用于设置其修饰符
    /*    elementFactory
              .createField(fieldValue, PsiType
                  .getTypeByName(fieldTypePk, project,
                      GlobalSearchScope.allScope(aClass.getProject())));*/
          //为Field创建注解
          PsiModifierList fieldModifierList = field.getModifierList();
          if (null != fieldModifierList) {
            fieldModifierList.setModifierProperty(PsiModifier.PRIVATE, true);
            PsiAnnotation columnAnn = elementFactory
                .createAnnotationFromText("@Column(name = \"" + columnName.toLowerCase() + "\")", field);
            fieldModifierList.addAfter(columnAnn, null);
            if (isId) {
              PsiAnnotation idAnn = elementFactory
                  .createAnnotationFromText("@Id", field);
              PsiAnnotation denericGenerator = elementFactory
                  .createAnnotationFromText(
                      "@GenericGenerator(name = \"salepayment-uuid\", strategy = \"uuid\")", field);
              PsiAnnotation deneratedValue = elementFactory
                  .createAnnotationFromText("@GeneratedValue(generator = \"salepayment-uuid\")",
                      field);
              fieldModifierList.addAfter(deneratedValue, null);
              fieldModifierList.addAfter(denericGenerator, null);
              fieldModifierList.addAfter(idAnn, null);
            }
          }
          if (null != clsModifierList) {
            aClass.add(field);
          }
        });
        if (directory != null) {
          directory.add(aClass);
        }
        dispose();
      }));
    });

  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

}
