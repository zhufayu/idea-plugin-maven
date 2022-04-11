package com.dmall;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsNotifier;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;

public class ProjectUtil {
    private static final NotificationGroup STICKY_GROUP =
            new NotificationGroup("notificationGroup",
                    NotificationDisplayType.STICKY_BALLOON, true);

    public void sendNotification(String title, String message) {
        Notification msg =
                STICKY_GROUP.createNotification(
                        title, "", message,
                        NotificationType.INFORMATION);
        Notifications.Bus.notify(msg);
    }

    public void sendError(Project project, String title, String message) {
        VcsNotifier.getInstance(project).notifyError(title, message);
    }

    public void checkPorject(Project project) {
        try {
            String OS = System.getProperty("os.name").toLowerCase();
            String command = "";
            if (OS.indexOf("windows") >= 0) {
                command = "git.exe";
            }

            if (OS.indexOf("linux") >= 0 || OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0) {
                command = "git";
            }
            Process p = Runtime.getRuntime().exec(command + " remote -v", null,
                    new File(project.getBasePath()));
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            p.waitFor();
            is.close();
            reader.close();
            p.destroy();

            String result = buffer.toString();
            if (result.length() == 0) {
                sendError(project, "Get Project Git Information Error...", "");
            } else {
                String gitUrl = result.substring(result.indexOf("\t") + 1, result.indexOf("(fetch)") - 1);
                String setupResult = "";
//                    String url = "http://10.12.205.209:8080/qa/getCodeConfig?gitCodeUrl=" + gitUrl;
                String url = "http://midhub.dmall.com/qa/getCodeConfig?gitCodeUrl=" + gitUrl;
                String serverResult = HttpUtils.httpGet(url);
                if (serverResult == null || "".equals(serverResult)) {
                    sendError(project, "Get Error From MidHub...",
                            "Can not get message from " + url);
                }

                String decodeResult = URLDecoder.decode(serverResult, "UTF-8");

                String[] getR = decodeResult.replace("{", "")
                        .replace("}", "").split(",");
                HashMap<String, String> map = new HashMap<>();
                for (String s : getR) {
                    String[] ky = s.split("==");
                    map.put(ky[0], ky[1]);
                }
                MavenGeneralSettings settings = MavenProjectsManager.getInstance(project).getGeneralSettings();

                String userHome = System.getProperty("user.home") + File.separator + ".m2";
                String localRepository = settings.getLocalRepository();
                String localSettingFile = settings.getUserSettingsFile();
                String serverRepository = userHome + File.separator + map.get("repositoriesName");
                String serverSettingFile =userHome + File.separator + map.get("settingName");

                if ("".equals(localRepository) || !serverRepository.equals(localRepository)) {
                    File file = new File(serverRepository);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    settings.setLocalRepository(serverRepository);
                    setupResult = "LocalRepository:" + serverRepository;
                }
                if ("".equals(localSettingFile) || !serverSettingFile.equals(localSettingFile)) {
                    File file = new File(serverSettingFile);
                    if (!file.exists()) {
                        file.createNewFile();
                        String settingsXml = map.get("settings");
                        PrintWriter output = new PrintWriter(file);
                        output.print(settingsXml);
                        output.flush();
                        output.close();
                    }
                    settings.setUserSettingsFile(serverSettingFile);
                    setupResult = setupResult + "<br>" + "SettingsFile:" + serverSettingFile;
                }

                if (!"".equals(setupResult)) {
                    sendNotification("Project Repository Setup Success...", setupResult);
                }
            }
        } catch (Exception e) {
            sendError(project, "Get Error From MidHub When Setup Maven...",
                    e.getMessage());
        }
    }
}
