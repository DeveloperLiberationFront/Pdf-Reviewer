# Milestone: DEPLOYMENT

We configured an OpenShift instance using the following script:
```
rhc app-create pdfreviewcanary tomcat7
rhc cartridge add mongodb-2.4 -a pdfreviewcanary
rhc env set GITHUB_API=123456 GITHUB_ID=123456 PICASSA_PASSWORD=123456 PICASSA_PDF_ALBUM_ID=123456 PICASSA_USER=123456 -a pdfreviewcanary
rhc add-cartridge -a pdfreviewcanary -e OPENSHIFT_NEWRELIC_LICENSE_KEY=123456 -c https://raw.github.com/kjlubick/openshift-cartridge-newrelic-agent/v3.14/metadata/manifest.yml
rhc app restart pdfreviewcanary

(Obviously, the API keys should be substitued in for their real values)
```

We deploy to that OpenShift instance automatically by chaining onto the end of our build pipeline:

Travis Build + Deploy:
![image](https://cloud.githubusercontent.com/assets/5032534/7123213/a2364db4-e1ee-11e4-9ec4-52c0df8c8bfd.png)

Open Shift:
![image](https://cloud.githubusercontent.com/assets/5032534/7123031/564c3e14-e1ed-11e4-8d22-48aa79489f07.png)


We have created a canary canary environment that duplicates our normal deployment environment and configured Travis.ci to automatically [deploy](http://docs.travis-ci.com/user/deployment/openshift/) all code that passes unit tests to the canary environment.

```
deploy:
  provider: openshift
  user: $OPENSHIFT_USER
  password: $OPENSHIFT_PASS
  domain: ncsudlf
  app: pdfreviewcanary

```

We have configured New Relic to monitor several application metrics including error rate and uptime.
![image](https://cloud.githubusercontent.com/assets/5032534/7122951/c8214a44-e1ec-11e4-9c9a-13e713d9a0b5.png)

We tested this by deploying [some erroneous code](https://github.com/DeveloperLiberationFront/Pdf-Reviewer/commit/1a468c0adad12ee49e29edc1d934221411edc0c9). 
New Relic detected these errors...

![image](https://cloud.githubusercontent.com/assets/5032534/7122938/afe09746-e1ec-11e4-8b80-d9806e33ec56.png)

... and sent us an email alert!

![image](https://cloud.githubusercontent.com/assets/5032534/7122967/e538ab90-e1ec-11e4-8214-93e98afd2504.png)

