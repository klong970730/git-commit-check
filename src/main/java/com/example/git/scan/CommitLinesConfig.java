package com.example.git.scan;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author klong
 */
@State(name = "CommitLinesConfig", storages = {@Storage(value = "smallcl/commitlines.xml")})
public class CommitLinesConfig implements PersistentStateComponent<CommitLinesConfig> {
  boolean scanBeforeCommit = false;
  Integer maxCommitLines = 200;


  public Integer getMaxCommitLines() {
    return maxCommitLines;
  }

  public void setMaxCommitLines(Integer maxCommitLines) {
    this.maxCommitLines = maxCommitLines;
  }


  public boolean isScanBeforeCommit() {
    return scanBeforeCommit;
  }

  public void setScanBeforeCommit(boolean scanBeforeCommit) {
    this.scanBeforeCommit = scanBeforeCommit;
  }


  @Override
  public @Nullable CommitLinesConfig getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull CommitLinesConfig state) {
    XmlSerializerUtil.copyBean(state, this);
  }


}
