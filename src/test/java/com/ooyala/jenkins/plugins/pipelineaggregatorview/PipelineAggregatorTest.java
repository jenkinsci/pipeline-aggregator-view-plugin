package com.ooyala.jenkins.plugins.pipelineaggregatorview;




import static org.mockito.Mockito.when;

import hudson.model.AbstractBuild;
import hudson.model.ItemGroup;
import hudson.util.RunList;
import javafx.util.Duration;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by paul on 2017-03-29.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WorkflowRun.class)
public class PipelineAggregatorTest {


   @Test
   public void regexTest()throws Exception{

      List runList = new ArrayList();
      WorkflowRun run = Mockito.mock(WorkflowRun.class);
      runList.add(run);
      when(run.getFullDisplayName()).thenReturn("test");
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = Pattern.compile("test");
      List filteredList = pipelineAggregator.filter(runList,p);
      Assert.assertEquals(filteredList.size(),1);


   }


}
