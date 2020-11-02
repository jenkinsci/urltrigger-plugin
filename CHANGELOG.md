# Changelog

## Release 0.47

Bugfix for the 'inspect content' checkbox being incorrectly set
automatically.

## Release 0.46

Pipeline compatibility.  As well as now supporting pipeline jobs,
declarative syntax for triggers is supported.

## Release 0.45

Bugfix: Triggers configured to check ETag values no longer build
immediately on Jenkins startup

## Release 0.44

Bugfix: Request Header functionality was causing null pointer exceptions
when old config was used (JENKINS-51892)

## Release 0.42

Tidied URL verification and removed unnecessary url GET.

## Release 0.41

* allow to use jsonarray as root of input message

## Release 0.40

* Fix
[JENKINS-28834](https://issues.jenkins-ci.org/browse/JENKINS-28834) -
Declare dependency on matrix project plugin

## Release 0.39

* Fix
[JENKINS-29610](https://issues.jenkins-ci.org/browse/JENKINS-29610) -
Scheduling skipped when there is no label restriction (trigger-lib
0.33)  
* Fix
[JENKINS-20712](https://issues.jenkins-ci.org/browse/JENKINS-20712) -
ETag/MTime saved in config.xml but there is no flag in job web page

## Release 0.38

* Assign meaningful name to executor thread  
* Be more explicit about failed content check preconditions

## Release 0.37

* Fix
[JENKINS-20359](https://issues.jenkins-ci.org/browse/JENKINS-20359) -
Monitoring URL for JSON content change not working

## Release 0.36

* Making the build compatible with Java 7

## Release 0.35

* Fix
[JENKINS-17961](https://issues.jenkins-ci.org/browse/JENKINS-17961) -
URLTrigger does not poll when URL starts with environment variable

## Release 0.34

* Fix
[JENKINS-18035](https://issues.jenkins-ci.org/browse/JENKINS-18035) -
Request to support HTTPS url monitoring in URLTrigger plugin

## Release 0.33

* Fix regression on polling log from previous version

## Release 0.32

* Fix
[JENKINS-18683](https://issues.jenkins-ci.org/browse/JENKINS-18683) -
Jenkins 1.522 config changes cannot be saved  
* Fix
[JENKINS-18764](https://issues.jenkins-ci.org/browse/JENKINS-18764) -
NPE in URLTrigger when saving project configuration

## Release 0.31

* Fix
[JENKINS-17641](https://issues.jenkins-ci.org/browse/JENKINS-17641) -
Unknown field 'logEnabled' in org.jenkinsci.lib.xtrigger.XTriggerCause

## Release 0.30

* Fix
[JENKINS-17468](https://issues.jenkins-ci.org/browse/JENKINS-17468) -
NullPointerException in URLTrigger.getFTPResponse during startup

## Release 0.29

* Add FTP Support

## Release 0.28

* Fix
[JENKINS-16774](https://issues.jenkins-ci.org/browse/JENKINS-16774) -
URLTrigger gives severe error message instead of detecting change

## Release 0.27

* warn user that only http is supported for URL protocol (added by
ndeloof)

## Release 0.26

* Fix
[JENKINS-14620](https://issues.jenkins-ci.org/browse/JENKINS-14620) -
Invalid configurations

## Release 0.25

* Fix
[JENKINS-15564](https://issues.jenkins-ci.org/browse/JENKINS-15564) -
URLTrigger: Allow timeouts to be configurable

## Release 0.24

* Fix
[JENKINS-14607](https://issues.jenkins-ci.org/browse/JENKINS-14607) -
URLTrigger "Polling error null"

## Release 0.23

* Add the capability to check ETag response header

## Release 0.22

* Add JENKINS\_URL resolution at startup check  
* Update to xtrigger-lib 0.17  
* Update to envinject-lib 1.10

## Release 0.21

* Add environment variables resolution at the trigger startup
lifecycle  
* Update to xtrigger-lib 0.16  
* Update to envinject-lib 1.9

## Release 0.20

* Upgrade to xtrigger-lib 0.14 (more log)

## Release 0.19

* Restrict to successful family for URL content  
* Exclude polling on unavailable services

## Release 0.18

* Fix job restart when JENKINS URLs to check are unavailable at Jenkins
startup

## Release 0.17

* Update to xtrigger-lib 0.12 (fix link to polling log to appear on
build console)

## Release 0.16

* Fix TXT content type detection

## Release 0.15

* Fix
[JENKINS-12912](https://issues.jenkins-ci.org/browse/JENKINS-12912) -
URLTtrigger does not poll on jobs which are tied to disconnected slaves

## Release 0.14

* Update to xtrigger-lib 0.8

## Release 0.13

* Fix the hang problem
([JENKINS-12696](https://issues.jenkins-ci.org/browse/JENKINS-12696))

## Release 0.11

* Update to xtrigger-lib 0.7

## Release 0.10

* Update to xtrigger-lib 0.2  
* Fix
[JENKINS-12213](https://issues.jenkins-ci.org/browse/JENKINS-12213) -
Polling error org/jenkinsci/plugins/envinject/EnvInjectAction" when
monitoring build number url

## Release 0.9

* Fix
[JENKINS-11859](https://issues.jenkins-ci.org/browse/JENKINS-11859) -
java.io.IOException: Stream closed" when monitoring a jenkins job build
number

## Release 0.8

* Environment variables are taken into account

## Release 0.7

* Add proxy configuration if needed

## Release 0.6

* Fix
[JENKINS-11273](https://issues.jenkins-ci.org/browse/JENKINS-11273) -
Basic Authentication support in urltrigger

## Release 0.5.1

* Fix TXT Content type saving

## Release 0.5

* Change 'Add Button' label  
* Fix
[JENKINS-10731](https://issues.jenkins-ci.org/browse/JENKINS-10731) -
XMLContentType didn't update initial results HashMap  
* Fix
[JENKINS-10728](https://issues.jenkins-ci.org/browse/JENKINS-10728) -
URLTrigger, config.jelly and checkLastModificationDate should be
optionalBlock

## Release 0.4.3

* Add message when there are no URLs to poll

## Release 0.4.2

* Fix NullPointerException when there is no URL entry

## Release 0.4.1

* Fix a NullPointerException at Jenkins startup  
* Add a delete button on the configuration page to remove an entry
section

## Release 0.4

* Add Text Content check

## Release 0.3

* Add JSON content check  
* Technical features: additional unit tests

## Release 0.2

* Add poll changes of URL contents

## Release 0.1

* Initial release 
