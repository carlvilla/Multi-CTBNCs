# MCTBNCs [![Build Status](https://travis-ci.com/carlvilla/MCTBNCs.svg?token=aJzHjLbR53QnhrMdqpW5&branch=master)](https://travis-ci.com/carlvilla/MCTBNCs)

## Supported datasets
Datasets can be stored and presented in different formats. Currently, this software supports the following:
* Unique CSV file (TO DO)
* Sequences stored in individual CSV files (see, for example, the post-stroke rehabilitation dataset [REFERENCE])

## Learning MCTBNCs
This software provides the following algorithms for the learning of a MCTBNC.
### Parameter learning algorithms
* Maximum likelihood estimation (MLE)

### Structure learning algorithms
#### Optimization algorithms
* Greedy hill climbing 

#### Score functions
* Log-likelihood score
  * BIC penalization

#### Available models by structure constraints
* Multi-dimensional continuous time Bayesian network classifier (MCTBNC): no structure constraints.
* Multi-dimensional continuous time naive Bayes classifier (MCTNBC): assumes independence between class variables and between features. The model is formed by a complete bridge subgraph, so each class variable is parent of all features.

## Inference over MCTBNCs


