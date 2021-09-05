<h1 align="center">
  Multi-CTBNCs
</h1>
<p align="center"> <img src="./imgs/Multi-CTBNC.png" width="40%" style="border-radius: 3%;"/> </p>
<p align="center">Multi-dimensional continuous time Bayesian network classifiers</p>
<p align="center">
<a href="https://travis-ci.com/carlvilla/Multi-CTBNCs"> <img align="center" hspace="10" src="https://travis-ci.com/carlvilla/Multi-CTBNCs.svg?token=aJzHjLbR53QnhrMdqpW5&branch=master" alt="Build Status"> </a>     
<a href="http://cig.fi.upm.es"> <img align="center" src="http://cig.fi.upm.es/sites/default/files/logo_CIG.png" width="35" height="50"> </a>
</p>

## Description

The multi-dimensional continuous time Bayesian network classifiers (Multi-CTBNCs) are an extension of continuous time Bayesian networks for the multi-dimensional classification of multi-variate time series. These probabilistic graphical models are able to modelate temporal data evolving over continuous time and classify them into multiple class variables taking advantage of the information provided by inter-class dependecies. This is a common and important task for domains such as finance, industry, medicine or signal processing, but applications can be found in almost any field.

This software provides an easy-to-use tool, so anyone can apply Multi-CTBNCs in their research. Multiple options are available to learn the structure and parameters of different families of Multi-CTBNCs, as well as to evaluate the models or classify previously unseen datasets.

## Table of content

- [Installation](#installation)
- [Usage](#usage)
- [Supported datasets](#supported-datasets)
- [Learning Multi-CTBNCs](#learning-multi-ctbncs)
    - [Parameter learning algorithms](#parameter-learning-algorithms)
    - [Structure learning algorithms](#structure-learning-algorithms)
        - [Optimization algorithms](#optimization-algorithms)
        - [Score functions](#score-functions)
        - [Available models by structure constraints](#available-models-by-structure-constraints)
- [Reproducibility of experiments](#reproducibility-of-experiments)
- [Sampling synthetic datasets](#sampling-synthetic-datasets)
- [References](#references)

## Installation
This software has been developed as a Gradle project to facilitate its usage and the management of its dependencies. As the Gradle Wrapper is provided, only a Java Development Kit (JDK) needs to be installed ([Java SE Downloads](https://www.oracle.com/java/technologies/javase-downloads.html)).


## Usage

1. Download the repository:

    ```sh
    git clone https://github.com/carlvilla/Multi-CTBNCs.git
    ```
  
2. Navigate to the repository's root directory:

    ```sh
    cd Multi-CTBNCs
    ```

3. Run the application:

      On Linux / macOS:
      ```sh
      ./gradlew run
      ```
      On Windows:
      ```bat
      .\gradlew.bat run
      ```
  

These steps will open the application interface, through which you can specify your datasets for training, evaluation or classification, model to build, learning algorithms and evaluation methods. The interface consists of four tabs:

* The **Dataset tab**: allows to specify the data to train and evaluate an Multi-CTBNC. We can specify how the sequences will be extracted and which time, class and feature variables will be used.
* The **Model tab**: allows to specify the model that will be trained. 
* The **Evaluation tab**: allows to define how the model selected in the *Model* tab will be evaluated using the data provided in the *Dataset* tab.
* The **Classification tab**: allows to train the model selected in the *Model* tab with the data provided in the *Dataset* tab and to classify a second dataset with this model. The classification results are saved in the folder *results/classifications* in the project root directory.

<p align="center"><img src="./imgs/demo.gif" alt="Software demo" /></p>

## Supported datasets
Time series datasets can be stored and presented in different formats. Currently, this software supports the following:

* **Multiple CSVs**: sequences are stored in individual CSV files.
* **Single CSV**: sequences of a predefined maximum length are extracted from a single CSV file. In the case of the training dataset, the extracted sequences will have the same length as long as their observations have the same class configuration.

## Learning Multi-CTBNCs
This software provides the following learning algorithms for a Multi-CTBNC.
### Parameter learning algorithms
* **Maximum likelihood estimation**: assumes that the parameters are constants, seeking those values that maximize the probability of the observable data.
* **Bayesian estimation**: parameters are considered random variables and a prior distribution is defined over them.

### Structure learning algorithms
#### Optimization algorithms
* **Hill climbing**: iterative algorithm that performs incremental modifications over the model structure and selects those that yield to a better solution.
* **Random-restart hill climbing**: performs a series of hill climbing optimizations starting from random initial structures and keeps the best solution.

#### Score functions
* **Log-likelihood score**
* **Conditional log-likelihood score**
* **Bayesian Dirichlet equivalent score**

The following penalization functions can be applied over the structure complexity when optimizing the log-likelihood and conditional log-likelihood scores:
  * **BIC penalization**
  * **AIC penalization**

#### Available models by structure constraints

Different families of Multi-CTBNCs can be proposed depending on the search spaces considered for the class and feature subgraphs. Currently, this software supports the following Multi-CTBNC families:

* **Multi-dimensional continuous time Bayesian network classifier (Multi-CTBNC)**: no structure constraints.

<p align="center"> <img src="./imgs/Multi-CTBNC.png" width="30%" style="border-radius: 3%;" alt="Multi-CTBNC"/> </p>

* **Multi-dimensional continuous time naive Bayes classifier (Multi-CTNBC)**: assumes conditional independence between features given the class variables and independence between the latter. The model is formed by a complete bridge subgraph, so each class variable is parent of all features.

<p align="center"> <img src="./imgs/Multi-CTNBC.png" width="30%" style="border-radius: 3%;" alt="Multi-CTNBC"/> </p>

* **DAG-maxK multi-dimensional continuous time Bayesian network classifier (DAG-maxK Multi-CTBNC)**: feature nodes have at most k parents (excluding class variables).

<p align="center"> <img src="./imgs/DAG-maxK_Multi-CTBNC.png" width="30%" style="border-radius: 3%;" alt="DAG-maxK Multi-CTBNC"/> </p>

* **Empty-digraph multi-dimensional continuous time Bayesian network classifier (Empty-digraph Multi-CTBNC)**: dependencies between class variables are ignored.

<p align="center"> <img src="./imgs/Empty-digraph_Multi-CTBNC.png" width="30%" style="border-radius: 3%;" alt="Empty-digraph Multi-CTBNC"/> </p>

* **Empty-maxK multi-dimensional continuous time Bayesian network classifier (Empty-maxK Multi-CTBNC)**: feature nodes have at most k parents (excluding class variables) and dependencies between class variables are ignored.

<p align="center"> <img src="./imgs/Empty-maxK_Multi-CTBNC.png" width="30%" style="border-radius: 3%;" alt="Empty-maxK Multi-CTBNC"/> </p>

## Reproducibility of experiments

Datasets used in the article [[1]](#1) can be found at this [link](https://upm365-my.sharepoint.com/:u:/g/personal/carlos_villa_upm_es/EYvXkGE0i3ZNtxWRcILKyPoB6Vq-spGVxZOUJjtOZy4eLQ). The following Gradle tasks perform the experiments of the article if the *dataset* folder is placed in the root directory of this project:

* **emptyDigraphMultiCTBNC** - Compares the performance of CTBNCs and an empty-digraph Multi-CTBNC on the synthetic datasets when they are learned with the Bayesian Dirichlet equivalent score.
* **energyBDe** - Compares the performance of max1 CTBNCs and a DAG-max1 Multi-CTBNC on the energy dataset when they are learned with the Bayesian Dirichlet equivalent score.
* **energyLL** - Compares the performance of max1 CTBNCs and a DAG-max1 Multi-CTBNC on the energy dataset when they are learned with the BIC-penalized log-likelihood score.
* **runAllExperiments** - Runs all the experiments with the synthetic and energy datasets.
* **syntheticBDe** - Compares the performance of CTBNCs and an Multi-CTBNC on the synthetic experiments when they are learned with the Bayesian Dirichlet equivalent score.
* **syntheticCLL** - Compares the performance of CTBNCs and an Multi-CTBNC when learned with the conditional log-likelihood score penalized with BIC.
* **syntheticLL** - Compares the performance of CTBNCs and an Multi-CTBNC when learned with the log-likelihood score penalized with BIC.

The tasks can be executed with the following command:

On Linux / macOS:
```sh
./gradlew <task>
```
On Windows:
```bat
gradlew.bat <task>
```

Excel files with the results of the experiments will be saved in the folder *results/experiments* in the project root directory. The same experiments are always performed since predefined seeds are used to shuffle the datasets.

## Sampling synthetic datasets

This software provides the tools to sample discrete state multi-variate time series datasets with multiple class variables. To do so, first specify the characteristics of your dataset in the class *src/main/java/es/upm/fi/cig/multictbnc/MainSampling.java*. These are, among others, the number of feature and class variables and their sample space and dependencies, the number of sequences and their maximum duration or the destination path of the dataset. Then, the dataset can be generated with the following command:

On Linux / macOS:
```sh
./gradlew sampleDataset
```
On Windows:
```bat
gradlew.bat sampleDataset
```

## References


<a id="1">[1]</a> 
Villa‐Blanco C, Larrañaga P, Bielza C. Multidimensional continuous time Bayesian network classifiers. <em>Int J Intell Syst.</em> 2021;1‐28. https://doi.org/10.1002/int.22611.
