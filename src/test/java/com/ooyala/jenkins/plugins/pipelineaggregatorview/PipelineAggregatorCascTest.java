package com.ooyala.jenkins.plugins.pipelineaggregatorview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import java.util.List;
import org.junit.jupiter.api.Test;

@WithJenkinsConfiguredWithCode
class PipelineAggregatorCascTest {
  @Test
  @ConfiguredWithCode("/com/ooyala/jenkins/plugins/pipelineaggregatorview/casc.yaml")
  void testCasc(JenkinsConfiguredWithCodeRule rule) throws Exception {
    PipelineAggregator view = (PipelineAggregator) rule.jenkins.getView("Pipeline Aggregator");
    assertNotNull(view);

    assertEquals(20, view.getFontSize());
    assertEquals(20, view.getBuildHistorySize());
    assertEquals(30, view.getRefreshInterval());
    assertTrue(view.isUseCondensedTables());
    assertTrue(view.isOnlyLastBuild());
    assertTrue(view.isUseScrollingCommits());
    assertEquals(".*Aggregator.*", view.getFilterRegex());
    assertFalse(view.isShowCommitInfo());
    assertFalse(view.isShowBuildNumber());
    assertFalse(view.isShowBuildTime());
    assertFalse(view.isShowBuildDuration());

    String exportedYaml = rule.exportToString(true);
    List<String> expectedLines = List.of(
        ">> other unrelated configuration >>",
        "  views:",
        "  - pipelineAggregator:",
        "      buildHistorySize: 20",
        "      filterRegex: \".*Aggregator.*\"",
        "      fontSize: 20",
        "      name: \"Pipeline Aggregator\"",
        "      onlyLastBuild: true",
        "      refreshInterval: 30",
        "      showBuildDuration: false",
        "      showBuildNumber: false",
        "      showBuildTime: false",
        "      showCommitInfo: false",
        "      useCondensedTables: true",
        "      useScrollingCommits: true",
        ">> other unrelated configuration >>");
    assertLinesMatch(expectedLines, exportedYaml.lines().toList());
  }
}
