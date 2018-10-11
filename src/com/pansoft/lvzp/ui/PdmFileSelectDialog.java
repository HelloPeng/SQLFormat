package com.pansoft.lvzp.ui;

import static com.pansoft.lvzp.utils.PDMUtils.buildConfig;
import static com.pansoft.lvzp.utils.PDMUtils.getTablesElement;

import com.google.gson.Gson;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
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
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.pansoft.lvzp.AnnType;
import com.pansoft.lvzp.AnnValue;
import com.pansoft.lvzp.entity.ColumnEntity;
import com.pansoft.lvzp.entity.PDMConfigEntity;
import com.pansoft.lvzp.entity.PansoftFieldEntity;
import com.pansoft.lvzp.entity.TableEntity;
import com.pansoft.lvzp.utils.PDMUtils;
import com.pansoft.lvzp.utils.PdmXmlParserUtils;
import cucumber.api.java.cs.A;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

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
  private List<PansoftFieldEntity> pansoftFieldEntifies;

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
      pansoftFieldEntifies = null;
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
      pdmFileTableEntityList.forEach(it -> it.forEach(clsIt -> {
        String tableName = clsIt.getCode();
        //表名被存在code中，表名的创建规则为“简称_名称”,创建实体类时，需要把简称去除
        String clsName = tableName.replace("_", "");
        System.out.println("当前需要创建的文件为 = " + clsName);
        if (directory != null) {
          PsiFile currentFile = directory.findFile(clsName + ".java");
          //获取该类的主键列的ID
          String ref = clsIt.getKeys().get(0).getKeyColumnsEntryList().get(0).getRef();
          //Todo 2018年10月5日 在文件存在的情况下，需要考虑按照pdm的格式排序成员变量
          //判断当前的文件是否不为空，如果不为空需要对Java文件的属性重构
          if (currentFile != null) {
            //通过PsiFile获取对应的PsiClass对象
            PsiClass aClass = PsiTreeUtil.findChildOfAnyType(currentFile, PsiClass.class);
            if (aClass != null) {
              if (isPsiAnnEmpty(aClass, AnnType.DATA)) {
                addAnnBindPsi(elementFactory, aClass, AnnType.DATA);
              }
              if (isPsiAnnEmpty(aClass, AnnType.ENTITY)) {
                addAnnBindPsi(elementFactory, aClass, AnnType.ENTITY);
              }
              if (isPsiAnnEmpty(aClass, AnnType.TABLE)) {
                addAnnBindPsi(elementFactory, aClass, AnnType.TABLE,
                    new AnnValue("name", tableName));
              }
              List<ColumnEntity> columnEntitieList = new ArrayList<>(clsIt.getColumns());
              PsiField[] allFields = aClass.getAllFields();
              if (allFields.length <= 0) {
                createNewField(elementFactory, clsIt, ref, aClass);
              } else {
                Arrays.stream(allFields).forEach(psiFieldIt -> {
                  System.out.println("当前要检测的成员变量名称为：" + psiFieldIt.getName());
                  //判断该成员变量是否有列的注解
                  if (!isPsiAnnEmpty(psiFieldIt, AnnType.COLUMN)) {
                    ColumnEntity columnEntity = findFieldByClass(columnEntitieList, psiFieldIt);
                    //判断目前的PDM中是否有该列的存在
                    if (columnEntity != null) {
                      //如果该列存在，判断该成员变量的注解属性是否齐全
                      //根据列的ID判断该字段是否为主键
                      if (ref.equals(columnEntity.getId())) {
                        //判断是否有GenericGenerator注解
                        if (isPsiAnnEmpty(psiFieldIt, AnnType.GENERICGENERATOR)) {
                          addAnnBindPsi(elementFactory, psiFieldIt, AnnType.GENERICGENERATOR,
                              new AnnValue("name", "salepayment-uuid")
                              , new AnnValue("strategy", "uuid"));
                        }
                        //判断是否有GeneratedValue注解
                        if (isPsiAnnEmpty(psiFieldIt, AnnType.GENERATEDVALUE)) {
                          addAnnBindPsi(elementFactory, psiFieldIt, AnnType.GENERATEDVALUE,
                              new AnnValue("generator", "salepayment-uuid"));
                        }
                        //判断是否有Id注解
                        if (isPsiAnnEmpty(psiFieldIt, AnnType.ID)) {
                          addAnnBindPsi(elementFactory, psiFieldIt, AnnType.ID);
                        }
                      }
                      columnEntitieList.remove(columnEntity);
                    } else {
                      psiFieldIt.delete();
                    }
                  }
                });
                columnEntitieList.forEach(
                    columnEntity -> createNewFieldByOne(elementFactory, ref, aClass,
                        columnEntity));
              }
              formatJavCode(aClass);
            } else {
              Toast.make(project, MessageType.ERROR, "当前文件不是java类，无法执行");
            }
          } else {
            // 创建类
            PsiClass aClass = elementFactory
                .createClass(clsName);

            //为类创建注解
            addAnnBindPsi(elementFactory, aClass, AnnType.TABLE, new AnnValue("name", tableName));
            addAnnBindPsi(elementFactory, aClass, AnnType.ENTITY);
            addAnnBindPsi(elementFactory, aClass, AnnType.DATA);
            //创建新的成员变量
            createNewField(elementFactory, clsIt, ref, aClass);
            formatJavCode(aClass);
            directory.add(aClass);
          }
        }
        dispose();
      }));
    });

  }

  private ColumnEntity findFieldByClass(List<ColumnEntity> columnEntitieList, PsiField psiFieldIt) {
    for (int i = 0; i < columnEntitieList.size(); i++) {
      ColumnEntity columnEntity = columnEntitieList.get(i);
      //获取列的名字，因为表结构的列就一个参数，先这样写死处理
      /*getPsiAnn(psiFieldIt, AnnType.COLUMN)*/
      String columnValue = psiFieldIt.getAnnotation(AnnType.COLUMN.getClassName())
          .getAttributes().get(0)
          .getAttributeValue().getSourceElement().getText().replaceAll("\"", "");
      String pdmColumnCode = columnEntity.getCode().toLowerCase();
      if (pdmColumnCode.equals(columnValue)) {
        return columnEntity;
      }
    }
    return null;
  }

  protected void formatJavCode(PsiClass cls) {
    if (cls == null) {
      return;
    }
    JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(cls.getProject());
    styleManager.optimizeImports(cls.getContainingFile());
    styleManager.shortenClassReferences(cls);
  }

  private void createNewField(PsiElementFactory elementFactory,
      TableEntity clsIt, String ref,
      PsiClass aClass) {
    //循环判断该类的列
    clsIt.getColumns().forEach(
        columnEntity -> createNewFieldByOne(elementFactory, ref, aClass,
            columnEntity));
  }

  private void createNewFieldByOne(PsiElementFactory elementFactory,
      String ref, PsiClass aClass,
      ColumnEntity columnEntity) {
    //根据列的ID判断该字段是否为主键
    boolean isId = ref.equals(columnEntity.getId());
    //创建该类的成员变量
    PsiField field = createPsiField(elementFactory, pansoftFieldEntifies, aClass,
        columnEntity, isId);
    //为Field创建注解
    addAnnBindPsi(elementFactory, field, AnnType.COLUMN,
        new AnnValue("name", columnEntity.getCode()));
    //判断该变量是否为主键
    if (isId) {
      addAnnBindPsi(elementFactory, field, AnnType.GENERICGENERATOR,
          new AnnValue("name", "salepayment-uuid")
          , new AnnValue("strategy", "uuid"));
      addAnnBindPsi(elementFactory, field, AnnType.GENERATEDVALUE,
          new AnnValue("generator", "salepayment-uuid"));
      addAnnBindPsi(elementFactory, field, AnnType.ID);
    }
    aClass.addBefore(field, null);
  }

  @NotNull
  private PsiField createPsiField(PsiElementFactory elementFactory,
      List<PansoftFieldEntity> finalPansoftFieldEntityList, PsiClass aClass,
      ColumnEntity columnEntity, boolean isId) {
    String fieldType = getFieldType(columnEntity, finalPansoftFieldEntityList);
    String fieldName = getFieldName(columnEntity.getCode(), isId);
    StringBuilder sb = new StringBuilder();
    sb.append("/**\n* ").append(columnEntity.getName());
    if (!StringUtil.isEmpty(columnEntity.getComment())) {
      sb.append("   ").append(columnEntity.getComment());
    }
    fieldType = fieldType == null ? "void" : fieldType;
    sb.append("\n */\n");
    sb.append("private")
        .append(" ")
        .append(fieldType)
        .append(" ")
        .append(fieldName)
        .append(";");
    //创建Field对象
    return elementFactory.createFieldFromText(sb.toString(), aClass);
  }

  private String getFieldName(String columnName, boolean isId) {
    String[] columns = columnName.split("_");
    String fieldName = columns[1];
    if (isId) {
      fieldName = "id";
    } else {
      fieldName = columns[0].toLowerCase() + fieldName;
    }
    return fieldName;
  }

  private String getFieldType(ColumnEntity columnEntity, List<PansoftFieldEntity> fieldEntityList) {
    for (PansoftFieldEntity fieldEntity : fieldEntityList) {
      if (columnEntity.getDataType().contains(fieldEntity.getName())) {
        return fieldEntity.getPackages();
      }
    }
    return null;
  }

  private boolean isPsiAnnEmpty(PsiModifierListOwner psiTag, AnnType annType) {
    return psiTag.getAnnotation(annType.getClassName()) == null;
  }

  private void addAnnBindPsi(PsiElementFactory factory, PsiModifierListOwner owner,
      AnnType annType,
      AnnValue... annValues) {
    PsiModifierList modifierList = owner.getModifierList();
    if (modifierList == null) {
      return;
    }
    StringBuilder sb = new StringBuilder("@" + annType.getClassName());
    if (annValues != null && annValues.length > 0) {
      sb.append("(");
      int length = annValues.length;
      for (int i = 0; i < length; i++) {
        AnnValue annValue = annValues[i];
        sb.append(annValue.getName())
            .append(" = \"")
            .append(annValue.getValue().toLowerCase())
            .append("\"");
        if (i + 1 < length) {
          sb.append(", ");
        }
      }
      sb.append(")");
    }
    PsiAnnotation columnAnn = factory
        .createAnnotationFromText(sb.toString(), owner);
    modifierList.addAfter(columnAnn, null);
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

}
