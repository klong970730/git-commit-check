package com.example.git.scan;

import com.intellij.openapi.application.ApplicationManager;
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
public class SmallCellSettingConfig implements PersistentStateComponent<SmallCellSettingConfig> {
  Boolean analyseCommitLines = true;
  Integer maxCommitLines = 200;

  Boolean analyseCommitMessage = true;

  Integer maxCommitMessageLength = 10;

  String regrex = "(.*)(]-|] -|~|])(.*)";

  Integer groupIndex = 3;

  public Integer getGroupIndex() {
    return groupIndex;
  }

  public void setGroupIndex(Integer groupIndex) {
    this.groupIndex = groupIndex;
  }

  public String getRegrex() {
    return regrex;
  }
  public void setRegrex(String regrex) {
    this.regrex = regrex;
  }

  public boolean isAnalyseCommitMessage() {
    return analyseCommitMessage;
  }

  public void setAnalyseCommitMessage(boolean analyseCommitMessage) {
    this.analyseCommitMessage = analyseCommitMessage;
  }

  public Integer getMaxCommitMessageLength() {
    return maxCommitMessageLength;
  }

  public void setMaxCommitMessageLength(Integer maxCommitMessageLength) {
    this.maxCommitMessageLength = maxCommitMessageLength;
  }

  public Integer getMaxCommitLines() {
    return maxCommitLines;
  }

  public void setMaxCommitLines(Integer maxCommitLines) {
    this.maxCommitLines = maxCommitLines;
  }


  public boolean isAnalyseCommitLines() {
    return analyseCommitLines;
  }

  public void setAnalyseCommitLines(boolean analyseCommitLines) {
    this.analyseCommitLines = analyseCommitLines;
  }

  public static SmallCellSettingConfig getConfig() {
    return ApplicationManager.getApplication().getService(SmallCellSettingConfig.class).getState();
  }


  @Override
  public @Nullable SmallCellSettingConfig getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull SmallCellSettingConfig state) {
    XmlSerializerUtil.copyBean(state, this);
  }


}
