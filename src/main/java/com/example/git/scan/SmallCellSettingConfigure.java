package com.example.git.scan;

import com.example.git.scan.form.SmallCellSettingView;
import com.intellij.openapi.options.SearchableConfigurable;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import javax.swing.*;

/**
 * @author klong
 */
public class SmallCellSettingConfigure implements SearchableConfigurable {

  private SmallCellSettingView view;

  private final SmallCellSettingConfig config;

  /**
   * project级别可以直接通过构造方法把project对象注入，该构造方法只有在项目中打开setting页面时，才会调用
   */
  public SmallCellSettingConfigure() {
    this.config = SmallCellSettingConfig.getConfig();
  }

  @Override
  public @NotNull @NonNls String getId() {
    return "Small Cell Setting";
  }



  @Override
  public String getDisplayName() {
    return getId();
  }

  @Override
  public @Nullable JComponent createComponent() {
    if (Objects.isNull(view)) {
      // 生成含默认值的panel
      this.view = new SmallCellSettingView(config);
    }
    return view.getMainPanel();
  }

  @Override
  public boolean isModified() {
    return view.analyseDiffLinesBeforeCheckBox() != config.isAnalyseCommitLines()
            || view.analyseCommitMessageCheckBox() != config.isAnalyseCommitMessage()
            || view.commitLinesMax().compareTo(config.getMaxCommitLines()) != 0
            || view.commitMessageLengthMinimum().compareTo(config.getMaxCommitMessageLength()) != 0
            || !view.commitMessageRegrex().equals(config.getRegrex())
            || view.commitMessageRegrexGroup().compareTo(config.getGroupIndex()) != 0;
  }

  @Override
  public void apply() {
    config.setMaxCommitLines(view.commitLinesMax());
    config.setMaxCommitMessageLength(view.commitMessageLengthMinimum());
    config.setAnalyseCommitLines(view.analyseDiffLinesBeforeCheckBox());
    config.setAnalyseCommitMessage(view.analyseCommitMessageCheckBox());
    config.setRegrex(view.commitMessageRegrex());
    config.setGroupIndex(view.commitMessageRegrexGroup());
  }
}
