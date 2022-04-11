package com.dmall;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class ProjectsSetupAction extends DumbAwareAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Project project =  anActionEvent.getProject();
        new ProjectUtil().checkPorject(project);
    }
}