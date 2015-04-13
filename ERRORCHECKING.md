# Milestone: DEPLOYMENT

### Properties

Your pipeline should support the following properties.

* The ability to configure a deployment environment *automatically*, using a configuration management tool, such as ansible, or configured using vagrant/docker.

* The ability to deploy a *self-contained/built* application to the deployment environment.  That is, this action should occur after a build step in your pipeline.

* The deployment must occur on an actual remote machine/VM (e.g. AWS, droplet, VCL), and not a local VM.

* The ability to perform a canary release.

* The ability to monitor the deployed application for alerts/failures (using at least 2 metrics).

## Evalution


We deploy to OpenShift by chaining onto the end of our build pipeline:
//Travis deploy
![image](https://cloud.githubusercontent.com/assets/5032534/7123213/a2364db4-e1ee-11e4-9ec4-52c0df8c8bfd.png)

//Open Shift
![image](https://cloud.githubusercontent.com/assets/5032534/7123031/564c3e14-e1ed-11e4-8d22-48aa79489f07.png)

We have created a canary canary environment that duplicates our normal deployment environment and configured Travis.ci to automatically deploy all code that passes unit tests to the canary environment.

```
TRAVIS CI CODE
```

We have configured New Relic to monitor several application metrics including error rate and uptime.
![image](https://cloud.githubusercontent.com/assets/5032534/7122951/c8214a44-e1ec-11e4-9c9a-13e713d9a0b5.png)

We tested this by deploying [some erroneous code](https://github.com/DeveloperLiberationFront/Pdf-Reviewer/commit/1a468c0adad12ee49e29edc1d934221411edc0c9). 
New Relic detected these errors...
![image](https://cloud.githubusercontent.com/assets/5032534/7122938/afe09746-e1ec-11e4-8b80-d9806e33ec56.png)

... and sent us an email alert!
![image](https://cloud.githubusercontent.com/assets/5032534/7122967/e538ab90-e1ec-11e4-8214-93e98afd2504.png)

