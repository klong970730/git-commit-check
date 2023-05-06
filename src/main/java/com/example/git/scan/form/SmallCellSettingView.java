package com.example.git.scan.form;

import com.example.git.scan.SmallCellSettingConfig;

import javax.swing.*;

/**
 * @author klong
 */
public class SmallCellSettingView {
  private JCheckBox analyseDiffLinesBeforeCheckBox;

  private JCheckBox analyseCommitMessageCheckBox;

  /**
   * max commit lines once
   */
  private JSpinner commitLinesMax;
  /**
   * commit message least length
   */
  private JSpinner commitMessageLengthMinimum;
  private JPanel mainPanel;
  private JTextField commitMessageRegrex;
  private JSpinner commitMessageRegrexGroup;
  private JCheckBox disableAlertForeverIfCheckBox;

  public SmallCellSettingView(SmallCellSettingConfig smallCellSettingConfig) {
    init(smallCellSettingConfig);
  }

  public void init(SmallCellSettingConfig smallCellSettingConfig) {
    analyseDiffLinesBeforeCheckBox.setSelected(smallCellSettingConfig.isAnalyseCommitLines());
    analyseCommitMessageCheckBox.setSelected(smallCellSettingConfig.isAnalyseCommitMessage());
    commitLinesMax.setValue(smallCellSettingConfig.getMaxCommitLines());
    commitMessageLengthMinimum.setValue(smallCellSettingConfig.getMaxCommitMessageLength());
    commitMessageRegrex.setText(smallCellSettingConfig.getRegrex());
    commitMessageRegrexGroup.setValue(smallCellSettingConfig.getGroupIndex());
    disableAlertForeverIfCheckBox.setSelected(smallCellSettingConfig.isDisableAlertForever());
  }


  public SmallCellSettingView(){}

  public JComponent getMainPanel() {
    return mainPanel;
  }

  public Boolean disableAlertForeverIfCheckBox() {
    return disableAlertForeverIfCheckBox.isSelected();
  }

  public Boolean analyseDiffLinesBeforeCheckBox() {
    return analyseDiffLinesBeforeCheckBox.isSelected();
  }

  public Boolean analyseCommitMessageCheckBox() {
    return analyseCommitMessageCheckBox.isSelected();
  }

  public Integer commitLinesMax() {
    return Integer.parseInt(commitLinesMax.getValue().toString());
  }

  public Integer commitMessageLengthMinimum() {
    return Integer.parseInt(commitMessageLengthMinimum.getValue().toString());
  }
  public String commitMessageRegrex() {
    return commitMessageRegrex.getText();
  }
  public Integer commitMessageRegrexGroup() {
    return Integer.parseInt(commitMessageRegrexGroup.getValue().toString());
  }
}
