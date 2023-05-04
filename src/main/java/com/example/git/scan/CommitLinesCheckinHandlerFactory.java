package com.example.git.scan;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;

import org.jetbrains.annotations.NotNull;


/**
 * @author klong
 */
public class CommitLinesCheckinHandlerFactory extends CheckinHandlerFactory {

  @Override
  public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
    return new CommitLinesCheckinHandler(panel.getProject(), panel);
  }

}