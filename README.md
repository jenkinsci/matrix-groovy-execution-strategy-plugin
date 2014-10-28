

# A plugin to decide the order of matrix project combinations

## Bindings
There are several variables available in the script

* combinations
* workspace
* jenkins
* execution
* result

### Combinations
This is a List of a String map of all the matrix combinations
For example 
```groovy
[
    { axis1: "a", axis2: "x" },
    { axis1: "a", axis2: "y" },
    { axis1: "a", axis2: "z" },
    { axis1: "b", axis2: "x" },
    { axis1: "b", axis2: "y" },
    { axis1: "b", axis2: "z" }
]
```

### workspace
a string of the workspace

### execution 
The is the [OneOffExecutor]( http://javadoc.jenkins-ci.org/hudson/model/OneOffExecutor.html) running the setup for the matrix job

### result
The script is expected to return a [TreeMap](http://groovy.codehaus.org/JN1035-Maps). This is passed in for use but does not have to be the one returned

```groovy
{
  x: [
    { axis1: "a", axis2: "x" },
    { axis1: "b", axis2: "x" }
  ],
  y: [
    { axis1: "a", axis2: "y" },
    { axis1: "b", axis2: "y" }
  ]
}
```
Notice that axis2: 'z' has not been returned, so these combinations will not be executed

## Execution
The entries in the TreeMap returned will be executed in sequence.

That is to say, the first list will be executed in parallel, then the second, and so on

A failure will stop the build on the next entry.</p>

If no script is specified, all combinations will be run in parallel in one go.

## Scripts
### Location
The script can be in three locations
* Added in directly
* An absolute path available to the node running the job setting up the matrix
* A relative path in the workspace

###Example
How to transform the example above
```groovy
combinations.each{

             if(it.axis2 == 'z')
                   return

             result[it.axis2] = result[it.axis2] ?: []
             result[it.axis2] << it
}

result
```

