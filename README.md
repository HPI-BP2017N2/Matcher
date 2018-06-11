# Matcher [![Build Status](https://travis-ci.org/HPI-BP2017N2/Matcher.svg?branch=master)](https://travis-ci.org/HPI-BP2017N2/Matcher)
The matcher is a microservice component, which finds counterparts of parsed offers in the idealo offer database.
It is written in Java and uses the Spring framework.

## Getting started
### Prerequisites
1. Cache  
 The matcher requests idealo's offers for specific shops from [the cache](https://github.com/HPI-BP2017N2/Cache).
2. MongoDB  
 The matcher uses several MongoDB databases for storing and retrieving various information.  
 2.1. One database is used for reading the information extracted by [the parser](https://github.com/HPI-BP2017N2/Parser). It is expected that those information is separated into multiple collections (one for every shop, named by the corresponding shop ID).  
 2.2. One database is used for saving the results of the matching process. Those data can be used for further visualization or processing. [The model generator](https://github.com/HPI-BP2017N2/MachineLearningModelGenerator) can use some of this data for training various classifiers. The collection structure is similar to the parsed offers.  
 2.3. One database is used for storing and reading the state of the matcher. When restarted, the matcher will continue matching the shops stored there.  
2.4. One database is used for reading the different classifiers used during the matching process. It is expected to have two collections.   
  2.4.1. One collection is named scoredModel. It contains a serialized classifier used for deciding whether a parsed offer and an idealo offer match or not.  
  2.4.2. One collection is named serializedParagraphVectors. It contains one serialized neural network each for classifying the brand and the category of a parsed offer.  
2. RabbitMQ  
 The matcher consumes commands for starting the matching process from RabbitMQ.
 
 
### Configuration
#### Environment variables
- MONGO_IP: The IP of the MongoDB instance
- MONGO_PORT: The port of the MongoDB instance
- MONGO_MATCHER_USER: The username to access the MongoDB
- MONGO_MATCHER_PW: The password to access the MongoDB
- RABBIT_IP: The IP of the RabbitMQ
- RABBIT_PORT: The port of the RabbitMQ
- RABBIT_USER: The username to access the Rabbit MQ
- RABBIT_PW: The password to access the RabbitMQ
- CACHE_IP: The URI of the cache microservice

#### Component properties
- labelThreshold: The minimum probability to classify the category and the brand of a parsed offer
- matchingThreshold: The minimum probability of a match to decide whether two offers are equivalent or not
- collectTrainingData: If set to true, the matcher will accept POST requests to match shops via unique identifiers and does not read from the queue.

## How it works
1. Matcher gets a message to start matching one specific shop or reads a state from the database.
2. Matcher tells cache to download the offers idealo knows from this shop.
3. Matcher requests every idealo offer and searches for a "safe" match (EAN or combination of HAN and brand can be found in parsed offers). Those matches are being stored.
4. Matcher loads classifiers from database. If there are no classifiers, matcher will save the current shop ID in the state database and abort the matching process.
5. Matcher classifies match probabilities for every combination of remaining parsed offers and shop offers. Features are equality of SKU, HAN, URL, brand, classified brand,  category and image ID, text similarities for title and description, percentaged deviance of price.
6. The highest scored matches with a probability above the specified threshold are stored as matches, the remaining shop offers as newly discovered ones.
