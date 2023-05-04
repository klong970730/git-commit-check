package com.example.git.scan;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

/**
 * @author klong
 */
public class BalloonNotifications {
  /** auto close after 10s */
  public static String displayId = "SmallCL Intellij IDEA Balloon Notification";
  public static String stickyBalloonDisplayId = "SmallCL Intellij IDEA Notification";
  public static String TITLE = "SmallCL Intellij IDEA Plugin";

  public static void showNotification(String message, Project project, String title,
                                      NotificationType notificationType,
                                      boolean sticky) {
    Project p = getDefault(project,ProjectManager.getInstance().getDefaultProject());
    NotificationType n = getDefault(notificationType, NotificationType.INFORMATION);
    String t = getDefault(title, TITLE);
    if (sticky) {
      Notifications.Bus.notify(new Notification(displayId,t,message,n), p);
    } else {
      Notifications.Bus.notify(new Notification(stickyBalloonDisplayId,t,message,n), p);
    }

  }

  public static void showSuccessNotification(String message, Project project, String title) {
    showNotification(message, project, title, NotificationType.INFORMATION, false);
  }

  private static <T> T getDefault(T nullable, T def) {
    return nullable == null ? def : nullable;
  }
}
