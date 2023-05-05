package com.example.git.scan;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.util.PairConsumer;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.git.scan.SmallCellSettingConfig.getConfig;

/**
 * @author klong
 */
public class CommitMessageCheckinHandler extends CheckinHandler {

  private final CheckinProjectPanel myPanel;
  private final SmallCellSettingConfig config;

  private String notifyMessage;

  public CommitMessageCheckinHandler(CheckinProjectPanel myPanel) {
    this.myPanel = myPanel;
    this.config = getConfig();
  }

  @Override
  public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
    if (!getConfig().isAnalyseCommitMessage() || isValidate(myPanel.getCommitMessage())) {
      return ReturnResult.COMMIT;
    }

    if (Messages.showOkCancelDialog(myPanel.getProject(),
            notifyMessage + "Commit Message Length Should Equals Or Greater Than " + config.getMaxCommitMessageLength(),
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
      notifyMessage = "Your Commit Message Is Blank.\n";
      return false;
    }

    // no regrex configured
    if (Objects.isNull(config.getRegrex()) || config.getRegrex().isBlank()) {
      notifyMessage = "Your Commit Message: "+commitMessage.trim()+".\n";
      return commitMessage.trim().length() >= config.getMaxCommitMessageLength();
    }

    // regrex configured
    Pattern compile = Pattern.compile(config.getRegrex());
    Matcher matcher = compile.matcher(commitMessage);
    if (matcher.matches()) {
      String realCommitMessage = matcher.group(config.getGroupIndex());
      if (realCommitMessage != null) {
        notifyMessage = "Your Commit Message: " + realCommitMessage.trim() + "\n";
        return realCommitMessage.trim().length() >= config.getMaxCommitMessageLength();
      }
    }
    notifyMessage = "Commit Message Regrex Matches Noting!\n";
    return false;
  }
}
