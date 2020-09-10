# MCTBNCs [![Build Status](https://travis-ci.com/carlvilla/MCTBNCs.svg?token=aJzHjLbR53QnhrMdqpW5&branch=master)](https://travis-ci.com/carlvilla/MCTBNCs)

## Supported datasets
Datasets can be stored and presented in different formats. Currently, this software supports the following:
* Single CSV: sequences of a maximum length are extracted from a single CSV file.
* Multiple CSVs: sequences are stored in individual CSV files (e.g., post-stroke rehabilitation dataset [REFERENCE])

## Learning MCTBNCs
This software provides the following algorithms for the learning of a MCTBNC.
### Parameter learning algorithms
* Maximum likelihood estimation
* Bayesian estimation

### Structure learning algorithms
#### Optimization algorithms
* Greedy hill climbing 

#### Score functions
* Log-likelihood score
  * BIC penalization
  * AIC penalization

#### Available models by structure constraints
* Multi-dimensional continuous time Bayesian network classifier (MCTBNC): no structure constraints.
* Multi-dimensional continuous time naive Bayes classifier (MCTNBC): assumes independence between class variables and between features. The model is formed by a complete bridge subgraph, so each class variable is parent of all features.
* DAG-k multi-dimensional continuous time Bayesian network classifier (DAG-k MCTBNC): feature nodes are limited to k maximum parents.
* Empty-DAG multi-dimensional continuous time Bayesian network classifier (DAG-k MCTBNC): dependencies between class variables are ignored.

## Inference over MCTBNCs


