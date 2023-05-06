package com.example.git.scan;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.util.Objects;

/**
 * @author klong
 */
public class BalloonNotifications {
  /**
   * auto close after 10s
   */
  public static String displayId = "SmallCL Intellij IDEA Balloon Notification";
  public static String stickyBalloonDisplayId = "SmallCL Intellij IDEA Notification";
  public static String TITLE = "SmallCL Intellij IDEA Plugin";

  public static void showNotification(String message, Project project, String title,
                                      NotificationType notificationType,
                                      boolean sticky, AnAction... actions) {
    Project p = getDefault(project, ProjectManager.getInstance().getDefaultProject());
    NotificationType n = getDefault(notificationType, NotificationType.INFORMATION);
    String t = getDefault(title, TITLE);
    Notification notification;
    if (sticky) {
      notification = new Notification(displayId, t, message, n);
    } else {
      notification = new Notification(stickyBalloonDisplayId, t, message, n);
    }

    // new Notification(...).addAction(...), can add actions to notification, so user can disable or do something with notify
    if (Objects.nonNull(actions)) {
      for (AnAction action : actions) {
        notification.addAction(action);
      }
    }

    Notifications.Bus.notify(notification, p);

  }

  public static void showSuccessNotification(String message, Project project, String title) {
    showNotification(message, project, title, NotificationType.INFORMATION, false);
  }

  public static void showErrorNotification(String message, Project project, String title) {
    showNotification(message, project, title, NotificationType.ERROR, true);
  }

  public static void showWarnNotification(String message, Project project, String title, AnAction... action) {
    showNotification(message, project, title, NotificationType.WARNING, true, action);
  }

  private static <T> T getDefault(T nullable, T def) {
    return nullable == null ? def : nullable;
  }
}
