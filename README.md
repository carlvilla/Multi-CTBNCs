# MCTBNCs [![Build Status](https://travis-ci.com/carlvilla/MCTBNCs.svg?token=aJzHjLbR53QnhrMdqpW5&branch=master)](https://travis-ci.com/carlvilla/MCTBNCs)
## Installation

## Supported datasets
Datasets can be stored and presented in different formats. Currently, this software supports the following:
* Single CSV: sequences of a predefined maximum length are extracted from a single CSV file.
* Multiple CSVs: sequences are stored in individual CSV files

## Learning MCTBNCs
This software provides the following algorithms for the learning of a MCTBNC.
### Parameter learning algorithms
* Maximum likelihood estimation
* Bayesian estimation

### Structure learning algorithms
#### Optimization algorithms
* Hill climbing
* Random-restart hill-climbing

#### Score functions
* Log-likelihood score
* Conditional log-likelihood
* Bayesian scores

The following penalizations can be applied over the log-likelihood and conditional log-likelihood:
  * BIC penalization
  * AIC penalization

#### Available models by structure constraints
* Multi-dimensional continuous time Bayesian network classifier (MCTBNC): no structure constraints.
* Multi-dimensional continuous time naive Bayes classifier (MCTNBC): assumes independence between class variables and between features. The model is formed by a complete bridge subgraph, so each class variable is parent of all features.
* DAG-maxK multi-dimensional continuous time Bayesian network classifier (DAG-maxK MCTBNC): feature nodes have at most k parents (excluding class variables).
* Empty-digraph multi-dimensional continuous time Bayesian network classifier (Empty-digraph MCTBNC): dependencies between class variables are ignored.
* Empty-maxK multi-dimensional continuous time Bayesian network classifier (Empty-maxK MCTBNC): feature nodes have at most k parents (excluding class variables) and dependencies between class variables are ignored.

Note that if only one class variable is provided, a continuous time Bayesian network classifier (CTBNC) will be learnt instead with the selected constraints.