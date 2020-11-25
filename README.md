# dataflow-production-ready
##Introduction
This repo aims to provide a reference implementation for Google Dataflow best practices around multiple topics through 
sample data pipelines. These samples don't focus on complex transformations or specific business logic, but rather the
on the scaffolding around data pipelines in terms of: 
* Unit testing
* Integration testing
* Infrastructure automation
* Deployment automation

The repo uses many concepts as explained in the Google Cloud blog series 
[Building production-ready data pipelines using Dataflow](https://cloud.google.com/solutions/building-production-ready-data-pipelines-using-dataflow-overview)  
##Best Practices
###Developing and testing data pipelines
Reference: [Building production-ready data pipelines using Dataflow: Developing and testing data pipelines](https://cloud.google.com/solutions/building-production-ready-data-pipelines-using-dataflow-developing-and-testing)

####Structuring Pipeline Transforms
Use PTransform to split the pipeline into main components that could be tested in isolation
for different levels of testing (e.g. unit tests, transform integration tests, system integration tests). These
components are Extract, Transform and Load. Each of them can further contain multiple steps composed of smaller
building blocks such as DoFn's. 

For example, [XmlPipelineWithDeadLetter](src/main/pipeline/XmlPipelineWithDeadLetter.java) contains the following
PTransforms as main building blocks:
* PipelineDataReader: encapsulates the input XML parsing logic.
* PipelineDataTransformer: encapsulates all transformation steps before writing the output to sinks.

Note that writing results to sinks are part of the run() function of the pipeline

####Unit Testing
[XmlPipelineWithDeadLetterUnitTest](src/test/pipeline/XmlPipelineWithDeadLetterUnitTest.java)
####Integration Testing
#####Transform integration testing
[XmlPipelineWithDeadLetterTransformIntegrationTest](src/test/pipeline/XmlPipelineWithDeadLetterTransformIntegrationTest.java)
#####System integration testing
###Deploying data pipelines
Reference: [Building production-ready data pipelines using Dataflow: Deploying data pipelines](https://cloud.google.com/solutions/building-production-ready-data-pipelines-using-dataflow-deploying)
###Monitoring data pipelines
Reference: [Building production-ready data pipelines using Dataflow: Monitoring data pipelines](https://cloud.google.com/solutions/building-production-ready-data-pipelines-using-dataflow-monitoring)