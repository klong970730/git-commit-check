package com.example.git.scan;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;

/**
 * @author klong
 */
public class GitCommandRunner {
  private final VirtualFile vcsRoot;

  private final CheckinProjectPanel panel;

  private String resetPoint;

  private final Git git;

  public GitCommandRunner(VirtualFile vcsRoot, CheckinProjectPanel panel) {
    this.vcsRoot = vcsRoot;
    this.panel = panel;
    this.git = Git.getInstance();
  }

  public VirtualFile getVcsRoot() {
    return vcsRoot;
  }


  public List<String> getDiffNumStatOutPut(@NotNull ProgressIndicator indicator) {

    // git commit -m "plugin-commit" -o [files...]
    indicator.setText2("Commit");
    commit();

    // git diff HEAD~1 HEAD~0 --numstat
    indicator.setText2("Diff");
    List<String> diffResult = diff();

    // git reset --soft [commit id]
    indicator.setText2("Reset");
    reset();

    // 10 10  file.txt : insert  deleted  filename
    return diffResult;
  }

  public void revParse() {
    // git rev-parse HEAD
    GitLineHandler revParse = new GitLineHandler(panel.getProject(), vcsRoot, GitCommand.REV_PARSE);
    revParse.addParameters("HEAD");
    GitCommandResult revParsed = git.runCommand(revParse);

    // save HEAD commit id, if some error occurred during plugin commit background, workspace can reset soft to this revision.
    resetPoint = revParsed.getOutputAsJoinedString();
  }

  private void commit() {
    ProjectLevelVcsManager manager = ProjectLevelVcsManager.getInstance(panel.getProject());
    GitLineHandler commit = new GitLineHandler(panel.getProject(), vcsRoot, GitCommand.COMMIT);
    commit.addParameters("-m");
    commit.addParameters("plugin-commit");
    commit.addParameters("-o");
    for (Change change : panel.getSelectedChanges()) {

      // when change type is Change.Type.DELETED, after revision will be null, use before revision to commit deleted file.
      ContentRevision revision = change.getAfterRevision();
      if (Objects.isNull(revision)) {
        revision = change.getBeforeRevision();
      }

      VcsRoot fileVcsRoot = manager.getVcsRootObjectFor(revision.getFile());
      // only commit files in specific vcs root.
      if (vcsRoot.equals(fileVcsRoot.getPath())) {
        Optional.ofNullable(change.getBeforeRevision()).ifPresent(v-> commit.addParameters(v.getFile().getPath()));
        Optional.ofNullable(change.getAfterRevision()).ifPresent(v-> commit.addParameters(v.getFile().getPath()));
      }
    }
    git.runCommand(commit);
  }


  private List<String> diff() {
    GitLineHandler diff = new GitLineHandler(panel.getProject(), vcsRoot, GitCommand.DIFF);
    diff.addParameters("HEAD~1");
    diff.addParameters("HEAD~0");
    diff.addParameters("--numstat");
    return git.runCommand(diff).getOutput();
  }

  public void reset() {
    if (Objects.nonNull(resetPoint)) {
      GitLineHandler reset = new GitLineHandler(panel.getProject(), vcsRoot, GitCommand.RESET);
      reset.addParameters("--soft");
      reset.addParameters(resetPoint);
      git.runCommand(reset);
    }
  }
}
