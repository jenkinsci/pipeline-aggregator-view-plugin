package com.ooyala.jenkins.plugins.pipelineaggregatorview;




import static org.mockito.Mockito.when;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by paul on 2017-03-29.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WorkflowRun.class,WorkflowJob.class})
public class PipelineAggregatorTest {


   @Test
   public void filterJobsWithOneBuild(){
      List<WorkflowJob> jobList = new ArrayList();
      WorkflowJob job = Mockito.mock(WorkflowJob.class);
      jobList.add(job);
      WorkflowRun run = Mockito.mock(WorkflowRun.class);
      when(run.getFullDisplayName()).thenReturn("test");
      when(job.getLastBuild()).thenReturn(run);
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = Pattern.compile("test");
      List filteredList = pipelineAggregator.filterJobs(jobList,p);
      Assert.assertEquals(filteredList.size(),1);
   }

   @Test
   public void filterJobsWithNoBuilds(){
      List<WorkflowJob> jobList = new ArrayList();
      WorkflowJob job = new WorkflowJob(null,"name");
      jobList.add(job);
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = Pattern.compile("test");
      List filteredList = pipelineAggregator.filterJobs(jobList,p);
      Assert.assertEquals(filteredList.size(),0);
   }

   @Test
   public void filterWithNoRegex(){
      List<WorkflowJob> jobList = new ArrayList();
      WorkflowJob job = Mockito.mock(WorkflowJob.class);
      WorkflowJob job1 = Mockito.mock(WorkflowJob.class);
      jobList.add(job);
      jobList.add(job1);
      WorkflowRun run = Mockito.mock(WorkflowRun.class);
      when(run.getFullDisplayName()).thenReturn("test");
      WorkflowRun run1 = Mockito.mock(WorkflowRun.class);
      when(run1.getFullDisplayName()).thenReturn("the job");
      when(job1.getLastBuild()).thenReturn(run1);
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = null;
      List filteredList = pipelineAggregator.filterJobs(jobList,p);
      Assert.assertEquals(filteredList.size(),2);

   }

}
