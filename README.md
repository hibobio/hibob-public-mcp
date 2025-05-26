# kotlin-template

https://docs.google.com/document/d/1Z8iZJEUJCuQU9y5WGjTUowxDytbd930Bommk98m_fM8/edit

## The Project

Please add a few words about what this project is supposed to accomplish and how.

## CI/CD

The CI/CD master build pipeline system will need to know that your project is running on Java 21.  
You can update the pipeline configuration for your project by adding your repo name to the Java 21 repos embedded here -
https://github.com/hibobio/reusable-workflows/blob/master/.github/workflows/master.yaml

## JDK Installation

This project runs on the Eclipse Temurin distribution of Java 21 with Kotlin 1.9.  
The JDK can be installed by IntelliJ from `File -> Project Structure -> SDKs -> +`, and selecting the appropriate JDK for download.

## IntelliJ Configuration

**Before** opening this project in intellij, first set the correct java sdk, and then open intellij.  
Otherwise intellij may lock on to the wrong version of java.

In intellij, verify:
1. File -> Project Structure -> Platform Settings -> SDKs.  Set to `temurin-21`
2. File -> Project Structure -> Project Settings
    1. Project -> SDK `temurin-21`
    2. Modules -> Sandbox -> Module SDK `java 21`
