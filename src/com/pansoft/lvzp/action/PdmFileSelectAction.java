package com.pansoft.lvzp.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.pansoft.lvzp.ui.PdmFileSelectDialog;

import javax.swing.*;
import java.awt.*;

/**
 * 作者：吕振鹏
 * E-mail:lvzhenpeng@pansoft.com
 * 创建时间：2018年05月10日
 * 时间：16:33
 * 版本：v1.0.0
 * 类描述：
 * 修改时间：
 */
public class PdmFileSelectAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PdmFileSelectDialog dialog = new PdmFileSelectDialog(e.getProject());
        dialog.setSize(600, 530);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
