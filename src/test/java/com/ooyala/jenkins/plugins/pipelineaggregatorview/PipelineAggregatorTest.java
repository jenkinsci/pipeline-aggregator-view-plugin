package com.ooyala.jenkins.plugins.pipelineaggregatorview;




import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by paul on 2017-03-29.
 */
class PipelineAggregatorTest {


   @Test
   void filterJobsWithOneBuild(){
      List<WorkflowJob> jobList = new ArrayList<>();
      WorkflowJob job = mock(WorkflowJob.class);
      jobList.add(job);
      WorkflowRun run = mock(WorkflowRun.class);
      when(run.getFullDisplayName()).thenReturn("test");
      when(job.getLastBuild()).thenReturn(run);
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = Pattern.compile("test");
      List<WorkflowJob> filteredList = pipelineAggregator.filterJobs(jobList,p);
      assertEquals(1, filteredList.size());
   }

   @Test
   void filterJobsWithNoBuilds(){
      List<WorkflowJob> jobList = new ArrayList<>();
      WorkflowJob job = new WorkflowJob(null,"name");
      jobList.add(job);
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = Pattern.compile("test");
      List<WorkflowJob> filteredList = pipelineAggregator.filterJobs(jobList,p);
      assertEquals(0, filteredList.size());
   }

   @Test
   void filterWithNoRegex(){
      List<WorkflowJob> jobList = new ArrayList<>();
      WorkflowJob job = mock(WorkflowJob.class);
      WorkflowJob job1 = mock(WorkflowJob.class);
      jobList.add(job);
      jobList.add(job1);
      WorkflowRun run = mock(WorkflowRun.class);
      when(run.getFullDisplayName()).thenReturn("test");
      WorkflowRun run1 = mock(WorkflowRun.class);
      when(run1.getFullDisplayName()).thenReturn("the job");
      when(job1.getLastBuild()).thenReturn(run1);
      PipelineAggregator pipelineAggregator = new PipelineAggregator("testName","testViewName");
      Pattern p = null;
      List<WorkflowJob> filteredList = pipelineAggregator.filterJobs(jobList,p);
      assertEquals(2, filteredList.size());

   }

}
