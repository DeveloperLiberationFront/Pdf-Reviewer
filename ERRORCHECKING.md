# Milestone: DEPLOYMENT

### Properties

Your pipeline should support the following properties.

* The ability to configure a deployment environment *automatically*, using a configuration management tool, such as ansible, or configured using vagrant/docker.

* The ability to deploy a *self-contained/built* application to the deployment environment.  That is, this action should occur after a build step in your pipeline.

* The deployment must occur on an actual remote machine/VM (e.g. AWS, droplet, VCL), and not a local VM.

* The ability to perform a canary release.

* The ability to monitor the deployed application for alerts/failures (using at least 2 metrics).

## Evalution

### Automatic deployment environment configuration: 20%
### Deployment of binaries created by build step: 20%
### Remote deployment: 20%
### Canary releasing: 20%
### Canary analysis: 20%

![image](https://cloud.githubusercontent.com/assets/5032534/7122938/afe09746-e1ec-11e4-8b80-d9806e33ec56.png)
![image](https://cloud.githubusercontent.com/assets/5032534/7122951/c8214a44-e1ec-11e4-9c9a-13e713d9a0b5.png)
![image](https://cloud.githubusercontent.com/assets/5032534/7122967/e538ab90-e1ec-11e4-8214-93e98afd2504.png)

