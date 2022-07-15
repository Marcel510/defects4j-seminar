### This repository containes the files for Seminar Software Quality at TUM, topic: Test Suite Minimization:
Toolchain to obtain test wise coverage using Teamscale JaCoCo agent and upload it automatically to Teamscale and conduct
a Test Suite Minmization with fault detection evaluation.

## Contents

- ``defects4j/``: The orginal defects4j repository adjusted to support running the Teamscale Jacoco agent, 2 Test Listeners and a custom Test Runner.
- ``seminar-test-listener/``: Contains the source code for two JUnit Test Listener: One Test Listener to record test execution information and another to inform Teamscale JaCoCo agent about the currently executed test method.
- ``seminar-test-runner/``: A custom test runner to run reduced test suites given as a file of uniform paths of selected test methods.
- ``seminar-util/``: Various converters between different format, a config generator and automation tools to automatically conduct test suite minimization with Teamscale.
- ``/``: Some other shell scripts used to orchestrate automatic testwise coverage generation and test suite minimization.
