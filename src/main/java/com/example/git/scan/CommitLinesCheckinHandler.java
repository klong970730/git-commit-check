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
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerUtil;
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

import javax.swing.*;

import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;

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

  private Map<String, String[]> gitDiffNumStat = new HashMap<>(16);

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
    if (!config.isAnalyseCommitLines()) {
      return ReturnResult.COMMIT;
    }
    if (DumbService.getInstance(myProject).isDumb()) {
      if (Messages.showOkCancelDialog(myProject, MESSAGE, dialogTitle, waitingText, commitText, null) == Messages.OK) {
        return ReturnResult.CANCEL;
      }
      return ReturnResult.COMMIT;
    }
    List<VirtualFile> virtualFiles = CheckinHandlerUtil.filterOutGeneratedAndExcludedFiles(panel.getVirtualFiles(), myProject);
    boolean hasViolation = hasViolation(virtualFiles, myProject);
    if (!hasViolation) {
      BalloonNotifications.showSuccessNotification(randomPhase(total), myProject, "Analyze Finished");
      return CheckinHandler.ReturnResult.COMMIT;
    }

    if (Messages.showOkCancelDialog(myProject, "Commit lines greater than " + config.getMaxCommitLines() + "!,continue commit?",
            dialogTitle, commitText, cancelText, null) == Messages.OK) {
      return CheckinHandler.ReturnResult.COMMIT;
    } else {
      doAnalysis(myProject, virtualFiles);
      return ReturnResult.CANCEL;
    }


  }

  private void doAnalysis(Project myProject, List<VirtualFile> virtualFiles) {
    //todo show every file's modify lines
  }

  private boolean hasViolation(List<VirtualFile> virtualFiles, Project myProject) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
      throw new RuntimeException("Must not run under write action");
    }

//    try {
//      DiffApplicationBase.refreshAndEnsureFilesValid(virtualFiles);
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }

    gitDiffNumStat.clear();
    getGitDiffNumStat(virtualFiles, myProject);
    total = gitDiffNumStat.values().stream().mapToLong(this::calculateChangeLinesNum).sum();
    return total > config.getMaxCommitLines();
  }

  private Long calculateChangeLinesNum(String[] v) {
    Long added = parse(v[0]);
    Long deleted = parse(v[1]);
    return added + deleted;
  }

  private static Long parse(String number) {
    try {
      return Long.parseLong(number);
    } catch (NumberFormatException ignore) {
      return 0L;
    }
  }

  private void getGitDiffNumStat(List<VirtualFile> virtualFiles, Project myProject) {
    ProjectLevelVcsManager manager = ProjectLevelVcsManager.getInstance(myProject);
    VirtualFile vcsRoot = null;
    // 本次提交有被版本控制的文件
    if (!manager.hasAnyMappings()) {
      return;
    }
    // 找出任意一个被版本控制的文件, 通过它获取到版本控制的根对象
    for (VirtualFile virtualFile : virtualFiles) {
      if ((vcsRoot = manager.getVcsRootFor(virtualFile)) != null) {
        break;
      }
    }

    GitLineHandler commit = new GitLineHandler(myProject, vcsRoot, GitCommand.COMMIT);
    commit.addParameters("-o");
    for (Change change : panel.getSelectedChanges()) {
      commit.addParameters(change.getAfterRevision().getFile().getPath());
    }
    commit.addParameters("-m");
    commit.addParameters("test-commit");

    GitLineHandler diff = new GitLineHandler(myProject, vcsRoot, GitCommand.DIFF);
    diff.addParameters("HEAD~1");
    diff.addParameters("HEAD~0");
    diff.addParameters("--numstat");

    GitLineHandler reset = new GitLineHandler(myProject, vcsRoot, GitCommand.RESET);
    reset.addParameters("HEAD~1");
    reset.addParameters("--soft");
    ProgressManager.getInstance().run(new Task.Modal(myProject, dialogTitle, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          Git git = Git.getInstance();
          git.runCommand(commit);
          GitCommandResult gitCommandResult = git.runCommand(diff);
          git.runCommand(reset);

          // 10 10  file.txt : insert  deleted  filename
          String output = gitCommandResult.getOutputAsJoinedString();
          String[] lines = output.split("\n");
          for (String line : lines) {
            String[] split = line.split("\t", 3);
            gitDiffNumStat.put(split[2], split);
          }
        } catch (ProcessCanceledException canceledException) {
          gitDiffNumStat.clear();
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
