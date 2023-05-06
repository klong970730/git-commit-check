package com.example.git.scan;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.util.PairConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.git.scan.SmallCellSettingConfig.getConfig;

/**
 * @author klong
 */
public class CommitMessageCheckinHandler extends CheckinHandler {

  private Boolean disableOnceRuntime = false;
  private final CheckinProjectPanel myPanel;
  private final SmallCellSettingConfig config;

  private String notifyMessage;

  public CommitMessageCheckinHandler(CheckinProjectPanel myPanel) {
    this.myPanel = myPanel;
    this.config = getConfig();
  }

  @Override
  public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
    if (Boolean.FALSE.equals(config.isAnalyseCommitMessage()) || isValidate(myPanel.getCommitMessage())) {
      return ReturnResult.COMMIT;
    }

    if (Messages.showOkCancelDialog(myPanel.getProject(),
            notifyMessage + "Commit message length should equals or greater than " + config.getMaxCommitMessageLength(),
            "Commit Message Length Analyse",
            "Commit Anyway",
            "Cancel",
            null) == Messages.OK) {
      return CheckinHandler.ReturnResult.COMMIT;
    } else {
      return ReturnResult.CANCEL;
    }
  }

  private boolean isValidate(String commitMessage) {
    // blank commit message
    if (Objects.isNull(commitMessage) || commitMessage.isBlank()) {
      notifyMessage = "Your commit message is blank.\n";
      return false;
    }

    // no regrex configured
    if (Objects.isNull(config.getRegrex()) || config.getRegrex().isBlank()) {
      notifyMessage = "Your commit message: " + commitMessage.trim() + "\n";
      return commitMessage.trim().length() >= config.getMaxCommitMessageLength();
    }

    // regrex configured
    Pattern compile = Pattern.compile(config.getRegrex());
    Matcher matcher = compile.matcher(commitMessage);
    if (matcher.matches()) {
      String realCommitMessage = matcher.group(config.getGroupIndex());
      if (realCommitMessage != null) {
        notifyMessage = "Your commit message: " + realCommitMessage.trim() + "\n";
        return realCommitMessage.trim().length() >= config.getMaxCommitMessageLength();
      }
    }
    notifyMessage = "Commit message regrex matches nothing!\nYour original commit message:\n\t" + commitMessage.trim() + "\n";
    notifyWithAction();
    return commitMessage.trim().length() >= config.getMaxCommitMessageLength();
  }

  private void notifyWithAction() {
    if (Boolean.TRUE.equals(config.isDisableAlertForever()) || Boolean.TRUE.equals(disableOnceRuntime)) {
      return;
    }
    NotificationAction disableAlertOnce = new NotificationAction("Disable alert in this runtime") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        disableOnceRuntime = Boolean.TRUE;

        notifyCommon("Commit message regrex nomatch warning will not show in this runtime.\n" +
                "Always check original commit message's length when regrex not matched.");
        notification.expire();
      }
    };

    NotificationAction disableAlertForever = new NotificationAction("Disable alert forever") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        config.setDisableAlertForever(Boolean.TRUE);
        notifyCommon("Commit message regrex nomatch warning will not show forever.\n" +
                "Always check original commit message's length when regrex not matched.");
        notification.expire();
      }
    };

    NotificationAction removeRegrex = new NotificationAction("Remove regrex setting") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        config.setRegrex(null);
        notifyCommon("Commit message regrex removed forever, always check original commit message's length.\n" +
                "You can reconfigure it at Small Cell Setting.");
        notification.expire();
      }
    };
    NotificationAction disableCheck = new NotificationAction("Disable commit message check") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        config.setAnalyseCommitMessage(Boolean.FALSE);
        notifyCommon("Commit message length check disabled forever.\nYou can enable it at Small Cell Setting.");
        notification.expire();
      }
    };

    BalloonNotifications.showWarnNotification(notifyMessage +
                    "Please check or remove your regrex setting.",
            myPanel.getProject(), null, disableAlertOnce, disableAlertForever, removeRegrex, disableCheck);
  }

  private void notifyCommon(String message) {
    BalloonNotifications.showSuccessNotification(message, myPanel.getProject(),null);
  }
}
