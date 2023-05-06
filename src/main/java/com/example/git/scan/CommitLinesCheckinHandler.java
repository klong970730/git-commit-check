package com.example.git.scan;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.PairConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.swing.*;

import static com.example.git.scan.SmallCellSettingConfig.getConfig;

/**
 * @author klong
 */
public class CommitLinesCheckinHandler extends CheckinHandler {

  public static final String MESSAGE = "Commit Lines Checkin is impossible until indices are up-to-date";
  public String dialogTitle = "Commit Lines Analyse";
  private String cancelText = "&Cancel";
  private String commitText = "&Commit Anyway";
  private String waitingText = "Wait";
  protected Project myProject;

  private SmallCellSettingConfig config;
  private CheckinProjectPanel panel;

  private Map<String, List<FileDiff>> gitDiffNumStat = new HashMap<>(8);

  private Long total;

  private static final ArrayList<String> PHRASES = new ArrayList<>(List.of(
          "辛苦了,好棒!",
          "真不错,继续!",
          "你太厉害了!",
          "努力终有回报!",
          "坚持就是胜利!",
          "你值得信赖!",
          "伟大的开始!",
          "你会成功的!",
          "相信自己就好!",
          "机会来了!",
          "天赋异禀!",
          "实至名归!",
          "真棒,加油!",
          "你最棒了!",
          "一步一个脚印!"));


  public CommitLinesCheckinHandler(Project myProject, CheckinProjectPanel panel) {
    this.config = getConfig();
    this.myProject = myProject;
    this.panel = panel;
  }

  @Override
  public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
    if (Boolean.FALSE.equals(config.isAnalyseCommitLines())) {
      return ReturnResult.COMMIT;
    }

    if (DumbService.getInstance(myProject).isDumb()) {
      if (Messages.showOkCancelDialog(myProject, MESSAGE, dialogTitle, waitingText, commitText, null) == Messages.OK) {
        return ReturnResult.CANCEL;
      }
      return ReturnResult.COMMIT;
    }
    boolean hasViolation = hasViolation(myProject);
    if (!hasViolation) {
      BalloonNotifications.showSuccessNotification(randomPhase(total), myProject, "Analyze Finished");
      return CheckinHandler.ReturnResult.COMMIT;
    }

    if (Messages.showOkCancelDialog(myProject,
            "Commit " + total + " lines greater than " + config.getMaxCommitLines() + "!,continue commit?",
            dialogTitle, commitText, cancelText, null) == Messages.OK) {
      return CheckinHandler.ReturnResult.COMMIT;
    } else {
      showDiffStat(myProject);
      return ReturnResult.CANCEL;
    }


  }

  private void showDiffStat(Project myProject) {
    //todo show every file's modify lines with inspection
    BalloonNotifications.showSuccessNotification(prettyNumDiff(), myProject, "Diff Num Stat");
  }

  private String prettyNumDiff() {
    StringJoiner builder = new StringJoiner("\n");
    gitDiffNumStat.forEach((root, diffs) -> {
      builder.add("insert\tdelete\tfile");
      diffs.forEach(diff -> builder.add(diff.toString()));
    });
    return builder.toString();
  }

  private boolean hasViolation(Project myProject) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
      throw new RuntimeException("Must not run under write action");
    }

    gitDiffNumStat.clear();
    getGitDiffNumStat(myProject);
    total = gitDiffNumStat.values().stream().mapToLong(this::calculateChangeLinesNum).sum();
    return total > config.getMaxCommitLines();
  }

  private Long calculateChangeLinesNum(List<FileDiff> v) {
    long commitLines = 0L;
    for (FileDiff fileDiff : v) {
      commitLines += parse(fileDiff.getInsert());
      commitLines += parse(fileDiff.getDelete());
    }
    return commitLines;
  }

  private static long parse(String number) {
    try {
      return Long.parseLong(number);
    } catch (NumberFormatException ignore) {
      return 0L;
    }
  }

  private void getGitDiffNumStat(Project myProject) {

    // check any vcs root exists, although when this panel show, vcs root always exist.
    if (panel.getRoots().isEmpty()) {
      return;
    }

    List<GitCommandRunner> commands = new ArrayList<>();
    for (VirtualFile root : panel.getRoots()) {
      commands.add(new GitCommandRunner(root, panel));
    }

    ProgressManager.getInstance().run(new Task.Modal(myProject, dialogTitle, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        for (GitCommandRunner command : commands) {

          // must not be interrupted, ensure we can reset workspace to here.
          command.revParse();

          try {
            indicator.checkCanceled();
            indicator.setText("Vcs Root: " + command.getVcsRoot().getPath());
            List<String> diffNumStatOutPut = command.getDiffNumStatOutPut(indicator);
            List<FileDiff> fileDiffs = new ArrayList<>(16);
            for (String line : diffNumStatOutPut) {
              String[] split = line.split("\t", 3);
              fileDiffs.add(new FileDiff(split[0], split[1], split[2]));
            }
            gitDiffNumStat.put(command.getVcsRoot().getPath(), fileDiffs);

            // canceled manually, ignore
          } catch (ProcessCanceledException canceledException) {
            gitDiffNumStat.clear();
            command.reset();

          } catch (Exception e) {
            BalloonNotifications.showErrorNotification(command.getVcsRoot().getPath()
                    + ": Vcs Root Analyse Failed\n" + e, myProject, null);
            gitDiffNumStat.clear();
            command.reset();
          }
        }
      }
    });
  }

  @Override
  public @Nullable RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
    return new RefreshableOnComponent() {
      final NonFocusableCheckBox checkBox = new NonFocusableCheckBox(dialogTitle);

      @Override
      public void refresh() {
        //do nothing, just compatible lower idea version
      }

      @Override
      public void saveState() {
        getConfig().setAnalyseCommitLines(checkBox.isSelected());
      }

      @Override
      public void restoreState() {
        checkBox.setSelected(getConfig().isAnalyseCommitLines());
      }

      @Override
      public JComponent getComponent() {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(checkBox);
        boolean dumb = DumbService.isDumb(myProject);
        checkBox.setEnabled(!dumb);
        if (dumb) {
          checkBox.setToolTipText(MESSAGE);
        } else {
          checkBox.setToolTipText("");
        }
        return jPanel;
      }
    };
  }


  private static String randomPhase(Long lines) {
    return "本次提交" + lines + "行,".concat(PHRASES.get((int) (Math.random() * PHRASES.size())));
  }


}
