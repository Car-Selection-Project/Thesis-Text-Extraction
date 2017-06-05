# Text extraction: a highway to systematically process car reviews
In this thesis project we show how to apply NLP tools to get scores from a vast amount of car reviews.

## Requirements
To run this tool, you need to have at least version 8 of JRE of JDK. If you want to run this tool from the command line, be sure to have set up the global [JAVA_HOME](https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/) environment variable. 

## How to use this tool?
There are two ways to use this tool. First of all, you may simply run the program by double clicking the jar file. Secondly, you may want to use it as an API and pass arguments. To do so, simply run the following line from a terminal:

``` batch
cd /your/path/to/the/jar java -jar "Car Reviewer V2.jar"
```

This is the minimal way to run the program from the commandline. If no arguments are passed, the GUI is activated by default. To pass arguments, simply append them behind the command provided above. Arguments that can be passed are:
* API or GUI.
* A specific car, for example bmw_x5-edrive_2016. This can be more than one car.
* A number indicating the maximum of shown cars (the top-x output).
* One or more categories. For example quality, mileage, price, or performance.

Note: The arguments passed are not case sensitive with exception of the categories. If a category has an uppercase level, it means the whole category gets taken as input instead of just a subcategory.

Let's make this more clear with an example.

Franks wants to buy a high quality car for a good price/value. He does not care much about specifics as long as it is a good overall car. Franks is also an application developer and uses the API. From the commandline he runs:
``` java
java -jar "Car Reviewer V2.jar" api Quality price Value
``` 
The output looks like this:
```
Categories interested in: [Quality, price, Value]
Cars interested in : []

Categories interested in: [Quality, price, Value]
Cars interested in : []
Cadillac xts 2016
Quality: 5.0
Average: 5.0

Mercedes-benz g-class 2016
Quality: 5.0
Average: 5.0

Mercedes-benz e-class 2016
Quality: 5.0
Average: 5.0

Kia sorento 2017
Quality: 5.0
Average: 5.0

Ford transit-connect 2016
Value: 5.0
Average: 5.0
```
